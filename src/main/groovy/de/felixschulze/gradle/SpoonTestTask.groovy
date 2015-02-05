/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Felix Schulze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.felixschulze.gradle

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
import com.squareup.spoon.DeviceResult
import com.squareup.spoon.SpoonRunner
import com.squareup.spoon.SpoonSummary
import de.felixschulze.teamcity.TeamCityImportDataType
import de.felixschulze.teamcity.TeamCityProgressType
import de.felixschulze.teamcity.TeamCityStatusMessageHelper
import de.felixschulze.teamcity.TeamCityStatusType
import org.gradle.api.DefaultTask
import org.gradle.api.Nullable
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static com.squareup.spoon.SpoonUtils.GSON as GSON

class SpoonTestTask extends DefaultTask {

    @InputFile
    File instrumentationApk

    @InputFile
    File applicationApk

    @OutputDirectory
    File output

    String title

    String sdkDir

    String testClassName

    String testMethodName

    @Nullable
    IRemoteAndroidTestRunner.TestSize testSize;

    @TaskAction
    def runInstrumentationTests() throws IOException {

        if (!applicationApk || !instrumentationApk) {
            throw new IllegalArgumentException("apk files not found.")
        }

        Boolean isDebugEnabled = logger.isDebugEnabled() || project.spoon.debug;

        SpoonRunner spoonRunner = new SpoonRunner.Builder() //
                .setTitle(title)
                .setApplicationApk(applicationApk)
                .setInstrumentationApk(instrumentationApk)
                .setOutputDirectory(output)
                .setAndroidSdk(cleanFile(sdkDir))
                .setDebug(isDebugEnabled)
                .setTestSize(testSize)
                .setClasspath(project.buildscript.configurations.classpath.asPath)
                .setFailIfNoDeviceConnected(project.spoon.failIfNoDeviceConnected)
                .setAdbTimeout(project.spoon.adbTimeout * 1000)
                .setClassName(testClassName)
                .setMethodName(testMethodName)
                .useAllAttachedDevices()
                .build();

        if (project.spoon.teamCityLog) {
            println TeamCityStatusMessageHelper.buildProgressString(TeamCityProgressType.START, "Spoon-Tests running...")
        }

        boolean succeeded = spoonRunner.run()

        if (project.spoon.teamCityLog) {
            println TeamCityStatusMessageHelper.buildProgressString(TeamCityProgressType.FINISH, succeeded ? "Spoon-Tests finished." : "Spoon-Test failed.")
        }

        if (project.spoon.teamCityLog) {
            boolean logSuccessful = logJUnitXmlToTeamCity()
            if (succeeded && !logSuccessful) {
                succeeded = false
            }
        }

        if (project.spoon.zipReport) {
            new AntBuilder().zip(
                    destfile: new File(project.getBuildDir(),"spoon.zip").absolutePath,
                    basedir: output.absolutePath
            )
        }

        if (!succeeded && project.spoon.failOnFailure) {
            //Check if apk could install on all devices
            output.eachFile {
                if (it.name.endsWith('.json')) {
                    if (it.exists()) {
                        FileReader resultFile = new FileReader(it);
                        SpoonSummary result = GSON.fromJson(resultFile, SpoonSummary.class);
                        if (result) {
                            Map<String, DeviceResult> deviceResultMap = result.results;
                            for (DeviceResult deviceResult in deviceResultMap.values()) {
                                if (deviceResult.installFailed && deviceResult.installMessage) {
                                    if (project.spoon.teamCityLog) {
                                        println TeamCityStatusMessageHelper.buildStatusString(TeamCityStatusType.ERROR, "Error: " + deviceResult.installMessage)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.exit(1)
        }
    }

    private boolean logJUnitXmlToTeamCity() {
        File jUnitDir = new File(output, "junit-reports")
        if (jUnitDir.exists()) {
            jUnitDir.eachFile {
                if (it.name.endsWith('.xml')) {
                    println TeamCityStatusMessageHelper.importDataString(TeamCityImportDataType.JUNIT, it.canonicalPath);
                }
            }
            return true
        } else {
            return false
        }
    }

    private static File cleanFile(String path) {
        if (path == null) {
            return null;
        }
        return new File(path);
    }


}
