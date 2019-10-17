package pipelines.examples.carly.aggregator

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.examples.carly.data._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class CallRecordGeneratorIngressSpec extends SparkScalaTestSupport {

  val streamlet = new CallRecordGeneratorIngress()
  val testKit = SparkStreamletTestkit(session).withConfigParameterValues(ConfigParameterValue(streamlet.RecordsPerSecond, "50"))

  "CallRecordGeneratorIngress" should {
    "produce elements to its outlet" in {

      // setup outlet tap on outlet port
      val out = testKit.outletAsTap[CallRecord](streamlet.out)

      testKit.run(streamlet, Seq.empty, Seq(out), 40.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)

      // assert
      results.size must be > 0

    }
  }
}

