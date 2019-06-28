package warez
package dsl

import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.scaladsl._

import akka.actor.ActorSystem

abstract class FlowEgress[In](val in: CodecInlet[In])
  extends AkkaStreamlet {

  final override val shape = StreamletShape.withInlets(in)
  def flowWithContext(system: ActorSystem): FlowWithPipelinesContext[In, In]

  override def createLogic = new RunnableGraphStreamletLogic {
    def runnableGraph =
      atLeastOnceSource(in)
        .via(flowWithContext(system).asFlow)
        .to(atLeastOnceSink)
  }
}
