package org.contract_lib.context_providers;

import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.contexts.LogLevelContext;
import org.contract_lib.contract_chameleon.contexts.MessageContext.LogLevel;

import picocli.CommandLine.Option;

public class LogLevelContextProvider
    implements SharedContextManager.SharedContextProvider<LogLevelContext> {

  public LogLevelContextProvider() {
  }

  @Option(names = { "--logLevel", "-l" }, description = {
      "Select a log level: ${COMPLETION-CANDIDATES}%n(default: ${DEFAULT-VALUE})." }, defaultValue = "INFO")
  private LogLevel logLevel;

  @Override
  public Class<LogLevelContext> getContext() {
    return LogLevelContext.class;
  }

  @Override
  public LogLevelContext createContext(SharedContextManager sharedContextManager) {
    return new LogLevelContext(this.logLevel);
  }
}
