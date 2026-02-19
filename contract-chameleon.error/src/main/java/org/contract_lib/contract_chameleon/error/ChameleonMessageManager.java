
package org.contract_lib.contract_chameleon.error;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
