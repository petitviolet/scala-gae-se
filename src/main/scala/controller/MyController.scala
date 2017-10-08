package controller

//import skinny.logging.Logger
import skinny.micro._

class MyController extends AsyncWebApp {
//  override protected val logger = Logger(this.getClass)
  error {
    case e =>
      logger.error(e.getMessage, e)
      throw e // re-throwing the exception keeps the response as status 500
  }

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

