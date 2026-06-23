package org.contract_lib.adapters.translations.types;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;

import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.PrimitiveType;

import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.MethodCallExpr;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

//TODO: Yes, one could just use the correct math mode, but I kind of like this approach :)
@AutoService(TypeTranslation.class)
public class BoundedIntTranslation implements TypeTranslation {

  public Sort getClibSort() {
    return new Sort.Type("BoundInt");
  }

  public Type getJmlType(Sort sort) {
    return PrimitiveType.intType();
  }

  public KeySort getKeySort(Sort sort) {
    return KeySort.Internal.getInt();
  }

  public boolean hasFootprint() {
    return false;
  }

  public List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return List.of(
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\dl_inInt"),
            NodeList.nodeList(field)));
  };

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return Optional.empty();
  }
}
