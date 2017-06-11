package sync.db

import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import play.api.libs.json._
import sync.FirehoseMessage

object FirehoseEncodings {
  implicit object FirehoseMessageIndexable extends Indexable[FirehoseMessage] {
    implicit val jsonWriter = Json.format[FirehoseMessage]

    override def json(t: FirehoseMessage): String =
      Json.obj(
        "message_body" -> t.body,
        "user_id" -> t.userId,
        "correlation_id" -> t.correlationId,
        "exchange_name" -> t.exchangeName,
        "message_id" -> t.messageId.toString,
        "app_id" -> t.appId.toString,
        "routingKeys" -> t.routingKey
      ).toString

  }

  implicit object FirehoseMessageHitReader extends HitReader[FirehoseMessage] {
    override def read(hit: Hit): Either[Throwable, FirehoseMessage] = {
      val body = Json.parse(hit.sourceAsString)
      Json.fromJson[JsObject](body) match {
        case s: JsSuccess[FirehoseMessage] => Right(s.get)
        case e: JsError => Left(new Exception(s"couldnt parse search result: $e"))
      }
    }
  }
}
