package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import java.util.List;
import java.util.Optional;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;

public final record JarSrcFile(
    List<String> jarFiles,
    List<String> javaFiles) implements TranslationResult {

  @Override
  public Optional<String> extendSubDirectory() {
    return Optional.empty();
  }

  @Override
  public String fileName() {
    return "sources";
  }

  @Override
  public String fileEnding() {
    return ".jarsrc";
  }

  @Override
  public void writeTo(Writer writer) throws IOException {
    for (String f : jarFiles) {
      writer.write(f + System.lineSeparator());
    }
    for (String f : javaFiles) {
      writer.write(f + System.lineSeparator());
    }
  }
}
