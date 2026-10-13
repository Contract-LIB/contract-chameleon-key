package org.contract_lib.adapters.translations.custom_functions;

import java.util.List;

import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Symbol;
import org.contract_lib.lang.contract_lib.ast.Term.Identifier.IdentifierValue;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.Type;

public final record CustomFunctionTranslation(
    String clibIdentifier,
    String jmlIdentifer,
    List<Sort> clibParameterTypes,
    Sort clibResult,
    List<Type> jmlParameterTypes,
    Type jmlResultType) implements FuncTranslation {

  @Override
  public IdentifierValue getClibIdentifier() {
    return new IdentifierValue(new Symbol(clibIdentifier));
  }

  @Override
  public List<Sort> getClibParameterTypes() {
    return clibParameterTypes;
  }

  @Override
  public Sort getClibResultType() {
    return clibResult;
  }

  @Override
  public List<Type> getJmlParameterTypes() {
    return jmlParameterTypes;
  }

  @Override
  public Type getJmlResultType() {
    return jmlResultType;
  }

  @Override
  public Expression getJmlTerm(List<Expression> parameters) {
    return new MethodCallExpr(null, jmlIdentifer, NodeList.nodeList(parameters));
  }

}
