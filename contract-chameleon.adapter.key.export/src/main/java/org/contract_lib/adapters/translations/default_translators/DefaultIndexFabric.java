package org.contract_lib.adapters.translations.default_translators;

import org.contract_lib.adapters.translations.IndexFabric;

import com.github.javaparser.ast.expr.SimpleName;

public final class DefaultIndexFabric implements IndexFabric {
  private char i = 'a';

  public SimpleName getNextIndex() {
    char res = i;
    i++;
    if (i > 'z') {
      //TODO: report Error
      i = 'a';
    }
    return new SimpleName(String.valueOf(res));
  }
}
