
package org.contract_lib.adapters.translations.custom_functions;

import org.contract_lib.lang.contract_lib.ast.Function;

public final class JMLFunctionsStrategy implements CustomFunctionTranslationStrategy {

  public JMLFunctionsStrategy() {

  }

  public Void addDec(Function.FunctionDec dec) {

    return null;
  }

  public Void addDef(Function.FunctionDef def) {

    return null;
  }

  @Override
  public void store() {
    System.err.println("JML Function Strategy does not store the translations.");
  }

  public String getIdentifier(String clibId) {
    return clibId;
  }
}
