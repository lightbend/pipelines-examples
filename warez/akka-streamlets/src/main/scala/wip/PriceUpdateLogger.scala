package warez

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object PriceUpdateLogger extends FlowEgress[PriceUpdate] {
  override def createLogic = new FlowEgressLogic[PriceUpdate]() {
    def flow = {
      flowWithPipelinesContext()
        .map { priceUpdate ⇒
          system.log.warning(s"Price Update! $priceUpdate")

          priceUpdate
        }
    }
  }
}
