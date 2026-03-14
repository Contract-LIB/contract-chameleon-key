package org.contract_lib.adapters;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.ExportAdapter;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.generator.ContractLibGenerator;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class KeyProvider extends ExportAdapter {

  public String defaultOutputDir() {
    return "key-provider";
  }

  public String getAdapterName() {
    return "key-provider";
  }

  public List<TranslationResult> perform(
      List<Path> sourceFiles,
      ChameleonMessageManager messageManager) throws IOException {

    //TODO: Support mulitple files
    System.err.println("This provider supports only one class at the moment.");
    Path fileName = sourceFiles.get(0);

    ContractLibGenerator generator = new ContractLibGenerator(messageManager);

    ContractLibAst ast = generator.generateFromPath(fileName);
    SimpleKeyProviderTranslator trans = new SimpleKeyProviderTranslator(messageManager);
    List<TranslationResult> results = trans.translateContractLibAstProvider(ast);

    return results;
  }
}
