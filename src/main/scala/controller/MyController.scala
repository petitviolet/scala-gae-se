package controller

//import skinny.logging.Logger
import common.TaskQueueHelper
import skinny.micro._

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

