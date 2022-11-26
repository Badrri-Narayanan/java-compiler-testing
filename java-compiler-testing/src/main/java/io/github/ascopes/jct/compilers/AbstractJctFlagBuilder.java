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
package io.github.ascopes.jct.compilers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Abstract base for flag builders with basic common functionality.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractJctFlagBuilder implements JctFlagBuilder {

  private final List<String> craftedFlags;

  /**
   * Initialize this flag builder.
   */
  protected AbstractJctFlagBuilder() {
    craftedFlags = new ArrayList<>();
  }

  @Override
  public List<String> build() {
    // Immutable copy.
    return List.copyOf(craftedFlags);
  }

  protected final void addFlag(String flag) {
    craftedFlags.add(flag);
  }

  protected final void addFlagIfTrue(boolean condition, String flag) {
    if (condition) {
      craftedFlags.add(flag);
    }
  }

  protected final void addVersionIfPresent(String flagPrefix, @Nullable String version) {
    if (version != null) {
      craftedFlags.add(flagPrefix);
      craftedFlags.add(version);
    }
  }
}
