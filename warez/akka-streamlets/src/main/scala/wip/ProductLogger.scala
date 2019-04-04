package warez

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object ProductLogger extends FlowEgress[Product] {
  override def createLogic = new FlowEgressLogic[Product]() {
    def flow = {
      flowWithPipelinesContext()
        .map { product ⇒
          system.log.warning(s"Product! $product")

          product
        }
    }
  }
}
