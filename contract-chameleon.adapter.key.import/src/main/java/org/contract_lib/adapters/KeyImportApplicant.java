package org.contract_lib.adapters;

import java.io.IOException;

import java.util.List;

import java.nio.file.Path;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.adapters.ImportAdapter;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import com.google.auto.service.AutoService;

@AutoService(Adapter.class)
public final class KeyImportApplicant extends ImportAdapter {

  public String defaultOutputDir() {
    return "key-import";
  }

  public String getAdapterName() {
    return "key-import";
  }

  public List<TranslationResult> perform(
      List<Path> sourceFiles,
      ChameleonMessageManager messageManager) throws IOException {

    SimpleKeyImportTranslator translator = new SimpleKeyImportTranslator(messageManager);

    return translator.translate(sourceFiles);
  }
}
