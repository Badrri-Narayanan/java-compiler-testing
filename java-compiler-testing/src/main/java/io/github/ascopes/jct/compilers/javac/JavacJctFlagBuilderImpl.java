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
package io.github.ascopes.jct.compilers.javac;

import io.github.ascopes.jct.compilers.AbstractJctFlagBuilder;
import java.util.List;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper to build flags for a standard Javac implementation for the OpenJDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JavacJctFlagBuilderImpl extends AbstractJctFlagBuilder {

  private static final String VERBOSE = "-verbose";
  private static final String ENABLE_PREVIEW = "--enable-preview";
  private static final String NOWARN = "-nowarn";
  private static final String WERROR = "-Werror";
  private static final String DEPRECATION = "-deprecation";
  private static final String RELEASE = "--release";
  private static final String SOURCE = "-source";
  private static final String TARGET = "-target";
  private static final String ANNOTATION_OPT = "-A";
  private static final String RUNTIME_OPT = "-J";

  /**
   * Initialize this flag builder.
   */
  public JavacJctFlagBuilderImpl() {
  }

  @Override
  public JavacJctFlagBuilderImpl verbose(boolean enabled) {
    addFlagIfTrue(enabled, VERBOSE);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl previewFeatures(boolean enabled) {
    addFlagIfTrue(enabled, ENABLE_PREVIEW);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl showWarnings(boolean enabled) {
    addFlagIfTrue(!enabled, NOWARN);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl failOnWarnings(boolean enabled) {
    addFlagIfTrue(enabled, WERROR);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl showDeprecationWarnings(boolean enabled) {
    addFlagIfTrue(enabled, DEPRECATION);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl release(@Nullable String version) {
    addVersionIfPresent(RELEASE, version);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl source(@Nullable String version) {
    addVersionIfPresent(SOURCE, version);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl target(@Nullable String version) {
    addVersionIfPresent(TARGET, version);
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl annotationProcessorOptions(List<String> options) {
    options.forEach(option -> addFlag(ANNOTATION_OPT + option));
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl runtimeOptions(List<String> options) {
    options.forEach(option -> addFlag(RUNTIME_OPT + option));
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl compilerOptions(List<String> options) {
    options.forEach(this::addFlag);
    return this;
  }
}
