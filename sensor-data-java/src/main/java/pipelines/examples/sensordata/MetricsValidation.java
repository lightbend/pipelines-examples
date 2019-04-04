package pipelines.examples.sensordata;

import akka.stream.javadsl.*;
import pipelines.akkastream.javadsl.util.Either;

import akka.NotUsed;
import akka.actor.*;
import akka.stream.*;

import com.typesafe.config.Config;

import pipelines.streamlets.*;
import pipelines.akkastream.*;
import pipelines.akkastream.javadsl.*;

public class MetricsValidation extends Splitter<Metric, InvalidMetric, Metric> {
  public MetricsValidation() {
    super(KeyedSchemas.instance.metricKeyed,
          KeyedSchemas.instance.invalidMetricKeyed,
          KeyedSchemas.instance.metricKeyed);
  }

  public SplitterLogic createLogic() {
    return new SplitterLogic(this) {
      @Override
      public FlowWithContext<Metric, PipelinesContext, Either<InvalidMetric, Metric>, PipelinesContext, NotUsed> createFlow() {
          return
            createFlowWithPipelinesContext()
              .map(metric -> {
                if (!SensorDataUtils.isValidMetric(metric)) return Either.left(new InvalidMetric(metric, "All measurements must be positive numbers!"));
                else return Either.right(metric);
              });
      }
    };
  }
}
