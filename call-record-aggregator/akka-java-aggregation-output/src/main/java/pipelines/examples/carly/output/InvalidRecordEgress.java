package pipelines.examples.carly.output;

import akka.NotUsed;
import akka.stream.javadsl.*;
import pipelines.akkastream.PipelinesContext;
import pipelines.akkastream.javadsl.FlowEgress;
import pipelines.examples.carly.data.Codecs;
import pipelines.examples.carly.data.*;

public class InvalidRecordEgress extends FlowEgress<InvalidRecord> {

  public InvalidRecordEgress() {
    super(Codecs.instance().invalidCallRecordCodec());
  }

  private Object doPrint(final InvalidRecord record) {
    System.out.println(record);
    return record;
  }

  public FlowEgressLogic createLogic() {
    return new FlowEgressLogic(this) {
      public FlowWithContext<InvalidRecord, PipelinesContext, Object, PipelinesContext, NotUsed> createFlow() {
        return FlowWithContext.<InvalidRecord, PipelinesContext>create().map(record -> doPrint(record));
      }
    };
  }
}
