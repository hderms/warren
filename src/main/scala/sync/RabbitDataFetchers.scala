package sync

import java.util.{HashMap => JavaHashMap}

import com.spingo.op_rabbit.Delivery

trait RabbitDataFetchers {
  type OptionJavaHashMap[A, B] = Option[JavaHashMap[A, B]]
  type MaybeHeadersMap = Option[JavaHashMap[String, String]]

  def getAppId(d: Delivery): Option[String] = getProperty(d, "app_id")

  def getUserId(d: Delivery): Option[String] = getProperty(d, "user_id")

  def getCorrelationId(d: Delivery): Option[String] = getProperty(d, "correlation_id")

  def getMessageId(d: Delivery): Option[String] = getProperty(d, "message_id")

  def getHeaders(d: Delivery): OptionJavaHashMap[String, String] = Option(getProperty(d, "headers")).flatMap {
    coerceToHashMap(_)
  }

  def getProperty(d: Delivery, propertyName: String): Option[String] = {
    Option {
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
}
