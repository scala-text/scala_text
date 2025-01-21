scalaVersion := "2.13.16"

crossScalaVersions += "3.6.3"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.3.2",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
