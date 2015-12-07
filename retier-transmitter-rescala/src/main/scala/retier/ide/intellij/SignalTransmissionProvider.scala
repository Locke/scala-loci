package retier
package ide.intellij

import typeconstraints._
import transmission._
import rescala.Signal
import scala.language.implicitConversions
import scala.language.higherKinds

protected[retier] trait SignalTransmissionProvider {
    this: rescalaTransmitter.type =>

  @annotation.compileTimeOnly("Used to guide IntelliJ IDEA Scala Plugin type inference. Do not use directly.")
  implicit def $$retier$transmitMultipleRescalaSignal
    [V, T, U, S, P, Sig[U] <: Signal[U], L <: Peer, R <: Peer](v: V)
    (implicit
        dummy: IntelliJDummy,
        ev0: TransmissionProperties[V, T, R, L, MultipleConnection],
        ev1: T <:< Sig[U],
        ev2: Transmittable[U, S, P],
        ev3: MultipleTransmission[Sig[P], R, L] => RescalaSignalMultipleTransmissionProvider[Sig, P, R, L]):
    RescalaSignalMultipleTransmissionProvider[Sig, P, R, L] = ???

  @annotation.compileTimeOnly("Used to guide IntelliJ IDEA Scala Plugin type inference. Do not use directly.")
  implicit def $$retier$transmitOptionalRescalaSignal
    [V, T, U, S, P, Sig[U] <: Signal[U], L <: Peer, R <: Peer](v: V)
    (implicit
        dummy: IntelliJDummy,
        ev0: TransmissionProperties[V, T, R, L, OptionalConnection],
        ev1: T <:< Sig[U],
        ev2: Transmittable[U, S, P],
        ev3: OptionalTransmission[Sig[P], R, L] => RescalaSignalOptionalTransmissionProvider[Sig, P, R, L]):
    RescalaSignalOptionalTransmissionProvider[Sig, P, R, L] = ???

  @annotation.compileTimeOnly("Used to guide IntelliJ IDEA Scala Plugin type inference. Do not use directly.")
  implicit def $$retier$transmitSingleRescalaSignal
    [V, T, U, S, P, Sig[U] <: Signal[U], L <: Peer, R <: Peer](v: V)
    (implicit
        dummy: IntelliJDummy,
        ev0: TransmissionProperties[V, T, R, L, SingleConnection],
        ev1: T <:< Sig[U],
        ev2: Transmittable[U, S, P],
        ev3: SingleTransmission[Sig[P], R, L] => RescalaSignalSingleTransmissionProvider[Sig, P, R, L]):
    RescalaSignalSingleTransmissionProvider[Sig, P, R, L] = ???
}