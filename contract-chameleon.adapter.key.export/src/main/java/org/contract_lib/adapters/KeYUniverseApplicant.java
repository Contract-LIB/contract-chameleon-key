package org.contract_lib.adapters;

import java.io.IOException;
import java.nio.file.Path;

import org.contract_lib.adapters.translations.universe_translators.UniverseKeYTranslator;
import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.ExportAdapter;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.Dir;
import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.generator.ContractLibGenerator;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class KeYUniverseApplicant extends ExportAdapter {
  private static String ADAPTER_NAME = "key-universe-applicant";

  @Override
  public String getAdapterName() {
    return ADAPTER_NAME;
  }

  @Override
  public void performForPath(Path p, Dir finalDir) {
    try {
      var generator = new ContractLibGenerator(getMessageContext().getMessageManager());
      var trans = new UniverseKeYTranslator(getMessageContext().getMessageManager());

      ContractLibAst ast = generator.generateFromPath(p);
      var results = trans.translateContractLibAst(ast, false);
      UniverseKeYTranslator.addImport(results, "universe.qual.*");
      results.forEach(finalDir::writeResult);

    } catch (IOException e) {
      getMessageContext().logException(e);
    }
  }
}
