package exercises

import zio.test._

object E1_IntroSpec extends ZIOSpecDefault {
  override def spec =
    suite("☃ Ex. 1 — Intro")(
      test("helloWorld") {
        assertTrue(E1_Intro.helloWorld == "Hello, World!")
      }
    )
}
