
package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;

import org.contract_lib.lang.verifast.ast.VeriFastSpec;

import org.contract_lib.lang.verifast.tools.VeriFastPrinter;

public final record JavaSpecFile(
    String directoryName,
    String fileName,
    VeriFastSpec spec) implements TranslationResult {

  public String fileEnding() {
    return ".javaspec";
  }

  public void write(Writer writer) throws IOException {
    VeriFastPrinter p = new VeriFastPrinter(writer);
    p.printVeriFastSpec(this.spec);
  }
}
