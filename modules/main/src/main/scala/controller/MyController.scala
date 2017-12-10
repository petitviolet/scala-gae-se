package controller

import java.util.concurrent.Executors

import com.google.appengine.api.ThreadManager
import common.TaskQueueHelper
import model.User
import net.petitviolet.gae.infra.Database
import skinny.micro._
import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.json4s.JSONSupport

import scala.concurrent.{ExecutionContext, Future}

class MyController extends AsyncWebApp {
  get("/") { implicit ctx =>
    logger.info(s"`/` params: $params")
    s"HELLO!!!! cookie: ${cookies}, params: ${params}, request: ${request}, path: ${requestPath}"
  }

  get("/echo/*") { implicit ctx =>
    logger.info(s"`/echo`")
    multiParams("splat").head
  }

  get("/say-hello") { implicit ctx =>
    logger.info(s"`/say-hello` params: $params")
    s"Hello, ${params.getOrElse("name", "Anonymous")}!\n"
  }

}


class SelectController extends AsyncWebApp with JSONSupport {

  private def run(executionContext: ExecutionContext)(implicit ctx: SkinnyContext) = {
    val f = Future {
      logger.info(s"[START]path: $requestPath")
      val result = Database.MyDatabase.withRead { implicit s =>
        val users = User.findAllWithLimitOffset(limit = 10)
        toJSONString(users)
      }
      logger.info(s"[END]path: $requestPath")
      result
    }(executionContext)
    responseFuture(f)
  }

  private val ec1: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(3))
  get("/users1") { implicit ctx =>
    run(ec1)
  }

  private val ec2: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit =
      ThreadManager.createThreadForCurrentRequest(runnable).run()

    override def reportFailure(cause: Throwable): Unit = ExecutionContext.defaultReporter
  }
  get("/users2") { implicit ctx =>
    run(ec2)
  }

  get("/users3") { implicit ctx =>
    // no database operation/query
    logger.info("request: /users3")
    "OK"
  }
}


class TQController extends AsyncWebApp {
  get("/push") { implicit ctx =>
    logger.info(s"`/push` params: $params")
    val (key, value) = params.head
    TaskQueueHelper.pushTask(key, value)
    s"pushed. $key = $value"
  }

  post("/_ah/push-handlers/push") { implicit ctx =>
    logger.info(s"`/_ah/push_handlers/push` body: ${request.body}")
    "OK"
  }

}

