package loci
package registry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class RegistrySelfSpec extends AnyFlatSpec with Matchers with NoLogging {
  behavior of "RegistrySelf"

  it should "handle self binding and lookup correctly" in {
    for (_ <- 1 to 50) {
      RegistryTests.`handle self binding and lookup correctly`(
        cleanup = {}
      )
    }
  }

  it should "handle self subjective binding and lookup correctly" in {
    for (_ <- 1 to 50) {
      RegistryTests.`handle self subjective binding and lookup correctly`(
        cleanup = {}
      )
    }
  }
}
