package org.contract_lib.contract_chameleon.error;

public final class ChameleonException implements ChameleonReportable {

  private final Exception exception;

  public ChameleonException(Exception exception) {
    this.exception = exception;
  }

  @Override
  public String getMessage() {
    return exception.getMessage();
  }
}
