package org.contract_lib.contract_chameleon.contexts;

import org.contract_lib.contract_chameleon.SharedContextManager;

public final class LogLevelContext implements
    SharedContextManager.InterfaceProvidedContext,
    SharedContextManager.SettableContext,
    SharedContextManager.DefaultContext {

  private final MessageContext.LogLevel logLevel;

  public LogLevelContext(MessageContext.LogLevel logLevel) {
    this.logLevel = logLevel;
  }

  public MessageContext.LogLevel getLogLevel() {
    return this.logLevel;
  }
}
