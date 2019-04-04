package pipelines.examples.sensordata;

import org.apache.avro.Schema;
import pipelines.streamlets.*;
import pipelines.streamlets.avro.*;


public final class KeyedSchemas {
  static final AvroKeyedSchema<Metric> metricKeyed = 
    new AvroKeyedSchema<Metric>() {
      @Override public Schema schema() { return Metric.SCHEMA$; }
      @Override public Keyed<Metric> keyed() { 
	      return new Keyed<Metric>() { 
          @Override public String key(Metric m) {
            return m.getDeviceId().toString() + m.getTimestamp().toString(); 
          }
        };
      }
    };

  static final AvroKeyedSchema<InvalidMetric> invalidMetricKeyed = 
    new AvroKeyedSchema<InvalidMetric>() {
      @Override public Schema schema() { return InvalidMetric.SCHEMA$; }
      @Override public Keyed<InvalidMetric> keyed() { 
	      return new Keyed<InvalidMetric>() { 
          @Override public String key(InvalidMetric m) {
            return m.getMetric().toString();
    	    }
    	  };
      }
    };

  static final AvroKeyedSchema<SensorData> sensorDataKeyed = 
    new AvroKeyedSchema<SensorData>() {
      @Override public Schema schema() { return SensorData.SCHEMA$; }
      @Override public Keyed<SensorData> keyed() { 
      	return new Keyed<SensorData>() { 
          @Override public String key(SensorData s) {
            return s.getDeviceId().toString() + s.getTimestamp().toString();
          }
        };
      }
    };

  public static final KeyedSchemas instance = new KeyedSchemas();
}
