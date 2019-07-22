package pipelines.example

import pipelines.streamlets.StreamletShape

import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamletLogic, SparkStreamlet }
import pipelines.spark.sql.SQLImplicits._
import org.apache.spark.sql.streaming.OutputMode

class SparkConsoleEgress extends SparkStreamlet {
  val in = AvroInlet[Agg]("in")
  val shape = StreamletShape(in)

  override def createLogic() = new SparkStreamletLogic {
    //tag::docs-checkpointDir-example[]
    override def buildStreamingQueries = {
      readStream(in).writeStream
        .format("console")
        .option("checkpointLocation", context.checkpointDir("console-egress"))
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
    }
    //end::docs-checkpointDir-example[]
  }
}
