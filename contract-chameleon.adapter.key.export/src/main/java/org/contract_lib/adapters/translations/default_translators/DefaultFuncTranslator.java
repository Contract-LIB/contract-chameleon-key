
package org.contract_lib.adapters.translations.default_translators;

import java.util.List;
import java.util.Map;

import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.adapters.translations.FuncTranslator;
import org.contract_lib.lang.contract_lib.ast.Term.Identifier.IdentifierValue;

public record DefaultFuncTranslator(
    Map<String, FuncTranslation> map) implements FuncTranslator {

  public void store(List<FuncTranslation> translations) {
    //TODO: Error check if ambigous
    for (FuncTranslation trans : translations) {
      this.map().put(trans.getClibIdentifier().identifier().identifier(), trans);
    }
  }

  public FuncTranslation translate(IdentifierValue identifier) {
    FuncTranslation t = this.map().get(identifier.identifier().identifier());
    if (t == null) {
      System.err.println(String.format("ERROR: Translations for the identifier '%s' not found.", identifier));
      return null;
    }
    return t;
  }
}
