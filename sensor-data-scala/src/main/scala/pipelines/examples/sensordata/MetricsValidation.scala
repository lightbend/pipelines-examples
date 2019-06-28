package pipelines.examples.sensordata

import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._

object MetricsValidation extends AkkaStreamlet {
  val in = AvroInlet[Metric]("in")
  val invalid = AvroOutlet[InvalidMetric]("invalid", m ⇒ m.metric.deviceId.toString + m.metric.timestamp.toString)
  val valid = AvroOutlet[Metric]("valid", m ⇒ m.deviceId.toString + m.timestamp.toString)
  val shape = StreamletShape(in).withOutlets(invalid, valid)

  override def createLogic = new SplitterLogic(in, invalid, valid) {
    def flow = flowWithPipelinesContext()
      .map { metric ⇒
        if (!SensorDataUtils.isValidMetric(metric)) Left(InvalidMetric(metric, "All measurements must be positive numbers!"))
        else Right(metric)
      }
  }
}
