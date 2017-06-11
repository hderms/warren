
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.spingo.op_rabbit.RabbitControl
import rabbitadmin.RabbitAdmin
import sync.Sync
import sync.db.Db.FireHoseDb
/**
  * Created by rtuser on 6/10/17.
  */


  object LoggingDecider {
  val decider: Supervision.Decider = { e =>
    println("Unhandled exception in stream", e)
    Supervision.Stop
  }
}


object Main extends App {
  implicit val actorSystem = ActorSystem("such-system")
  val actorMaterializerSettings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(LoggingDecider.decider)
  implicit val materializer = ActorMaterializer(actorMaterializerSettings)(actorSystem)
  implicit val ec = materializer.executionContext
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
  val sync = new Sync(rabbitControl)(FireHoseDb.subscriber(actorSystem))
  RabbitAdmin.firehose.start
  //FireHoseDb.init
  http.Service.builder.start.runAsync(x => println(x))
  sync.run { _ => RabbitAdmin.firehose.stop }

  //Source.fromPublisher(FireHoseDb.publisher).log("found one").runWith(Sink.onComplete( _ => println("we finished")))
}
