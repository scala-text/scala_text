scalaVersion := "2.13.12"

crossScalaVersions += "3.3.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.2.1",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.scalatest" %% "scalatest-wordspec" % "3.2.18" % "test"
)
