package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object RotorspeedWindowLogger extends FlowEgress[Metric] {
  override def createLogic = new FlowEgressLogic[Metric]() {
    def flow = {
      flowWithPipelinesContext()
        .grouped(5)
        .map { rotorSpeedWindow ⇒
          val (avg, _) = rotorSpeedWindow.map(_.value).foldLeft((0.0, 1)) { case ((avg, idx), next) ⇒ (avg + (next - avg) / idx, idx + 1) }

          system.log.info(s"Average rotorspeed is: $avg")

          avg
        }
        .mapContext(_.last) // TODO: this is a tricky one to understand...
    }
  }
}
