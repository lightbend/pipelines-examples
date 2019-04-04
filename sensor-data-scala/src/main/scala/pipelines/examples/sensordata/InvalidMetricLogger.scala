package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object InvalidMetricLogger extends FlowEgress[InvalidMetric] {
  override def createLogic = new FlowEgressLogic[InvalidMetric]() {
    def flow = {
      flowWithPipelinesContext()
        .map { invalidMetric ⇒
          system.log.warning(s"Invalid metric detected! $invalidMetric")

          invalidMetric
        }
    }
  }
}
