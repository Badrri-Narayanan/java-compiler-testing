/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.acceptancetests.autofactory

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

import javax.tools.StandardLocation
import java.time.Instant

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory
import static org.assertj.core.api.SoftAssertions.assertSoftly

@DisplayName("AutoFactory integration tests")
@SuppressWarnings('GrUnresolvedAccess')
class AutoFactoryTest {

  @DisplayName("The AutoFactory class is created as expected")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest
  void autoFactoryClassIsCreatedAsExpected(JctCompiler compiler) {
    // Given
    def sources = newRamDirectory("sources")
        .createDirectory("org/example")
        .copyContentsFrom("src/test/resources/code")

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutput()
        .packages()
        .fileExists("org/example/UserFactory.class")
        .isNotEmptyFile()

    def userFactory = compilation
        .fileManager
        .getClassLoader(StandardLocation.CLASS_OUTPUT)
        .loadClass("org.example.UserFactory")
        .getConstructor()
        .newInstance()

    def now = Instant.now()

    def user = userFactory.create("12345", "Roy Rodgers McFreely", now)

    assertSoftly { softly ->
      softly.assertThatObject(user)
          .hasFieldOrPropertyWithValue("id", "12345")
      softly.assertThatObject(user)
          .hasFieldOrPropertyWithValue("name", "Roy Rodgers McFreely")
      softly.assertThatObject(user)
          .hasFieldOrPropertyWithValue("createdAt", now)
    }
  }
}
