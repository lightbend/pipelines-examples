package pipelines.examples.carly.ingestor

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.testkit._
import org.scalatest._
import org.scalatest.concurrent._

import pipelines.akkastream.testkit._

import pipelines.examples.carly.data._

class CallRecordValidationSpec extends WordSpec with MustMatchers with ScalaFutures with BeforeAndAfterAll {
  private implicit val system = ActorSystem("CallRecordValidationSpec")
  private implicit val mat = ActorMaterializer()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A CallRecordValidation" should {
    "split incoming data into valid call records and those outside the time range" in {
      val testkit = AkkaStreamletTestKit(system, mat)

      val instant = Instant.now.toEpochMilli / 1000
      val past = Instant.now.minus(5000, ChronoUnit.DAYS).toEpochMilli / 1000

      val cr1 = CallRecord("user-1", "user-2", "f", 10L, instant)
      val cr2 = CallRecord("user-1", "user-2", "f", 15L, instant)
      val cr3 = CallRecord("user-1", "user-2", "f", 18L, instant)
      val cr4 = CallRecord("user-1", "user-2", "f", 40L, past)
      val cr5 = CallRecord("user-1", "user-2", "f", 70L, past)

      val source = Source(Vector(cr1, cr2, cr3, cr4, cr5))

      val in = testkit.inletFromSource(CallRecordValidation.in, source)
      val left = testkit.outletAsTap(CallRecordValidation.left)
      val right = testkit.outletAsTap(CallRecordValidation.right)

      testkit.run(CallRecordValidation, in, List(left, right), () ⇒ {
        right.probe.expectMsg(("user-1", cr1))
        right.probe.expectMsg(("user-1", cr2))
        right.probe.expectMsg(("user-1", cr3))
        left.probe.expectMsg((cr4.toString, InvalidRecord(cr4.toString, "Timestamp outside range!")))
        left.probe.expectMsg((cr5.toString, InvalidRecord(cr5.toString, "Timestamp outside range!")))
      })

      left.probe.expectMsg(Completed)
      right.probe.expectMsg(Completed)
    }
  }
}

