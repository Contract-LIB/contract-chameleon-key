package org.contract_lib.contract_chameleon.error;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ChameleonMessageGroup implements ChameleonReportable {

  List<ChameleonFileReportable> messages;

  ChameleonMessageGroup() {
    this.messages = new ArrayList<>();
  }

  void addMessage(ChameleonFileReportable reportable) {
    this.messages.add(reportable);
  }

  public String getMessage() {
    //TODO: sort messages before printing
    return messages.stream()
        .map(this::messageDescription)
        .collect(Collectors.joining(System.lineSeparator()));
  }

  /** Checks if there was an error reported.
   * 
   * @return {@value true} if there was an error, otherwise {@value false}.
   */
  public boolean errorFound() {
    return !this.messages.isEmpty();
  }

  public List<ChameleonFileReportable> getMessages() {
    return messages;
  }

  private String messageDescription(ChameleonFileReportable message) {
    return String.format(
        "%s in %s: %d|%d -> %s",
        message.messageType(),
        message.getLocationIdentifier(),
        message.getLine(),
        message.getCharIndex(),
        message.getMessage());
  }
}
