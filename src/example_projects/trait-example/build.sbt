scalaVersion := "2.13.14"

crossScalaVersions += "3.5.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.3.1",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
