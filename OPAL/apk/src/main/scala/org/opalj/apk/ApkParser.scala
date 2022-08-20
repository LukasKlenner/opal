/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.apk

import com.typesafe.config.Config
import net.dongliu.apk.parser.ApkFile
import org.opalj.apk.ApkComponentType.ApkComponentType
import org.opalj.br.analyses.Project
import org.opalj.ll.LLVMProjectKey
import org.opalj.log.{GlobalLogContext, LogContext, OPALLogger}
import org.opalj.util.PerformanceEvaluation.time

import java.io.{File, StringWriter}
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.util.zip.ZipFile
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.sys.process._
import scala.xml.{Node, XML}

/**
 * Parses an APK file and generates a [[Project]] for it.
 *
 * The generated [[Project]] contains the APK's Java bytecode, its native code and its entry points.
 *
 * Following external tools are utilized:
 *   - enjarify or dex2jar (for creating .jar from .dex)
 *   - RetDec (for lifting native code to LLVM IR)
 *
 * @param apkPath path to the APK file.
 *
 * @author Nicolas Gross
 */
class ApkParser(val apkPath: String) {

    private implicit val LogContext: LogContext = GlobalLogContext
    private val LogCategory = "APK parser"

    private var tmpDir: Option[File] = None
    private val ApkUnzippedDir = "/apk_unzipped"

    /**
     * Parses the components / static entry points of the APK from AndroidManifest.xml.
     *
     * @return a Seq of [[ApkComponent]]
     */
    def parseComponents: Seq[ApkComponent] = {
        val apkFile = new ApkFile(apkPath)
        val manifestXmlString = apkFile.getManifestXml
        val manifestXml = XML.loadString(manifestXmlString)

        val xmlns = "http://schemas.android.com/apk/res/android"
        val nodeToEntryPoint = (compType: ApkComponentType, n: Node) =>
            new ApkComponent(
                compType,
                (n \ ("@{"+xmlns+"}name")).text, // class
                (n \\ "action" \\ ("@{"+xmlns+"}name")).map(_.text) // intents / triggers
            )
        val entryPoints: ListBuffer[ApkComponent] = ListBuffer.empty

        // collect all Activities, Services, Broadcast Receivers and Content Providers, which are all entry points
        val activities = manifestXml \ "application" \ "activity"
        activities.foreach(a => entryPoints.append(nodeToEntryPoint(ApkComponentType.Activity, a)))
        val services = manifestXml \ "application" \ "service"
        services.foreach(s => entryPoints.append(nodeToEntryPoint(ApkComponentType.Service, s)))
        val receivers = manifestXml \ "application" \ "receiver"
        receivers.foreach(r => entryPoints.append(nodeToEntryPoint(ApkComponentType.BroadcastReceiver, r)))
        val providers = manifestXml \ "application" \ "provider"
        providers.foreach(p => entryPoints.append(nodeToEntryPoint(ApkComponentType.ContentProvider, p)))

        entryPoints.toSeq
    }

