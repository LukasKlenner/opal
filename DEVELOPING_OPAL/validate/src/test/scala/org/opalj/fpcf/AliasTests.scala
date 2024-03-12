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
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.NoContext
import org.opalj.br.fpcf.properties.SimpleContexts
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasNull
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.tac.Call
import org.opalj.tac.DUVar
import org.opalj.tac.Expr
import org.opalj.tac.ExprStmt
import org.opalj.tac.PutField
import org.opalj.tac.PutStatic
import org.opalj.tac.ReturnValue
import org.opalj.tac.Stmt
import org.opalj.tac.UVar
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.persistentUVar
import org.opalj.tac.fpcf.analyses.alias.pointsto.LazyPointsToBasedAliasAnalysisScheduler
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
                // EagerPointsToBasedAliasAnalysisScheduler
                LazyPointsToBasedAliasAnalysisScheduler)
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

        implicit val simpleContexts: SimpleContexts = as.project.get(SimpleContextsKey)
        implicit val declaredMethods: DeclaredMethods = as.project.get(DeclaredMethodsKey)

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
                case (e, fun, a) =>
                    val ase1 = if (isUVarAlias(a) && (hasTwoUVars(a) || isNullAlias(a)))
                        resolveUVar(a, IDToMethod, useSecond = hasTwoUVars(a))
                    else
                        AliasSourceElement(e)(as.project)

                    val ase2 = resolveOtherElement(ase1, a, IDToMethod, IDToEntity, useSecond = false)

                    val entity = AliasEntity(createContext(ase1), createContext(ase2), ase1, ase2)

                    (entity, makeIdentifierFunctionUnique(a, fun), Seq(a))
            }
            .groupBy(_._1)
            .map(_._2.head) // remove duplicate entities

        properties.foreach {
            case (e, _, _) => as.propertyStore.force(e, Alias.key)
        }

        as.propertyStore.shutdown()

        validateProperties(as, properties, Set("AliasProperty"))

        // println("reachable methods: " + as.project.get(TypeBasedPointsToCallGraphKey).reachableMethods().toList.size)
    }

    private[this] def makeIdentifierFunctionUnique(an: AnnotationLike, fun: String => String)(
        implicit as: TestContext
    ): String => String = {
        an match {
            case _: AnnotationLike if isUVarAlias(an) =>
                (s: String) => fun(s) + ";lineNumber=" + getIntValue(an, "lineNumber")
            case _ => (s: String) => fun(s) + ";id=" + getID(an)
        }
    }

    private[this] def createContext(ase: AliasSourceElement)(
        implicit
        simpleContexts:  SimpleContexts,
        declaredMethods: DeclaredMethods
    ): Context = {

        if (ase.isMethodBound) {
            simpleContexts(declaredMethods(ase.method))
        } else {
            NoContext
        }
    }

    private[this] def resolveOtherElement(
        firstElement: AliasSourceElement,
        a:            AnnotationLike,
        IDToMethod:   Map[String, Method],
        IDToEntity:   Map[String, Iterable[AliasSourceElement]],
        useSecond:    Boolean
    )(implicit as: TestContext): AliasSourceElement = {

        a match {
            case _: AnnotationLike if isNullAlias(a) => AliasNull
            case _: AnnotationLike if isUVarAlias(a) => resolveUVar(a, IDToMethod, useSecond)
            case _ =>
                val matchingEntities = IDToEntity(getID(a)).toSeq.filter(!_.equals(firstElement))
                if (matchingEntities.isEmpty)
                    throw new IllegalArgumentException("No other entity with id " + getID(a) + " found")
                if (matchingEntities.size > 1)
                    throw new IllegalArgumentException("Multiple other entities with id " + getID(a) + " found")
                matchingEntities.head
        }
    }

    private[this] def resolveUVar(
        a:          AnnotationLike,
        IDToMethod: Map[String, Method],
        useSecond:  Boolean
    )(implicit as: TestContext): AliasSourceElement = {
        val method = IDToMethod(getMethodID(a, useSecond))
        val tac: EOptionP[Method, TACAI] = as.propertyStore(method, TACAI.key)
        val body: Code = method.body.get
        val lineNumber = getIntValue(a, if (useSecond) "secondLineNumber" else "lineNumber")

        val pc = body.instructions.zipWithIndex
            .filter(_._1 != null)
            .filter(inst => body.lineNumber(inst._2).isDefined && body.lineNumber(inst._2).get == lineNumber)
            .filterNot(inst =>
                inst._1.isLoadConstantInstruction || inst._1.isLoadLocalVariableInstruction || inst._1.isStackManagementInstruction
            )
            .map(_._2)
            .headOption
            .orElse(throw new IllegalArgumentException(
                "No instruction found for line number " + lineNumber + " in method " + method.toJava
            ))
            .get

        val stmts = tac.ub.tac.get.stmts
        val stmt: Stmt[DUVar[ValueInformation]] = tac.ub.tac.get.stmts(tac.ub.tac.get.pcToIndex(pc))

        stmt match {
            case c: Call[_]                              => handleCall(a, c, method, stmts, useSecond)
            case expr: ExprStmt[DUVar[ValueInformation]] => handleExpr(a, expr.expr, method, stmts, useSecond)
            case putStatic: PutStatic[DUVar[ValueInformation]] =>
                handleExpr(a, putStatic.value, method, stmts, useSecond)
            case putField: PutField[DUVar[ValueInformation]] => handleExpr(a, putField.value, method, stmts, useSecond)
            case returnValue: ReturnValue[DUVar[ValueInformation]] =>
                handleExpr(a, returnValue.expr, method, stmts, useSecond)
            case _ => throw new IllegalArgumentException(
                    "No UVar found at line number " + lineNumber + " in method " + method.toJava
                )
        }
    }

    private[this] def handleCall(
        an:        AnnotationLike,
        c:         Call[_],
        method:    Method,
        stmts:     Array[Stmt[DUVar[ValueInformation]]],
        useSecond: Boolean
    )(implicit as: TestContext): AliasSourceElement = {

        val parameterIndex = getIntValue(an, if (useSecond) "secondParameterIndex" else "parameterIndex")
        val param: Expr[_] = if (parameterIndex == -1) c.receiverOption.get else c.params(parameterIndex)

        param match {
            case uVar: UVar[ValueInformation] => AliasUVar(persistentUVar(uVar)(stmts), method, as.project)
            case _                            => throw new IllegalArgumentException("No UVar found")
        }
    }

    private[this] def handleExpr(
        an:        AnnotationLike,
        expr:      Expr[DUVar[ValueInformation]],
        method:    Method,
        stmts:     Array[Stmt[DUVar[ValueInformation]]],
        useSecond: Boolean
    )(implicit as: TestContext): AliasSourceElement = {

        expr match {
            case c: Call[_]                   => handleCall(an, c, method, stmts, useSecond)
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

    private[this] def getMethodID(a: AnnotationLike, useSecond: Boolean)(implicit as: TestContext): String = {
        getStringValue(a, "clazz") + "." + getIntValue(a, if (useSecond) "secondMethodID" else "methodID")
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
        }.getOrElse(as.project.classFile(a.annotationType.asObjectType).get.findMethod(element)
            .head.annotationDefault.get.asIntValue.value)
    }

    private[this] def getStringValue(a: AnnotationLike, element: String): String = {

        val evp = a.elementValuePairs.filter(_.name == element)

        if (evp.isEmpty) {
            throw new RuntimeException("No element value pair found for " + element)
        }

        evp.head.value match {
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

    private[this] def isUVarAlias(a: AnnotationLike): Boolean = {
        annotationName(a).endsWith("UVar")
    }

    private[this] def hasTwoUVars(a: AnnotationLike)(implicit as: TestContext): Boolean = {
        getIntValue(a, "secondMethodID") != -1
    }

    /**
     * Returns all alias annotations that are contained in the given annotation.
     * The given annotation must be an alias annotation.
     * @param a The annotation.
     * @return All alias annotations that are contained in the given annotation.
     */
    private[this] def getAliasAnnotations(a: Iterable[AnnotationLike]): Iterable[AnnotationLike] = {
        a.filter(annotationName(_).contains("Alias"))
            .filter(annotationName(_) != "AliasMethodID")
            .flatMap {
                case a: AnnotationLike
                    if annotationName(a).endsWith("UVars") || annotationName(a).endsWith("Aliases") =>
                    val x = a.elementValuePairs.filter(_.name == "value")
                    x.head.value.asArrayValue.values.map(_.asAnnotationValue.annotation)

                case a => Seq(a)
            }
    }

    private[this] def annotationName(a: AnnotationLike): String = {
        a.annotationType.asObjectType.simpleName
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
