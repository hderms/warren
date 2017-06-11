package sync.db

import com.sksamuel.elastic4s.http.HttpClient

import scala.concurrent.duration._

object MessageIndex {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  //TODO: this doesn't work:
  def init(client: HttpClient) = client.execute {
    createIndex("messages").mappings(
      mapping("body") as textField("body"),
      mapping("exchangeName") as textField("exchange_name"),
      mapping("routingKeys") as objectField("routing_keys"),
      mapping("appId") as textField("app_id"),
      mapping("messageId") as textField("messageId"),
      mapping("userId") as textField("userId"),
      mapping("correlationId") as textField("correlationId"),
      mapping("headers") as objectField("headers")
    ).shards(1).waitForActiveShards(1)
  }.await(10.seconds)

}
