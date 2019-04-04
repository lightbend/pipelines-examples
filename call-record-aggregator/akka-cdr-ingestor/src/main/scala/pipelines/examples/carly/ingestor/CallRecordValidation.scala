package pipelines.examples.carly.ingestor

import pipelines.akkastream.scaladsl._

import pipelines.examples.carly.data.Codecs._
import pipelines.examples.carly.data._

object CallRecordValidation extends Splitter[CallRecord, InvalidRecord, CallRecord] {
  val oldDataWatermark = java.sql.Timestamp.valueOf("2010-01-01 00:00:00.000").getTime / 1000 //seconds

  override def createLogic = new SplitterLogic() {
    def flow =
      flowWithPipelinesContext()
        .map { record ⇒
          if (record.timestamp < oldDataWatermark) Left(InvalidRecord(record.toString, "Timestamp outside range!"))
          else Right(record)
        }
  }
}

