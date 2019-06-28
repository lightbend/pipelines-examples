// Resolver for the pipelines-sbt plugin
//
// NOTE: Lightbend Commercial repository!
//  Please add your Lightbend Commercial download credentials to the global SBT config.
//
// Refer to https://github.com/lightbend/pipelines-docs/blob/master/user-guide/getting-started.md
// for details on how to setup your Lightbend Commercial download credentials.
//
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)
resolvers += "Akka Snapshots" at "https://repo.akka.io/snapshots/"

addSbtPlugin("com.lightbend.pipelines" % "sbt-pipelines" % "1.1.0")
