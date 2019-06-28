package pipelines.examples.sensordata

import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._

object RotorSpeedFilter extends AkkaStreamlet {
  val in = AvroInlet[Metric]("in")
  val out = AvroOutlet[Metric]("out", m ⇒ m.deviceId.toString + m.timestamp.toString)
  val shape = StreamletShape(in, out)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = atLeastOnceSource(in).via(flow).to(atLeastOnceSink(out))
    def flow = FlowWithPipelinesContext[Metric].filter(_.name == "rotorSpeed")
  }
}
