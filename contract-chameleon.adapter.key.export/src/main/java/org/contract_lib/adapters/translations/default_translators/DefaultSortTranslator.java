
package org.contract_lib.adapters.translations.default_translators;

import java.util.Map;

import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

public record DefaultSortTranslator(
    Map<String, TypeTranslation> map) implements TypeTranslator {

  public void store(TypeTranslation trans) {
    //TODO: Do error check if translation is ambigous
    this.map().put(trans.getClibSort().getName(), trans);
  }

  public TypeTranslation translate(Sort sort) {
    TypeTranslation translation = this.map().get(sort.getName());
    if (translation == null) {
      //TODO: Report proper error
      System.err.println(String.format("ERROR: The type '%s' could not be translated.", sort));
      return null;
    }
    return translation;
  }
}
