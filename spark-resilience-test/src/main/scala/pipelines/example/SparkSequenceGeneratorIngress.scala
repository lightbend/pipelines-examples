package pipelines.example

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.streaming.OutputMode

import pipelines.streamlets._
import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamletLogic, SparkStreamlet }
import pipelines.spark.sql.SQLImplicits._

class SparkSequenceGeneratorIngress extends SparkStreamlet {
  val out = AvroOutlet[Data]("out", d ⇒ d.key.toString)
  val shape = StreamletShape(out)

  val RecordsPerSecond = IntegerConfigParameter(
    "records-per-second",
    "Records per second to process.",
    Some(50))

  override def configParameters = Vector(RecordsPerSecond)

  override def createLogic() = new SparkStreamletLogic {
    val recordsPerSecond = context.streamletConfig.getInt(RecordsPerSecond.key)

    override def buildStreamingQueries = {
      writeStream(process, out, OutputMode.Append).toQueryExecution
    }

    private def process: Dataset[Data] = {
      session.readStream
        .format("rate")
        .option("rowsPerSecond", recordsPerSecond)
        .load()
        .withColumn("key", ($"value" / SequenceSettings.GroupSize).cast(LongType))
        .as[Data]
    }
  }
}
