package com.iheart.playSwagger

import scala.util.matching.Regex
import scala.quoted.Type

case class ParametricType(
    tpe: Type[?],
    reifiedTypeName: String,
    className: String,
    typeArgsMapping: Map[Line, String]
) {
  val resolve: String => String = {
    case ParametricType.ParametricTypeClassName(className, typeArgs) =>
      val resolvedTypes =
        typeArgs
          .split(",")
          .map(_.trim)
          .map(tn => typeArgsMapping.getOrElse(tn, resolve(tn)))
      s"$className[${resolvedTypes.mkString(",")}]"
    case cn => typeArgsMapping.getOrElse(cn, cn)
  }
}

object ParametricType {
  final val ParametricTypeClassName: Regex = "^(.*?)\\[(.*)\\]$".r
}