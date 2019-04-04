package pipelines.examples.carly.aggregator

import java.time.Instant
import java.time.temporal.ChronoUnit

import scala.collection.immutable.Seq
import scala.concurrent.duration._
import scala.util.Random

import org.apache.spark.sql.execution.streaming.state.StateStore
import com.typesafe.config._

import pipelines.examples.carly.data._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._

class CallStatsAggregatorSpec extends SparkTestSupport {
  override def config: Config = ConfigFactory.parseString("""
    pipelines.streamlets.testSparklet.group-by-window=1 minute
    pipelines.streamlets.testSparklet.watermark=1 minute
""")

  override def afterAll(): Unit = {
    super.afterAll()
    StateStore.stop() // stop the state store maintenance thread and unload store providers
  }

  "CallStatsAggregator" should {
    "produce elements to its outlet" in {

      val aggregator = new CallStatsAggregator

      // setup inlet tap on inlet port
      val in = inletAsTap[CallRecord](aggregator.shape.inlet)

      // setup outlet tap on outlet port
      val out = outletAsTap[AggregatedCallStats](aggregator.shape.outlet)

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

      run(aggregator, Seq(in), Seq(out), 30.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)

      // assert
      results.size should be > 0
    }
  }
}

