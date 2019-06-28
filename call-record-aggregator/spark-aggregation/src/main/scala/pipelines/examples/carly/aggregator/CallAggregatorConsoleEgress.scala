package pipelines.examples.carly.aggregator

import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamlet, SparkStreamletLogic }
import pipelines.spark.sql.SQLImplicits._
import org.apache.spark.sql.streaming.OutputMode

import org.apache.log4j.{ Level, Logger }

import pipelines.examples.carly.data._

class CallAggregatorConsoleEgress extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in = AvroInlet[AggregatedCallStats]("in")
  val shape = StreamletShape(in)

  override def createLogic = new SparkStreamletLogic {
    override def buildStreamingQueries = {
      readStream(in).writeStream
        .format("console")
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
    }
  }
}
