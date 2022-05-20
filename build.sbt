import org.scalajs.sbtplugin.ScalaJSCrossVersion
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbtcrossproject.CrossProject

enablePlugins(GitVersioning)

Global / excludeLintKeys += git.useGitDescribe

ThisBuild / git.useGitDescribe := true

ThisBuild / organization := "io.github.scala-loci"

ThisBuild / homepage := Some(url("https://scala-loci.github.io/"))

ThisBuild / licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")

ThisBuild / scalacOptions ++= {
  if (`is 3+`(scalaVersion.value))
    Seq("-feature", "-deprecation", "-unchecked")
  else
    Seq("-feature", "-deprecation", "-unchecked", "-Xlint", "-language:higherKinds")
}

ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.15", "2.13.8", "3.1.2")

ThisBuild / scalaVersion := {
  val versions = (ThisBuild / crossScalaVersions).value
  val version = Option(System.getenv("SCALA_VERSION")) getOrElse versions(versions.size - 2)
  versions.reverse find { _ startsWith version } getOrElse versions.last
}


def `is 2.12+`(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) exists { case (m, n) => m >= 3 || m == 2 && n >= 12 }

def `is 2.13+`(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) exists { case (m, n) => m >= 3 || m == 2 && n >= 13 }

def `is 3+`(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) exists { case (m, _) => m >= 3 }


def scala2only(project: CrossProject) = project settings (
  compile / skip := (compile / skip).value || `is 3+`(scalaVersion.value),
  publish / skip := (publish / skip).value || `is 3+`(scalaVersion.value))


val build = taskKey[Unit]("Builds the system")


val aggregatedProjects = ScopeFilter(inAggregates(ThisProject, includeRoot = false))

def taskSequence(tasks: TaskKey[_]*) =
  Def.sequential(tasks map { task => Def.task { Def.unit(task.value) } all aggregatedProjects })


def rebaseFile(file: File, oldBase: File, newBase: File) =
  IO.relativize(oldBase, file) map { newBase / _ }


val copyCompiledFiles = taskKey[Unit]("Copies the compiled files from one project to another.")

def copyCompiledFilesFrom(project: Project) = {
  def copyCompiledFilesFrom(project: Project, config: Configuration) = Seq(
    config / copyCompiledFiles := IO.copyDirectory(
      (project / config / classDirectory).value,
      (config / classDirectory).value,
      overwrite = false, preserveLastModified = true, preserveExecutable = true),
    config / copyCompiledFiles :=
      ((config / copyCompiledFiles) dependsOn (project / config / compile)).value,
    config / compile :=
      ((config / compile) dependsOn (config / copyCompiledFiles)).value)

  copyCompiledFilesFrom(project, Compile) ++ copyCompiledFilesFrom(project, Test)
}


val macroparadise = Seq(
  scalacOptions ++= {
    if (`is 2.13+`(scalaVersion.value) && !`is 3+`(scalaVersion.value))
      Seq("-Ymacro-annotations")
    else
      Seq.empty
  },
  libraryDependencies ++= {
    if (`is 2.13+`(scalaVersion.value))
      Seq.empty
    else
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
  })

val macrodeclaration = libraryDependencies ++= {
  if (`is 3+`(scalaVersion.value))
    Seq.empty
  else
    Seq(scalaOrganization.value % "scala-reflect" % scalaVersion.value % "compile-internal;test-internal")
}

val jsweakreferences = libraryDependencies += 
  "org.scala-js" %%% "scalajs-weakreferences" % "1.0.0" cross CrossVersion.for3Use2_13

val jsmacrotaskexecutor = libraryDependencies +=
  "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0"

val jsjavasecurerandom = libraryDependencies +=
  "org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0" cross CrossVersion.for3Use2_13

val scalatest = libraryDependencies +=
  "org.scalatest" %%% "scalatest" % "3.2.12" % "test-internal"

val scribe = libraryDependencies +=
  "com.outr" %%% "scribe" % "3.8.2"

val retypecheck = libraryDependencies ++= {
  if (`is 3+`(scalaVersion.value))
    Seq.empty
  else
    Seq("io.github.scala-loci" %% "retypecheck" % "0.10.0")
}

val rescala = libraryDependencies +=
  "de.tu-darmstadt.stg" %%% "rescala" % "0.31.0"

