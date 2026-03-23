package org.contract_lib.contract_chameleon.contexts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.contract_lib.contract_chameleon.SharedContextManager;

/** A source paths context contains all path objects to source files.
 * <p>
 * Those files must only be read,
 * and never changed.
 */
public class SourcePathsContext implements
    SharedContextManager.InterfaceProvidedContext,
    SharedContextManager.MergableContext<SourcePathsContext> {

  /** Given path to a root directory, create a source object from all child objects.
   * 
   * @param rootDirectory the root directory which is walked.
   * @throws IOException exception thrown by the file system.
   */
  public SourcePathsContext(Path rootDirectory) throws IOException {
    this(walkDirectory(rootDirectory));
  }

  /** Create a new context from a list of files.
   */
  public SourcePathsContext(Collection<Path> paths) {
    this.paths = new ArrayList<>(paths);
  }

  private List<Path> paths;

  /** The source paths provided through this context.
   */
  public List<Path> getPaths() {
    return new ArrayList<>(paths);
  }

  private static List<Path> walkDirectory(Path path) throws IOException {
    return Files.walk(path)
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());
  }

  @Override
  public SourcePathsContext merge(SourcePathsContext first, SourcePathsContext second) {
    List<Path> list = new ArrayList<>();
    list.addAll(first.getPaths());
    list.addAll(second.getPaths());
    return new SourcePathsContext(list);
  }
}
