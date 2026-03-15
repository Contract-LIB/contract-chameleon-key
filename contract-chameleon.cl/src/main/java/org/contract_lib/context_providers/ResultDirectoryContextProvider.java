
package org.contract_lib.context_providers;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;

import picocli.CommandLine.Option;

public class ResultDirectoryContextProvider
    implements SharedContextManager.SharedContextProvider<ResultDirectoryContext> {

  public ResultDirectoryContextProvider() {
  }

  @Option(names = { "-o", "--out", "--output", "--result" }, description = {
      "Set a custom path for the result directory%n(default: \"./${COMMAND-NAME}\")." })
  private String directoryPath;

  @Option(names = { "-m",
      "--mode" }, description = "Set the override mode for the written files.%nValid values: ${COMPLETION-CANDIDATES}%n(default: ${DEFAULT-VALUE}).", defaultValue = "NO_OVERRIDE")
  private ResultDirectoryContext.OverrideMode overrideMode;

  @Override
  public Class<ResultDirectoryContext> getContext() {
    return ResultDirectoryContext.class;
  }

  @Override
  public ResultDirectoryContext createContext(SharedContextManager sharedContextManager) {
    return new ResultDirectoryContext(sharedContextManager, directoryPath, overrideMode);
  }
}
