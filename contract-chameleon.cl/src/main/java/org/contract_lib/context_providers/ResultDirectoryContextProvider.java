
package org.contract_lib.context_providers;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import picocli.CommandLine.Option;

public class ResultDirectoryContextProvider
    implements SharedContextManager.SharedContextProvider<ResultDirectoryContext> {

  public ResultDirectoryContextProvider() {
  }

  @Option(names = { "-o", "--out", "--output", "--result" }, description = {
      "Set a custom filepath for the result directory." })
  String filename;

  @Override
  public Class<ResultDirectoryContext> getContext() {
    return ResultDirectoryContext.class;
  }

  @Override
  public ResultDirectoryContext createContext(ChameleonMessageManager messageManager) {
    if (filename == null) {
      return new ResultDirectoryContext();
    }

    Path rootPath = Paths.get(filename);

    return new ResultDirectoryContext(rootPath);
  }
}
