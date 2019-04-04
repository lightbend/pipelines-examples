package pipelines.examples.carly.aggregator

import scala.util.Try
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import pipelines.spark.{ SparkProcessor, ProcessorLogic }
import org.apache.spark.sql.streaming.OutputMode
import pipelines.spark.sql.SQLImplicits._

import pipelines.examples.carly.data._
import pipelines.examples.carly.data.Codecs._

class CallStatsAggregator extends SparkProcessor[CallRecord, AggregatedCallStats] {
  val GroupByWindow = "group-by-window"
  val Watermark = "watermark"

  override def configKeys = Set(GroupByWindow, Watermark)
  override def createLogic = new ProcessorLogic[CallRecord, AggregatedCallStats](OutputMode.Update) {

    val watermark = Try(context.streamletRefConfig.getString(Watermark)).getOrElse("1 minute")
    val groupByWindow = Try(context.streamletRefConfig.getString(GroupByWindow)).getOrElse("1 minute")

    override def process(inDataset: Dataset[CallRecord]): Dataset[AggregatedCallStats] = {
      // TODO spark logging?
      println(s"Starting query with watermark $watermark, group-by-window $groupByWindow")
      val query =
        inDataset
          .withColumn("ts", $"timestamp".cast(TimestampType))
          .withWatermark("ts", watermark)
          .groupBy(window($"ts", groupByWindow))
          .agg(avg($"duration") as "avgCallDuration", sum($"duration") as "totalCallDuration")
          .withColumn("windowDuration", $"window.end".cast(LongType) - $"window.start".cast(LongType))

      query
        .select($"window.start".cast(LongType) as "startTime", $"windowDuration", $"avgCallDuration", $"totalCallDuration")
        .as[AggregatedCallStats]
    }
  }
}
