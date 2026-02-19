package org.contract_lib.contract_chameleon.contexts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.error.ChameleonException;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

public class FilePathContext implements SharedContextManager.SharedContext {

  private FilePathContext(List<Path> paths) {
    this.paths = paths;
  }

  private List<Path> paths;

  public List<Path> getPaths() {
    return new ArrayList<>(paths);
  }

  public class Provider implements SharedContextManager.SharedContextProvider<FilePathContext> {

    private String inputDirName;

    public Provider(String inputDirName) {
      this.inputDirName = inputDirName;
    }

    @Override
    public Class<FilePathContext> getContext() {
      return FilePathContext.class;
    }

    @Override
    public FilePathContext createContext(ChameleonMessageManager messageManager) {

      Path rootPath = Paths.get(inputDirName);

      try {
        return new FilePathContext(
            Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList()));
      } catch (IOException exception) {
        messageManager.report(new ChameleonException(exception));
        return new FilePathContext(List.of());
      }
    }
  }
}
