package org.contract_lib.adapters.result;

import java.io.Writer;
import java.io.IOException;

import org.contract_lib.contract_chameleon.adapters.ImportAdapter.TranslationResult;
import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.printer.ContractLibPrettyPrinter;

public record ContractLibResult(
    String packageName,
    String className,
    ContractLibAst ast) implements TranslationResult {

  public String directoryName() {
    return packageName;
  }

  public String fileName() {
    return className;
  }

  public String fileEnding() {
    return ".clib";
  }

  public void write(Writer writer) throws IOException {
    //TODO: handle error handling
    ContractLibPrettyPrinter printer = new ContractLibPrettyPrinter(writer, null);
    printer.printContractLibAst(this.ast);
  }
}
