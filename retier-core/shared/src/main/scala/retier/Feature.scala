package retier

object feature extends impl.Feature {
  implicit val noImplicitConversionBridge = new NoImplicitConversionBridge
}
