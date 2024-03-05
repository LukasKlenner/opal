/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package fpcf

import java.net.URL

import org.opalj.ai.domain.l1
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.br.AnnotationLike
import org.opalj.br.ClassValue
import org.opalj.br.Code
import org.opalj.br.ElementValuePair
import org.opalj.br.Field
import org.opalj.br.Method
import org.opalj.br.StringValue
import org.opalj.br.analyses.DeclaredMethods
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasFP
import org.opalj.br.fpcf.properties.alias.AliasNull
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.tac.Call
import org.opalj.tac.DUVar
import org.opalj.tac.Expr
import org.opalj.tac.ExprStmt
import org.opalj.tac.PutStatic
import org.opalj.tac.Stmt
import org.opalj.tac.UVar
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.persistentUVar
import org.opalj.tac.fpcf.analyses.alias.pointsto.EagerPointsToBasedAliasAnalysisScheduler
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

/**
 * Tests if the alias properties defined in the classes of the package org.opalj.fpcf.fixtures.alias (and it's subpackage)
 * are computed correctly.
 */
class AliasTests extends PropertiesTest {

    override def fixtureProjectPackage: List[String] = {
        List("org/opalj/fpcf/fixtures/alias")
    }

    override def init(p: Project[URL]): Unit = {

        p.updateProjectInformationKeyInitializationData(AIDomainFactoryKey) { _ =>
            Set[Class[_ <: AnyRef]](classOf[l1.DefaultDomainWithCFGAndDefUse[URL]])
        }

        p.get(AllocationSiteBasedPointsToCallGraphKey)

    }

    describe("run all alias analyses") {

        implicit val as: TestContext = executeAnalyses(
            Set( // TODO add analyses to execute
                // AllocationSiteBasedPointsToAnalysisScheduler,
                EagerPointsToBasedAliasAnalysisScheduler)
        )

        val fields = fieldsWithTypeAnnotations(as.project)
            .flatMap { case (f, fun, a) => getAliasAnnotations(a).map((f, fun, _)) }

        val allocations = allocationSitesWithAnnotations(as.project)
            .flatMap { case (ds, fun, a) => getAliasAnnotations(a).map((ds, fun, _)) }
            .map { case (ds, fun, a) => ((ds.pc, ds.method), fun, a) }

        val formalParameters = explicitFormalParametersWithAnnotations(as.project)
            .flatMap { case (ds, fun, a) => getAliasAnnotations(a).map((ds, fun, _)) }

        val methods = methodsWithAnnotations(as.project)
            .flatMap { case (m, fun, a) => getAliasAnnotations(a).map((m, fun, _)) }

        val simpleContexts = as.project.get(SimpleContextsKey)
        val declaredMethods: DeclaredMethods = as.project.get(DeclaredMethodsKey)

        // The annotations only contain one of the two sourceElements of an alias property.
        // Therefore, we first have to combine elements with the same id and store them in this ArrayBuffer.

        val IDToMethod: Map[String, Method] = getMethodIDs()

        val IDToEntity: Map[String, Iterable[AliasSourceElement]] =
            (fields ++ allocations ++ formalParameters ++ methods)
                .filterNot(_._3.annotationType.asObjectType.simpleName.endsWith("AliasUVar"))
                .map { case (e, _, a) => getID(a) -> AliasSourceElement(e)(as.project) }
                .groupMap(_._1)(_._2)

        val properties = (fields ++ allocations ++ formalParameters ++ methods)
            .map {
                case (e, str, a) => {

                    val element1 = AliasSourceElement(e)(as.project)
                    val method = element1 match {
                        case fp: AliasFP => fp.method
                        case _           => IDToMethod(getMethodID(a))
                    }
                    val element2 = resolveSecondElement(a, method, element1, IDToEntity)
                    val context = simpleContexts(declaredMethods(method))
                    val entity = AliasEntity(context, element1, element2)

                    (entity, str, Seq(a))
                }
            }.toSet

        as.propertyStore.shutdown()

        validateProperties(as, properties, Set("AliasProperty"))

        // println("reachable methods: " + as.project.get(TypeBasedPointsToCallGraphKey).reachableMethods().toList.size)
    }

