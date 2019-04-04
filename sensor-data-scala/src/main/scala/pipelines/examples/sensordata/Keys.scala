package pipelines.examples.sensordata

import pipelines.streamlets._
import pipelines.streamlets.avro._

object KeyedSchemas {
  implicit val metricKeyed: Keyed[Metric] = Keyed(m ⇒ m.deviceId.toString + m.timestamp.toString)
  implicit val invalidMetricKeyed: Keyed[InvalidMetric] = Keyed(_.metric.toString)
  implicit val sensorDataKeyed: Keyed[SensorData] = Keyed(s ⇒ s.deviceId.toString + s.timestamp.toString)

  implicit val metricKeyedSchema: AvroKeyedSchema[Metric] = AvroKeyedSchema(Metric.SCHEMA$)
  implicit val invalidMetricKeyedSchema: AvroKeyedSchema[InvalidMetric] = AvroKeyedSchema(InvalidMetric.SCHEMA$)
  implicit val sensorDataKeyedSchema: AvroKeyedSchema[SensorData] = AvroKeyedSchema(SensorData.SCHEMA$)
}
