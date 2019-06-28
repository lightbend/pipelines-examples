package pipelines.example

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class SparkSequenceValidatorEgressTest extends SparkScalaTestSupport {

  val testKit = SparkStreamletTestkit(session).withConfigParameterValues(ConfigParameterValue(SparkSequenceGeneratorIngress.RecordsPerSecond, "50"))

  "SparkSequenceValidatorEgress" should {
    "output streaming data" in {

      // Setup inlet tap on inlet(s) port(s)
      val in: SparkInletTap[Data] = testKit.inletAsTap[Data](SparkSequenceValidatorEgress.in)

      // Build data and send to inlet tap
      val now = System.currentTimeMillis()
      val data = (0 until SequenceSettings.GroupSize).map(i ⇒ Data(now + i * 1000, 1, i.toLong)) ++
        (0 until SequenceSettings.GroupSize - 1).map(i ⇒ Data(now + i * 1000, 2, i.toLong))
      in.addData(data)

      testKit.run(SparkSequenceValidatorEgress, Seq(in), Seq.empty, 10.seconds)

    }
  }
}
