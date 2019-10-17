package pipelines.examples
package logger

import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.flink.avro._

class FarePerRideLogger extends AkkaStreamlet {
  val inlet = AvroInlet[TaxiRideFare]("in")
  val shape = StreamletShape.withInlets(inlet)

  val LogLevel = RegExpConfigParameter(
    "log-level",
    "Provide one of the following log levels, debug, info, warning or error",
    "^debug|info|warning|error$",
    Some("info")
  )

  val MsgPrefix = StringConfigParameter(
    "msg-prefix",
    "Provide a prefix for the log lines",
    Some("valid-logger"))

  override def configParameters = Vector(LogLevel, MsgPrefix)

  override def createLogic = new RunnableGraphStreamletLogic() {
    val logF: String ⇒ Unit = streamletConfig.getString(LogLevel.key).toLowerCase match {
      case "debug"   ⇒ system.log.debug _
      case "info"    ⇒ system.log.info _
      case "warning" ⇒ system.log.warning _
      case "error"   ⇒ system.log.error _
    }

    val msgPrefix = streamletConfig.getString(MsgPrefix.key)

    def log(rideFare: TaxiRideFare) = {
      logF(s"$msgPrefix $rideFare")
    }

    def flow = {
      FlowWithOffsetContext[TaxiRideFare]
        .map { taxiRideFare ⇒
          log(taxiRideFare)
          taxiRideFare
        }
    }

    def runnableGraph =
      sourceWithOffsetContext(inlet)
        .via(flow)
        .to(sinkWithOffsetContext)
  }
}
