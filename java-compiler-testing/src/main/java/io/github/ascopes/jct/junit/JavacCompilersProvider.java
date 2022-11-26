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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.javac.JavacJctCompilerImpl;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Argument provider for the {@link JavacCompilerTest} annotation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class JavacCompilersProvider extends AbstractCompilersProvider<JavacCompilerTest> {

  JavacCompilersProvider() {
    // Do nothing, but keep this package private.
  }

  @Override
  protected JctCompiler<?, ?> compilerForVersion(int release) {
    return new JavacJctCompilerImpl("javac release " + release).release(release);
  }

  @Override
  protected int minSupportedVersion(boolean modules) {
    return JavacJctCompilerImpl.getEarliestSupportedVersionInt(modules);
  }

  @Override
  protected int maxSupportedVersion(boolean modules) {
    return JavacJctCompilerImpl.getLatestSupportedVersionInt(modules);
  }

  @Override
  public void accept(JavacCompilerTest javacCompilers) {
    // Super is needed here to prevent IntelliJ getting confused.
    super.configure(
        javacCompilers.minVersion(),
        javacCompilers.maxVersion(),
        javacCompilers.modules(),
        javacCompilers.configurers()
    );
  }
}
