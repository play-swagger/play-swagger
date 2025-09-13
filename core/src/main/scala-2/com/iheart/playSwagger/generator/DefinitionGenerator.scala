package com.iheart.playSwagger.generator

import scala.meta.internal.parsers.ScaladocParser
import scala.meta.internal.{Scaladoc => iScaladoc}
import scala.reflect.runtime.universe._

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

  private val refinedTypePattern = raw"(eu\.timepit\.refined\.api\.Refined(?:\[.+])?)".r

  private def dealiasParams(t: Type): Type = {
    t.toString match {
      case refinedTypePattern(_) => t.typeArgs.headOption.getOrElse(t)
      case _ =>
        appliedType(
          t.dealias.typeConstructor,
          t.typeArgs.map { arg =>
            dealiasParams(arg.dealias)
          }
        )
    }
  }

  def definition: ParametricType => Definition = {
    case parametricType @ ParametricType(tpe, reifiedTypeName, _, _) =>
      val properties = if (swaggerPlayJava) {
        val clazz = runtimeMirror(cl).runtimeClass(tpe.typeSymbol.asClass)
        definitionForPOJO(clazz)
      } else {
        val fields = tpe.decls.collectFirst {
          case m: MethodSymbol if m.isPrimaryConstructor => m
        }.toList.flatMap(_.paramLists).headOption.getOrElse(Nil)

        val paramDescriptions = if (embedScaladoc) {
          val scaladoc = for {
            annotation <- tpe.typeSymbol.annotations
            if typeOf[Scaladoc] == annotation.tree.tpe
            value <- annotation.tree.children.tail.headOption
            docTree <- value.children.tail.headOption
            docString = docTree.toString().tail.init.replace("\\n", "\n")
            doc <- ScaladocParser.parse(docString)
          } yield doc

          (for {
            doc <- scaladoc
            paragraph <- doc.para
            term <- paragraph.terms
            tag <- term match {
              case iScaladoc.Tag(iScaladoc.TagType.Param, Some(iScaladoc.Word(key)), Seq(text)) =>
                Some(key -> text)
              case _ => None
            }
          } yield tag).map {
            case (name, term) => name -> scalaDocToMarkdown(term).toString
          }.toMap
        } else {
          Map.empty[String, String]
        }

        fields.map { (field: Symbol) =>
          // TODO: find a better way to get the string representation of typeSignature
          val name = namingConvention(field.name.decodedName.toString)

          val rawTypeName = dealiasParams(field.typeSignature).toString match {
            case refinedTypePattern(_) => field.info.dealias.typeArgs.head.toString
            case v => v
          }
          val typeName = parametricType.resolve(rawTypeName)
          // passing None for 'fixed' and 'default' here, since we're not dealing with route parameters
          val param = Parameter(name, typeName, None, None)
          mapper.mapParam(param, paramDescriptions.get(field.name.decodedName.toString))
        }
      }

      Definition(
        name = reifiedTypeName,
        properties = properties
      )
  }

  def definition[T: TypeTag]: Definition = definition(ParametricType[T])

  def definition(className: String): Definition = definition(ParametricType(className))
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
