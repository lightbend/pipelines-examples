package warez

import pipelines.streamlets.avro._
import pipelines.akkastream.scaladsl._
import akka.actor.ActorSystem
import warez.dsl._

object StockUpdateLogger extends FlowEgress[StockUpdate](AvroInlet[StockUpdate]("in")) {
  def flowWithContext(system: ActorSystem) =
    FlowWithPipelinesContext[StockUpdate].map { stockUpdate ⇒
      system.log.warning(s"Stock Update! $stockUpdate")
      stockUpdate
    }
}
