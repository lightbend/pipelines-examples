package pipelines.example

import pipelines.akkastream._
import pipelines.akkastream.scaladsl.{ FlowWithOffsetContext, RunnableGraphStreamletLogic }
import pipelines.streamlets._
import pipelines.streamlets.avro._

class IdentityAkkaStreamsProcessor2 extends AkkaStreamlet {
  val in = AvroInlet[Data]("in")
  val out = AvroOutlet[Data]("out", _.src)

  val shape = StreamletShape(in).withOutlets(out)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = sourceWithOffsetContext(in).via(flow).to(sinkWithOffsetContext(out))
    def flow = FlowWithOffsetContext[Data].map(d ⇒ d.copy(t2 = TimeOps.nowAsOption))
  }
}
