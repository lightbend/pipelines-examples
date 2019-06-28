package pipelines.examples.carly.aggregator

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamlet, SparkStreamletLogic }
import org.apache.spark.sql.streaming.OutputMode
import pipelines.spark.sql.SQLImplicits._
import org.apache.log4j.{ Level, Logger }

import pipelines.examples.carly.data._

object CallStatsAggregator extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in = AvroInlet[CallRecord]("in")
  val out = AvroOutlet[AggregatedCallStats]("out", _.startTime.toString)
  val shape = StreamletShape(in, out)

  val GroupByWindow = DurationConfigParameter(
    "group-by-window",
    "Window duration for the moving average computation",
    Some("1 minute"))

  val Watermark = DurationConfigParameter(
    "watermark",
    "Late events watermark duration: how long to wait for late events",
    Some("1 minute"))

  override def configParameters = Vector(GroupByWindow, Watermark)
  override def createLogic = new SparkStreamletLogic {

    override def buildStreamingQueries = {
      val dataset = readStream(in)
      val outStream = process(dataset)
      writeStream(outStream, out, OutputMode.Update).toQueryExecution
    }

    val watermark = context.streamletConfig.getDuration(Watermark.key)
    val groupByWindow = context.streamletConfig.getDuration(GroupByWindow.key)

    private def process(inDataset: Dataset[CallRecord]): Dataset[AggregatedCallStats] = {
      // TODO spark logging?
      println(s"Starting query with watermark $watermark, group-by-window $groupByWindow")
      val query =
        inDataset
          .withColumn("ts", $"timestamp".cast(TimestampType))
          .withWatermark("ts", s"${watermark.toMillis()} milliseconds")
          .groupBy(window($"ts", s"${groupByWindow.toMillis()} milliseconds"))
          .agg(avg($"duration") as "avgCallDuration", sum($"duration") as "totalCallDuration")
          .withColumn("windowDuration", $"window.end".cast(LongType) - $"window.start".cast(LongType))

      query
        .select($"window.start".cast(LongType) as "startTime", $"windowDuration", $"avgCallDuration", $"totalCallDuration")
        .as[AggregatedCallStats]
    }
  }
}
