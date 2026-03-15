package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import java.util.List;
import java.util.Optional;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;

public final record JarSpecFile(
    String fileName, //Name of the jar
    List<String> javaSpecFiles) implements TranslationResult {

  @Override
  public Optional<String> extendSubDirectory() {
    return Optional.empty();
  }

  @Override
  public String fileEnding() {
    return ".jarspec";
  }

  @Override
  public void writeTo(Writer writer) throws IOException {
    for (String f : javaSpecFiles) {
      writer.write(f + System.lineSeparator());
    }
  }
}
