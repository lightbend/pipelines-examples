package pipelines.example

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.LongType

import pipelines.spark.{ IngressLogic, SparkIngress }
import pipelines.spark.sql.SQLImplicits._
import pipelines.example.KeyedSchemas._

object SparkSequenceGeneratorIngress extends SparkIngress[Data] {

  override def createLogic(): IngressLogic[Data] = new IngressLogic[Data]() {
    override def process: Dataset[Data] = {
      session.readStream
        .format("rate")
        .option("rowsPerSecond", SequenceSettings.RecordsPerSecond)
        .load()
        .withColumn("key", ($"value" / SequenceSettings.GroupSize).cast(LongType))
        .as[Data]
    }
  }
}
