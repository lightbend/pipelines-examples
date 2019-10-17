package pipelines.example

import pipelines.streamlets.StreamletShape

import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamletLogic, SparkStreamlet }

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType
import pipelines.spark.sql.SQLImplicits._
import org.apache.spark.sql.streaming.OutputMode

class IdentitySparkProcessor1 extends SparkStreamlet {

  val in = AvroInlet[Data]("in")
  val out = AvroOutlet[Data]("out", _.src)
  val shape = StreamletShape(in, out)

  override def createLogic() = new SparkStreamletLogic {
    override def buildStreamingQueries = {
      writeStream(readStream(in).map(d ⇒ d.copy(t1 = TimeOps.nowAsOption)), out, OutputMode.Append).toQueryExecution
    }
  }
}