    private[this] def resolveSecondElement(
        an:           AnnotationLike,
        method:       Method,
        firstElement: AliasSourceElement,
        IDToEntity:   Map[String, Iterable[AliasSourceElement]]
    )(implicit as: TestContext): AliasSourceElement = {

        val tac: EOptionP[Method, TACAI] = as.propertyStore(method, TACAI.key)

        an match {
            case _: AnnotationLike if an.annotationType.asObjectType.simpleName.endsWith("UVar") => {

                val body: Code = method.body.get
                val lineNumber = getIntValue(an, "lineNumber")

                val pc = body.instructions.zipWithIndex
                    .filter(_._1 != null)
                    .filter(inst => body.lineNumber(inst._2).isDefined && body.lineNumber(inst._2).get == lineNumber)
                    .filterNot(inst =>
                        inst._1.isLoadConstantInstruction || inst._1.isLoadLocalVariableInstruction || inst._1.isStackManagementInstruction
                    )
                    .map(_._2)
                    .head

                val stmts = tac.ub.tac.get.stmts
                val stmt: Stmt[DUVar[ValueInformation]] = tac.ub.tac.get.stmts(tac.ub.tac.get.pcToIndex(pc))

                stmt match {
                    case c: Call[_]                                    => handleCall(an, c, method, stmts)
                    case expr: ExprStmt[DUVar[ValueInformation]]       => handleExpr(an, expr.expr, method, stmts)
                    case putStatic: PutStatic[DUVar[ValueInformation]] => handleExpr(an, putStatic.value, method, stmts)
                    case _                                             => throw new IllegalArgumentException("No UVar found")
                }

            }

            case _ => if (isNullAlias(an)) {
                    new AliasNull
                } else {
                    val matchingEntities = IDToEntity(getID(an)).toSeq.filter(!_.equals(firstElement))
                    if (matchingEntities.isEmpty) {
                        throw new IllegalArgumentException("No other entity with id " + getID(an) + " found")
                    }
                    if (matchingEntities.size > 1) {
                        throw new IllegalArgumentException("Multiple other entities with id " + getID(an) + " found")
                    }
                    matchingEntities.head
                }

        }
    }

    private[this] def handleCall(
        an:     AnnotationLike,
        c:      Call[_],
        method: Method,
        stmts:  Array[Stmt[DUVar[ValueInformation]]]
    )(implicit as: TestContext): AliasSourceElement = {

        val parameterIndex = getIntValue(an, "parameterIndex")
        val param: Expr[_] = if (parameterIndex == -1) c.receiverOption.get else c.params(parameterIndex)

        param match {
            case uVar: UVar[ValueInformation] => AliasUVar(persistentUVar(uVar)(stmts), method, as.project)
            case _                            => throw new IllegalArgumentException("No UVar found")
        }
    }

    private[this] def handleExpr(
        an:     AnnotationLike,
        expr:   Expr[DUVar[ValueInformation]],
        method: Method,
        stmts:  Array[Stmt[DUVar[ValueInformation]]]
    )(implicit as: TestContext): AliasSourceElement = {

        expr match {
            case c: Call[_]                   => handleCall(an, c, method, stmts)
            case uVar: UVar[ValueInformation] => AliasUVar(persistentUVar(uVar)(stmts), method, as.project)
            case _                            => throw new IllegalArgumentException("No UVar found")
        }
    }

    /**
     * Returns the id of the alias relation that is described by the given annotation.
     *
     * @param a The annotation that describes the alias relation.
     * @return The id of the alias relation.
     */
    private[this] def getID(a: AnnotationLike)(implicit as: TestContext): String = {
        getStringValue(a, "clazz") + "." + getIntValue(a, "id")
    }

    private[this] def getMethodID(a: AnnotationLike)(implicit as: TestContext): String = {
        getStringValue(a, "clazz") + "." + getIntValue(a, "methodID")
    }

