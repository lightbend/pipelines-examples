package warez

import akka.NotUsed
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.stream.alpakka.elasticsearch.{ ReadResult, WriteMessage, WriteResult }
import akka.stream.alpakka.elasticsearch.scaladsl.{ ElasticsearchFlow, ElasticsearchSource }
import akka.stream.scaladsl.Source

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import spray.json.{ JsObject, JsonFormat }

import pipelines.akkastream.scaladsl.FlowWithOffsetContext

/**
 * Alpakka Kafka graph stages used to index and search for Warez domain entities.
 */
object ElasticSearchClient {
  case class Config(hostname: String, port: Int, indexName: String, typeName: String = "_doc")

  /**
   * Factory method for `ElasticSearchClient`.  Uses Context Bound on `JsonFormat` to make the type of our domain
   * entity visible (i.e. `Product`) as well as an implicit json format (i.e. `JsonFormat[Product]`).
   */
  def apply[T: JsonFormat](config: Config): ElasticSearchClient[T] =
    new ElasticSearchClient(config)
}

class ElasticSearchClient[T: JsonFormat](config: ElasticSearchClient.Config) {
  import config._

  /**
   * An ElasticSearch REST client used by Alpakka ElasticSearch to connect to the ES API.
   */
  implicit val esClient: RestClient = RestClient.builder(new HttpHost(hostname, port)).build()

  def indexFlow(): FlowWithOffsetContext[WriteMessage[T, NotUsed], WriteResult[T, CommittableOffset]] =
    ElasticsearchFlow.createWithContext[T, CommittableOffset](indexName, typeName)

  def querySource(searchCriteria: String): Source[ReadResult[JsObject], NotUsed] =
    ElasticsearchSource
      .create(indexName, typeName, query = s"""{
          "bool": {
            "must": {
              "query_string": {
                "query": "$searchCriteria"
              }
            }
          }
        }""")
}
