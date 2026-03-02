package org.contract_lib.contract_chameleon.contexts;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.contract_lib.contract_chameleon.SharedContextManager;

public class ResultDirectoryContext implements SharedContextManager.UserProvidedContext {

  /// Default constructor when no custom path is set for the result directory.
  public ResultDirectoryContext() {
  }

  /// Set the path of the result directory to a custom path. 
  public ResultDirectoryContext(Path resultDirectory) {
    this.resultDirectory = resultDirectory;
  }

  private Path resultDirectory;

  /** Access a path to a directory where the results should be stored.
   * 
   * @param adapterName the default name under which the results should be stored.
   * @return the path under which all results should be stored.
   */
  public Path getResultDirectory(String defaultName) {
    if (resultDirectory == null) {
      return Paths.get(".", defaultName);
    }
    return this.resultDirectory;
  }
}
