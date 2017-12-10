package net.petitviolet.gae.infra

import java.time.{LocalDateTime, ZoneId}
import java.util.UUID

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import net.petitviolet.gae.common.MixInConfig
import scalikejdbc._
import scalikejdbc.config._
import skinny.orm._

/**
 * class for accessing Database
 */
sealed abstract class Database(val dbName: Symbol) extends DBs with TypesafeConfigReader with TypesafeConfig {
  import Database.OptConfig

  private def db = NamedDB(dbName)

  def withInTx[A](execution: DBSession => A): A = {
    db withinTx execution
  }

  def localTx[A](execution: DBSession => A): A = {
    db localTx execution
  }

  def withRead[A](execution: DBSession => A): A = {
    db readOnly execution
  }

  def readSession: DBSession = {
    ReadOnlyNamedAutoSession(dbName)
  }

  def writeSession: DBSession = {
    NamedAutoSession(dbName)
  }

  private[infra] def close(): Unit = {
    source.close()
  }

  private[infra] lazy val source: HikariDataSource = {
    val _conf = config.getConfig(s"db.${dbName.name}")

    val ds = new HikariDataSource()

    // must
    ds.setJdbcUrl(_conf.getString("url"))
    ds.setUsername(_conf.getString("user"))
    ds.setPassword(_conf.getString("password"))
    ds.setPoolName(dbName.name)

    val driver = _conf.getString("driver")
    Class.forName(driver)
    ds.setDriverClassName(driver)

    // optional(not implemented all yet...)
    _conf.optionalLong("connectionTimeout") foreach ds.setConnectionTimeout
    _conf.optionalLong("idleTimeout") foreach ds.setIdleTimeout
    _conf.optionalLong("maxLifetime") foreach ds.setMaxLifetime

    _conf.optionalInt("maximumPoolSize") foreach ds.setMaximumPoolSize
    _conf.optionalBool("autoCommit") foreach ds.setAutoCommit
    _conf.optionalBool("registerMbeans") foreach ds.setRegisterMbeans

    ds
  }

  private[infra] final def setup(): Unit = {
    ConnectionPool.add(dbName, new DataSourceConnectionPool(source))
  }
}

object Database {
  private implicit class OptConfig(val config: Config) extends AnyVal {
    def optional(key: String): Option[AnyRef] = if (config.hasPath(key)) Some(config.getAnyRef(key)) else None
    def optionalLong(key: String): Option[Long] = if (config.hasPath(key)) Some(config.getLong(key)) else None
    def optionalInt(key: String): Option[Int] = if (config.hasPath(key)) Some(config.getInt(key)) else None
    def optionalString(key: String): Option[String] = if (config.hasPath(key)) Some(config.getString(key)) else None
    def optionalBool(key: String): Option[Boolean] = if (config.hasPath(key)) Some(config.getBoolean(key)) else None
  }

  def setup(): Unit = {
    scalikejdbc.config.DBs.loadGlobalSettings()

    dbs foreach { _.setup() }
  }

  def shutDown(): Unit = {
    dbs foreach { _.close() }
  }

  private lazy val dbs: Seq[Database] = MyDatabase :: Nil

  case object MyDatabase extends Database('my_database) with MixInConfig

  private lazy val ZONE_ID = ZoneId.systemDefault()
  def now(): LocalDateTime = LocalDateTime.now(ZONE_ID)
}

/**
 * ORM
 * @tparam T entity type
 */
sealed trait DatabaseMapper[T] extends SkinnyMapperBase[T] {
  protected def db: Database

  final override def connectionPoolName: Any = db.dbName

  override def autoSession: DBSession = db.readSession

  override lazy val schemaName = Some(db.dbName.name)
}

trait MyDatabaseMapper[T] extends DatabaseMapper[T] with SkinnyCRUDMapperWithId[String, T] {
  protected lazy val db: Database = Database.MyDatabase
  override def defaultAlias = syntax(getClass.getSimpleName.take(1))

  override def useExternalIdGenerator: Boolean = true
  override def generateId: String = Id.generate
  override def idToRawValue(id: String): String = id
  override def rawValueToId(value: Any): String = value.toString
}

object Id {
  def generate: String = UUID.randomUUID().toString
}
