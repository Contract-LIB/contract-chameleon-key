
package org.contract_lib.context_providers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;
import org.contract_lib.contract_chameleon.error.ChameleonException;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import picocli.CommandLine.Parameters;

public class SourcePathsContextProvider implements SharedContextManager.SharedContextProvider<SourcePathsContext> {

  public SourcePathsContextProvider() {
  }

  @Parameters(description = { "The file path to load." })
  String filename;

  @Override
  public Class<SourcePathsContext> getContext() {
    return SourcePathsContext.class;
  }

  @Override
  public SourcePathsContext createContext(ChameleonMessageManager messageManager) {
    if (filename == null) {
      return new SourcePathsContext(List.of());
    }

    Path rootPath = Paths.get(filename);

    try {
      return new SourcePathsContext(rootPath);
    } catch (IOException exception) {
      messageManager.report(new ChameleonException(exception));
      return new SourcePathsContext(List.of());
    }
  }
}
