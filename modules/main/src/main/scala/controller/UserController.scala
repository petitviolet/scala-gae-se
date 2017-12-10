package controller

import java.util.concurrent.Executors

import com.google.appengine.api.ThreadManager
import net.petitviolet.gae.infra.Database.MyDatabase
import org.slf4j.LoggerFactory
import service.UserService
import skinny.micro.AsyncWebApp
import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.json4s.JSONSupport

import scala.concurrent.{ExecutionContext, Future}

class UserController extends AsyncWebApp with JSONSupport {

  private val ec1: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(3))

  private val ec2: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit =
      ThreadManager.createThreadForCurrentRequest(runnable).run()
    override def reportFailure(cause: Throwable): Unit =
      ExecutionContext.defaultReporter
  }
  private def _run(executionContext: ExecutionContext)(implicit ctx: SkinnyContext) = {
    val param = fromJSONString[CreateParam](ctx.request.body).get

    val resF = UserController.run(param)(executionContext)
    responseFuture(resF)
  }

  post("/user/new1") { implicit ctx =>
    _run(ec1)
  }

  post("/user/new2") { implicit ctx =>
    _run(ec2)
  }
}

private object UserController {
  private lazy val logger = LoggerFactory.getLogger(getClass)

  def run(param: CreateParam)(ec: ExecutionContext): Future[String] = {
    val CreateParam(name, email) = param
    logger.debug(s"[DEBUG]name: $name, email: $email")
    logger.info(s"[INFO]name: $name, email: $email")
    Future {
      val user = UserService.create(name, email)
      MyDatabase localTx { implicit s =>
        UserService.store(user)
      }
      logger.info(s"[INFO]created user: $user")
      user.id
    }(ec)
  }

}

case class CreateParam(name: String, email: String)
