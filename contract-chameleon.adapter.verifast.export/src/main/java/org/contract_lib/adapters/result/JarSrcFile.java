package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import java.util.List;

import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;

import org.contract_lib.lang.verifast.ast.VeriFastSpec;

public final record JarSrcFile(
    List<String> jarFiles,
    List<String> javaFiles) implements TranslationResult {

  public String directoryName() {
    return ".";
  }

  public String fileName() {
    return "sources";
  }

  public String fileEnding() {
    return ".jarsrc";
  }

  public void write(Writer writer) throws IOException {
    for (String f : jarFiles) {
      writer.write(f + System.lineSeparator());
    }
    for (String f : javaFiles) {
      writer.write(f + System.lineSeparator());
    }
  }
}
