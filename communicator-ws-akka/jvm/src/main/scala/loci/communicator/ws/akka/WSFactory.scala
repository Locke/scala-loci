package loci
package communicator
package ws.akka

import scala.concurrent.duration.FiniteDuration

private object WSSetupParser
    extends ConnectionSetupParser
    with SimpleConnectionSetupProperties {
  val self: WS.type = WS

  def properties(implicit props: ConnectionSetupFactory.Properties) =
    WS.Properties()
      .set[FiniteDuration]("heartbeat-delay") { v => _.copy(heartbeatDelay = v) }
      .set[FiniteDuration]("heartbeat-timeout") { v => _.copy(heartbeatTimeout = v) }

  def parse(location: String): (Option[String], Option[Int]) =
    try {
      val index = location lastIndexOf ':'
      if (index != -1) {
        val hostInterface = location.substring(0, index)
        val port = Some(location.substring(index + 1).toInt)
        if (hostInterface.nonEmpty &&
            hostInterface.head == '[' && hostInterface.last == ']')
          (Some(hostInterface.substring(1, hostInterface.length - 1)), port)
        else
          (Some(hostInterface), port)
      }
      else
        (None, Some(location.toInt))
    }
    catch {
      case _: NumberFormatException =>
        (None, None)
    }
}

trait WSSetupFactory extends ConnectionSetupFactory.Implementation[WS] {
  val self: WS.type = WS

  val schemes = Seq("ws", "wss")

  protected def properties(
      implicit props: ConnectionSetupFactory.Properties): WS.Properties =
    WSSetupParser.properties

  protected def listener(
      url: String, scheme: String, location: String, properties: WS.Properties) =
    WSSetupParser parse location match {
      case (Some(interface), Some(port)) => Some(WS(port, interface, properties))
      case (None, Some(port)) => Some(WS(port, properties))
      case _ => None
    }

  protected def connector(
      url: String, scheme: String, location: String, properties: WS.Properties) =
    Some(WS(url, properties))
}

trait WSSecureSetupFactory extends ConnectionSetupFactory.Implementation[WS.Secure] {
  val self: WS.type = WS

  val schemes = Seq("wss")

  protected def properties(
      implicit props: ConnectionSetupFactory.Properties): WS.Properties =
    WSSetupParser.properties

  protected def listener(
      url: String, scheme: String, location: String, properties: WS.Properties) =
    WSSetupParser parse location match {
      case (Some(interface), Some(port)) => Some(WS.Secure(port, interface, properties))
      case (None, Some(port)) => Some(WS.Secure(port, properties))
      case _ => None
    }

  protected def connector(
      url: String, scheme: String, location: String, properties: WS.Properties) =
    Some(WS.Secure(url, properties))
}
