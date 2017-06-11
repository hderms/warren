package http.endpoints

import com.sksamuel.elastic4s.http.search.SearchResponse
import org.http4s.HttpService
import org.http4s.QueryParamDecoder._
import org.http4s.dsl._

import scala.concurrent.{ExecutionContext, Future}


trait Searchable {
  def search(query: Query): Future[SearchResponse]
}

trait WarrenApp[Search <: Searchable] {

}

case class Query(appId: Option[String], messageId: Option[String], correlationId: Option[String], userId: Option[String], headers: Option[String]) {
  def appIdToQuery = appId.map { s => ("app_id", s) }

  def messageIdToQuery = messageId.map { s => ("message_id", s) }

  def correlationIdToQuery = correlationId.map { s => ("correlation_id", s) }

  def userIdToQuery = userId.map { s => ("user_id", s) }

  def headersToQuery = headers.map { s => ("headers", s) }

  def toTupleList = List(appIdToQuery, messageIdToQuery, correlationIdToQuery, userIdToQuery, headersToQuery).flatten
}

object ProductionSearchable extends Searchable {
  def search(query: Query): concurrent.Future[SearchResponse] = sync.db.Db.FireHoseDb.query(query.toTupleList)
}

object Endpoints {

  object messageIdQueryParamDecoder extends OptionalQueryParamDecoderMatcher[String]("message_id")

  object appIdQueryParamDecoder extends OptionalQueryParamDecoderMatcher[String]("app_id")

  object correlationIdQueryParamDecoder extends OptionalQueryParamDecoderMatcher[String]("correlation_id")

  object userIdQueryParamDecoder extends OptionalQueryParamDecoderMatcher[String]("user_id")

  object headersQueryParamDecoder extends OptionalQueryParamDecoderMatcher[String]("headers")

  def homeService(implicit ec: ExecutionContext) = HttpService {
    case GET -> Root =>
      Ok("Hello, better world.")
  }

  import delorean._

  def searchService(implicit ec: ExecutionContext) = HttpService {
    case GET -> Root / "messages" :? messageIdQueryParamDecoder(messageId)
      +& appIdQueryParamDecoder(appId)
      +& correlationIdQueryParamDecoder(correlationId)
      +& userIdQueryParamDecoder(userId)
      +& headersQueryParamDecoder(headers) => ProductionSearchable
      .search(http.endpoints.Query(appId, messageId, correlationId, userId, headers))
      .map { (s: SearchResponse) => s.hits.hits.map(_.toString).mkString(" ") }
      .toTask
      .flatMap {
        (result: String) =>
          Ok(result)
      }
  }
}
