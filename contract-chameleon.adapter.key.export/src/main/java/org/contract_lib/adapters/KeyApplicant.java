package org.contract_lib.adapters;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.ExportAdapter;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.Dir;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.generator.ContractLibGenerator;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class KeyApplicant extends ExportAdapter {

  public String getAdapterName() {
    return "key-applicant";
  }

  @Override
  public final void performForPath(Path p, Dir finalDir) {
    try {
      ContractLibGenerator generator = new ContractLibGenerator(getMessageContext().getMessageManager());
      ContractLibAst ast = generator.generateFromPath(p);
      SimpleKeyProviderTranslator trans = new SimpleKeyProviderTranslator(getMessageContext().getMessageManager());
      List<TranslationResult> results = trans.translateContractLibAstApplicant(ast);
      results.forEach(finalDir::writeResult);
    } catch (IOException e) {
      getMessageContext().logException(e);
    }
  }
}
