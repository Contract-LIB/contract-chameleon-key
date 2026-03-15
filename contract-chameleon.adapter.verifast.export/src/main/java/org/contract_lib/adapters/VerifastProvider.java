package org.contract_lib.adapters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.TranslationAdapter;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.Dir;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.generator.ContractLibGenerator;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class VerifastProvider extends TranslationAdapter {

  public String getAdapterName() {
    return "verifast-provider";
  }

  private ContractLibGenerator generator;
  private ResultDirectoryContext result;

  @Override
  public void performTranslation() {

    generator = new ContractLibGenerator(getMessageContext().getMessageManager());
    Optional<SourcePathsContext> sourcesContext = getContext(SourcePathsContext.class);
    Optional<ResultDirectoryContext> resultContext = getContext(ResultDirectoryContext.class);
    if (sourcesContext.isEmpty()) {
      getMessageContext().logError("Source Context required.");
      return;
    }
    if (resultContext.isEmpty()) {
      getMessageContext().logError("Result Context required.");
      return;
    }
    this.result = resultContext.get();

    // generate no subdir if only one source is provided.
    if (sourcesContext.get().getPaths().size() == 1) {
      try {
        ContractLibAst ast = generator.generateFromPath(sourcesContext.get().getPaths().getFirst());

        SimpleVerifastTranslator trans = new SimpleVerifastTranslator(sourcesContext.get().getPaths().getFirst(),
            getMessageContext().getMessageManager());
        List<TranslationResult> results = trans.translateContractLibAstProvider(ast);
        results.forEach(result.getResultDirectory()::writeResult);
      } catch (IOException e) {
        getMessageContext().logException(e);
      }
    } else {
      sourcesContext.get().getPaths().forEach(this::performForPath);
    }
  }

  private void performForPath(Path p) {
    String filename = p.getFileName().toString();
    Dir finalDir = result.getResultDirectory().addSubDirectories(filename);

    try {
      ContractLibAst ast = generator.generateFromPath(p);
      SimpleVerifastTranslator trans = new SimpleVerifastTranslator(p,
          getMessageContext().getMessageManager());
      List<TranslationResult> results = trans.translateContractLibAstProvider(ast);
      results.forEach(finalDir::writeResult);
    } catch (IOException e) {
      getMessageContext().logException(e);
    }
  }
}
