package pipelines.examples.sensordata

import pipelines.akkastream.scaladsl._
import KeyedSchemas._

object ValidMetricLogger extends FlowEgress[Metric] {
  val LogLevel = "log-level"
  val MsgPrefix = "msg-prefix"
  override def configKeys = Set(LogLevel, MsgPrefix)

  override def createLogic = new FlowEgressLogic[Metric]() {
    val logF: String ⇒ Unit = streamletRefConfig.getString(LogLevel).toLowerCase match {
      case "debug"   ⇒ system.log.debug _
      case "info"    ⇒ system.log.info _
      case "warning" ⇒ system.log.warning _
      case "error"   ⇒ system.log.error _
    }

    val msgPrefix = streamletRefConfig.getString(MsgPrefix)

    def log(metric: Metric) = {
      logF(s"$msgPrefix $metric")
    }

    def flow = {
      flowWithPipelinesContext()
        .map { validMetric ⇒
          log(validMetric)
          validMetric
        }
    }
  }
}
