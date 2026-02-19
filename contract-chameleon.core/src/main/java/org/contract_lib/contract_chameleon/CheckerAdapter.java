
package org.contract_lib.contract_chameleon;

import java.util.List;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

public abstract class CheckerAdapter extends Adapter {

  /**
   * Performs the checks on the provided source files.
   * 
   * @param sourceFiles The source files that should be checked by the adapter
   * @param messageManager A message manager to supporting structured messages to be printed to the user.
   * @return If the check was successful {@value CheckerAdapterResult#SUCCESS} otherwise {@value CheckerAdapterResult#FAILURE}.
   */
  public abstract CheckerAdapterResult perform(
      List<Path> sourceFiles,
      ChameleonMessageManager messageManager) throws IOException;

  private ChameleonMessageManager messageManager = new ChameleonMessageManager();

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
      this.perform(List.of(Paths.get(inputFileName)), messageManager);
    } catch (IOException e) {
      //TODO: Proper error handling, permission checks, …
      System.err.println(e);
    }
  }

  public enum CheckerAdapterResult {
    SUCCESS,
    FAILURE;
  }
}
