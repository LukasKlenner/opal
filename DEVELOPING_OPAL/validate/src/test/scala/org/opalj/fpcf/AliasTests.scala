/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package fpcf

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.opalj.ai.domain.l1
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.br.AnnotationLike
import org.opalj.br.analyses.Project
import org.opalj.br.ClassValue
import org.opalj.br.StringValue
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.tac.cg.TypeBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.AliasDS
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.AliasFP
import org.opalj.tac.fpcf.analyses.alias.AliasNull
import org.opalj.tac.fpcf.analyses.alias.AliasReturnValue
import org.opalj.tac.fpcf.analyses.alias.AliasSourceElement
import org.opalj.tac.fpcf.analyses.alias.EagerIntraProceduralAliasAnalysis

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
                EagerIntraProceduralAliasAnalysis
            )
        )

        as.propertyStore.shutdown()

        val allocations = allocationSitesWithAnnotations(as.project).flatMap { case (ds, fun, a) => getAliasAnnotations(a.head).map((ds, fun, _)) }
        val formalParameters = explicitFormalParametersWithAnnotations(as.project).flatMap { case (ds, fun, a) => getAliasAnnotations(a.head).map((ds, fun, _)) }
        val methods = methodsWithAnnotations(as.project).flatMap { case (m, fun, a) => getAliasAnnotations(a.head).map((m, fun, _)) }

        val simpleContexts = as.project.get(SimpleContextsKey)
        val declaredMethods = as.project.get(DeclaredMethodsKey)

        val properties: ArrayBuffer[(AliasEntity, String => String, Iterable[AnnotationLike])] = ArrayBuffer.empty

        val nameToDs: Iterable[(String, (AliasDS, String => String))] = allocations.map { case (ds, str, a) => getName(a) -> (AliasDS(ds, as.project), str) }
        val nameToFP: Iterable[(String, (AliasFP, String => String))] = formalParameters.map { case (fp, str, a) => getName(a) -> (AliasFP(fp), str) }
        val nameToM: Iterable[(String, (AliasReturnValue, String => String))] = methods.map { case (m, str, a) => getName(a) -> (AliasReturnValue(m, as.project), str) }

        val nameToEntity: Map[String, Iterable[(AliasSourceElement, String => String)]] = (nameToDs ++ nameToFP ++ nameToM).groupMap(_._1)(_._2)

        for ((e: Entity, str: (String => String), an: AnnotationLike) <- allocations ++ formalParameters ++ methods) {
            val element1: AliasSourceElement = AliasSourceElement(e)(as.project)
            val other: (AliasSourceElement, String => String) = if (isNullAlias(an, "aliasWithNull")) {
                (new AliasNull, s => "null")
            } else {
                nameToEntity(getName(an)).find(_._1 != element1).getOrElse(throw new RuntimeException("No other entity found"))
            }

            val context = simpleContexts(declaredMethods(element1.method))
            val entity = AliasEntity(context, element1, other._1)
            if (!properties.exists(_._1 == entity)) {
                properties.addOne((entity, s => str(s) + other._2(s), Seq(an)))
            }
        }

        validateProperties(as, properties, Set("AliasProperty"))

        println("reachable methods: "+as.project.get(TypeBasedPointsToCallGraphKey).reachableMethods().toList.size)
    }

    def getName(a: AnnotationLike): String = {
        getStringValue(a, "testClass")+"."+getStringValue(a, "id")
    }

    def getStringValue(a: AnnotationLike, name: String): String = {
        a.elementValuePairs.filter(_.name == name).head.value match {
            case str: StringValue => str.value
            case ClassValue(t)    => t.asObjectType.fqn
            case _                => throw new RuntimeException("Unexpected value type")
        }
    }

    def isNullAlias(a: AnnotationLike, name: String): Boolean = {
        a.elementValuePairs.find(_.name == name).exists(_.value.asBooleanValue.value)
    }

    def getAliasAnnotations(a: AnnotationLike): Iterable[AnnotationLike] = {
        getAliasAnnotations(a, "noAlias") ++ getAliasAnnotations(a, "mayAlias") ++ getAliasAnnotations(a, "mustAlias")
    }

    def getAliasAnnotations(a: AnnotationLike, name: String): Iterable[AnnotationLike] = {
        a.elementValuePairs.filter(_.name == name).collect { case ev => ev.value.asArrayValue.values.map(_.asAnnotationValue.annotation) }.flatten
    }

}
