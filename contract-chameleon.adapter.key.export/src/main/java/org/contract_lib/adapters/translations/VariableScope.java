package org.contract_lib.adapters.translations;

import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.ast.type.Type;

import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Symbol;

public interface VariableScope {
  Symbol getClibSymbol();

  Expression getJmlTerm();

  Sort getClibResultType();

  Type getJmlResultType();

  boolean hasFootprint();
}
