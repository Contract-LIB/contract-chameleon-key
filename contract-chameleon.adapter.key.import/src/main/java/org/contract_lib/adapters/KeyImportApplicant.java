package org.contract_lib.adapters;

import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.nio.file.Path;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.ImportAdapter;
import org.contract_lib.contract_chameleon.adapters.TranslationAdapter;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.Dir;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class KeyImportApplicant extends TranslationAdapter {

  public String getAdapterName() {
    return "key-import";
  }

  @Override
  public void performTranslation() {

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

    SimpleKeyImportTranslator translator = new SimpleKeyImportTranslator(getMessageContext().getMessageManager());

    List<TranslationResult> results = translator.translate(sourcesContext.get().getPaths());
    Dir resultDirectory = resultContext.get().getResultDirectory();
    results.forEach(resultDirectory::writeResult);
  }
}
