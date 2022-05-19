/*
 * Copyright (C) 2022 Ashley Scopes
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

package io.github.ascopes.jct.paths.v2.groups;

import io.github.ascopes.jct.paths.ModuleLocation;
import io.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A container group implementation that holds zero or more modules.
 *
 * <p>These modules can be accessed by their module name. Each one holds a separate group of
 * containers, and has the ability to produce a custom class loader.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface ModuleOrientedContainerGroup extends ContainerGroup {

  /**
   * Add a path to this group for a module.
   *
   * @param module the name of the module that this is for.
   * @param path   the path to add.
   * @throws IOException if an IO exception occurs.
   */
  void addPath(String module, Path path) throws IOException;

  /**
   * Add a RAM path to this group for a module.
   *
   * <p>This is the same as {@link #addPath(String, Path)}, but ensures that the RAM path is kept
   * allocated for at least as long as this group is.

   * @param module  the name of the module that this is for.
   * @param ramPath the RAM path to add.
   * @throws IOException if an IO exception occurs.
   */
  void addPath(String module, RamPath ramPath) throws IOException;

  /**
   * Get the {@link PackageOrientedContainerGroup} for a given module name, creating it if it does
   * not yet exist.
   *
   * @param moduleName the module name to look up.
   * @return the container group.
   */
  PackageOrientedContainerGroup forModule(String moduleName);

  /**
   * Get the module-oriented location.
   *
   * @return the module-oriented location.
   */
  @Override
  Location getLocation();

  /**
   * Get all locations that are modules.
   *
   * @return the locations that are modules.
   */
  List<Set<Location>> getLocationsForModules();

  /**
   * Determine if this group contains a given module.
   *
   * @param location the module location to look for.
   * @return {@code true} if present, or {@code false} if not.
   */
  boolean hasLocation(ModuleLocation location);
}