import com.google.inject.AbstractModule
import com.typesafe.config.{ Config, ConfigFactory }
import play.api.{ Configuration, Environment }

import config.SbrConfigManager
import services._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    val config = SbrConfigManager.envConf(ConfigFactory.load())
    bind(classOf[Config]).toInstance(config)
    bind(classOf[DataAccess]).toInstance(new H2Data(config))
  }
}