/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.fixtures.publish.maven

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.test.fixtures.maven.MavenFileModule

import static org.gradle.integtests.fixtures.RepoScriptBlockUtil.mavenCentralRepositoryDefinition

abstract class AbstractMavenPublishIntegTest extends AbstractIntegrationSpec {

    protected def resolveArtifact(MavenFileModule module, def extension, def classifier) {
        doResolveArtifacts("""
    dependencies {
        resolve group: '${sq(module.groupId)}', name: '${sq(module.artifactId)}', version: '${sq(module.version)}', classifier: '${sq(classifier)}', ext: '${sq(extension)}'
    }
""")
    }

    protected def resolveArtifacts(MavenFileModule module) {
        doResolveArtifacts("""
    dependencies {
        resolve group: '${sq(module.groupId)}', name: '${sq(module.artifactId)}', version: '${sq(module.version)}'
    }
""")
    }

    protected def resolveArtifacts(MavenFileModule module, Map... additionalArtifacts) {
        def dependencies = """
    dependencies {
        resolve group: '${sq(module.groupId)}', name: '${sq(module.artifactId)}', version: '${sq(module.version)}'
        resolve(group: '${sq(module.groupId)}', name: '${sq(module.artifactId)}', version: '${sq(module.version)}') {
"""
        additionalArtifacts.each {
            // Docs say type defaults to 'jar', but seems it must be set explicitly
            def type = it.type == null ? 'jar' : it.type
            dependencies += """
            artifact {
                name = '${sq(module.artifactId)}'
                classifier = '${it.classifier}'
                type = '${type}'
            }
"""
        }
        dependencies += """
        }
    }
"""
        doResolveArtifacts(dependencies)
    }

    protected def doResolveArtifacts(def dependencies) {
        // Replace the existing buildfile with one for resolving the published module
        settingsFile.text = "rootProject.name = 'resolve'"
        buildFile.text = """
            configurations {
                resolve {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME))
                    }
                }
            }
            repositories {
                maven { url "${mavenRepo.uri}" }
                ${mavenCentralRepositoryDefinition()}
            }
            $dependencies
            task resolveArtifacts(type: Sync) {
                from configurations.resolve
                into "artifacts"
            }

"""

        run "resolveArtifacts"
        def artifactsList = file("artifacts").exists() ? file("artifacts").list() : []
        return artifactsList.sort()
    }


    String sq(String input) {
        return escapeForSingleQuoting(input)
    }

    String escapeForSingleQuoting(String input) {
        return input.replace('\\', '\\\\').replace('\'', '\\\'')
    }
}
