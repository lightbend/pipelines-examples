package pipelines.examples.carly.ingestor

import spray.json._
import pipelines.examples.carly.data.CallRecord

case object JsonCallRecord extends DefaultJsonProtocol {
  implicit val crFormat = jsonFormat(CallRecord.apply, "user", "other", "direction", "duration", "timestamp")
}
