package pipelines.examples.sensordata

import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._

object SensorDataToMetrics extends AkkaStreamlet {
  val in = AvroInlet[SensorData]("in")
  val out = AvroOutlet[Metric]("out", m ⇒ m.deviceId.toString + m.timestamp.toString)
  val shape = StreamletShape(in, out)
  def flow = {
    FlowWithPipelinesContext[SensorData]
      .mapConcat { data ⇒
        List(
          Metric(data.deviceId, data.timestamp, "power", data.measurements.power),
          Metric(data.deviceId, data.timestamp, "rotorSpeed", data.measurements.rotorSpeed),
          Metric(data.deviceId, data.timestamp, "windSpeed", data.measurements.windSpeed)
        )
      }
  }
  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = atLeastOnceSource(in).via(flow).to(atLeastOnceSink(out))
  }
}
