package pipelines.example

import scala.collection.immutable.Seq
import org.apache.spark.sql.Dataset

import pipelines.spark.{ EgressLogic, SparkEgress }
import pipelines.spark.sql.SQLImplicits._
import pipelines.example.KeyedSchemas._
import org.apache.spark.sql.streaming.{ OutputMode, StreamingQuery }

class SparkConsoleEgress extends SparkEgress[Agg] {

  override def createLogic(): EgressLogic[Agg] = new EgressLogic() {
    def process(inDataset: Dataset[Agg]): Seq[StreamingQuery] = {
      Seq(inDataset.writeStream
        .format("console")
        .outputMode(OutputMode.Append())
        .start())
    }
  }
}
