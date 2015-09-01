package retier
package impl
package engine.generators

import engine._
import scala.reflect.macros.blackbox.Context

trait Generation {
  val c: Context
  import c.universe._

  object types {
    val retier = typeOf[_root_.retier.`package`.type]
    val peer = typeOf[Peer]

    val localOn = typeOf[_ localOn _]
    val issued = typeOf[_ <-> _]
    val issuedControlled = typeOf[_ <=> _]

    val placing = typeOf[PlacingExpression[_]]
    val issuing = typeOf[IssuingExpression[_, _]]
    val overriding = typeOf[OverridingExpression[_]]

    val functionPlacing = Seq(typeOf[_ => _], typeOf[(_ => _) localOn _])
    val issuedPlacing = Seq(typeOf[_ <=> _], typeOf[(_ <=> _) localOn _])

    val transmissionProvider = typeOf[transmission.TransmissionProvider]
  }

  object symbols {
    val retier = types.retier.termSymbol

    val placedApply = types.placing member TermName("apply")
    val placedShared = types.placing member TermName("shared")
    val placedLocal = types.placing member TermName("local")
    val placedIssued = types.placing member TermName("issued")
    val placedIssuedApply = types.issuing member TermName("apply")
    val placedOverriding = types.overriding member TermName("overriding")

    val placed = Seq(placedApply, placedShared, placedLocal, placedIssuedApply)

    val transmitMultiple = types.retier member TermName("transmitMultiple")
    val transmitOptional = types.retier member TermName("transmitOptional")
    val transmitSingle = types.retier member TermName("transmitSingle")

    val transmit = Seq(transmitMultiple, transmitOptional, transmitSingle)

    val discardValue = types.retier member TermName("discardValue")
    val issueValue = types.retier member TermName("issueValue")
    val issueValueControlled = types.retier member TermName("issueValueControlled")

    val reduceCapture = types.retier member TermName("reduceCapture")
    val liftCapture = types.retier member TermName("liftCapture")

    val liftValueGlobally = types.retier member TermName("liftValueGlobally")
    val liftValueLocally = types.retier member TermName("liftValueLocally")
    val downcastValueGlobally = types.retier member TermName("downcastValueGlobally")
    val downcastValueLocally = types.retier member TermName("downcastValueLocally")

    val globalCasts = Seq(discardValue, issueValue, issueValueControlled,
      liftValueGlobally, downcastValueGlobally)
    val localCasts = Seq(reduceCapture, liftCapture, liftValueLocally,
      downcastValueLocally)
    val casts = globalCasts ++ localCasts
  }

  object trees {
    val Unit = tq"_root_.scala.Unit"
    val Option = tq"_root_.scala.Option"
    val OptionEmpty = q"_root_.scala.Option.empty"
    val Some = q"_root_.scala.Some"
    val None = q"_root_.scala.None"
    val Try = tq"_root_.scala.util.Try"
    val Success = q"_root_.scala.util.Success"
    val Failure = q"_root_.scala.util.Failure"
    val implicitly = q"_root_.scala.Predef.implicitly"
    val AbstractionId = tq"_root_.retier.transmission.AbstractionId"
    val AbstractionIdCreate = q"_root_.retier.impl.AbstractionId.create"
    val Marshallable = tq"_root_.retier.transmission.Marshallable"
    val PeerTypeTag = tq"_root_.retier.PeerTypeTag"
    val TransmissionProperties = tq"_root_.retier.impl.TransmissionProperties"
    val TransmissionPropertiesCreate = q"_root_.retier.impl.TransmissionProperties.create"
  }


  case class InputStatement(stat: Tree)

  case class PeerDefinition(tree: Tree, peer: Type, parents: List[Tree])

  case class PlacedStatement(tree: Tree, peerType: Type, exprType: Type,
      declTypeTree: Option[Tree], overridingDecl: Option[TermName], expr: Tree)

  case class NonPlacedStatement(tree: Tree)


  val echo = Echo(c)

  val typer = Typer(c)


  def retierTermName(name: String) = TermName(s"$$$$retier$$$name")

  def retierTypeName(name: String) = TypeName(s"$$$$retier$$$name")


  implicit class TypeOps(tpe: Type) {
    def =:!=(that: Type): Boolean = !(tpe =:= that)
    def <:!<(that: Type): Boolean = !(tpe <:< that)
    def isGeneric: Boolean = tpe exists { _.typeSymbol.isParameter }
  }
}