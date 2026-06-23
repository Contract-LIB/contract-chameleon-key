package org.contract_lib.adapters.translations.types;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

@AutoService(TypeTranslation.class)
public class RefTranslation implements TypeTranslation {

  public Sort getClibSort() {
    return new Sort.Type("Ref");
  }

  public Type getJmlType(Sort sort) {
    Optional<Sort> paramter = getTypeOfParameter(sort);
    if (paramter.isEmpty()) {
      return new ClassOrInterfaceType(
          null,
          new SimpleName("InvalidType"),
          null);
    }
    return new ClassOrInterfaceType(
        null,
        new SimpleName(paramter.get().getName()),
        null);
  }

  public KeySort getKeySort(Sort sort) {
    Optional<Sort> paramter = getTypeOfParameter(sort);
    if (paramter.isEmpty()) {
      return new KeySort.Custom("InvalidType");
    }
    return new KeySort.Custom(paramter.get().getName());
  }

  public boolean hasFootprint() {
    return false;
  }

  public List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
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

  public Optional<Sort> matchType(Sort.Type t) {
    //TODO: Throw proper error
    return Optional.empty();
  }

  public Optional<Sort> matchParametricType(Sort.ParametricType pt) {
    if (pt.arguments().size() != 1) {
      //TODO: Throw proper error
      return Optional.empty();
    }
    return Optional.of(pt.arguments().get(0));
  }

  public Optional<Sort> getTypeOfParameter(Sort seq) {
    return seq.perform(
        this::matchType,
        this::matchParametricType);
  }
}