    /**
     * Parses the Dex files of the APK.
     *
     * Uses enjarify to create .jar files from .dex files. This can take some time,
     * please be patient.
     *
     * @param useEnjarify: defaults to true, uses dex2jar if set to false
     * @return (directory containing all .jar files, Seq of every single .jar file)
     */
    def parseDexCode(useEnjarify: Boolean = true): (Path, Seq[Path]) = {
        // TODO docker
        // --- ONLY TEMPORARY ---
        val enjarifyPath = "/home/nicolas/git/enjarify/enjarify.sh"
        val dex2jarPath = "/home/nicolas/Downloads/dex2jar-2.1/dex-tools-2.1/d2j-dex2jar.sh"
        // --- ONLY TEMPORARY ---

        OPALLogger.info(LogCategory, "dex code parsing started")

        var jarsDir: Path = null
        val jarFiles: ListBuffer[Path] = ListBuffer.empty
        time {
            unzipApk()
            val apkRootDir = new File(tmpDir.get.toString + ApkUnzippedDir)
            val dexFiles = apkRootDir.listFiles
                .filter(_.isFile)
                .filter(_.getName.endsWith(".dex"))

            jarsDir = Files.createDirectory(Paths.get(tmpDir.get.toString + "/jars"))
            val getJarPath = (dexPath: String) => jarsDir.toString + "/" + ApkParser.getFileBaseName(dexPath) + ".jar"
            val getCmd =
                if (useEnjarify) {
                    (dex: File) => s"$enjarifyPath -o ${getJarPath(dex.toString)} $dex"
                } else {
                    (dex: File) => s"$dex2jarPath -o ${getJarPath(dex.toString)} $dex"
                }

            // generate .jar files from .dex files, this can take some time ...
            dexFiles.foreach(dex => {
                jarFiles.append(Paths.get(getJarPath(dex.toString)))
                val (retval, _, _) = ApkParser.runCmd(getCmd(dex))
                if (retval != 0) {
                    throw ApkParserException("could not convert .dex files to .jar files")
                }
            })
        } {
            t => OPALLogger.info(LogCategory, s"dex code parsing finished, took ${t.toSeconds}")
        }
        (jarsDir, jarFiles.toSeq)
    }

    /**
     * Parses the native code / .so files of the APK.
     *
     * Uses RetDec to lift .so .files to LLVM .bc files. This can take some time,
     * please be patient.
     *
     * @return Option(directory containing all .bc files, Seq of every single .bc file) or
     *         None if APK contains no native code
     */
    def parseNativeCode: Option[(Path, Seq[Path])] = {
        // TODO docker
        // --- ONLY TEMPORARY ---
        val retdecPath = "/home/nicolas/bin/retdec/bin/retdec-decompiler.py"
        // --- ONLY TEMPORARY ---

        OPALLogger.info(LogCategory, "native code parsing started")

        var llvmDir: Path = null
        val llvmFiles: ListBuffer[Path] = ListBuffer.empty
        time {
            unzipApk()
            val apkLibPath = tmpDir.get.toString + ApkUnzippedDir + "/lib"
            val archs = new File(apkLibPath).listFiles.filter(_.isDirectory).map(_.getName)
            if (!Files.isDirectory(Paths.get(apkLibPath)) || archs.isEmpty) {
                // APK does not contain native code
                return None
            }

            val soFilesPerArch = archs.map(arch => {
                val archDir = new File(apkLibPath + "/" + arch)
                val soFiles = archDir.listFiles.filter(_.isFile).filter(_.getName.endsWith(".so"))
                (archDir, soFiles)
            })

            // prefer arm64, then arm, then anything else that comes first
            val selectedArchSoFiles = soFilesPerArch.find(t => t._1.getName.startsWith("arm64")) match {
                case None => soFilesPerArch.find(t => t._1.getName.startsWith("arm")) match {
                    case None => soFilesPerArch.head
                    case Some(t) => t
                }
                case Some(t) => t
            }

            // generate .bc files from .so files, this can take some time ...
            llvmDir = Files.createDirectory(Paths.get(tmpDir.get.toString + "/llvm"))
            val getLlvmPath = (soPath: String) => llvmDir.toString + "/" + ApkParser.getFileBaseName(soPath)
            val getCmd = (so: File) => s"$retdecPath --stop-after=bin2llvmir -o ${getLlvmPath(so.toString)} $so"
            selectedArchSoFiles._2.foreach(so => {
                llvmFiles.append(Paths.get(getLlvmPath(so.toString) + ".bc"))
                val (retval, _, _) = ApkParser.runCmd(getCmd(so))
                if (retval != 0) {
                    throw ApkParserException("could not convert .so files to .bc files")
                }
            })

        } {
            t => OPALLogger.info(LogCategory, s"native code parsing finished, took ${t.toSeconds}")
        }
        Some((llvmDir, llvmFiles.toSeq))
    }

