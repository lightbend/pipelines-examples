package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object MetricsValidation extends Splitter[Metric, InvalidMetric, Metric] {
  override def createLogic = new SplitterLogic() {
    def flow = flowWithPipelinesContext()
      .map { metric ⇒
        if (!SensorDataUtils.isValidMetric(metric)) Left(InvalidMetric(metric, "All measurements must be positive numbers!"))
        else Right(metric)
      }
  }
}
