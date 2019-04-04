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

import pipelines.examples.carly.data.Codecs._
import pipelines.examples.carly.data._

class CallRecordMergeSpec extends WordSpec with MustMatchers with ScalaFutures with BeforeAndAfterAll {

  private implicit val system = ActorSystem("CallRecordMergeSpec")
  private implicit val mat = ActorMaterializer()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A CallRecordMerge" should {
    "merge incoming data" in {
      val testkit = AkkaStreamletTestKit(system, mat)

      val instant = Instant.now.toEpochMilli / 1000
      val past = Instant.now.minus(5000, ChronoUnit.DAYS).toEpochMilli / 1000

      val cr1 = CallRecord("user-1", "user-2", "f", 10L, instant)
      val cr2 = CallRecord("user-1", "user-2", "f", 15L, instant)
      val cr3 = CallRecord("user-1", "user-2", "f", 18L, instant)
      val cr4 = CallRecord("user-1", "user-2", "f", 40L, past)
      val cr5 = CallRecord("user-1", "user-2", "f", 70L, past)
      val cr6 = CallRecord("user-3", "user-1", "f", 80L, past)

      val source0 = Source(Vector(cr1, cr2, cr3))
      val source1 = Source(Vector(cr4, cr5))
      val source2 = Source(Vector(cr6))

      val in0 = testkit.inletFromSource(CallRecordMerge.shape.inlets(0), source0)
      val in1 = testkit.inletFromSource(CallRecordMerge.shape.inlets(1), source1)
      val in2 = testkit.inletFromSource(CallRecordMerge.shape.inlets(2), source2)
      val out = testkit.outletAsTap(CallRecordMerge.shape.outlet)

      testkit.run(CallRecordMerge, List(in0, in1, in2), out, () ⇒ {
        out.probe.expectMsg(("user-1", cr1))
        out.probe.expectMsg(("user-1", cr4))
        out.probe.expectMsg(("user-3", cr6))
        out.probe.expectMsg(("user-1", cr2))
        out.probe.expectMsg(("user-1", cr5))
        out.probe.expectMsg(("user-1", cr3))
      })

      out.probe.expectMsg(Completed)
    }
  }
}

