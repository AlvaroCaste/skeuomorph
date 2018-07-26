package skeuomorph
package protobuf

import turtles._
import turtles.data.Fix
import turtles.implicits._

object util {

  import Schema._

  def printOption(o: Option): String = s"${o.name} = ${o.value}"

  def printType: Algebra[Schema, String] = {
    case TDouble()        => "double"
    case TFloat()         => "float"
    case TInt32()         => "int32"
    case TInt64()         => "int64"
    case TUint32()        => "uint32"
    case TUint64()        => "uint64"
    case TSint32()        => "sint32"
    case TSint64()        => "sint64"
    case TFixed32()       => "fixed32"
    case TFixed64()       => "fixed64"
    case TSfixed32()      => "sfixed32"
    case TSfixed64()      => "sfixed64"
    case TBool()          => "bool"
    case TString()        => "string"
    case TBytes()         => "bytes"
    case TNamedType(name) => name

    case TRequired(value) => s"required $value"
    case TOptional(value) => s"optional $value"
    case TRepeated(value) => s"repeated $value"

    case TEnum(name, symbols, options, aliases) =>
      val printOptions = options.map(o => s"option ${o.name} = ${o.value}").mkString("\n")
      val printSymbols = symbols.map({ case (s, i) => s"$s = $i;" }).mkString("\n")
      val printAliases = aliases.map({ case (s, i) => s"$s = $i;" }).mkString("\n")

      s"""
      enum $name {
        $printOptions
        $printSymbols
        $printAliases
      }
      """
    case TMessage(name, fields, reserved) =>
      val printReserved = reserved.map(l => s"reserved " + l.mkString(", ")).mkString("\n")
      def printOptions(options: List[Option]) =
        if (options.isEmpty) {
          ""
        } else {
          options.map(printOption).mkString(" [", ", ", "]")
        }

      val printFields =
        fields
          .map(f => s"""${f.tpe} ${f.name} = ${f.position}${printOptions(f.options)};""")
          .mkString("\n")
      s"""
      message $name {
        $printReserved
        $printFields
      }
      """
  }

  /*
message SearchRequest {
  required string query = 1;
  optional int32 page_number = 2;
  optional int32 result_per_page = 3 [default = 10];
  optional Corpus corpus = 4 [default = UNIVERSAL];
}
   */
  def searchRequest[T](implicit T: Corecursive.Aux[T, Schema]): T =
    TMessage[T](
      "SearchRequest",
      List(
        Field("query", TRequired[T](TString[T]().embed).embed, 1, Nil),
        Field("page_number", TOptional[T](TInt32[T]().embed).embed, 2, Nil),
        Field("results_per_page", TOptional[T](TInt32[T]().embed).embed, 3, List(Option("default", "10"))),
        Field("corpus", TOptional[T](TNamedType[T]("Corpus").embed).embed, 3, List(Option("default", "UNIVERSAL")))
      ),
      Nil
    ).embed

  val a = searchRequest[Fix[Schema]].cata(printType)

}