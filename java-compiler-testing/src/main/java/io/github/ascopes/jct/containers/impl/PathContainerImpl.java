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
package io.github.ascopes.jct.containers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.annotations.WillNotClose;
import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container that wraps a known directory of files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public class PathContainerImpl implements Container {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathContainerImpl.class);

  private final Location location;
  private final @WillNotClose PathLike root;
  private final String name;

  /**
   * Initialize this container.
   *
   * @param location the location.
   * @param root     the root directory to hold.
   */
  public PathContainerImpl(Location location, @WillNotClose PathLike root) {
    this.location = requireNonNull(location, "location");
    this.root = requireNonNull(root, "root");
    name = root.toString();
  }

  @Override
  public void close() throws IOException {
    // Do nothing for this implementation. We have nothing to close.
    // This also has the side effect of not closing RamPaths early, as we treat those as
    // DirectoryContainer types.
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var path = fileObject.getFullPath();
    return path.startsWith(root.getPath()) && Files.isRegularFile(path);
  }

  @Override
  @Nullable
  public Path findFile(String path) {
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Absolute paths are not supported (got '" + path + "')");
    }

    var realPath = FileUtils.relativeResourceNameToPath(root.getPath(), path);

    return Files.isRegularFile(realPath)
        ? realPath
        : null;
  }

  @Override
  @Nullable
  public byte[] getClassBinary(String binaryName) throws IOException {
    var path = FileUtils.binaryNameToPath(root.getPath(), binaryName, Kind.CLASS);
    return Files.isRegularFile(path)
        ? Files.readAllBytes(path)
        : null;
  }

  @Override
  @Nullable
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    var path = FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName);

    return Files.isRegularFile(path)
        ? new PathFileObject(location, root.getPath(), path)
        : null;
  }

  @Override
  @Nullable
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    var path = FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName);
    return new PathFileObject(location, root.getPath(), path);
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForInput(String binaryName, Kind kind) {
    var path = FileUtils.binaryNameToPath(root.getPath(), binaryName, kind);
    return Files.isRegularFile(path)
        ? new PathFileObject(location, root.getPath(), path)
        : null;
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    var path = FileUtils.binaryNameToPath(root.getPath(), className, kind);
    return new PathFileObject(location, root.getPath(), path);
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public ModuleFinder getModuleFinder() {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PathLike getPath() {
    return root;
  }

  @Override
  @Nullable
  public URL getResource(String resourcePath) throws IOException {
    var path = FileUtils.relativeResourceNameToPath(root.getPath(), resourcePath);
    // Getting a URL of a directory within a JAR breaks the JAR file system implementation
    // completely.
    return Files.isRegularFile(path)
        ? path.toUri().toURL()
        : null;
  }

  @Override
  @Nullable
  public String inferBinaryName(PathFileObject javaFileObject) {
    return javaFileObject.getFullPath().startsWith(root.getPath())
        ? FileUtils.pathToBinaryName(javaFileObject.getRelativePath())
        : null;
  }

  @Override
  public void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException {
    var maxDepth = recurse ? Integer.MAX_VALUE : 1;
    var basePath = FileUtils.packageNameToPath(root.getPath(), packageName);

    try (var walker = Files.walk(basePath, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObject(location, root.getPath(), path))
          .forEach(collection::add);
    } catch (NoSuchFileException ex) {
      LOGGER.trace("Directory {} does not exist so is being ignored", root.getPath());
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", root.getUri())
        .attribute("location", location)
        .toString();
  }
}