package pipelines.examples.carly.ingestor

import pipelines.streamlets.avro._
import pipelines.streamlets.StreamletShape
import pipelines.akkastream.AkkaStreamlet
import pipelines.akkastream.util.scaladsl.SplitterLogic

import pipelines.examples.carly.data._

object CallRecordValidation extends AkkaStreamlet {
  private val oldDataWatermark = java.sql.Timestamp.valueOf("2010-01-01 00:00:00.000").getTime / 1000 //seconds

  val in = AvroInlet[CallRecord]("in")
  val left = AvroOutlet[InvalidRecord]("invalid", _.record)
  val right = AvroOutlet[CallRecord]("valid", _.user)

  final override val shape = StreamletShape(in).withOutlets(left, right)
  final override def createLogic = new SplitterLogic(in, left, right) {
    def flow =
      flowWithPipelinesContext()
        .map { record ⇒
          if (record.timestamp < oldDataWatermark) Left(InvalidRecord(record.toString, "Timestamp outside range!"))
          else Right(record)
        }
  }
}

