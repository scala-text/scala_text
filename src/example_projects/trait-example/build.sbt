scalaVersion := "2.13.12"

crossScalaVersions += "3.3.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.1.0",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
