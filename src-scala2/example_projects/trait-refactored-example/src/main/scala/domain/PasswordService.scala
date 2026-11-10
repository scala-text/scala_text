package domain

import org.mindrot.jbcrypt.BCrypt

trait PasswordService {
  def hashPassword(rawPassword: String): String

  def checkPassword(rawPassword: String, hashedPassword: String): Boolean
}

trait PasswordServiceImpl extends PasswordService {
  def hashPassword(rawPassword: String): String =
    BCrypt.hashpw(rawPassword, BCrypt.gensalt())

  def checkPassword(rawPassword: String, hashedPassword: String): Boolean =
    BCrypt.checkpw(rawPassword, hashedPassword)
}
