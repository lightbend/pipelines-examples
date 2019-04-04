package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object SensorDataToMetrics extends FlowProcessor[SensorData, Metric] {
  override def createLogic = new FlowLogic() {
    def flow = {
      flowWithPipelinesContext()
        .mapConcat { data ⇒
          List(
            Metric(data.deviceId, data.timestamp, "power", data.measurements.power),
            Metric(data.deviceId, data.timestamp, "rotorSpeed", data.measurements.rotorSpeed),
            Metric(data.deviceId, data.timestamp, "windSpeed", data.measurements.windSpeed)
          )
        }
    }
  }
}
