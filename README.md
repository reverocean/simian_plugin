simian_plugin
=============
This is a simian gradle plugin
ant.exec(dir: "${project.rootDir}", executable: "${project.rootDir}/gradlew.bat", spawn: true) {
    arg line: "-i"
    arg line: "--gradle-user-home"
    arg line: "${bootstrapDir}/privatecache"
    arg line: "listenForStartServerSignal"
    arg line: ">"
    arg line: "${bootstrapDir}/bootstrap_listener.log"
  }
