package org.contract_lib.adapters.translations;

import java.util.List;

import org.contract_lib.lang.contract_lib.ast.Term.Identifier.IdentifierValue;

public interface FuncTranslator {
  public void store(List<FuncTranslation> translations);

  public FuncTranslation translate(IdentifierValue identifier);
}
