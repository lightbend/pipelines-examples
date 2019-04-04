package pipelines.example

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType

import pipelines.spark.{ ProcessorLogic, SparkProcessor }
import pipelines.spark.sql.SQLImplicits._
import pipelines.example.KeyedSchemas._

class MovingAverageSparklet extends SparkProcessor[Data, Agg] {

  override def createLogic(): ProcessorLogic[Data, Agg] = new ProcessorLogic[Data, Agg]() {
    override def process(inDataset: Dataset[Data]): Dataset[Agg] = {
      val query = inDataset
        .withColumn("ts", $"timestamp".cast(TimestampType))
        .withWatermark("ts", "1 minutes")
        .groupBy(window($"ts", "1 minute", "30 seconds"), $"src", $"gauge").agg(avg($"value") as "avg")
      query.select($"src", $"gauge", $"avg" as "value").as[Agg]
    }
  }

}
