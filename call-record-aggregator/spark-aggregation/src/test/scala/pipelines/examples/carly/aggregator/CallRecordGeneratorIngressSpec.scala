package pipelines.examples.carly.aggregator

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.examples.carly.data._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class CallRecordGeneratorIngressSpec extends SparkTestSupport {

  "CallRecordGeneratorIngress" should {
    "produce elements to its outlet" in {

      // setup outlet tap on outlet port
      val out = outletAsTap[CallRecord](CallRecordGeneratorIngress.shape.outlet)

      run(CallRecordGeneratorIngress, Seq.empty, Seq(out), 40.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)

      // assert
      results.size should be > 0

    }
  }
}

