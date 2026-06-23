package org.contract_lib.adapters.translations.types;

import java.util.Optional;
import java.util.List;

import com.github.javaparser.ast.NodeList;

import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

public class AbstractionTranslation implements TypeTranslation {

  private final String name;

  public AbstractionTranslation(String name) {
    this.name = name;
  }

  public Sort getClibSort() {
    return new Sort.Type(name);
  }

  public Type getJmlType(Sort sort) {
    return new ClassOrInterfaceType(
        null,
        new SimpleName(name),
        null);
  }

  public KeySort getKeySort(Sort sort) {
    //TODO: Discuss this, not supported by KeY
    System.err.println(
        String.format("ERROR: Translation to not value types, here %s, is not allowed at the moment",
            getJmlType(sort)));
    return new KeySort.Custom(this.getJmlType(sort).toString());
  }

  public boolean hasFootprint() {
    return true;
  }

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    return Optional.of(
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\subset"),
            NodeList.nodeList(
                new FieldAccessExpr(
                    new EnclosedExpr(
                        new CastExpr(
                            this.getJmlType(sort),
                            field)),
                    NodeList.nodeList(),
                    new SimpleName("footprint")),
                new FieldAccessExpr(
                    new ThisExpr(),
                    NodeList.nodeList(),
                    new SimpleName("footprint")))));
  }

  public List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    // Requires that the invariants for this abstraction hold
    return List.of(
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\invariant_for"),
            NodeList.nodeList(
                new CastExpr(
                    this.getJmlType(sort),
                    field))));
  }
}
