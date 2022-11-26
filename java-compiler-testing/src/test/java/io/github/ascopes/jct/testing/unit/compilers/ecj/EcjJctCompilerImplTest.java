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
package io.github.ascopes.jct.testing.unit.compilers.ecj;

import static io.github.ascopes.jct.compilers.ecj.EcjJctCompilerImpl.getEarliestSupportedVersionInt;
import static io.github.ascopes.jct.compilers.ecj.EcjJctCompilerImpl.getLatestSupportedVersionInt;
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory;
import static javax.tools.StandardLocation.SOURCE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.ecj.EcjJctCompilerImpl;
import io.github.ascopes.jct.compilers.ecj.EcjJctFlagBuilderImpl;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Random;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link EcjJctCompilerImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EcjJctCompilerImpl tests")
class EcjJctCompilerImplTest {

  EclipseCompiler eclipseCompiler;
  EcjJctCompilerImpl compiler;

  @BeforeEach
  void setUp() {
    eclipseCompiler = mock(EclipseCompiler.class);
    compiler = new EcjJctCompilerImpl(eclipseCompiler);
  }

  @DisplayName("cannot add RamDirectory paths to the compiler")
  @Test
  void cannotAddRamDirectoryPathsToTheCompiler() {
    // Then
    assertThatThrownBy(() -> compiler.addPath(SOURCE_PATH, newRamDirectory("test")))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Cannot use RamDirectory objects with ECJ due to limitations within the ECJ "
            + "implementation. Consider using TempDirectory instead for this test.");
  }

  @DisplayName("cannot add RamDirectory module paths to the compiler")
  @Test
  void cannotAddRamDirectoryModulePathsToTheCompiler() {
    // Then
    assertThatThrownBy(() -> compiler.addPath(SOURCE_PATH, "foo.bar", newRamDirectory("test")))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Cannot use RamDirectory objects with ECJ due to limitations within the ECJ "
            + "implementation. Consider using TempDirectory instead for this test.");
  }

  @DisplayName("cannot add paths that do not use the default file system to the compiler")
  @Test
  void cannotAddNonDefaultFileSystemPathsToTheCompiler(@TempDir Path someTempDir) {
    // Given
    var path = mock(Path.class);
    var fs = mock(FileSystem.class);
    when(path.getFileSystem()).thenReturn(fs);
    when(path.toUri()).thenReturn(someTempDir.toUri());

    // Then
    assertThatThrownBy(() -> compiler.addPath(SOURCE_PATH, path))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage(
            "Cannot use non-default FileSystems with ECJ due to limitations within the ECJ "
                + "implementation. Consider using TempDirectory instead for this test.");
  }

  @DisplayName("cannot add module paths that do not use the default file system to the compiler")
  @Test
  void cannotAddNonDefaultFileSystemModulePathsToTheCompiler(@TempDir Path someTempDir) {
    // Given
    var path = mock(Path.class);
    var fs = mock(FileSystem.class);
    when(path.getFileSystem()).thenReturn(fs);
    when(path.toUri()).thenReturn(someTempDir.toUri());

    // Then
    assertThatThrownBy(() -> compiler.addPath(SOURCE_PATH, "foo.bar", path))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage(
            "Cannot use non-default FileSystems with ECJ due to limitations within the ECJ "
                + "implementation. Consider using TempDirectory instead for this test.");
  }

  @DisplayName("compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    // Then
    assertThat(compiler.getName()).isEqualTo("ecj");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Then
    assertThat(compiler.getJsr199Compiler()).isSameAs(eclipseCompiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    // Then
    assertThat(compiler.getFlagBuilder()).isInstanceOf(EcjJctFlagBuilderImpl.class);
  }

  @DisplayName("compilers have the expected default release string")
  @Test
  void compilersHaveTheExpectedDefaultRelease() {
    // Given
    try (var compilerClassMock = mockStatic(EcjJctCompilerImpl.class)) {
      var latestSupportedInt = 11 + new Random().nextInt(10);

      compilerClassMock
          .when(() -> getLatestSupportedVersionInt(anyBoolean()))
          .thenReturn(latestSupportedInt);

      // When
      var defaultRelease = compiler.getDefaultRelease();

      // Then
      compilerClassMock
          .verify(() -> getLatestSupportedVersionInt(false));

      assertThat(defaultRelease)
          .isEqualTo("%d", latestSupportedInt);
    }
  }

  @DisplayName("the earliest supported version int has the expected value")
  @CsvSource({
      "true, 9",
      "false, 8",
  })
  @ParameterizedTest(name = "expect {1} when modules = {0}")
  void theEarliestSupportedVersionIntHasTheExpectedValue(boolean modules, int expect) {
    // Then
    assertThat(getEarliestSupportedVersionInt(modules))
        .isEqualTo(expect);
  }

  @DisplayName("the latest supported version int has the expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for modules = {0}")
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void theLatestSupportedVersionIntHasTheExpectedValue(boolean modules) {
    // Given
    try (var sourceVersionMock = mockStatic(ClassFileConstants.class)) {
      var latestVersion = 8 + new Random().nextInt(14);
      var classFileVersion = ((long) ClassFileConstants.MAJOR_VERSION_0 + latestVersion) << 16L;

      sourceVersionMock.when(ClassFileConstants::getLatestJDKLevel)
          .thenReturn(classFileVersion);

      // Then
      assertThat(getLatestSupportedVersionInt(modules))
          .isEqualTo(latestVersion);
    }
  }
}
