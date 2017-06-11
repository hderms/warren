package http

import org.http4s.{Request, Response, Service => Http4sService}

import scala.concurrent.ExecutionContext

/**
  * Created by rtuser on 6/11/17.
  */
object Service {

  import org.http4s.server.blaze._
  // import org.http4s.server.blaze._
  import org.http4s.server.syntax._

  def services(implicit ec: ExecutionContext): Http4sService[Request, Response] = endpoints.Endpoints.homeService orElse endpoints.Endpoints.searchService

  // services: org.http4s.Service[org.http4s.Request,org.http4s.Response] = Kleisli(scalaz.Kleisli$$Lambda$34881/801441550@455367d9)

  def builder(implicit ec: ExecutionContext): BlazeBuilder = BlazeBuilder.bindHttp(8080, "localhost").mountService(services, "/")
}