    /**
     * Returns the value of the given annotation element.
     * @param a The annotation.
     * @param element The name of the element.
     * @return The value of the element.
     */
    private[this] def getIntValue(a: AnnotationLike, element: String)(implicit as: TestContext): Int = {
        a.elementValuePairs.filter(_.name == element).collectFirst {
            case ElementValuePair(`element`, value) => value.asIntValue.value
        }.getOrElse(as.project.classFile(a.annotationType.asObjectType).get.findMethod(
            element
        ).head.annotationDefault.get.asIntValue.value)
    }

    private[this] def getStringValue(a: AnnotationLike, element: String): String = {
        a.elementValuePairs.filter(_.name == element).head.value match {
            case str: StringValue => str.value
            case ClassValue(t)    => t.asObjectType.fqn
            case _                => throw new RuntimeException("Unexpected value type")
        }
    }

    //    private[this] def getIntArrayValue(a: AnnotationLike, element: String): Seq[Int] = {
    //        a.elementValuePairs.filter(_.name == element).head.value match {
    //            case arr: ArrayValue => arr.values.map(_.asIntValue.value)
    //            case _               => throw new RuntimeException("Unexpected value type")
    //        }
    //    }

    //    /**
    //     * Returns the value of the given annotation element.
    //     * @param a The annotation.
    //     * @param element The name of the element.
    //     * @return The value of the element.
    //     */
    //    private[this] def getStringValue(a: AnnotationLike, element: String): String = {
    //        a.elementValuePairs.filter(_.name == element).head.value match {
    //            case str: StringValue => str.value
    //            case ClassValue(t)    => t.asObjectType.fqn
    //            case _                => throw new RuntimeException("Unexpected value type")
    //        }
    //    }

    /**
     * Returns true if the given annotation describes an alias relation with null.
     * @param a The annotation.
     * @return True if the given annotation describes an alias relation with null.
     */
    private[this] def isNullAlias(a: AnnotationLike): Boolean = {
        a.elementValuePairs.find(_.name == "aliasWithNull").exists(_.value.asBooleanValue.value)
    }

    /**
     * Returns all alias annotations that are contained in the given annotation.
     * The given annotation must be an alias annotation.
     * @param a The annotation.
     * @return All alias annotations that are contained in the given annotation.
     */
    private[this] def getAliasAnnotations(a: Iterable[AnnotationLike]): Iterable[AnnotationLike] = {
        // getAliasAnnotations(a, "noAlias") ++ getAliasAnnotations(a, "mayAlias") ++ getAliasAnnotations(a, "mustAlias")
        a.filter(_.annotationType.asObjectType.simpleName.contains("Alias"))
            .filter(_.annotationType.asObjectType.simpleName != "AliasMethodID")
    }

    //    /**
    //     * Returns all alias annotations of the given type that are contained in the given annotation.
    //     * The given annotation must be an alias annotation.
    //     * @param a The annotation.
    //     * @param aliasType The type of the alias annotations.
    //     * @return All alias annotations of the given type that are contained in the given annotation.
    //     */
    //    private[this] def getAliasAnnotations(a: AnnotationLike, aliasType: String): Iterable[AnnotationLike] = {
    //        a.elementValuePairs.filter(_.name == aliasType).collect { case ev => ev.value.asArrayValue.values.map(_.asAnnotationValue.annotation) }.flatten
    //    }

    private[this] def getMethodIDs()(implicit as: TestContext): Map[String, Method] = {
        methodsWithAnnotations(as.project)
            .filter { case (_, _, a) => a.exists(_.annotationType.asObjectType.simpleName.equals("AliasMethodID")) }
            .map { case (m, _, a) =>
                getID(a.find(_.annotationType.asObjectType.simpleName.equals("AliasMethodID")).get) -> m
            }.toMap
    }

    // equivalent to PropertiesTest.fieldsWithAnnotations but with type annotations because the annotations are
    // recognized as type annotations for some reason
    private[this] def fieldsWithTypeAnnotations(
        recreatedFixtureProject: SomeProject
    ): Iterable[(Field, String => String, Iterable[AnnotationLike])] = {
        for {
            f <- recreatedFixtureProject.allFields // cannot be parallelized; "it" is not thread safe
            annotations = f.runtimeInvisibleTypeAnnotations
            if annotations.nonEmpty
        } yield {
            (f, (a: String) => f.toJava(s"@$a").substring(24), annotations)
        }
    }

}
