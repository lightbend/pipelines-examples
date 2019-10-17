package pipelines.examples.sensordata

import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._

class RotorSpeedFilter extends AkkaStreamlet {
  val in = AvroInlet[Metric]("in")
  val out = AvroOutlet[Metric]("out").withPartitioner(RoundRobinPartitioner)
  val shape = StreamletShape(in, out)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = sourceWithOffsetContext(in).via(flow).to(sinkWithOffsetContext(out))
    def flow = FlowWithOffsetContext[Metric].filter(_.name == "rotorSpeed")
  }
}
