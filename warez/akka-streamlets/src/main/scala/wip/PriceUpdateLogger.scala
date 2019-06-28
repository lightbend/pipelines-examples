package warez

import pipelines.streamlets.avro._
import pipelines.akkastream.scaladsl._
import akka.actor.ActorSystem
import warez.dsl._

object PriceUpdateLogger extends FlowEgress[PriceUpdate](AvroInlet[PriceUpdate]("in")) {
  def flowWithContext(system: ActorSystem) =
    FlowWithPipelinesContext[PriceUpdate].map { priceUpdate ⇒
      system.log.warning(s"Price Update! $priceUpdate")
      priceUpdate
    }
}
