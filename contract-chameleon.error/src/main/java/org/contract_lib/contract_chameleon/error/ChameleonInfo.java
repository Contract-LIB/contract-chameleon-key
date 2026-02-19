
package org.contract_lib.contract_chameleon.error;

public abstract class ChameleonInfo implements ChameleonFileReportable {
  @Override
  public final String messageType() {
    return "INFO";
  }
}
