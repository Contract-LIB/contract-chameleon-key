
package org.contract_lib.contract_chameleon.error;

public abstract class ChameleonError implements ChameleonFileReportable {

  @Override
  public final String messageType() {
    return "ERROR";
  }
}
