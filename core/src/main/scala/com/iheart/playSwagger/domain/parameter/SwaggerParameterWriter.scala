package com.iheart.playSwagger.domain.parameter

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
class SwaggerParameterWriter(swaggerV3: Boolean) {

  private val nullableName: String = if (swaggerV3) "nullable" else "x-nullable"

  private val under: JsPath = if (swaggerV3) __ \ "schema" else __

  val referencePrefix: String = if (swaggerV3) "#/components/schemas/" else "#/definitions/"

  private lazy val propWrites: Writes[SwaggerParameter] = Writes {
    case g: GenSwaggerParameter => genPropWrites.writes(g)
    case c: CustomSwaggerParameter => customPropWrites.writes(c)
  }

  private val customPropWrites: Writes[CustomSwaggerParameter] = Writes { cwp =>
    (__ \ "default").writeNullable[JsValue].writes(cwp.default) ++
      (__ \ nullableName).writeNullable[Boolean].writes(cwp.nullable) ++
      (cwp.specAsProperty orElse cwp.specAsParameter.headOption).getOrElse(Json.obj())
  }

  def customParamWrites(csp: CustomSwaggerParameter): List[JsObject] = {
    csp.specAsParameter match {
      case head :: tail =>
        val w = (
          (__ \ Symbol("name")).write[String] ~
            (__ \ Symbol("required")).write[Boolean] ~
            (under \ nullableName).writeNullable[Boolean] ~
            (under \ Symbol("default")).writeNullable[JsValue]
        )((c: CustomSwaggerParameter) => (c.name, c.required, c.nullable, c.default))
        (w.writes(csp) ++ withPrefix(head)) :: tail
      // 要素が1つの場合は `elem :: Nil` になるので残りは `Nil` のみ
      case Nil => Nil
    }
  }

  private def withPrefix(input: JsObject): JsObject = {
    if (swaggerV3) Json.obj("schema" -> input) else input
  }

  private val refWrite: Writes[String] = Writes { (refType: String) =>
    Json.obj("$ref" -> JsString(referencePrefix + refType))
  }

  val genParamWrites: OWrites[GenSwaggerParameter] = {
    (
      (__ \ "name").write[String] ~
        (__ \ "required").write[Boolean] ~
        (__ \ "description").writeNullable[String] ~
        // referenceType は `schema: $ref: ` という表記になる
        (__ \ "schema").writeNullable[String](refWrite) ~
        (under \ "type").writeNullable[String] ~
        (under \ "format").writeNullable[String] ~
        (under \ nullableName).writeNullable[Boolean] ~
        (under \ "default").writeNullable[JsValue] ~
        (under \ "example").writeNullable[JsValue] ~
        (under \ "items").writeNullable[SwaggerParameter](propWrites) ~
        (under \ "enum").writeNullable[Seq[String]]
    ) { p =>
      Tuple11(
        _1 = p.name,
        _2 = p.required,
        _3 = p.description,
        _4 = p.referenceType,
        _5 = p.`type`,
        _6 = p.format,
        _7 = p.nullable,
        _8 = p.default,
        _9 = p.example,
        _10 = p.items,
        _11 = p.`enum`
      )
    }
  }

  private val genPropWrites: Writes[GenSwaggerParameter] = {

    val writesBuilder = (__ \ "type").writeNullable[String] ~
      (__ \ "format").writeNullable[String] ~
      (__ \ nullableName).writeNullable[Boolean] ~
      (__ \ "default").writeNullable[JsValue] ~
      (__ \ "example").writeNullable[JsValue] ~
      (__ \ "$ref").writeNullable[String] ~
      (__ \ "items").lazyWriteNullable[SwaggerParameter](propWrites) ~
      (__ \ "enum").writeNullable[Seq[String]] ~
      (__ \ "description").writeNullable[String]

    writesBuilder { p =>
      Tuple9(
        _1 = p.`type`,
        _2 = p.format,
        _3 = p.nullable,
        _4 = p.default,
        _5 = p.example,
        _6 = p.referenceType.map(referencePrefix + _),
        _7 = p.items,
        _8 = p.`enum`,
        _9 = p.description
      )
    }
  }

  implicit val propertiesWriter: Writes[Seq[SwaggerParameter]] = Writes[Seq[SwaggerParameter]] { ps =>
    JsObject(ps.map(p => p.name -> Json.toJson(p)(propWrites)))
  }

}
