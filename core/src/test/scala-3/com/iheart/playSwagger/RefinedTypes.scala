package com.iheart.playSwagger

import scala.collection.immutable

import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.{MinSize, NonEmpty}
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._

object RefinedTypes {
  type SpotifyAccount = String Refined And[MinSize[6], MatchesRegex["""@?(\\w){1,15}"""]]
  type Age = Int Refined Positive
  type NonEmptyList[A] = immutable.List[A] Refined NonEmpty
  type Albums = NonEmptyList[String]
}
