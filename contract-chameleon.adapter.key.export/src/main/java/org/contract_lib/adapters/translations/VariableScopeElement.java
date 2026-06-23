
package org.contract_lib.adapters.translations;

import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Symbol;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;

public record VariableScopeElement(
    Symbol symbol,
    Expression expression,
    Sort clibResultType,
    Type jmlResultType,
    boolean hasFootprint) implements VariableScope {

  public Symbol getClibSymbol() {
    return symbol;
  }

  public Expression getJmlTerm() {
    return expression;
  }

  public Sort getClibResultType() {
    return clibResultType;
  }

  public Type getJmlResultType() {
    return jmlResultType;
  }

  public boolean hasFootprint() {
    return hasFootprint;
  }
}
