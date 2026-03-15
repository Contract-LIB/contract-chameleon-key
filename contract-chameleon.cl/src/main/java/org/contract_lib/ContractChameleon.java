package org.contract_lib;

import java.util.List;

import org.contract_lib.context_providers.ResultDirectoryContextProvider;
import org.contract_lib.context_providers.SourcePathsContextProvider;
import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.AdapterMap;
import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.SharedContextManager.SharedContextProvider;
import org.contract_lib.contract_chameleon.adapters.CheckerAdapter;
import org.contract_lib.contract_chameleon.contexts.MessageContext;
import org.contract_lib.contract_chameleon.contexts.MessageContext.StringLogger;
import org.contract_lib.contract_chameleon.SharedContextManager.InterfaceProvidedContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "contract-chameleon")
class ContractChameleon {

  @Option(names = { "-h", "--help" }, usageHelp = true) // description = "display description"
  boolean usageHelpRequested;

  private static String ERROR_REQUIRED_CONTEXT_NOT_SUPPORTED = "Adapter '%s' requires interface for context '%s', which is not supported by this interface.";

  private static List<SharedContextProvider<? extends InterfaceProvidedContext>> interfaceProvider = List.of(
      new SourcePathsContextProvider(),
      new ResultDirectoryContextProvider());

  private StringLogger logger = new MessageContext.StringLogger() {
    @Override
    public void log(String string) {
      System.err.println(string);
    }
  };

  private MessageContext messageContext = new MessageContext(logger);

  private SharedContextManager contextManager = new SharedContextManager(messageContext);

  private AdapterMap<Adapter> adapterMap = new AdapterMap<Adapter>(Adapter.class);
  private AdapterMap<CheckerAdapter> checkerAdapterMap = new AdapterMap<CheckerAdapter>(CheckerAdapter.class);

  public static void main(String[] args) {
    ContractChameleon cc = new ContractChameleon();
    CommandLine cl = new CommandLine(cc);

    interfaceProvider.forEach(cc.contextManager::putProvider);

    // Append the default help command with information about the loaded adapters.
    cl.getCommandSpec()
        .usageMessage()
        .footer("Loaded Adapters: ", cc.adapterMap.adapterList());

    // Add a command for each adapter loaded.
    cc.adapterMap
        .sortedElements()
        .forEach((e) -> {
          CommandLine cSub = new CommandLine(new AdapterCommand(e.getValue(), cc.contextManager));

          e.getValue()
              .argumentContextsFromInterface()
              .forEach(contexts -> {
                cc.contextManager
                    .getProvider(contexts)
                    .ifPresentOrElse(p -> cSub.addMixin(contexts.getName(), p),
                        () -> cc.messageContext.logError(String.format(
                            ERROR_REQUIRED_CONTEXT_NOT_SUPPORTED,
                            e.getValue().getClass(), contexts.getClass())));
              });

          e.getValue().helpMessage()
              .ifPresent(cSub.getCommandSpec().usageMessage()::footer);

          cl.addSubcommand(e.getKey(), cSub);
        });

    String classpath = String.format("Classpath: %s", System.getProperty("java.class.path"));
    String userDir = String.format("User Dir: %s", System.getProperty("user.dir"));
    cc.contextManager.getMessageContext().logInfo(classpath);
    cc.contextManager.getMessageContext().logInfo(userDir);

    // Execute the command (esp. the selected adapter).
    int res = cl.execute(args);

    // Log all messages.
    cc.messageContext.log();

    // Exit with exit code.
    System.exit(res);
  }
}
