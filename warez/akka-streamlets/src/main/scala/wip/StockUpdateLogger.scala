package warez

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object StockUpdateLogger extends FlowEgress[StockUpdate] {
  override def createLogic = new FlowEgressLogic[StockUpdate]() {
    def flow = {
      flowWithPipelinesContext()
        .map { stockUpdate ⇒
          system.log.warning(s"Stock Update! $stockUpdate")

          stockUpdate
        }
    }
  }
}
