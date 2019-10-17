package pipelines.example

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class SparkSequenceGeneratorIngressTest extends SparkScalaTestSupport {

  val streamlet = new SparkSequenceGeneratorIngress()
  val testKit = SparkStreamletTestkit(session).withConfigParameterValues(ConfigParameterValue(streamlet.RecordsPerSecond, "50"))

  "SparkSequenceGeneratorIngress" should {
    "produce data " in {

      // setup inlet tap on inlet(s) port(s)
      val out: SparkOutletTap[Data] = testKit.outletAsTap[Data](streamlet.out)

      // Run the streamlet using the testkit and the setup inlet taps and outlet probes
      testKit.run(streamlet, Seq.empty, Seq(out), 10.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)
      val ordered = results.map(data ⇒ data.value).sorted
      ordered.size mustBe >(SequenceSettings.RecordsPerSecond) // at least one second of data
      assert((ordered zip ordered.tail).forall { case (i, j) ⇒ j == (i + 1) }, "produced list missed elements")

    }
  }
}
