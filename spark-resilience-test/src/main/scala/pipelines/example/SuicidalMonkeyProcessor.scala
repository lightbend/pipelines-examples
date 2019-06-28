package pipelines.example

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.streaming.OutputMode

import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamletLogic, SparkStreamlet }
import pipelines.spark.sql.SQLImplicits._

object SuicidalMonkeyProcessor extends SparkStreamlet {
  val in = AvroInlet[Data]("in")
  val out = AvroOutlet[Data]("out", _.key.toString)
  val shape = StreamletShape(in, out)

  val rng = scala.util.Random
  override def createLogic() = new SparkStreamletLogic {
    override def buildStreamingQueries = {
      val outStream = process(readStream(in))
      writeStream(outStream, out, OutputMode.Append).toQueryExecution
    }

    private def process(inDataset: Dataset[Data]): Dataset[Data] = {
      inDataset.mapPartitions { iter ⇒
        // monkey business
        // The logic in this processor causes the current executor to crash with a certain probability.
        // comment out to see the process working
        if (rng.nextDouble() < SequenceSettings.FailureProbability) {
          sys.exit(-1)
        }
        iter
      }

    }
  }

}
