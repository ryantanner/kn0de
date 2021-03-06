import play.api._
import play.api.Logger

import models._
import models.auth._
import anorm._

import play.api.db._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._

import scala.slick.jdbc.meta.{ MTable, MQName }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Starting application...")
    InitialData.dbSetup
    InitialData.insert()
  }

}

object InitialData {

  def date(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)

  def dbSetup = {
    Logger.info("Creating DDLs...")
    def database = Database.forDataSource(DB.getDataSource())

    database.withSession { implicit db: Session => 
      val ddl = 
        Account.AccountTable.ddl ++
        Item.ItemTable.ddl ++
        Sub.SubTable.ddl ++
        Subscription.SubscriptionTable.ddl ++
        Moderator.ModeratorTable.ddl

        Logger.info(ddl.createStatements.reduceLeft(_ + "\n" + _))

      if(MTable.getTables.list.exists(t => t.name.name == "account"))
        ddl.drop
      ddl.create
    }
  }

  def insert() = {
    Logger.info("Inserting initial data...")

    if(Account.findAll.isEmpty)   {

      Seq(
        Account(None, "default@kn0de.com", "password", "default", NormalUser),
        Account(None, "user1@example.com", "password", "user1", NormalUser),
        Account(None, "user2@example.com", "password", "user2", NormalUser),
        Account(None, "user3@example.com", "password", "user3", NormalUser),
        Account(None, "user4@example.com", "password", "user4", NormalUser),
        Account(None, "user5@example.com", "password", "user5", NormalUser)
      ).foreach(Account.create)

      Seq(
        Sub(None, "AskScience", "A sub about science", 2),
        Sub(None, "Politics", "A sub about politics", 2),
        Sub(None, "Technology", "A sub about technology", 2),
        Sub(None, "Funny", "A sub about funny", 2),
        Sub(None, "Stuff", "A sub about stuff", 2),
        Sub(None, "Misc", "A sub about everything", 2)
      ).foreach(Sub.create)

      Subscription.create(1, 1, "AskScience")
      Subscription.create(1, 2, "Politics")
      Subscription.create(1, 3, "Technology")
      Subscription.create(1, 4, "Funny")
      Subscription.create(1, 5, "Stuff")
      Subscription.create(1, 6, "Misc")
      Subscription.create(2, 1, "AskScience")
      Subscription.create(2, 2, "Technology")
      Subscription.create(3, 4, "Funny")
      Subscription.create(3, 1, "AskScience")
      Subscription.create(3, 5, "Stuff")
      Subscription.create(3, 6, "Misc")
      Subscription.create(4, 1, "AskScience")
      Subscription.create(4, 2, "Politics")
      Subscription.create(4, 5, "Stuff")

    }

    if(Item.totalNumberItems == 0) {

      Seq(
        Item(None, "Lorem ipsum", 1, 1, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 2, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 3, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 4, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 5, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 6, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 1, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 2, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 3, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 4, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 5, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 6, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 1, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 2, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 3, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 4, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 5, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 6, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 1, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 2, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 3, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 4, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 5, Some("http://example.com"), "Lorem ipsum dolor set amet"),
        Item(None, "Lorem ipsum", 1, 6, Some("http://example.com"), "Lorem ipsum dolor set amet")
      ).foreach(Item.create)


    }

  }

}
        
