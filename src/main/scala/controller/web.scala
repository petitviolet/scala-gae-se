package controller

import skinny.logging.Logger
import skinny.micro._

object Web extends WebApp {
  override protected val logger = Logger(this.getClass)
  get("/") {
    logger.info(s"`/` params: $params")
    s"HELLO!!!! cookie: ${cookies}, params: ${params}, request: ${request}, path: ${requestPath}"
  }

  get("/say-hello") {
    logger.info(s"`/say-hello` params: $params")
    s"Hello, ${params.getOrElse("name", "Anonymous")}!\n"
  }
}

