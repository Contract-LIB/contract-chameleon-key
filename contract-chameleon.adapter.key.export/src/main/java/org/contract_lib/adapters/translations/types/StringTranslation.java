package org.contract_lib.adapters.translations.types;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

@AutoService(TypeTranslation.class)
public class StringTranslation implements TypeTranslation {

  public Sort getClibSort() {
    return new Sort.Type("String");
  }

  public Type getJmlType(Sort sort) {
    return new ClassOrInterfaceType(null, "String");
  }

  public KeySort getKeySort(Sort sort) {
    return new KeySort.Custom("String");
  }

  public boolean hasFootprint() {
    return false;
  }

  public List<Expression> getHelper(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return List.of();
  };

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return Optional.empty();
  }
}
