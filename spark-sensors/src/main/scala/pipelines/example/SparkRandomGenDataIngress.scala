package pipelines.example

import java.sql.Timestamp

import scala.util.Random

import org.apache.spark.sql.Dataset

import pipelines.spark.{ IngressLogic, SparkIngress }
import pipelines.spark.sql.SQLImplicits._
import pipelines.example.KeyedSchemas._

case class Rate(timestamp: Timestamp, value: Long)

object SparkRandomGenDataIngress extends SparkIngress[Data] {
  override def createLogic(): IngressLogic[Data] = new IngressLogic[Data]() {
    override def process: Dataset[Data] = {
      // do we need to expose this through configuration?
      val recordsPerSecond = 100

      val gaugeGen: () ⇒ String = () ⇒ if (Random.nextDouble() < 0.5) "oil" else "gas"

      val rateStream = session.readStream
        .format("rate")
        .option("rowsPerSecond", recordsPerSecond)
        .load()
        .as[Rate]

      rateStream.map {
        case Rate(timestamp, value) ⇒ Data(s"src-${value % 100}", timestamp.getTime, gaugeGen(), Random.nextDouble() * value)
      }
    }
  }
}
