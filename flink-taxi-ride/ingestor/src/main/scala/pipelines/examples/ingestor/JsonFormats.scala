package pipelines.examples
package ingestor

import spray.json._
import pipelines.flink.avro._

object TaxiRideJsonProtocol extends DefaultJsonProtocol {
  implicit object TaxiRideJsonFormat extends RootJsonFormat[TaxiRide] {
    def write(t: TaxiRide) = JsObject(
      "rideId" -> JsNumber(t.rideId),
      "isStart" -> JsBoolean(t.isStart),
      "taxiId" -> JsNumber(t.taxiId),
      "passengerCnt" -> JsNumber(t.passengerCnt),
      "driverId" -> JsNumber(t.driverId),
      "startLon" -> JsNumber(t.startLon.doubleValue()),
      "startLat" -> JsNumber(t.startLat.doubleValue()),
      "endLon" -> JsNumber(t.endLon.doubleValue()),
      "endLat" -> JsNumber(t.endLat.doubleValue()),
      "startTime" -> JsNumber(t.startTime),
      "endTime" -> JsNumber(t.endTime)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields(
        "rideId",
        "isStart",
        "taxiId",
        "passengerCnt",
        "driverId",
        "startLon",
        "startLat",
        "endLon",
        "endLat",
        "startTime",
        "endTime") match {
          case Seq(JsNumber(rideId),
            JsBoolean(isStart),
            JsNumber(taxiId),
            JsNumber(passengerCnt),
            JsNumber(driverId),
            JsNumber(startLon),
            JsNumber(startLat),
            JsNumber(endLon),
            JsNumber(endLat),
            JsNumber(startTime),
            JsNumber(endTime)) ⇒
            new TaxiRide(
              rideId.longValue(),
              isStart,
              taxiId.longValue(),
              passengerCnt.intValue(),
              driverId.longValue(),
              startLon.floatValue(),
              startLat.floatValue(),
              endLon.floatValue(),
              endLat.floatValue(),
              startTime.longValue(),
              endTime.longValue())
          case _ ⇒ throw new DeserializationException("TaxiRide expected")
        }
    }
  }
}

object TaxiFareJsonProtocol extends DefaultJsonProtocol {
  implicit object TaxiFareJsonFormat extends RootJsonFormat[TaxiFare] {
    def write(t: TaxiFare) = JsObject(
      "rideId" -> JsNumber(t.rideId),
      "taxiId" -> JsNumber(t.taxiId),
      "paymentType" -> JsString(t.paymentType),
      "driverId" -> JsNumber(t.driverId),
      "startTime" -> JsNumber(t.startTime),
      "tip" -> JsNumber(t.tip.floatValue()),
      "tolls" -> JsNumber(t.tolls.floatValue()),
      "totalFare" -> JsNumber(t.totalFare.floatValue())
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields(
        "rideId",
        "taxiId",
        "paymentType",
        "driverId",
        "startTime",
        "tip",
        "tolls",
        "totalFare") match {
          case Seq(JsNumber(rideId),
            JsNumber(taxiId),
            JsString(paymentType),
            JsNumber(driverId),
            JsNumber(startTime),
            JsNumber(tip),
            JsNumber(tolls),
            JsNumber(totalFare)) ⇒
            new TaxiFare(
              rideId.longValue(),
              taxiId.longValue(),
              paymentType,
              driverId.longValue(),
              startTime.longValue(),
              tip.floatValue(),
              tolls.floatValue(),
              totalFare.floatValue())
          case _ ⇒ throw new DeserializationException("TaxiFare expected")
        }
    }
  }
}

