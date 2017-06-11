package rabbitadmin

import scala.sys.process._
object RabbitAdmin {


  private val firehoseStart = "rabbitmqctl trace_on"
  private val firehoseStop = "rabbitmqctl trace_off"

  object firehose {
    def start = firehoseStart.!
    def stop = firehoseStop.!
  }
}