    /**
     * Cleans up temporary files/directories used for unzipping the APK and
     * generating .jar and .bc files.
     *
     * You should call this when you are done to not clutter up tmpfs.
     */
    def cleanUp(): Unit = tmpDir match {
        case Some(tmpDirPath) => {
            ApkParser.runCmd("rm -r " + tmpDirPath)
            tmpDir = None

        }
        case None =>
    }

    private[this] def unzipApk(): Unit = tmpDir match {
        case Some(_) =>
        case None => {
            val fileName = Paths.get(apkPath).getFileName
            tmpDir = Some(Files.createTempDirectory("opal_apk_" + fileName).toFile)
            val unzipDir = Files.createDirectory(Paths.get(tmpDir.get.getPath + ApkUnzippedDir))
            ApkParser.unzip(Paths.get(apkPath), unzipDir)
        }
    }
}

object ApkParser {

    private implicit val logContext: LogContext = GlobalLogContext

    /**
     * Set this to true if you want stdout and stderr of commands (retdec, enjarify, dex2jar) being logged.
     */
    var logOutput = false

    /**
     * Creates a new [[Project]] from an APK file.
     *
     * Generation of .jar and .bc files takes some time, please be patient.
     *
     * @param apkPath path to the APK file.
     * @param projectConfig config values for the [[Project]].
     * @return the newly created [[Project]] containing the APK's contents (dex code, native code and entry points).
     */
    def createProject(apkPath: String, projectConfig: Config, useEnjarify: Boolean = true): Project[URL] = {
        val apkParser = new ApkParser(apkPath)

        val jarDir = apkParser.parseDexCode(useEnjarify)._1

        val project =
            Project(
                jarDir.toFile,
                GlobalLogContext,
                projectConfig
            )

        project.updateProjectInformationKeyInitializationData(ApkComponentsKey)(
            current => apkParser
        )
        project.get(ApkComponentsKey)

        apkParser.parseNativeCode match {
            case Some((_, llvmModules)) => {
                project.updateProjectInformationKeyInitializationData(LLVMProjectKey)(
                    current => llvmModules.map(f => f.toString)
                )
                project.get(LLVMProjectKey)
            }
            case None =>
        }

        apkParser.cleanUp()

        project
    }

    /**
     * Runs an external command.
     *
     * @param cmd the command that is executed.
     * @return a tuple consisting of the return code, stdout and stderr.
     */
    private def runCmd(cmd: String, logOutput: Boolean = logOutput): (Int, StringWriter, StringWriter) = {
        val logCategory = "APK parser - command"
        OPALLogger.info(logCategory, s"run:  $cmd")
        val cmd_stdout = new StringWriter()
        val cmd_stderr = new StringWriter()
        val logger = if (logOutput) {
            ProcessLogger(
                o => {
                    OPALLogger.info(s"$logCategory stdout", o)
                    cmd_stdout.write(o + System.lineSeparator())
                },
                e => {
                    OPALLogger.info(s"$logCategory stderr", e)
                    cmd_stderr.write(e + System.lineSeparator())
                },
            )
        } else {
            ProcessLogger(
                o => cmd_stdout.write(o + System.lineSeparator()),
                e => cmd_stderr.write(e + System.lineSeparator()),
            )
        }
        val cmd_result = cmd ! logger
        (cmd_result, cmd_stdout, cmd_stderr)
    }

    private def unzip(zipPath: Path, outputPath: Path): Unit = {
        val zipFile = new ZipFile(zipPath.toFile)
        for (entry <- zipFile.entries.asScala) {
            val path = outputPath.resolve(entry.getName)
            if (entry.isDirectory) {
                Files.createDirectories(path)
            } else {
                Files.createDirectories(path.getParent)
                Files.copy(zipFile.getInputStream(entry), path)
            }
        }
    }

    private def getFileBaseName(fileName: String): String = {
        fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'))
    }
}
