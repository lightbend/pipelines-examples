package pipelines.example

import scala.concurrent.duration._

object SequenceSettings {

  val GroupSize: Int = 1500
  val FailureProbability: Double = 0.05
  val TimeoutDuration: Long = 1.minute.toMillis
  val RecordsPerSecond: Int = 50

}
