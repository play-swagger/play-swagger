package com.iheart.playSwagger.generator

import scala.meta.internal.parsers.ScaladocParser
import scala.meta.internal.{Scaladoc => iScaladoc}
import scala.quoted.staging

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.takezoe.scaladoc.Scaladoc
import com.iheart.playSwagger.ParametricType
import com.iheart.playSwagger.domain.Definition
import play.routes.compiler.Parameter

final case class DefinitionGenerator(
    mapper: SwaggerParameterMapper,
    swaggerPlayJava: Boolean = false,
    _mapper: ObjectMapper = new ObjectMapper(),
    namingConvention: NamingConvention = NamingConvention.None,
    embedScaladoc: Boolean = false
)(implicit cl: ClassLoader) extends DefinitionGeneratorCommons {
  private given staging.Compiler = staging.Compiler.make(cl)

  private def definition(pt: ParametricType): Definition = staging.withQuotes { q ?=>
    import q.reflect.{Definition as _, *}

    val scaladocAnnotSymbol = TypeRepr.of[Scaladoc].typeSymbol
    val RefinedTypeRepr = TypeRepr.of[eu.timepit.refined.api.Refined]

    def dealiasParams(tr: TypeRepr): TypeRepr = tr.dealias match
      case AppliedType(RefinedTypeRepr, List(t, _)) => t
      case AppliedType(tctor, args) => AppliedType(tctor, args.map(dealiasParams))
      case t => t

    def pprintTypeRepr(t: TypeRepr): String = t match
      case AppliedType(tctor, args) => s"${{pprintTypeRepr(tctor)}}[${args.map(pprintTypeRepr).mkString(",")}]"
      case t =>
        val s = t.show
        if s.startsWith("scala.") then t.show(using Printer.TypeReprShortCode) else s

    val ParametricType(tpe, reifiedTypeName, _, _) = pt
    val tr = TypeRepr.of(using tpe)
    val properties = if (swaggerPlayJava) {
      definitionForPOJO(Class.forName(tr.typeSymbol.fullName))
    } else {
      val paramDescriptions = Option.when(embedScaladoc)(tr.typeSymbol).toList
        .flatMap(_.getAnnotation(scaladocAnnotSymbol))
        .collect {
          case Apply(_, List(Literal(StringConstant(docstring)))) => docstring
        }
        .flatMap(ScaladocParser.parse)
        .flatMap(_.para)
        .flatMap(_.terms)
        .collect {
          case iScaladoc.Tag(iScaladoc.TagType.Param, Some(iScaladoc.Word(key)), Seq(text)) =>
            key -> scalaDocToMarkdown(text).toString
        }
        .toMap

      val fields =
        if tr.typeSymbol.primaryConstructor.isNoSymbol then Nil
        else tr.typeSymbol.primaryConstructor.tree.asInstanceOf[DefDef]
          .termParamss.headOption.map(_.params).getOrElse(Nil)
      fields.map { (field: ValDef) =>
        val sym = field.symbol
        val name = namingConvention(sym.name)
        val tpe = tr.memberType(sym)
        val rawTypeName = pprintTypeRepr(dealiasParams(tpe))
        val typeName = pt.resolve(rawTypeName)
        val param = Parameter(name, typeName, None, None)
        mapper.mapParam(param, paramDescriptions.get(sym.name))
      }
    }

    Definition(
      name = reifiedTypeName,
      properties = properties
    )
  }

  inline def definition[T]: Definition =
    val pt = staging.withQuotes { q ?=>
      import q.reflect.*
      val tpe = TypeRepr.of[T]
      ParametricType(tpe.asType, tpe.typeSymbol.fullName, tpe.typeSymbol.fullName, Map.empty)
    }
    definition(pt)

  def definition(reifiedTypeName: String): Definition =
    val pt = staging.withQuotes { q ?=>
      import q.reflect.*
      import scala.collection.immutable.SortedMap

      reifiedTypeName match {
        case ParametricType.ParametricTypeClassName(className, typeArgsStr) =>
          val sym = Symbol.requiredClass(className)
          val tpe = sym.typeRef
          val typeArgs = typeArgsStr.split(",").map(_.trim).toList
          val params = sym.declaredTypes.filter(_.isTypeParam).map(_.name)
          val typeArgsMapping = SortedMap(params.zip(typeArgs)*)
          ParametricType(tpe.asType, reifiedTypeName, className, typeArgsMapping)
        case className =>
          val sym = Symbol.requiredClass(className)
          ParametricType(sym.typeRef.asType, className, className, SortedMap.empty)
      }
    }
    definition(pt)
}

object DefinitionGenerator {
  def apply(
      mapper: SwaggerParameterMapper,
      swaggerPlayJava: Boolean,
      namingConvention: NamingConvention
  )(implicit cl: ClassLoader): DefinitionGenerator =
    new DefinitionGenerator(
      mapper,
      swaggerPlayJava,
      namingConvention = namingConvention
    )

  def apply(
      mapper: SwaggerParameterMapper,
      namingConvention: NamingConvention,
      embedScaladoc: Boolean
  )(implicit cl: ClassLoader): DefinitionGenerator =
    new DefinitionGenerator(
      mapper = mapper,
      namingConvention = namingConvention,
      embedScaladoc = embedScaladoc
    )
}
