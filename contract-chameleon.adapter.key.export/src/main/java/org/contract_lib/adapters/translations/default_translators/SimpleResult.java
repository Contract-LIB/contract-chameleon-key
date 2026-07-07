package org.contract_lib.adapters.translations.default_translators;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

public record SimpleResult(
    String packageName,
    String className,
    CompilationUnit cu) implements TranslationResult {
  @Override
  public Optional<String> extendSubDirectory() {
    return Optional.ofNullable(packageName);
  }

  public String fileName() {
    return className;
  }

  public String fileEnding() {
    return ".java";
  }

  public void writeTo(Writer writer) throws IOException {
    PrinterConfiguration config = new DefaultPrinterConfiguration();
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
    writer.write(printer.print(cu));
  }
}
