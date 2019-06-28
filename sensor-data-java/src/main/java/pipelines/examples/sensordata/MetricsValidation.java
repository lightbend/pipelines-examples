package pipelines.examples.sensordata;

import akka.stream.javadsl.*;
import pipelines.akkastream.javadsl.util.Either;

import akka.NotUsed;
import akka.actor.*;
import akka.stream.*;

import com.typesafe.config.Config;

import pipelines.streamlets.*;
import pipelines.streamlets.avro.*;
import pipelines.akkastream.*;
import pipelines.akkastream.util.javadsl.*;

public class MetricsValidation extends AkkaStreamlet {
  AvroInlet<Metric> inlet = AvroInlet.<Metric>create("in", Metric.class);
  AvroOutlet<InvalidMetric> invalidOutlet = AvroOutlet.<InvalidMetric>create("invalid",  m -> m.metric.toString(), InvalidMetric.class);
  AvroOutlet<Metric> validOutlet = AvroOutlet.<Metric>create("valid", m -> m.getDeviceId().toString() + m.getTimestamp().toString(), Metric.class);

  public StreamletShape shape() {
   return StreamletShape.createWithInlets(inlet).withOutlets(invalidOutlet, validOutlet);
  }

  public SplitterLogic createLogic() {
    return new SplitterLogic<Metric,InvalidMetric, Metric>(inlet, invalidOutlet, validOutlet, getStreamletContext()) {
      public FlowWithContext<Metric, PipelinesContext, Either<InvalidMetric, Metric>, PipelinesContext, NotUsed> createFlow() {
        return createFlowWithPipelinesContext()
          .map(metric -> {
            if (!SensorDataUtils.isValidMetric(metric)) return Either.left(new InvalidMetric(metric, "All measurements must be positive numbers!"));
            else return Either.right(metric);
          });
      }
    };
  }
}
