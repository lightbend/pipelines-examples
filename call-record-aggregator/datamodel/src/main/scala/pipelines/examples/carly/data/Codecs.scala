package pipelines.examples.carly.data

import pipelines.streamlets.avro._
import pipelines.streamlets._

class Codecs {
  implicit val callRecordKeyed: Keyed[CallRecord] = new Keyed[CallRecord] { def key(m: CallRecord) = m.user }
  implicit val callRecordCodec: KeyedSchema[CallRecord] = AvroKeyedSchema[CallRecord](CallRecord.SCHEMA$)

  implicit val invalidRecordKeyed: Keyed[InvalidRecord] = new Keyed[InvalidRecord] { def key(m: InvalidRecord) = m.record }
  implicit val invalidCallRecordCodec: KeyedSchema[InvalidRecord] = AvroKeyedSchema[InvalidRecord](InvalidRecord.SCHEMA$)

  implicit val aggregatedCallStatKeyed: Keyed[AggregatedCallStats] = new Keyed[AggregatedCallStats] { def key(m: AggregatedCallStats) = m.startTime.toString }
  implicit val aggregatedCallStatsCodec: KeyedSchema[AggregatedCallStats] = AvroKeyedSchema[AggregatedCallStats](AggregatedCallStats.SCHEMA$)
}

object Codecs extends Codecs {
  def instance: Codecs = this
}
