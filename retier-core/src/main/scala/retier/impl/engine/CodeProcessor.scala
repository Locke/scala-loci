package retier
package impl
package engine

import generators._
import scala.reflect.macros.blackbox.Context

object CodeProcessor {
  def apply[C <: Context](c: C): CodeProcessor[c.type] =
    new CodeProcessor[c.type](c)
}

class CodeProcessor[C <: Context](val c: C) extends
    Generation with
    PeerDefinitionCollector with
    StatementCollector with
    NamesValidator with
    PlacedExpressionsEraser with
    TransmissionGenerator with
    ProxyGenerator with
    OverrideBridgeGenerator with
    PeerDefinitionProcessor with
    PeerImplementationGenerator {
  import c.universe._

  def process(state: CodeWrapper[c.type]): CodeWrapper[c.type] = {
    val aggregator =
      Aggregator.create(state.body map InputStatement) add
      List(EnclosingContext(state.name, state.bases)) aggregate
      collectPeerDefinitions aggregate
      collectStatements aggregate
      validateNames aggregate
      erasePlacedExpressions aggregate
      generateTransmissions aggregate
      generateProxies aggregate
      generateOverrideBridge aggregate
      processPeerDefinitions aggregate
      generatePeerImplementations


    // TODO: erase conversions on placed values before erasing placed expressions

    state
  }
}
