package org.contract_lib.adapters.result;

import java.io.IOException;
import java.io.Writer;

import java.util.List;

import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;

import org.contract_lib.lang.verifast.ast.VeriFastSpec;

public final record JarSpecFile(
    String fileName, //Name of the jar
    List<String> javaSpecFiles) implements TranslationResult {

  public String directoryName() {
    return ".";
  }

  public String fileEnding() {
    return ".jarspec";
  }

  public void write(Writer writer) throws IOException {
    for (String f : javaSpecFiles) {
      writer.write(f + System.lineSeparator());
    }
  }
}
