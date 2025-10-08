package com.iheart.playSwagger.generator

import scala.collection.JavaConverters
import scala.meta.internal.{Scaladoc => iScaladoc}

import com.fasterxml.jackson.databind.BeanDescription
import com.iheart.playSwagger.domain.Definition
import com.iheart.playSwagger.domain.parameter.{GenSwaggerParameter, SwaggerParameter}
import net.steppschuh.markdowngenerator.MarkdownElement
import net.steppschuh.markdowngenerator.link.Link
import net.steppschuh.markdowngenerator.table.Table
import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.code.{Code, CodeBlock}
import net.steppschuh.markdowngenerator.text.heading.Heading
import play.routes.compiler.Parameter

// Common part of the DefinitionGenerator shared by the Scala 2 and Scala 3
abstract class DefinitionGeneratorCommons(implicit cl: ClassLoader) { self: DefinitionGenerator =>
  private[generator] def scalaDocToMarkdown: PartialFunction[iScaladoc.Term, MarkdownElement] = {
    case value: iScaladoc.Text =>
      new Text(value.parts.map(_.part).map {
        case word: iScaladoc.Word => new Text(word.value)
        case link: iScaladoc.Link => new Link(link.anchor.mkString(" "), link.ref)
        case code: iScaladoc.CodeExpr => new Code(code.code)
      }.mkString(" "))
    case code: iScaladoc.CodeBlock => new CodeBlock(code, "scala")
    case code: iScaladoc.MdCodeBlock =>
      new CodeBlock(code.code.mkString("\n"), code.info.mkString(":"))
    case head: iScaladoc.Heading => new Heading(head, 1)
    case table: iScaladoc.Table =>
      val builder = new Table.Builder().withAlignments(Table.ALIGN_RIGHT, Table.ALIGN_LEFT).addRow(
        table.header.cols: _*
      )
      table.rows.foreach(row => builder.addRow(row.cols: _*))
      builder.build()
    // TODO: Support List
    // https://github.com/Steppschuh/Java-Markdown-Generator/pull/13
    case _ => new Text("")
  }

  private[generator] def definitionForPOJO(clazz: java.lang.reflect.Type): Seq[SwaggerParameter] = {
    val `type` = _mapper.constructType(clazz)
    val beanDesc: BeanDescription = _mapper.getSerializationConfig.introspect(`type`)
    val beanProperties = beanDesc.findProperties
    val ignoreProperties = beanDesc.getIgnoredPropertyNames
    val propertySet = JavaConverters.asScalaIteratorConverter(beanProperties.iterator()).asScala.toSeq
    propertySet.filter(bd => !ignoreProperties.contains(bd.getName)).map { entry =>
      val name = entry.getName
      val className = entry.getPrimaryMember.getType.getRawClass.getName
      val generalTypeName = if (entry.getField != null && entry.getField.getType.hasGenericTypes) {
        val generalType = entry.getField.getType.getContentType.getRawClass.getName
        s"$className[$generalType]"
      } else {
        className
      }
      val typeName = if (!entry.isRequired) {
        s"Option[$generalTypeName]"
      } else {
        generalTypeName
      }
      val param = Parameter(name, typeName, None, None)
      mapper.mapParam(param, None)
    }
  }

  def allDefinitions(typeNames: Seq[String]): List[Definition] = {
    def genSwaggerParameter: PartialFunction[SwaggerParameter, GenSwaggerParameter] = {
      case p: GenSwaggerParameter => p
    }

    def allReferredDefs(defName: String, memo: List[Definition]): List[Definition] = {
      def findRefTypes(p: GenSwaggerParameter): Seq[String] =
        p.referenceType.toSeq ++ {
          p.items.toSeq.collect(genSwaggerParameter).flatMap(findRefTypes)
        }

      memo.find(_.name == defName) match {
        case Some(_) => memo
        case None =>
          val thisDef = definition(defName)
          val refNames: Seq[String] = for {
            p <- thisDef.properties.collect(genSwaggerParameter)
            className <- findRefTypes(p)
            if mapper.isReference(className)
          } yield className

          refNames.foldLeft(thisDef :: memo) { (foundDefs, refName) =>
            allReferredDefs(refName, foundDefs)
          }
      }
    }

    typeNames.foldLeft(List.empty[Definition]) { (memo, typeName) =>
      allReferredDefs(typeName, memo)
    }
  }
}