val upickle = libraryDependencies +=
  "com.lihaoyi" %%% "upickle" % "2.0.0"

val circe = Seq(
  libraryDependencies ++= {
    if (`is 2.12+`(scalaVersion.value))
      Seq(
        "io.circe" %%% "circe-core" % "0.14.1",
        "io.circe" %%% "circe-parser" % "0.14.1")
    else
      Seq.empty
  },
  compile / skip := (compile / skip).value || !`is 2.12+`(scalaVersion.value),
  publish / skip := (publish / skip).value || !`is 2.12+`(scalaVersion.value))

val jsoniter = Seq(
  libraryDependencies ++= {
    if (`is 2.12+`(scalaVersion.value))
      Seq("com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.13.22")
    else
      Seq.empty
  },
  compile / skip := (compile / skip).value || !`is 2.12+`(scalaVersion.value),
  publish / skip := (publish / skip).value || !`is 2.12+`(scalaVersion.value))

val akkaHttp = libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "[10.0,11.0)" % Provided cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-stream" % "[2.4,3.0)" % Provided cross CrossVersion.for3Use2_13)

val play = libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "[10.0,11.0)" % Provided cross CrossVersion.for3Use2_13,
  "com.typesafe.play" %% "play" % "[2.5,2.8)" % Provided cross CrossVersion.for3Use2_13)

val scalajsDom = libraryDependencies +=
  "org.scala-js" % "scalajs-dom" % "2.2.0" cross ScalaJSCrossVersion.binary

val javalin = libraryDependencies +=
  "io.javalin" % "javalin" % "4.6.0"

val jetty = libraryDependencies ++= Seq(
  "org.eclipse.jetty.websocket" % "websocket-server" % "9.4.46.v20220331",
  "org.eclipse.jetty.websocket" % "websocket-client" % "9.4.46.v20220331",
  "org.eclipse.jetty.websocket" % "websocket-api" % "9.4.46.v20220331",
  "org.slf4j" % "slf4j-nop" % "1.7.36" % "test-internal")


lazy val loci = (project
  in file(".")
  settings (publish / skip := true,
            Global / onLoad := {
              val project = System.getenv("SCALA_PLATFORM") match {
                case "jvm" => Some("lociJVM")
                case "js" => Some("lociJS")
                case _ => None
              }
              val transformation = { state: State =>
                project map { project => s"project $project" :: state } getOrElse state
              }
              transformation compose (Global / onLoad).value
            })
  aggregate (lociJVM, lociJS))

lazy val lociJVM = (project
  in file(".jvm")
  settings (publish / skip := true,
            build := taskSequence(Compile / compile, Test /compile).value)
  aggregate (lociLanguageJVM, lociLanguageRuntimeJVM, lociCommunicationJVM,
             lociSerializerUpickleJVM,
             lociSerializerCirceJVM,
             lociSerializerJsoniterScalaJVM,
             lociTransmitterRescalaJVM, lociLanguageTransmitterRescalaJVM,
             lociCommunicatorTcpJVM,
             lociCommunicatorWsWebNativeJVM,
             lociCommunicatorWsAkkaJVM,
             lociCommunicatorWsAkkaPlayJVM,
             lociCommunicatorWsJavalinJVM,
             lociCommunicatorWsJettyJVM,
             lociCommunicatorWebRtcJVM))

lazy val lociJS = (project
  in file(".js")
  settings (publish / skip := true,
            build := taskSequence(Compile / compile, Test /compile,
                                  Compile / fastLinkJS, Test /fastLinkJS).value)
  aggregate (lociLanguageJS, lociLanguageRuntimeJS, lociCommunicationJS,
             lociSerializerUpickleJS,
             lociSerializerCirceJS,
             lociSerializerJsoniterScalaJS,
             lociTransmitterRescalaJS, lociLanguageTransmitterRescalaJS,
             lociCommunicatorTcpJS,
             lociCommunicatorWsWebNativeJS,
             lociCommunicatorWsAkkaJS,
             lociCommunicatorWsAkkaPlayJS,
             lociCommunicatorWsJavalinJS,
             lociCommunicatorWsJettyJS,
             lociCommunicatorWebRtcJS))


lazy val lociLanguage = scala2only(crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Full
  in file("language")
  settings (name := "ScalaLoci language",
            normalizedName := "scala-loci-language",
            retypecheck, macroparadise, macrodeclaration, scalatest)
  dependsOn lociLanguageRuntime % "compile->compile;test->test")

