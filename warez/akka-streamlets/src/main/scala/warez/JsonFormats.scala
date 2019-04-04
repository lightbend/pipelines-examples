package warez

import spray.json._

object JsonFormats extends DefaultJsonProtocol {

  implicit val skuFormat: JsonFormat[Sku] = jsonFormat4(Sku.apply)

  implicit val priceUpdateFormat = jsonFormat3(PriceUpdate.apply)

  implicit val stockUpdateFormat = jsonFormat3(StockUpdate.apply)

  implicit val productFormat = jsonFormat5(Product.apply)

  implicit val recommenderModelFormat = jsonFormat4(RecommenderModel.apply)
}

