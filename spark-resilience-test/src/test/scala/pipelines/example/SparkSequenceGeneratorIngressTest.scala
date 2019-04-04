package pipelines.example

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class SparkSequenceGeneratorIngressTest extends SparkTestSupport {

  "SparkSequenceGeneratorIngress" should {
    "produce data " in {

      // setup inlet tap on inlet(s) port(s)
      val out: SparkOutletTap[Data] = outletAsTap[Data](SparkSequenceGeneratorIngress.shape.outlet)

      // Run the streamlet using the testkit and the setup inlet taps and outlet probes
      run(SparkSequenceGeneratorIngress, Seq.empty, Seq(out), 10.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)
      val ordered = results.map(data ⇒ data.value).sorted
      ordered.size should be > (SequenceSettings.RecordsPerSecond) // at least one second of data
      assert((ordered zip ordered.tail).forall { case (i, j) ⇒ j == (i + 1) }, "produced list missed elements")

    }
  }
}
