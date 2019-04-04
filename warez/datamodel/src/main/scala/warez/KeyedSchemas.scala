package warez

import pipelines.streamlets._
import pipelines.streamlets.avro._

class KeyedSchemas {
  implicit val productKeyed: Keyed[Product] = new Keyed[Product] { def key(p: Product) = p.id.toString }
  implicit val productCodec: KeyedSchema[Product] = AvroKeyedSchema[Product](Product.SCHEMA$)

  implicit val stockUpdateKeyed: Keyed[StockUpdate] = new Keyed[StockUpdate] { def key(p: StockUpdate) = p.productId.toString }
  implicit val stockUpdateCodec: KeyedSchema[StockUpdate] = AvroKeyedSchema[StockUpdate](StockUpdate.SCHEMA$)

  implicit val priceUpdateKeyed: Keyed[PriceUpdate] = new Keyed[PriceUpdate] { def key(p: PriceUpdate) = p.productId.toString }
  implicit val priceUpdateCodec: KeyedSchema[PriceUpdate] = AvroKeyedSchema[PriceUpdate](PriceUpdate.SCHEMA$)

  implicit val recommenderModelKeyed: Keyed[RecommenderModel] = new Keyed[RecommenderModel] { def key(m: RecommenderModel) = m.modelId.toString }
  implicit val recommenderModelCodec: KeyedSchema[RecommenderModel] = AvroKeyedSchema[RecommenderModel](RecommenderModel.SCHEMA$)
}

object KeyedSchemas extends KeyedSchemas {
  def instance: KeyedSchemas = this
}
