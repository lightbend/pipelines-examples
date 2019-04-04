package pipelines.example

import org.apache.spark.sql.Dataset

import pipelines.spark.{ ProcessorLogic, SparkProcessor }
import pipelines.spark.sql.SQLImplicits._
import pipelines.example.KeyedSchemas._

object SuicidalMonkeyProcessor extends SparkProcessor[Data, Data] {
  val rng = scala.util.Random
  override def createLogic(): ProcessorLogic[Data, Data] = new ProcessorLogic[Data, Data]() {

    override def process(inDataset: Dataset[Data]): Dataset[Data] = {
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
