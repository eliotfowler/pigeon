package models

import anorm.SqlParser._
import anorm.{~, _}
import play.api.Play.current
import play.api.db.DB
import securesocial.core._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

import scala.concurrent.Future

case class UserLevel(userLevelSeqId: Long, maxActiveUploads: Long, maxActiveUploadBytes: Long)

case class User(userSeqId: Long, userProfile: BasicProfile, userLevel: UserLevel)

case class UserUsage(currentActiveUploads: Option[Long], currentActiveUploadBytes: Option[Long])

  object User {
    val user = {
      get[Long]("seqId") ~
      get[String]("userId") ~
      get[String]("providerId") ~
      get[Option[String]]("firstName") ~
      get[Option[String]]("lastName") ~
      get[Option[String]]("fullName") ~
      get[Option[String]]("email") ~
      get[Option[String]]("avatarUrl") ~
      get[String]("authenticationMethod") ~
      get[Option[String]]("oAuth1Token") ~
      get[Option[String]]("oAuth1Secret") ~
      get[Option[String]]("oAuth2AccessToken") ~
      get[Option[String]]("oAuth2TokenType") ~
      get[Option[Int]]("oAuth2ExpiresIn") ~
      get[Option[String]]("oAuth2RefreshToken") ~
      get[Option[String]]("passwordHasher") ~
      get[Option[String]]("password") ~
      get[Option[String]]("passwordSalt") ~
      get[Long]("userLevelSeqId") ~
        get[Long]("maxActiveUploads")~
        get[Long]("maxActiveUploadBytes") map {
        case seqId ~ userId ~ providerId ~ firstName ~ lastName ~ fullName ~ email ~ avatarUrl ~
          authenticationMethod ~ oAuth1Token ~ oAuth1Secret ~ oAuth2AccessToken ~ oAuth2TokenType ~
          oAuth2ExpiresIn ~ oAuth2RefreshToken ~ passwordHasher ~ password ~ passwordSalt ~
          userLevelSeqId ~ maxActiveUploads ~ maxActiveUploadBytes =>
          User(seqId, BasicProfile(providerId, userId, firstName, lastName, fullName, email, avatarUrl, AuthenticationMethod(authenticationMethod),
            Option(OAuth1Info(oAuth1Token.get, oAuth1Secret.get)), Option(OAuth2Info(oAuth2AccessToken.get, oAuth2TokenType, oAuth2ExpiresIn, oAuth2RefreshToken)),
            Option(PasswordInfo(passwordHasher.get, password.get, passwordSalt))), UserLevel(userLevelSeqId, maxActiveUploads, maxActiveUploadBytes))
      }
    }

    def find(seqId: Long): Future[User] = {
      val result = DB.withConnection { implicit c =>
        SQL("SELECT * FROM p_user_profile JOIN p_user_level ON p_user_profile.seqId = p_user_level.userSeqId WHERE seqId = {seqId}")
          .on('seqId -> seqId).as(user *).headOption
      }

      Future.successful(result.get)
    }

    implicit val userWrites = new Writes[User] {
      def writes(user: User) = Json.obj(
        "id" -> user.userSeqId,
        "firstName" -> user.userProfile.firstName,
        "lastName" -> user.userProfile.lastName,
        "fullName" -> user.userProfile.fullName,
        "email" -> user.userProfile.email,
        "avatarUrl" -> user.userProfile.avatarUrl
      )
    }

  }