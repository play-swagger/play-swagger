package models

import scala.collection.immutable

import enumeratum.{Enum, EnumEntry}

sealed trait Color extends EnumEntry

object Color extends Enum[Color] {
  val values: immutable.IndexedSeq[Color] = findValues

  case object RED extends Color
  case object GREEN extends Color
  case object PUCE extends Color
}
