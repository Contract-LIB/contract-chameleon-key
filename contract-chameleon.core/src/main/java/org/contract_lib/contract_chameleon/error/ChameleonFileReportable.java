
package org.contract_lib.contract_chameleon.error;

public interface ChameleonFileReportable extends ChameleonReportable {
  String getLocationIdentifier();

  int getLine();

  int getCharIndex();

  String messageType();
}
