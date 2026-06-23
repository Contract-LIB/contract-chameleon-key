package org.contract_lib.adapters.translations.types;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

@AutoService(TypeTranslation.class)
public class BoolTranslation implements TypeTranslation {
  public Sort getClibSort() {
    return new Sort.Type("Bool");
  }

  public Type getJmlType(Sort sort) {
    return PrimitiveType.booleanType();
  }

  public KeySort getKeySort(Sort sort) {
    return KeySort.Internal.getBoolean();
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
  }

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return Optional.empty();
    //new MethodCallExpr(
    //  null, // scope
    //   new SimpleName("\\subset"),
    //  NodeList.nodeList(
    //   field,
    //    new ThisExpr()
    //  )
    //);
  }
}
