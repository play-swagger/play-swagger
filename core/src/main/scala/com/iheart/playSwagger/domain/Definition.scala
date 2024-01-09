package com.iheart.playSwagger.domain

import com.iheart.playSwagger.domain.parameter.SwaggerParameter
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Writes, __}

final case class Definition(
    name: String,
    properties: Seq[SwaggerParameter],
    description: Option[String] = None
)

object Definition {
  implicit def writer(implicit paramWriter: Writes[Seq[SwaggerParameter]]): Writes[Definition] = (
    (__ \ Symbol("description")).writeNullable[String] ~
      (__ \ Symbol("properties")).write[Seq[SwaggerParameter]] ~
      (__ \ Symbol("required")).writeNullable[Seq[String]]
  )((d: Definition) => (d.description, d.properties, requiredProperties(d.properties)))

  private def requiredProperties(properties: Seq[SwaggerParameter]): Option[Seq[String]] = {
    val required = properties.filter(_.required).map(_.name)
    if (required.isEmpty) None else Some(required)
  }

}
