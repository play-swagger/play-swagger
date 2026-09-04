package com.iheart.playSwagger.domain

import com.iheart.playSwagger.domain.parameter.SwaggerParameter
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Writes, __}

final case class Definition(
    name: String,
    properties: Seq[SwaggerParameter],
    description: Option[String] = None,
    `type`: Option[String] = None,
    `enum`: Option[Seq[String]] = None
)

object Definition {
  implicit def writer(implicit paramWriter: Writes[Seq[SwaggerParameter]]): Writes[Definition] = (
    (__ \ Symbol("description")).writeNullable[String] ~
      (__ \ Symbol("type")).writeNullable[String] ~
      (__ \ Symbol("enum")).writeNullable[Seq[String]] ~
      (__ \ Symbol("properties")).writeNullable[Seq[SwaggerParameter]] ~
      (__ \ Symbol("required")).writeNullable[Seq[String]]
  ) { d =>
    if (d.`enum`.isDefined)
      (d.description, d.`type`, d.`enum`, None, None)
    else
      (d.description, d.`type`, d.`enum`, Some(d.properties), requiredProperties(d.properties))
  }

  private def requiredProperties(properties: Seq[SwaggerParameter]): Option[Seq[String]] = {
    val required = properties.filter(_.required).map(_.name)
    if (required.isEmpty) None else Some(required)
  }

}
