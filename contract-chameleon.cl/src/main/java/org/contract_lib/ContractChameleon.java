package org.contract_lib;

import java.util.List;

import org.contract_lib.context_providers.ResultDirectoryContextProvider;
import org.contract_lib.context_providers.SourcePathsContextProvider;
import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.AdapterMap;
import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.SharedContextManager.SharedContextProvider;
import org.contract_lib.contract_chameleon.SharedContextManager.UserProvidedContext;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "contract-chameleon")
class ContractChameleon {

  @Option(names = { "-h", "--help" }, usageHelp = true) // description = "display description"
  boolean usageHelpRequested;

  private static List<SharedContextProvider<? extends UserProvidedContext>> interfaceProvider = List.of(
      new SourcePathsContextProvider(),
      new ResultDirectoryContextProvider());

  private AdapterMap<Adapter> adapterMap = new AdapterMap<Adapter>(Adapter.class);
  private ChameleonMessageManager messageManager = new ChameleonMessageManager();
  private SharedContextManager contextManager = new SharedContextManager(messageManager);

  public static void main(String[] args) {
    ContractChameleon cc = new ContractChameleon();
    CommandLine cl = new CommandLine(cc);

    interfaceProvider.forEach(cc.contextManager::putProvider);

    cl.getCommandSpec()
        .usageMessage()
        .footer("Loaded Adapters: ", cc.adapterMap.adapterList());

    cc.adapterMap
        .sortedElements()
        .forEach((e) -> {
          CommandLine cSub = new CommandLine(new AdapterCommand(e.getValue(), args));

          e.getValue()
              .argumentContextsFromInterface()
              .forEach(s -> {
                cc.contextManager
                    .getProvider(s)
                    .ifPresent(p -> cSub.addMixin(s.getName(), p));
              });

          cl.addSubcommand(e.getKey(), cSub);
        });

    int res = cl.execute(args);
    System.exit(res);
  }
}