lazy val lociLanguageJVM = lociLanguage.jvm
lazy val lociLanguageJS = lociLanguage.js


lazy val lociLanguageRuntime = scala2only(crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("language-runtime")
  settings (name := "ScalaLoci language runtime",
            normalizedName := "scala-loci-language-runtime",
            SourceGenerator.remoteSelection,
            retypecheck, macrodeclaration, scalatest)
  jsSettings jsweakreferences
  dependsOn lociCommunication % "compile->compile;test->test")

lazy val lociLanguageRuntimeJVM = lociLanguageRuntime.jvm
lazy val lociLanguageRuntimeJS = lociLanguageRuntime.js


lazy val lociCommunicationPrelude = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Full
  in file("communication") / ".prelude"
  settings (normalizedName := "scala-loci-communication-prelude",
            publish / skip := true,
            Compile / unmanagedSourceDirectories := (Compile / unmanagedSourceDirectories).value flatMap {
              rebaseFile(_,
                (ThisBuild / baseDirectory).value / "communication" / ".prelude",
                (ThisBuild / baseDirectory).value / "communication")
            },
            Test / unmanagedSourceDirectories := (Test / unmanagedSourceDirectories).value flatMap {
              rebaseFile(_,
                (ThisBuild / baseDirectory).value / "communication" / ".prelude",
                (ThisBuild / baseDirectory).value / "communication")
            },
            Compile / unmanagedSources / includeFilter :=
              "ReflectionExtensions.scala" || "CompileTimeUtils.scala" || "SelectorResolution.scala",
            macrodeclaration, scalatest))

lazy val lociCommunicationPreludeJVM = lociCommunicationPrelude.jvm
lazy val lociCommunicationPreludeJS = lociCommunicationPrelude.js


lazy val lociCommunication = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Full
  in file("communication")
  settings (name := "ScalaLoci communication",
            normalizedName := "scala-loci-communication",
            Compile / unmanagedSources / excludeFilter :=
              "ReflectionExtensions.scala" || "CompileTimeUtils.scala" || "SelectorResolution.scala",
            SourceGenerator.transmittableTuples,
            SourceGenerator.functionsBindingBuilder,
            SourceGenerator.functionSubjectiveBinding,
            macroparadise, macrodeclaration, scribe, scalatest)
  jvmSettings copyCompiledFilesFrom(lociCommunicationPreludeJVM)
  jsSettings (copyCompiledFilesFrom(lociCommunicationPreludeJS),
              jsmacrotaskexecutor, jsjavasecurerandom))

lazy val lociCommunicationJVM = lociCommunication.jvm
lazy val lociCommunicationJS = lociCommunication.js


lazy val lociSerializerUpickle = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("serializer-upickle")
  settings (name := "ScalaLoci µPickle serializer",
            normalizedName := "scala-loci-serializer-upickle",
            upickle)
  dependsOn lociCommunication)

lazy val lociSerializerUpickleJVM = lociSerializerUpickle.jvm
lazy val lociSerializerUpickleJS = lociSerializerUpickle.js


lazy val lociSerializerCirce = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("serializer-circe")
  settings (name := "ScalaLoci Circe serializer",
            normalizedName := "scala-loci-serializer-circe",
            circe)
  dependsOn lociCommunication)

lazy val lociSerializerCirceJVM = lociSerializerCirce.jvm
lazy val lociSerializerCirceJS = lociSerializerCirce.js


lazy val lociSerializerJsoniterScala = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("serializer-jsoniter-scala")
  settings (name := "ScalaLoci Jsoniter Scala serializer",
            normalizedName := "scala-loci-serializer-jsoniter-scala",
            jsoniter)
  dependsOn lociCommunication)

lazy val lociSerializerJsoniterScalaJVM = lociSerializerJsoniterScala.jvm
lazy val lociSerializerJsoniterScalaJS = lociSerializerJsoniterScala.js


lazy val lociTransmitterRescala = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("transmitter-rescala")
  settings (name := "ScalaLoci REScala transmitter",
            normalizedName := "scala-loci-transmitter-rescala",
            rescala, scalatest)
  dependsOn lociCommunication % "compile->compile;test->test")

