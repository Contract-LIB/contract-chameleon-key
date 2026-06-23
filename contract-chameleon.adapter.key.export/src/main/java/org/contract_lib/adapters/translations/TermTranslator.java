package org.contract_lib.adapters.translations;

import org.contract_lib.lang.contract_lib.ast.Quantor;
import org.contract_lib.lang.contract_lib.ast.Term;
import org.contract_lib.lang.contract_lib.ast.Term.SpecConstant;
import org.contract_lib.lang.contract_lib.ast.Term.Identifier.IdentifierAs;
import org.contract_lib.lang.contract_lib.ast.Term.Identifier.IdentifierValue;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.jml.expr.JmlQuantifiedExpr.JmlBinder;

public interface TermTranslator {

  Expression translateTerm(
      Term term,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermSpecConstant(
      SpecConstant specCons,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermIdentifierAs(
      IdentifierAs idAs,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermIdentifierValue(
      IdentifierValue idV,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermMethodApplication(
      Term.MethodApplication methAppl,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermOld(
      Term.Old oldTerm,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermBooleanLiteral(
      Term.BooleanLiteral booleanLit,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermNumeralLiteral(
      Term.NumberLiteral numeralLit,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateTermLetBinding(
      Term.LetBinding letBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  JmlBinder translateBinder(Quantor quantor);

  Expression translateQuantorBinding(
      Term.QuantorBinding quantBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateMatchBinding(
      Term.MatchBinding matchBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric);

  Expression translateAttributes(
      Term.Attributes attributes,
      VariableTranslator variableScope,
      IndexFabric indexFabric);
}
