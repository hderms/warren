package sync.db

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.index.{CreateIndexResponse, IndexResponse}
import com.sksamuel.elastic4s.http.search.SearchResponse
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import sync.FirehoseMessage

import scala.concurrent.Future
import scala.concurrent.duration._


object Db {

  object FireHoseDb {

    import FirehoseEncodings._

    val client: HttpClient = HttpClient(ElasticsearchClientUri("127.0.0.1", 9200)) //TcpClient.transport(ElasticsearchClientUri("127.0.0.1", 9300).copy(options = Map[String, String]("cluster.name" -> "firehose")  ))

    import com.sksamuel.elastic4s.http.ElasticDsl._



    def insert(firehoseMessage: FirehoseMessage): Future[IndexResponse] = client.execute {
      indexInto("messages" / "message").doc(firehoseMessage).refresh(RefreshPolicy.IMMEDIATE)
    }

    def query(queryList: List[(String, String)]): Future[SearchResponse] = client.execute {
      search("messages").query {
        must {
          queryList.map { tuple => termQuery(tuple) }
        }
      }
    }
  }
}
