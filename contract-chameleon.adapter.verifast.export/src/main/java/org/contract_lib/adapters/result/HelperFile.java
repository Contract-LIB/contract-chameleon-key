package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.lang.verifast.ast.VeriFastHelperSpecification;

import org.contract_lib.lang.verifast.tools.VeriFastPrinter;

public final record HelperFile(
    String fileName,
    VeriFastHelperSpecification spec) implements TranslationResult {

  @Override
  public Optional<String> extendSubDirectory() {
    return Optional.empty();
  }

  public String fileEnding() {
    return ".javaspec";
  }

  public void writeTo(Writer writer) throws IOException {
    VeriFastPrinter p = new VeriFastPrinter(writer);
    p.printVeriFastHelperSpecification(this.spec);
  }
}
