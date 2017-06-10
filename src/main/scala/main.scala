import akka.actor.{ActorSystem, Props}
import com.spingo.op_rabbit.RabbitControl
import sync.Sync
import rabbitadmin.RabbitAdmin
/**
  * Created by rtuser on 6/10/17.
  */


class Main extends App {
  implicit val actorSystem = ActorSystem("such-system")
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
  val sync = new Sync(rabbitControl)
  RabbitAdmin.firehose.start
  sync.run
  RabbitAdmin.firehose.stop
}
