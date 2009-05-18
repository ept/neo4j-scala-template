package com.example

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import com.jteigen.scalatest.JUnit4Runner

@RunWith(classOf[JUnit4Runner])
class DummySpec extends Spec with ShouldMatchers {

  describe("Dummy") {
    it("should pass the test") {
      1 should equal(1)
    }
  }
  
}
