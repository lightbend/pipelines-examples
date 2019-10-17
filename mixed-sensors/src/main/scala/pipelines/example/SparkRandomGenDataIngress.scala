package pipelines.example

import java.sql.Timestamp

import scala.util.Random

import pipelines.streamlets.{ DurationConfigParameter, IntegerConfigParameter, StreamletShape }
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamlet, SparkStreamletLogic }
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.streaming.{ OutputMode, Trigger }

import pipelines.spark.sql.SQLImplicits._

case class Rate(timestamp: Timestamp, value: Long)

class SparkRandomGenDataIngress extends SparkStreamlet {
  val out = AvroOutlet[Data]("out", d ⇒ d.src)
  val shape = StreamletShape(out)

  val RecordsPerSecond = IntegerConfigParameter(
    "records-per-second",
    "Records per second to produce.",
    Some(50))

  val RampUpTime = DurationConfigParameter(
    "ramp-up-time",
    "Time to reach max records per second.",
    Some("0 seconds"))

  override def configParameters = Vector(RecordsPerSecond, RampUpTime)

  override def createLogic() = new SparkStreamletLogic {

    override def buildStreamingQueries = {
      writeStream(process, out, OutputMode.Append).toQueryExecution
    }

    private def process: Dataset[Data] = {

      val recordsPerSecond = context.streamletConfig.getInt(RecordsPerSecond.key)
      val rampUpTime = context.streamletConfig.getDuration(RampUpTime.key, java.util.concurrent.TimeUnit.SECONDS)
      println(s"Using rampup time of $rampUpTime seconds")

      val gaugeGen: () ⇒ String = () ⇒ if (Random.nextDouble() < 0.5) "oil" else "gas"

      val rateStream = session.readStream
        .format("rate")
        .option("rowsPerSecond", recordsPerSecond)
        .option("rampUpTime", s"${rampUpTime}s")
        .load()
        .as[Rate]

      rateStream.map {
        case Rate(timestamp, value) ⇒ Data(s"src-${value % 1000}", timestamp.getTime, None, None, gaugeGen(), value)
      }
    }
  }
}
