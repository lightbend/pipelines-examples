package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object RotorSpeedFilter extends FlowProcessor[Metric, Metric] {
  override def createLogic = new FlowLogic() {
    def flow = flowWithPipelinesContext().filter(_.name == "rotorSpeed")
  }
}
