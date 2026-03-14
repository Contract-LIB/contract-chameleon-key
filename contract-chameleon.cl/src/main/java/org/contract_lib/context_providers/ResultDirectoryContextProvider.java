
package org.contract_lib.context_providers;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;

import picocli.CommandLine.Option;

public class ResultDirectoryContextProvider
    implements SharedContextManager.SharedContextProvider<ResultDirectoryContext> {

  public ResultDirectoryContextProvider() {
  }

  @Option(names = { "-o", "--out", "--output", "--result" }, description = {
      "Set a custom path for the result directory." })
  String directoryPath;

  @Override
  public Class<ResultDirectoryContext> getContext() {
    return ResultDirectoryContext.class;
  }

  @Override
  public ResultDirectoryContext createContext(SharedContextManager sharedContextManager) {
    if (directoryPath == null) {
      return new ResultDirectoryContext();
    }

    Path rootPath = Paths.get(directoryPath);

    return new ResultDirectoryContext(rootPath);
  }
}
