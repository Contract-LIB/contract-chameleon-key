
package org.contract_lib.contract_chameleon.error;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.contract_lib.contract_chameleon.contexts.MessageContext;

/** The {@link ChameleonMessageManager} is responsible for handeling log messages.
 * <p>
 * Note: This class is replaced by the {@link MessageContext} to match the new interface and design idea.
 * It is just 'kept' here untill all dependencies on this class have been removed and properly reimplemented.
 * <p>
 * The idea of the creation of error messages in contract-chameleon is,
 * that it does not crash on the first problem,
 * but collects all problems,
 * until it cannot continue the translation in all possible locations.
 * As a consequence each run should generate a rich list of meaningful errors,
 * that can be fixed, before running contract-chameleon again.
 * <p>
 * It differentiates between different log levels:
 * <ul>
 * <li> <b>Exception</b>: Exceptions that are not caused by the translations itself and rather by the system (e.g. file access, permissions, …)
 * <li> <b>Error</b>: Error in the translation. Any translation produced should be considered invalid, or no translation can be produced.
 * <li> <b>Warning</b>: There might be cases that (different) adapters are not able to handle the provided input.
 * <li> <b>Info</b>: Message to the user, what contract-chameleon is duing.
 * <li> <b>Debug</b>: Detailed messages about the translation stepps. Useful for debugging.
 * </ul>
 * <p>
 * It groups the error messages by the origin (file) where they appear, if such a location can be determined.
 * <p>
 * It provides information about the stage of the translation process, if such is provided.
 */
public class ChameleonMessageManager {

  private boolean errorFound;
  private ChameleonMessageGroup group;
  private List<ChameleonReportable> messages;

  public ChameleonMessageManager() {
    this.group = new ChameleonMessageGroup();
    this.messages = new ArrayList<>();
    this.messages.add(this.group);
    errorFound = false;
  }

  public void report(ChameleonReportable message) {
    errorFound = true;
    messages.add(message);
  }

  public void reportGroup(ChameleonFileReportable message) {
    errorFound = true;
    this.group.addMessage(message);
  }

  public void createNewGroup() {
    // Creates only a new group if an error was found in the previous
    if (this.group.errorFound()) {
      this.group = new ChameleonMessageGroup();
      this.messages.add(this.group);
    }
  }

  public Stream<ChameleonReportable> getMessages() {
    return messages.stream();
  }

  public void writeStdErr() {
    System.err.println();
    System.err.println(
        messages.stream().map(ChameleonReportable::getMessage).collect(Collectors.joining(System.lineSeparator())));
  }

  /** Checks if there was an error reported.
   * 
   * @return {@value true} if there was an error, otherwise {@value false}.
   */
  public boolean errorFound() {
    return errorFound;
  }
}
