package models.auth

import play.api.db._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.Logger
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import java.sql.Clob
import org.mindrot.jbcrypt.BCrypt

import models._

case class Account(id: Pk[Long], email: String, password: String, name: String, permission: Permission)

object Account {

  object Clob {
    def unapply(clob: Clob): Option[String] = Some(clob.getSubString(1, clob.length.toInt))
  }

  implicit val rowToPermission: Column[Permission] = {
    Column.nonNull[Permission] { (value, meta) =>
      value match {
        case Clob("Administrator") => Right(Administrator)
        case Clob("NormalUser") => Right(NormalUser)
        case _ => Left(TypeDoesNotMatch(
          "Cannot convert %s : %s to Permission for column %s".format(value, value.getClass, meta.column)))
      }
    }
  }

  implicit def permissionToString(permission: Permission): String = {
    permission match {
      case Administrator => "Administrator"
      case NormalUser => "NormalUser"
    }
  }
  
  implicit def stringToPermission(permission: String) = {
    permission match {
      case "Administrator" => Administrator
      case "NormalUser" => NormalUser
    }
  }

  val simple = {
    get[Pk[Long]]("account.account_id") ~
    get[String]("account.email") ~
    get[String]("account.password") ~
    get[String]("account.name") ~
    get[String]("account.permission") map {
      case id~email~pass~name~perm => Account(id, email, pass, name, perm)
    }
  }



  def authenticate(name: String, password: String): Option[Account] = {
    Logger.info("[Account] Authenticating %s/%s".format(name, password))
    Logger.info("[Account] Salt should equal %s".format(BCrypt.hashpw(password, BCrypt.gensalt())))

    DB.withConnection { implicit connection =>
      val salt = SQL("SELECT password FROM account WHERE account_id = 1").as(scalar[String].single)
      Logger.info("[Account] Salt in db equals %s".format(salt))
    }

    findByName(name).filter { account => BCrypt.checkpw(password, account.password) }
  }

  def findByEmail(email: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM account WHERE email = {email}").on(
        'email -> email
      ).as(simple.singleOpt)
    }
  }

  def findById(id: Long): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM account WHERE account_id = {id}").on(
        'id -> id
      ).as(simple.singleOpt)
    }
  }

  def findByName(name: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM account WHERE name = {name}").on(
        'name -> name 
      ).as(simple.singleOpt)
    }
  }

  def findAll: Seq[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from account").as(simple *)
    }
  }

  def isAdmin(accountId: Long): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select count(account.account_id) = 1 from account
          where account.id = {id} and account.permission = 'administrator'
        """
      ).on(
        'id -> accountId
      ).as(scalar[Boolean].single)
    }
  }

  def create(account: Account): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO account VALUES (DEFAULT, {name}, {email}, {pass}, {permission})").on(
        'email -> account.email,
        'pass -> BCrypt.hashpw(account.password, BCrypt.gensalt()),
        'name -> account.name,
        'permission -> account.permission.toString
      ).executeInsert()
    }
  }

  def update(account: Account): Int = {
    Logger.info("[Account] updating account id = %d".format(account.id.getOrElse(0)))
    DB.withConnection { implicit connection =>
      SQL("""
        update account
        set email = {email},
        password = {password},
        name = {name}
        where account_id = {id}
        """
      ).on(
        'email -> account.email,
        'password -> BCrypt.hashpw(account.password, BCrypt.gensalt()),
        'name -> account.name,
        'id -> account.id
      ).executeUpdate()
    }
  }

}
