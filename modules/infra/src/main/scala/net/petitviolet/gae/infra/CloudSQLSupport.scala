package net.petitviolet.gae.infra

import java.time.{ LocalDateTime, ZoneId }

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import scalikejdbc._
import scalikejdbc.config._
import skinny.orm._

/**
 * DBの共通部分
 * Cloud SQLを想定している
 */
sealed abstract class Database(val dbName: Symbol) extends DBs with TypesafeConfigReader with TypesafeConfig {
  import Database.OptConfig

  def withInTx[A](execution: DBSession => A): A = {
    NamedDB(dbName) withinTx execution
  }

  /**
   * 通常のinsertはこちらを利用すること！
   *
   * @param execution
   * @tparam A
   * @return
   */
  def localTx[A](execution: DBSession => A): A = {
    NamedDB(dbName) localTx execution
  }

  /**
   * トランザクション分離レベルを「ReadCommitted」にする
   * バッチ等で並列にbulk insertを行うような処理の場合、こちらを利用すること！
   *
   * 以下、この関数を作成した経緯を記載
   * https://fringe81.one-team.io/topics/8329
   *
   * @param execution
   * @tparam A
   * @return
   */
  def localReadCommittedTx[A](execution: DBSession => A): A = {
    NamedDB(dbName) isolationLevel (IsolationLevel.ReadCommitted) localTx execution
  }

  def withRead[A](execution: DBSession => A): A = {
    NamedDB(dbName) readOnly execution
  }

  def readSession: DBSession = {
    ReadOnlyNamedAutoSession(dbName)
  }

  def writeSession: DBSession = {
    NamedAutoSession(dbName)
  }

  private[gae_support] def close(): Unit = {
    source.close()
  }

  // HikariCPの設定を反映する
  // closeする際にDataSourceだとcloseが呼べないのでHikariDataSourceのままにしておく
  private[gae_support] lazy val source: HikariDataSource = {
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

  private[gae_support] final def setup(): Unit = {
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
    //    ConnectionPool.closeAll()
  }

  private val dbs: Seq[Database] = Columva :: Nil

  case object Columva extends Database('columva_v2) with MixInConfig

  private val ZONE_ID = ZoneId.systemDefault()
  def now() = LocalDateTime.now(ZONE_ID)
}

/**
 * DBに対するORM
 * @tparam T
 */
sealed trait DatabaseMapper[T] extends SkinnyMapperBase[T] {
  def db: Database

  final override def connectionPoolName: Any = db.dbName

  override def autoSession: DBSession = db.readSession

  override def schemaName = Some(db.dbName.name)
}

/**
 * Columva V2 DBを使用する場合はこちらをextendsする
 * @tparam T
 */
trait ColumvaMapper[T] extends DatabaseMapper[T] with SkinnyCRUDMapper[T] {
  lazy val db: Database = Database.Columva
}

trait ColumvaMapperNoId[T] extends DatabaseMapper[T] with SkinnyNoIdCRUDMapper[T] {
  lazy val db: Database = Database.Columva
}

trait ColumvaMapperWithId[Id, T] extends DatabaseMapper[T] with SkinnyCRUDMapperWithId[Id, T] {
  lazy val db: Database = Database.Columva
  override def useExternalIdGenerator: Boolean = true
  def generateId: Id
}

trait ColumvaJoinTable[T] extends DatabaseMapper[T] with SkinnyJoinTable[T] {
  lazy val db: Database = Database.Columva
}

