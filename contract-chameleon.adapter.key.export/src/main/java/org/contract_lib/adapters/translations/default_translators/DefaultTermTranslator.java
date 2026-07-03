package org.contract_lib.adapters.translations.default_translators;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.adapters.translations.FuncTranslator;
import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.VariableScope;
import org.contract_lib.adapters.translations.VariableScopeManager;
import org.contract_lib.adapters.translations.VariableTranslator;
import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.lang.contract_lib.ast.Quantor;
import org.contract_lib.lang.contract_lib.ast.SortedVar;
import org.contract_lib.lang.contract_lib.ast.Term;
import org.contract_lib.lang.contract_lib.ast.Term.Attributes;
import org.contract_lib.lang.contract_lib.ast.Term.MatchBinding;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.jml.expr.JmlQuantifiedExpr;

public class DefaultTermTranslator implements TermTranslator {

  private final FuncTranslator funcTranslator;
  private final TypeTranslator sortTranslator;

  public DefaultTermTranslator(
      TypeTranslator typeTranslator,
      FuncTranslator funcTranslator) {
    this.funcTranslator = funcTranslator;
    this.sortTranslator = typeTranslator;
  }

  public Expression translateTerm(
      Term term,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    return term.perform(
        t -> this.translateTermSpecConstant(t, variableScope, indexFabric),
        t -> this.translateTermIdentifierAs(t, variableScope, indexFabric),
        t -> this.translateTermIdentifierValue(t, variableScope, indexFabric),
        t -> this.translateTermMethodApplication(t, variableScope, indexFabric),
        t -> this.translateTermOld(t, variableScope, indexFabric),
        t -> this.translateTermBooleanLiteral(t, variableScope, indexFabric),
        t -> this.translateTermNumeralLiteral(t, variableScope, indexFabric),
        t -> this.translateTermLetBinding(t, variableScope, indexFabric),
        t -> this.translateQuantorBinding(t, variableScope, indexFabric),
        t -> this.translateMatchBinding(t, variableScope, indexFabric),
        t -> this.translateAttributes(t, variableScope, indexFabric));
  }

  public Expression translateTermSpecConstant(
      Term.SpecConstant specCons,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    System.err.println(String.format("Specific constants as are not supported yet: %s", specCons));
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  public Expression translateTermIdentifierAs(
      Term.Identifier.IdentifierAs idAs,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    System.err.println(String.format("As Terms are not supported yet: %s", idAs));
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  public Expression translateTermIdentifierValue(
      Term.Identifier.IdentifierValue idV,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    Optional<VariableScope> variable = variableScope.translate(idV.identifier());
    Optional<Expression> variableExpression = variable.map(VariableScope::getJmlTerm);

    return variableExpression.orElseGet(() -> {
      FuncTranslation trns = funcTranslator.translate(idV);
      if (trns == null) {
        System.err.println(String.format("Identifier value not found: %s", idV));
        return new BooleanLiteralExpr(false);
      }
      return trns.getJmlTerm(List.of());
    });
  }

  public Expression translateTermMethodApplication(
      Term.MethodApplication methAppl,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    Term.Identifier.IdentifierValue i = methAppl.identifier().getValue();

    List<Expression> terms = methAppl.parameters().stream()
        .map(t -> this.translateTerm(t, variableScope, indexFabric))
        .collect(Collectors.toList());

    FuncTranslation trans = funcTranslator.translate(i);

    //TODO: Use cleaner Optional syntax
    if (trans == null) {
      return new BooleanLiteralExpr(false);
    }

    return trans.getJmlTerm(terms);
  }

  public Expression translateTermOld(
      Term.Old oldTerm,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    Expression content = translateTerm(oldTerm.argument(), variableScope, indexFabric);

    return new MethodCallExpr(
        null,
        "\\old",
        NodeList.nodeList(content));
  }

  public Expression translateTermBooleanLiteral(
      Term.BooleanLiteral booleanLit,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    return new BooleanLiteralExpr(booleanLit.value());
  }

  public Expression translateTermNumeralLiteral(
      Term.NumberLiteral numeralLit,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    //TODO: Provide proper translation
    //One might to differentiate between (concider Type!)
    // - binary / hexadecimal numeral -> LongLiteral? / IntLiteral?, 
    // - decimal -> double / float
    return new DoubleLiteralExpr(numeralLit.value());
  }

  public Expression translateTermLetBinding(
      Term.LetBinding letBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    System.err.println("Let expressions are not supported yet.");
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  public JmlQuantifiedExpr.JmlBinder translateBinder(Quantor quantor) {
    return switch (quantor) {
      case Quantor.ALL -> JmlQuantifiedExpr.JmlDefaultBinder.FORALL;
      case Quantor.EXISTS -> JmlQuantifiedExpr.JmlDefaultBinder.EXISTS;
    };
  }

  public Expression translateQuantorBinding(
      Term.QuantorBinding quantBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    //TODO: Sort varaibles by type, only create one quantor per type.

    JmlQuantifiedExpr.JmlBinder binder = translateBinder(quantBind.quantor());
    JmlQuantifiedExpr top = null;
    JmlQuantifiedExpr last = null;
    JmlQuantifiedExpr act = null;

    for (SortedVar sv : quantBind.formals()) {
      TypeTranslation t = sortTranslator.translate(sv.sort());
      act = new JmlQuantifiedExpr();
      act.setBinder(binder);
      act.setVariables(NodeList.nodeList(
          new Parameter(
              t.getJmlType(sv.sort()),
              new SimpleName(sv.symbol().identifier()))));

      //Add the bounded variable to the scope
      ((VariableScopeManager) variableScope).add(sv);
      //TODO: remoe ugly typecasting

      if (top == null) {
        top = act;
      } else {
        last.setExpressions(NodeList.nodeList(act));
      }
      last = act;
    }

    Expression e = translateTerm(quantBind.term(), variableScope, indexFabric);

    //remove the bounded variables from the scope
    for (SortedVar sv : quantBind.formals()) {
      //TODO: remoe ugly typecasting
      ((VariableScopeManager) variableScope).remove(sv);
    }

    act.setExpressions(NodeList.nodeList(e));

    return top;
  }

  public Expression translateMatchBinding(
      MatchBinding matchBind,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    //TODO: Provide proper translation
    System.err.println("Match expressions are not supported yet.");
    return new BooleanLiteralExpr(false);
  }

  public Expression translateAttributes(
      Attributes attributes,
      VariableTranslator variableScope,
      IndexFabric indexFabric) {
    //TODO: Provide proper error 
    System.err.println("Attributes are not supported yet and are ignored.");
    return translateTerm(attributes.term(), variableScope, indexFabric);
  }
}
