package warez

import pipelines.streamlets.avro._
import pipelines.akkastream.scaladsl._
import akka.actor.ActorSystem
import warez.dsl._

object ProductLogger extends FlowEgress[Product](AvroInlet[Product]("in")) {
  def flowWithContext(system: ActorSystem) =
    FlowWithPipelinesContext[Product].map { product ⇒
      system.log.warning(s"Product! $product")
      product
    }
}
