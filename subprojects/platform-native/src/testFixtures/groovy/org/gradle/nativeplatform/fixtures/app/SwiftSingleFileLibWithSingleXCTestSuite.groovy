/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.nativeplatform.fixtures.app

class SwiftSingleFileLibWithSingleXCTestSuite extends MainWithXCTestSourceElement {
    final SwiftSingleFileLib main = new SwiftSingleFileLib()
    final XCTestSourceElement test = new XCTestSourceElement() {
        @Override
        List<XCTestSourceFileElement> getTestSuites() {
            return [new XCTestSourceFileElement() {
                final delegate = new SwiftLibTest(main.greeter, main.sum, main.multiply)

                @Override
                String getTestSuiteName() {
                    return "CombinedTests"
                }

                @Override
                List<XCTestCaseElement> getTestCases() {
                    return delegate.sumTest.testCases + delegate.greeterTest.testCases + delegate.multiplyTest.testCases
                }

                @Override
                String getModuleName() {
                    return delegate.sumTest.moduleName
                }

                @Override
                XCTestSourceFileElement withImport(String moduleName) {
                    return this.withTestableImport(moduleName)
                }
            }]
        }
    }
}
