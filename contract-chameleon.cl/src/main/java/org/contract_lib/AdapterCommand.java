package org.contract_lib;

import java.util.concurrent.Callable;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.SharedContextManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
class AdapterCommand implements Callable<Integer> {

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays this help description.")
  boolean usageHelpRequested;

  private Adapter adapter;

  AdapterCommand(Adapter adapter, SharedContextManager sharedContextManager) {
    this.adapter = adapter;
    this.adapter.setSharedContextManager(sharedContextManager);
  }

  public Integer call() {

    adapter.execute();

    return 0;
  }
}
