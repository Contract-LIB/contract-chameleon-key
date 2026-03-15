package org.contract_lib.adapters.result;

import java.io.Writer;
import java.util.Optional;
import java.io.IOException;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.printer.ContractLibPrettyPrinter;

public record ContractLibResult(
    String packageName,
    String className,
    ContractLibAst ast) implements TranslationResult {

  @Override
  public Optional<String> extendSubDirectory() {
    return Optional.ofNullable(packageName);
  }

  public String fileName() {
    return className;
  }

  public String fileEnding() {
    return ".clib";
  }

  public void writeTo(Writer writer) throws IOException {
    ContractLibPrettyPrinter printer = new ContractLibPrettyPrinter(writer, null);
    printer.printContractLibAst(this.ast);
  }
}
