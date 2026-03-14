package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;

import org.contract_lib.lang.verifast.ast.VeriFastHelperSpecification;

import org.contract_lib.lang.verifast.tools.VeriFastPrinter;

public final record HelperFile(
    String fileName,
    VeriFastHelperSpecification spec) implements TranslationResult {

  public String directoryName() {
    return ".";
  }

  public String fileEnding() {
    return ".javaspec";
  }

  public void write(Writer writer) throws IOException {
    VeriFastPrinter p = new VeriFastPrinter(writer);
    p.printVeriFastHelperSpecification(this.spec);
  }
}
