
package org.contract_lib.contract_chameleon.error;

public abstract class ChameleonWarning implements ChameleonFileReportable {
  @Override
  public final String messageType() {
    return "WARNING";
  }
}
