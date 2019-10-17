package warez
package dsl

import akka.actor.ActorSystem

import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.scaladsl._

abstract class FlowEgress[In](val in: CodecInlet[In])
  extends AkkaStreamlet {

  final override val shape = StreamletShape.withInlets(in)
  def flowWithContext(system: ActorSystem): FlowWithOffsetContext[In, In]

  override def createLogic = new RunnableGraphStreamletLogic {
    def runnableGraph =
      sourceWithOffsetContext(in)
        .via(flowWithContext(system))
        .to(sinkWithOffsetContext)
  }
}
