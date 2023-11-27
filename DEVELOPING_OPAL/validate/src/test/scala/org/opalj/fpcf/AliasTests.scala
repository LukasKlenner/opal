/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package fpcf

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.ai.domain.l1
import org.opalj.br.{AnnotationLike, ClassValue, StringValue}
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.tac.cg.TypeBasedPointsToCallGraphKey
import org.opalj.tac.common.DefinitionSite
import org.opalj.tac.fpcf.analyses.alias.AliasDS
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.AliasFP
import org.opalj.tac.fpcf.analyses.alias.EagerAliasAnalysis

import java.net.URL
import scala.collection.mutable.ArrayBuffer

class AliasTests extends PropertiesTest {

    override def withRT = true

    override def fixtureProjectPackage: List[String] = {
        List("org/opalj/fpcf/fixtures/alias")
    }

    override def createConfig(): Config = ConfigFactory.load("CommandLineProject.conf")

    override def init(p: Project[URL]): Unit = {

        p.updateProjectInformationKeyInitializationData(AIDomainFactoryKey) { _ =>

            Set[Class[_ <: AnyRef]](classOf[l1.DefaultDomainWithCFGAndDefUse[URL]])
        }

        p.get(TypeBasedPointsToCallGraphKey)

    }

    describe("run all alias analyses") {

        val as = executeAnalyses(
            Set(
                EagerAliasAnalysis
            )
        )

        as.propertyStore.shutdown()

        val allocations = allocationSitesWithAnnotations(as.project)
        val formalParameters = explicitFormalParametersWithAnnotations(as.project)

        val simpleContexts = as.project.get(SimpleContextsKey)
        val declaredMethods = as.project.get(DeclaredMethodsKey)

        val properties: ArrayBuffer[((Context, AliasEntity, AliasEntity), String => String, Iterable[AnnotationLike])] = ArrayBuffer.empty

        val nameToDs: Iterable[(String, AliasDS)] = allocations.map { case (ds, _, a) => getName(a.head)-> AliasDS(ds) }
        val nameToFP: Iterable[(String, AliasFP)] = formalParameters.map { case (fp, _, a) => getName(a.head) -> AliasFP(fp) }


        val nameToEntity: Map[String, Iterable[AliasEntity]] = (nameToDs ++ nameToFP).groupMap(_._1)(_._2)

        for ((e: Entity, str: (String => String), an: Iterable[AnnotationLike]) <- allocations ++ formalParameters) {
            val entity1: AliasEntity = e match {
                case ds: DefinitionSite         => AliasDS(ds)
                case fp: VirtualFormalParameter => AliasFP(fp)
            }
            val entity2: AliasEntity = nameToEntity(getName(an.head)).find(_ != entity1).getOrElse(throw new RuntimeException("No other entity found"))

            val context = simpleContexts(declaredMethods(entity1.method))
            val entity = if (entity1.hashCode() < entity2.hashCode()) (context, entity1, entity2) else (context, entity2, entity1)
            if (!properties.exists(_._1 == entity)) {
                properties.addOne((entity, str, an))
            }
        }

        validateProperties(as, properties, Set("AliasProperty"))

        println("reachable methods: "+as.project.get(TypeBasedPointsToCallGraphKey).reachableMethods().toList.size)
    }

    def getName(a: AnnotationLike): String = {
        getValue(a, "testClass")+"."+getValue(a, "id")
    }

    def getValue(a: AnnotationLike, name: String): String = {
        a.elementValuePairs.filter(_.name == name).head.value match {
          case str: StringValue => str.value
          case ClassValue(t) => t.asObjectType.fqn
          case _ => throw new RuntimeException("Unexpected value type")
        }
    }

}
