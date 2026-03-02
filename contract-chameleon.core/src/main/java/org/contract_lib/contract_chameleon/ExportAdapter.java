
package org.contract_lib.contract_chameleon;

import java.util.List;

import java.io.Writer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.contract_lib.contract_chameleon.SharedContextManager.UserProvidedContext;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;
import org.contract_lib.contract_chameleon.error.ChameleonException;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

//TODO: Rename to `TranslationAdapter` and remove Export-/Import-Adapter
public abstract class ExportAdapter extends Adapter {

  public abstract List<TranslationResult> perform(
      List<Path> sourceFiles,
      ChameleonMessageManager messageManager) throws IOException;

  @Override
  public List<Class<? extends UserProvidedContext>> argumentContextsFromInterface() {
    return List.of(
        SourcePathsContext.class,
        ResultDirectoryContext.class);
  }

  //public abstract String adapterTitle();
  public abstract String defaultOutputDir();

  private ChameleonMessageManager messageManager = new ChameleonMessageManager();

  /** The checker adapters that should run beforehand this adapter is called.
   * <p>
   * The adapters might run in arbitrary order,
   * or might be skipped.
   * <p>
   * Defaults to a empty list of contexts.
   */
  public List<CheckerAdapter> requiredAdapters() {
    return List.of();
  }

  @Override
  public final void perform(String[] args) {

    System.err.println("============================== ");
    System.err.println("==== Perform Key Provider ==== "); //TODO: proper title provider
    System.err.println("============================== ");

    if (args.length <= 1) {
      System.err.println("Expected path to files in command"); //TODO: proper error handling
      return;
    }
    String inputFileName = args[1];
    if (args.length > 2) {
      System.err.println("Only the first input file is handled at the moment.");
    }

    try {
      List<TranslationResult> results = this.perform(List.of(Paths.get(inputFileName)), messageManager);
      // Create adapter directory
      String outputDir = defaultOutputDir();
      String fileDir = getDirForFile(inputFileName);
      Path directoryPath = Paths.get(".", outputDir, fileDir);
      if (Files.isDirectory(directoryPath)) {
        System.err.println(String.format("INFO: Directory at %s does already exist.", directoryPath));
      } else {
        Files.createDirectories(directoryPath);
      }

      //Write results
      for (TranslationResult res : results) {
        String directoryName = res.directoryName();
        String fileName = res.fileName();
        String fileEnding = res.fileEnding();
        Path packagePath = Paths.get(".", outputDir, fileDir, directoryName);
        Path classPath = Paths.get(".", outputDir, fileDir, directoryName, fileName + fileEnding);

        if (Files.isDirectory(packagePath)) {
          System.err.println(String.format("INFO: Directory at %s does already exist.", packagePath));
        } else {
          Files.createDirectories(packagePath);
        }

        if (Files.exists(classPath)) {
          System.err.println(String.format("WARNING: File at %s does already exist, will be overridden!", classPath));
        }

        try (BufferedWriter writer = Files.newBufferedWriter(classPath)) {
          res.write(writer);
        }
      }
    } catch (IOException e) {
      messageManager.report(new ChameleonException(e));
    }
  }

  //TODO: find better solution
  private String getDirForFile(String file) {
    return file.replaceAll("(\\.|/)", "_");
  }

  public interface TranslationResult {
    /// The name of the directory where the file should be stored.
    String directoryName();

    /// The name under which the file should be stored. 
    String fileName();

    /// The fileending of the file stored.
    String fileEnding();

    /// Write handler how the content should be written to the file.
    void write(Writer writer) throws IOException;
  }
}