lazy val lociTransmitterRescalaJVM = lociTransmitterRescala.jvm
lazy val lociTransmitterRescalaJS = lociTransmitterRescala.js


lazy val lociLanguageTransmitterRescala = scala2only(crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Pure
  in file("language-transmitter-rescala")
  settings (name := "ScalaLoci language REScala transmitter",
            normalizedName := "scala-loci-language-transmitter-rescala",
            macroparadise, macrodeclaration, scalatest)
  dependsOn (lociLanguage % "test-internal",
             lociLanguageRuntime % "compile->compile;test->test",
             lociTransmitterRescala % "compile->compile;test->test"))

lazy val lociLanguageTransmitterRescalaJVM = lociLanguageTransmitterRescala.jvm
lazy val lociLanguageTransmitterRescalaJS = lociLanguageTransmitterRescala.js


lazy val lociCommunicatorTcp = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-tcp")
  settings (name := "ScalaLoci TCP communicator",
            normalizedName := "scala-loci-communicator-tcp",
            scalatest)
  dependsOn lociCommunication % "compile->compile;test->test")

lazy val lociCommunicatorTcpJVM = lociCommunicatorTcp.jvm
lazy val lociCommunicatorTcpJS = lociCommunicatorTcp.js


lazy val lociCommunicatorWsWebNative = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-ws-webnative")
  settings (name := "ScalaLoci Web-native WebSocket communicator",
            normalizedName := "scala-loci-communicator-ws-webnative",
            akkaHttp, scalajsDom, scalatest)
  dependsOn lociCommunication)

lazy val lociCommunicatorWsWebNativeJVM = lociCommunicatorWsWebNative.jvm
lazy val lociCommunicatorWsWebNativeJS = lociCommunicatorWsWebNative.js


lazy val lociCommunicatorWsAkka = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-ws-akka")
  settings (name := "ScalaLoci Akka WebSocket communicator",
            normalizedName := "scala-loci-communicator-ws-akka",
            akkaHttp, scalajsDom, scalatest)
  dependsOn lociCommunication % "compile->compile;test->test")

lazy val lociCommunicatorWsAkkaJVM = lociCommunicatorWsAkka.jvm
lazy val lociCommunicatorWsAkkaJS = lociCommunicatorWsAkka.js


lazy val lociCommunicatorWsAkkaPlay = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-ws-akka-play")
  settings (name := "ScalaLoci Play Framework Akka WebSocket communicator",
            normalizedName := "scala-loci-communicator-ws-akka-play",
            play)
  dependsOn lociCommunicatorWsAkka)

lazy val lociCommunicatorWsAkkaPlayJVM = lociCommunicatorWsAkkaPlay.jvm
lazy val lociCommunicatorWsAkkaPlayJS = lociCommunicatorWsAkkaPlay.js


lazy val lociCommunicatorWsJetty = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-ws-jetty")
  settings (name := "ScalaLoci Jetty WebSocket communicator",
            normalizedName := "scala-loci-communicator-ws-jetty",
            jetty, scalatest)
  dependsOn lociCommunication % "compile->compile;test->test")

lazy val lociCommunicatorWsJettyJVM = lociCommunicatorWsJetty.jvm
lazy val lociCommunicatorWsJettyJS = lociCommunicatorWsJetty.js


lazy val lociCommunicatorWsJavalin = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Dummy
  in file("communicator-ws-javalin")
  settings (name := "ScalaLoci Javalin WebSocket communicator",
            normalizedName := "scala-loci-communicator-ws-javalin",
            scalajsDom)
  jvmSettings javalin
  dependsOn lociCommunication)

lazy val lociCommunicatorWsJavalinJVM = lociCommunicatorWsJavalin.jvm
lazy val lociCommunicatorWsJavalinJS = lociCommunicatorWsJavalin.js


lazy val lociCommunicatorWebRtc = (crossProject(JSPlatform, JVMPlatform)
  crossType CrossType.Full
  in file("communicator-webrtc")
  settings (name := "ScalaLoci WebRTC communicator",
            normalizedName := "scala-loci-communicator-webrtc",
            scalajsDom)
  dependsOn lociCommunication)

lazy val lociCommunicatorWebRtcJVM = lociCommunicatorWebRtc.jvm
lazy val lociCommunicatorWebRtcJS = lociCommunicatorWebRtc.js
