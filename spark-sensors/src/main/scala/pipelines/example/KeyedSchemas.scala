package pipelines.example

import pipelines.streamlets._
import pipelines.streamlets.avro._

object KeyedSchemas {
  implicit val dataKeyed: Keyed[Data] = Keyed(s ⇒ s.src)
  implicit val dataKeyedSchema: AvroKeyedSchema[Data] = AvroKeyedSchema(Data.SCHEMA$)

  implicit val aggDataKeyed: Keyed[Agg] = Keyed(agg ⇒ agg.src)
  implicit val aggKeyedSchema: AvroKeyedSchema[Agg] = AvroKeyedSchema(Agg.SCHEMA$)
}
