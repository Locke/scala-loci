package loci
package transmitter

import _root_.rescala.core.{ReSerializable, Scheduler, Struct}
import _root_.rescala.interface.RescalaInterface
import _root_.rescala.macros.cutOutOfUserComputation
import _root_.rescala.reactives.Signal

import scala.language.higherKinds

protected[transmitter] trait SignalAccessor {
  private final val asLocalId = 0

  implicit class RescalaSignalMultipleAccessor
      [Sig[T, St <: Struct] <: Signal[T, St], St <: Struct, V, R, T, L](
       value: V from R)(implicit
       ev: Transmission[V, R, Sig[T, St], L, Multiple],
       val scheduler: Scheduler[St]) extends RemoteAccessor {
    val interface = RescalaInterface.interfaceFor(scheduler)

    import interface.{Signal, Var, transaction}

     @cutOutOfUserComputation lazy val asLocal: Signal[Seq[(Remote[R], Signal[T])]] =
      value.cache(asLocalId) {
        implicit val serializer: ReSerializable[Seq[(Remote[R], Signal[T])]] =
          ReSerializable.noSerializer

        val mapping = transaction() { _ => Var(Seq.empty[(Remote[R], Signal[T])]) }

        def update() = mapping.set(value.remotes zip value.retrieveValues)

        value.remoteJoined notify { _ => update() }
        value.remoteLeft notify { _ => update() }
        update()

        mapping
      }
  }

  implicit class RescalaSignalOptionalAccessor
      [Sig[T, St <: Struct] <: Signal[T, St], St <: Struct, V, R, T, L](
       value: V from R)(implicit
       ev: Transmission[V, R, Sig[T, St], L, Optional],
       val scheduler: Scheduler[St]) extends RemoteAccessor {
    val interface = RescalaInterface.interfaceFor(scheduler)

    import interface.{Signal, Var, transaction}

     @cutOutOfUserComputation lazy val asLocal: Signal[Option[T]] =
      value.cache(asLocalId) {
        implicit val serializer: ReSerializable[Option[Signal[T]]] =
          ReSerializable.noSerializer

        val option = transaction() { _ => Var(Option.empty[Signal[T]]) }

        def update() = option.set(value.retrieveValue)

        value.remoteJoined notify { _ => update() }
        value.remoteLeft notify { _ => update() }
        update()

        option.flatten
      }
  }

  implicit class RescalaSignalSingleAccessor
      [Sig[T, St <: Struct] <: Signal[T, St], St <: Struct, V, R, T, L](
       value: V from R)(implicit
       ev: Transmission[V, R, Sig[T, St], L, Single],
       val scheduler: Scheduler[St]) extends RemoteAccessor {
    val interface = RescalaInterface.interfaceFor(scheduler)

    import interface.Signal

     @cutOutOfUserComputation lazy val asLocal: Signal[T] =
      value.retrieveValue
  }
}
