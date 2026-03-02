package org.contract_lib;

import java.util.concurrent.Callable;

import org.contract_lib.contract_chameleon.Adapter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
class AdapterCommand implements Callable<Integer> {

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays this help description.")
  boolean usageHelpRequested;

  private Adapter adapter;
  private String[] args;

  AdapterCommand(Adapter adapter, String[] args) {
    this.adapter = adapter;
    this.args = args;
  }

  public Integer call() throws Exception {
    String classpath = System.getProperty("java.class.path");
    System.err.println("Classpath: " + classpath);
    System.err.println("User Dir:" + System.getProperty("user.dir"));

    adapter.perform(args);

    return 0;
  }
}
