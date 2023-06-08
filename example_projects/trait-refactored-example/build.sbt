scalaVersion := "2.13.11"

crossScalaVersions += "3.3.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.scalatest" %% "scalatest-wordspec" % "3.2.16" % "test"
)
