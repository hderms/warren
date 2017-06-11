package sync

import java.util.{HashMap => JavaHashMap}

import akka.Done
import akka.actor.ActorRef
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.sksamuel.elastic4s.streams.BulkIndexingSubscriber
import com.spingo.op_rabbit.Directives._
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit._
import com.spingo.op_rabbit.stream.RabbitSource
import play.api.libs.json.JsObject

import scala.util.Try



/**
  * Created by rtuser on 6/10/17.
  */
class Sync(rabbitControl: ActorRef)(subscriber: BulkIndexingSubscriber[FirehoseMessage])(implicit actorMaterializer: ActorMaterializer) {
  type OptionJavaHashMap[A, B] = Option[JavaHashMap[A, B]]
  type MaybeHeadersMap = Option[JavaHashMap[String, String]]
  private val firehoseExchange = Exchange.passive("amq.rabbitmq.trace")

  private val firehoseBinding = Queue.passive(topic(queue(
    "firehose",
    durable = false,
    exclusive = false,
    autoDelete = true),
    List("#"),
    exchange = firehoseExchange))


  implicit val recoveryStrategy = RecoveryStrategy.nack(false)

  def getProperty(d: Delivery, propertyName: String): Option[String] = {
    Option{
      d.properties
        .getHeaders
        .get("properties")
        .asInstanceOf[JavaHashMap[String, String]]
        .get(propertyName)
    }
  }
  def coerceToHashMap(obj: Object): MaybeHeadersMap = {
    obj.asInstanceOf[Option[JavaHashMap[String, String]]] match {
      case Some(hash) => Some(hash.asInstanceOf[JavaHashMap[String, String]])
      case None => None
    }
  }
  def getAppId(d: Delivery): Option[String] = getProperty(d, "app_id")
  def getUserId(d: Delivery): Option[String] = getProperty(d, "user_id")
  def getCorrelationId(d: Delivery): Option[String] = getProperty(d, "correlation_id")
  def getMessageId(d: Delivery): Option[String] = getProperty(d, "message_id")
  def getHeaders(d: Delivery): OptionJavaHashMap[String, String] = Option(getProperty(d, "headers")).flatMap{coerceToHashMap(_)}


  val source =  RabbitSource(
    rabbitControl,
    channel(qos = 3),
    consume(firehoseBinding),
    body(as[JsObject])
      & property(Headers.ExchangeName)
      & property(Headers.RoutingKeys)
      & extract(getAppId _)
      & extract(getMessageId _)
      & extract(getUserId _)
      & extract(getCorrelationId _)
      & extract(getHeaders _)
  ).map{(FirehoseMessage.fromJavaHashMap _).tupled}


  def run(callback: Try[Done] => Unit): SubscriptionRef =
    source
      .acked
      .alsoTo(Sink.fromSubscriber(subscriber))
      .to(Sink.onComplete(callback))
      .run()
}
