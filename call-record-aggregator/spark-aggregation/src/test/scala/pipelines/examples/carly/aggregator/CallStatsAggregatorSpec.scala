package pipelines.examples.carly.aggregator

import java.time.Instant
import java.time.temporal.ChronoUnit

import scala.concurrent.duration._

import scala.util.Random

import pipelines.examples.carly.data._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class CallStatsAggregatorSpec extends SparkScalaTestSupport {

  val testKit = SparkStreamletTestkit(session).withConfigParameterValues(
    ConfigParameterValue(CallStatsAggregator.GroupByWindow, "1 minute"),
    ConfigParameterValue(CallStatsAggregator.Watermark, "1 minute"))

  "CallStatsAggregator" should {
    "produce elements to its outlet" in {

      // setup inlet tap on inlet port
      val in = testKit.inletAsTap[CallRecord](CallStatsAggregator.in)

      // setup outlet tap on outlet port
      val out = testKit.outletAsTap[AggregatedCallStats](CallStatsAggregator.out)

      val maxUsers = 10
      val crs = (1 to 30).toList.map { i ⇒
        CallRecord(
          s"user-${Random.nextInt(maxUsers)}",
          s"user-${Random.nextInt(maxUsers)}",
          (if (i % 2 == 0) "incoming" else "outgoing"),
          Random.nextInt(50),
          Instant.now.minus(Random.nextInt(40), ChronoUnit.MINUTES).toEpochMilli / 1000
        )
      }

      in.addData(crs)

      testKit.run(CallStatsAggregator, Seq(in), Seq(out), 30.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)

      // assert
      results.size must be > 0
    }
  }
}

