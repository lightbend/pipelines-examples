package pipelines.example

import pipelines.streamlets._
import pipelines.streamlets.avro._

object KeyedSchemas {
  implicit val dataKeyed: Keyed[Data] = Keyed(s ⇒ s.key.toString)
  implicit val dataKeyedSchema: AvroKeyedSchema[Data] = AvroKeyedSchema(Data.SCHEMA$)
}
