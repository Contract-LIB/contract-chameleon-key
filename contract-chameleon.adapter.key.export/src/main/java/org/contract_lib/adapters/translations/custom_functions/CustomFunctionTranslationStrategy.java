package org.contract_lib.adapters.translations.custom_functions;

import org.contract_lib.lang.contract_lib.ast.Function;

public interface CustomFunctionTranslationStrategy {

  public void store();

  public String getIdentifier(String clibId);

  public default void addFunc(Function def) {
    def.perform(this::addDec, this::addDef);
  }

  public Void addDec(Function.FunctionDec dec);

  public Void addDef(Function.FunctionDef def);
}
