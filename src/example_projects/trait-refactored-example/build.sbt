scalaVersion := "2.13.9"

crossScalaVersions += "3.2.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
  "org.mindrot"     %  "jbcrypt"     % "0.4",
  "org.scalatest"   %% "scalatest"   % "3.2.13" % "test"
)
