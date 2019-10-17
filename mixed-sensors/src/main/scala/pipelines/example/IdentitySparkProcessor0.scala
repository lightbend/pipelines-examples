package pipelines.example

import org.apache.spark.sql.streaming.OutputMode

import pipelines.spark.{ SparkStreamlet, SparkStreamletLogic }
import pipelines.spark.sql.SQLImplicits._
import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._

class IdentitySparkProcessor0 extends SparkStreamlet {

  val in = AvroInlet[Data]("in")
  val out = AvroOutlet[Data]("out", _.src)
  val shape = StreamletShape(in, out)

  override def createLogic() = new SparkStreamletLogic {
    override def buildStreamingQueries = {
      writeStream(readStream(in).map { d ⇒ Thread.sleep(200); d }, out, OutputMode.Append).toQueryExecution
    }
  }
}
