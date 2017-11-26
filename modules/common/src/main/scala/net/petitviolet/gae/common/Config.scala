package net.petitviolet.gae.common

import com.typesafe.config.{Config, ConfigFactory}

trait UsesConfig {
  val config: Config
}

trait MixInConfig {
  val config: Config = ConfigContainer.config
}

object ConfigContainer {
  lazy val config: Config = ConfigFactory.load()
}
