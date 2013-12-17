package com.rever

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

class SimianPlugin implements Plugin<Project> {
    private static final DEFAULT_THRESHOLD = 7
    private static final SIMIAN = 'simian'

    @Override
    def void apply(Project project) {
        addConfiguration project
        createTasks project, addExtension(project)
        disableEmptyTasks project
    }


    private addConfiguration(Project project) {
        project.configurations.create(SIMIAN).with {
            description = 'The simian libraries to be used for this project.'
            visible = false
            transitive = true

            // Don't need these things, they're provided by the runtime
            exclude group: 'ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant-launcher'
            exclude group: 'org.codehaus.groovy', module: 'groovy'
            exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            exclude group: 'org.slf4j', module: 'slf4j-api'
            exclude group: 'org.slf4j', module: 'jcl-over-slf4j'
            exclude group: 'org.slf4j', module: 'log4j-over-slf4j'
            exclude group: 'commons-logging', module: 'commons-logging'
            exclude group: 'log4j', module: 'log4j'
        }
    }

    private SimianExtension addExtension(Project project) {
        SimianExtension extension = project.extensions.create(SIMIAN, SimianExtension)
        extension.with {
            stylesheet = project.file("${project.rootProject.projectDir}/config/simian/simian.xsl")
            threshold = DEFAULT_THRESHOLD
            ignoreFailures = false
            verbose = false
            reportFolder = project.file("${project.reporting.baseDir}/$SIMIAN")
            excludeFolders = ''
        }
        extension
    }

    private void createTasks(Project project, SimianExtension extension) {
        project.sourceSets.all { SourceSet sourceSet ->
            Simian task = project.tasks.create(sourceSet.getTaskName(SIMIAN, null), Simian)
            task.description = "Run simian checks on $sourceSet.name source files."
            project.check.dependsOn task
            task.conventionMapping.with {
                simianClasspath = {
                    def config = project.configurations[SIMIAN]
                    if (config.dependencies.empty) {
                        project.dependencies {
                            simian 'redhill:simian:2.3.33'
                        }
                    }
                    config
                }
                sourceFolders = { sourceSet.allSource.srcDirs - getSourceExcludes(project, extension.excludeFolders) }
                stylesheet = { extension.stylesheet }
                threshold = { extension.threshold }
                ignoreFailures = { extension.ignoreFailures }
                verbose = { extension.verbose }
                reportFolder = { extension.reportFolder }
                reportName = { sourceSet.name }
            }
        }
    }

    private def getSourceExcludes(Project project, String excludes) {
        excludes.split(",").collect {
            (project.projectDir.path + "/" + it.trim()).replace('\\', '/') as File
        }
    }

    private void disableEmptyTasks(Project project) {
        project.afterEvaluate {
            project.tasks.withType(Simian) {
                if (sourceFolders.find { it.exists() } == null) {
                    enabled = false
                }
            }
        }
    }
}

class SimianExtension {
    File stylesheet

    long threshold

    boolean ignoreFailures

    boolean verbose

    File reportFolder

    String excludeFolders
}

class Simian extends ConventionTask {
    @InputFiles
    FileCollection simianClasspath

    @InputFiles
    Collection<File> sourceFolders

    String stylesheet

    long threshold

    boolean ignoreFailures

    boolean verbose

    @OutputDirectory
    File reportFolder

    String reportName

    @TaskAction
    void run() {
        def xmlFile = new File(getReportFolder(), "${getReportName()}.xml")
        def htmlFile = new File(getReportFolder(), "${getReportName()}.html")
        println("aa")
        def stylesheet = new File(getStylesheet())

        if (!stylesheet.exists()) {
            stylesheet.getParentFile().mkdirs()
            stylesheet.text = getClass().getResource('/simian/simian﹣simple.xsl').text
        }
        getReportFolder().mkdirs()

        def failureProperty = 'simian.failure'
        services.get(IsolatedAntBuilder).withClasspath(getSimianClasspath()).execute {
            ant.taskdef name: 'simian', classname: 'com.harukizaemon.simian.SimianTask'

            ant.simian(threshold: getThreshold(), failOnDuplication: !getIgnoreFailures(), failureProperty: failureProperty) {
                getSourceFolders().each { folder ->
                    if (folder.exists()) {
                        ant.fileset dir: folder.path
                    }
                }
                formatter type: 'xml', toFile: xmlFile.path
                if (verbose) {
                    formatter type: 'plain'
                }
            }
            ant.xslt style: getStylesheet().path, in: xmlFile.path, out: htmlFile.path
            ant.fail if: failureProperty, message: "Simian check failed, check report at ${htmlFile}."
        }
    }
}
