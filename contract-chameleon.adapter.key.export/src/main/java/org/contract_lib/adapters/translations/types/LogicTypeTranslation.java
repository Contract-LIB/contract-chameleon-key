
package org.contract_lib.adapters.translations.types;

import java.util.List;
import java.util.Optional;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.key.ast.KeySort;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class LogicTypeTranslation implements TypeTranslation {

  private final KeySort.Custom sort;

  public LogicTypeTranslation(KeySort.Custom sort) {
    this.sort = sort;
  }

  public Sort getClibSort() {
    return new Sort.Type(sort.name());
  }

  public Type getJmlType(Sort sort) {
    return new ClassOrInterfaceType(
        null,
        new SimpleName(String.format("\\dl_%s", this.sort.name())),
        null);
  }

  public KeySort getKeySort(Sort sort) {
    return new KeySort.Custom(String.format("%s", this.sort.name()));
  }

  public boolean hasFootprint() {
    return false;
  }

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return Optional.empty();
  }

  public List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return List.of();
  }
}
