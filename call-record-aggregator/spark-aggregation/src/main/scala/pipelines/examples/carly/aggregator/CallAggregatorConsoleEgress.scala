package pipelines.examples.carly.aggregator

import scala.collection.immutable.Seq
import org.apache.spark.sql.Dataset

import pipelines.spark.{ SparkEgress, EgressLogic }
import pipelines.spark.sql.SQLImplicits._
import org.apache.spark.sql.streaming.{ OutputMode, StreamingQuery }

import pipelines.examples.carly.data._
import pipelines.examples.carly.data.Codecs._

class CallAggregatorConsoleEgress extends SparkEgress[AggregatedCallStats] {

  override def createLogic: EgressLogic[AggregatedCallStats] = new EgressLogic[AggregatedCallStats] {

    override def process(inDataset: Dataset[AggregatedCallStats]): Seq[StreamingQuery] = {
      Seq(inDataset.writeStream
        .format("console")
        .outputMode(OutputMode.Append())
        .start())
    }
  }

}
