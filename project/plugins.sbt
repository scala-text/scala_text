logLevel := Level.Warn

// TODO tutをアップデートするときに avoid_tpolecat_insecure_warn.sbt が必要なくなっていることを確認して削除
// https://github.com/scalajp/scala_text/commit/5ef3b9e14eabd1dc68b28980f6d34b29b080400d
addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.12")
