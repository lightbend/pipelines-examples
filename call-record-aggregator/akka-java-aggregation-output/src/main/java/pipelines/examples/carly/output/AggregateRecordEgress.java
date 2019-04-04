package pipelines.examples.carly.output;

import akka.NotUsed;
import akka.stream.javadsl.*;
import pipelines.akkastream.PipelinesContext;
import pipelines.akkastream.javadsl.FlowEgress;

import pipelines.examples.carly.data.*;


public class AggregateRecordEgress extends FlowEgress<AggregatedCallStats> {

  public AggregateRecordEgress() {
    super(Codecs.instance().aggregatedCallStatsCodec());
  }

  private Object doPrint(final AggregatedCallStats metric) {
    System.out.println(metric);
    return metric;
  }

  public FlowEgressLogic createLogic() {
    return new FlowEgressLogic(this) {
      public FlowWithContext<AggregatedCallStats, PipelinesContext, Object, PipelinesContext, NotUsed> createFlow() {
        return FlowWithContext.<AggregatedCallStats, PipelinesContext>create().map(metric -> doPrint(metric));
      }
    };
  }
}

