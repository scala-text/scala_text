package domain

import org.scalatest._

class UserServiceSpec extends WordSpec {
  val user = User(1, "test_name", "test_password")

  val sut = new UserService with PasswordServiceImpl with UserRepository {
    def find(id: Long): Option[domain.User] = Some(user)
    def find(name: String): Option[domain.User] = Some(user)
    def insert(user: domain.User): domain.User = user
  }

  "register" should {
    "throw an exception if a name is too long" in {
      val exception = intercept[Exception] {
        sut.register("too long name" * 100, "password")
      }
      assert(exception.getMessage === "Too long name!")
    }

    "throw an exception if a name is already registered" in {
      val exception = intercept[Exception] {
        sut.register("test_name", "password")
      }
      assert(exception.getMessage === "Already registered!")
    }
  }
}
