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
package io.github.ascopes.jct.compilers.ecj;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManagerBuilder;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.pathwrappers.RamDirectory;
import io.github.ascopes.jct.pathwrappers.TempDirectory;
import java.nio.file.FileSystems;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Implementation of an {@code ECJ} compiler.
 *
 * <p>This is highly experimental and some features may not work correctly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class EcjJctCompilerImpl extends AbstractJctCompiler<EcjJctCompilerImpl> {

  private static final String NAME = "ecj";

  /**
   * Initialize a new Java compiler.
   */
  public EcjJctCompilerImpl() {
    this(newCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public EcjJctCompilerImpl(JavaCompiler jsr199Compiler) {
    this(NAME, jsr199Compiler);
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name the name to give the compiler.
   */
  public EcjJctCompilerImpl(String name) {
    this(name, newCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name           the name to give the compiler.
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public EcjJctCompilerImpl(String name, JavaCompiler jsr199Compiler) {
    super(name, new JctFileManagerBuilder(), jsr199Compiler, new EcjJctFlagBuilderImpl());
    // Use TempDirectory by default: ECJ exploits the assumption that we usually would invoke
    // the compiler on the default file system.
    testDirectoryFactory(TempDirectory::newTempDirectory);
  }

  @Override
  public EcjJctCompilerImpl addPath(Location location, PathWrapper pathLike) {
    ensureDefaultFileSystem(pathLike);
    return super.addPath(location, pathLike);
  }

  @Override
  public EcjJctCompilerImpl addPath(Location location, String moduleName, PathWrapper pathLike) {
    ensureDefaultFileSystem(pathLike);
    return super.addPath(location, moduleName, pathLike);
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt(false));
  }

  /**
   * Get the minimum version of Java that is supported by this compiler.
   *
   * @param modules whether modules need to be supported or not.
   * @return the minimum supported version.
   */
  public static int getEarliestSupportedVersionInt(@SuppressWarnings("unused") boolean modules) {
    // There is some weird behaviour on JDK 11 with detecting the system modules on ECJ when
    // back-compiling to JDK 9 and JDK 10. Something triggering a NullPointerException.
    // For now, I am limiting this to JDK 11 in all cases. to avoid this.
    return classFileConstantToMajorVersion(ClassFileConstants.JDK11);
  }

  /**
   * Get the maximum version of Java that is supported by this compiler.
   *
   * @param modules whether to require module support or not. This is currently ignored but exists
   *                for future compatibility purposes.
   * @return the maximum supported version.
   */
  public static int getLatestSupportedVersionInt(@SuppressWarnings("unused") boolean modules) {
    return classFileConstantToMajorVersion(ClassFileConstants.getLatestJDKLevel());
  }

  private static int classFileConstantToMajorVersion(long classFileConstant) {
    return (int) ((classFileConstant >> 16L) - ClassFileConstants.MAJOR_VERSION_0);
  }

  private static EclipseCompiler newCompiler() {
    return new EclipseCompiler();
  }

  private static void ensureDefaultFileSystem(PathWrapper wrapper) {
    if (wrapper instanceof RamDirectory) {
      throw new UnsupportedOperationException(
          "Cannot use RamDirectory objects with ECJ due to limitations within the ECJ "
              + "implementation. Consider using TempDirectory instead for this test."
      );
    }

    var defaultFs = FileSystems.getDefault();

    if (!wrapper.getPath().getFileSystem().equals(defaultFs)) {
      throw new UnsupportedOperationException(
          "Cannot use non-default FileSystems with ECJ due to limitations within the ECJ "
              + "implementation. Consider using TempDirectory instead for this test."
      );
    }
  }
}
