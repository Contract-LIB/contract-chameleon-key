package org.contract_lib.adapters.translations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.contract_lib.lang.contract_lib.ast.Formal;
import org.contract_lib.lang.contract_lib.ast.SortedVar;
import org.contract_lib.lang.contract_lib.ast.Symbol;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.type.Type;

public final class VariableScopeManager implements VariableTranslator {

  private static String RESULT_LABEL = "\\result";

  private final TypeTranslator sortTranslator;

  private final Map<String, VariableScope> map = new HashMap<>();
  private final List<Parameter> parameters = new ArrayList<>();
  private Optional<Type> returnType = Optional.empty();
  private Optional<Type> ownerType = Optional.empty();

  public VariableScopeManager(TypeTranslator sortTranslator) {
    this.sortTranslator = sortTranslator;

  }

  public void add(SortedVar sortedVar) {
    TypeTranslation t = sortTranslator.translate(sortedVar.sort());
    VariableScopeElement el = new VariableScopeElement(
        sortedVar.symbol(),
        new NameExpr(sortedVar.symbol().identifier()),
        sortedVar.sort(),
        t.getJmlType(sortedVar.sort()),
        t.hasFootprint());
    map.put(el.getClibSymbol().identifier(), el);
  }

  public void remove(SortedVar sortedVar) {
    map.remove(sortedVar.symbol().identifier());
  }

  public void add(Formal formal) {
    TypeTranslation t = sortTranslator.translate(formal.sort());
    Symbol s = new Symbol(formal.identifier().identifier());
    //TODO: find better way for result
    if (formal.identifier().identifier().equals("result")) {
      VariableScopeElement el = new VariableScopeElement(
          s,
          new NameExpr(RESULT_LABEL),
          formal.sort(),
          t.getJmlType(formal.sort()),
          t.hasFootprint());
      this.returnType = Optional.ofNullable(t.getJmlType(formal.sort()));
      map.put(el.getClibSymbol().identifier(), el);
    } else if (formal.identifier().identifier().equals("this")) {
      this.ownerType = Optional.ofNullable(t.getJmlType(formal.sort()));
      VariableScopeElement el = new VariableScopeElement(
          s,
          new ThisExpr(),
          formal.sort(),
          t.getJmlType(formal.sort()),
          t.hasFootprint());
      map.put(el.getClibSymbol().identifier(), el);
    } else {
      VariableScopeElement el = new VariableScopeElement(
          s,
          new NameExpr(formal.identifier().identifier()),
          formal.sort(),
          t.getJmlType(formal.sort()),
          t.hasFootprint());
      map.put(el.getClibSymbol().identifier(), el);
      parameters.add(new Parameter(
          t.getJmlType(formal.sort()),
          new SimpleName(formal.identifier().identifier())));
    }
  }

  public Optional<VariableScope> translate(Symbol symbol) {
    return Optional.ofNullable(map.get(symbol.identifier()));
  }

  public Optional<Type> getReturnType() {
    return returnType;
  }

  public Optional<Type> getOwnerType() {
    return ownerType;
  }

  public List<Parameter> getParameters() {
    return new ArrayList<>(parameters);
  }
}
