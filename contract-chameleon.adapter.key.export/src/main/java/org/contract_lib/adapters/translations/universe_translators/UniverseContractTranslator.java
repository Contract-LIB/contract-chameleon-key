package org.contract_lib.adapters.translations.universe_translators;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.adapters.translations.VariableScope;
import org.contract_lib.adapters.translations.VariableTranslator;
import org.contract_lib.adapters.translations.default_translators.DefaultContractTranslator;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.lang.contract_lib.ast.ArgumentMode;
import org.contract_lib.lang.contract_lib.ast.Formal;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.jml.clauses.JmlClause;
import com.github.javaparser.ast.jml.clauses.JmlClauseKind;
import com.github.javaparser.ast.jml.clauses.JmlSimpleExprClause;

public class UniverseContractTranslator extends DefaultContractTranslator {

  public UniverseContractTranslator(
      TypeTranslator typeTranslator,
      TermTranslator termTranslator,
      String footprintName,
      String contractLibResultLabel,
      String jmlResultLabel,
      String contractLibThisLabel,
      String jmlThisLabel,
      ChameleonMessageManager messageManager) {
    super(
        typeTranslator,
        termTranslator,
        footprintName,
        contractLibResultLabel,
        jmlResultLabel,
        contractLibThisLabel,
        jmlThisLabel,
        messageManager);
  }

  private static MethodCallExpr footprint(Expression expr) {
    return new MethodCallExpr(
        null,
        new SimpleName("\\dl_createdRepfp"),
        NodeList.nodeList(expr));
  }

  private static NameExpr nothing() {
    return new NameExpr(new SimpleName("\\nothing"));
  }

  // remove the `\fresh(\result, footprint)` clause
  @Override
  protected Optional<List<JmlClause>> objectCreated(List<Formal> formals, VariableTranslator variableScope) {
    return Optional.empty();
  }

  // remove the `\new_elems_fresh(footprint)` clause
  @Override
  protected Optional<JmlClause> newElementsFreshClause(List<Formal> formals, VariableTranslator variableScope) {
    return Optional.empty();
  }

  // there is no need for clauses that require disjointness of footprints
  @Override
  protected List<JmlClause> disjuntClauses(
      JmlClauseKind kind,
      List<Formal> formals,
      Predicate<ArgumentMode> filter,
      VariableTranslator variableScope) {
    return List.of();
  }

  @Override
  protected List<JmlClause> translateAccessible(
      List<Formal> formals,
      VariableTranslator variableScope) {

    return formals
        .stream()
        .filter((f) -> f.identifier().identifier().equals("this"))
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ACCESSIBLE, variableScope))
        .toList();
  }

  @Override
  protected List<JmlClause> translateAssignable(
      List<Formal> formals,
      VariableTranslator variableScope) {

    return formals
        .stream()
        .filter((f) -> f.identifier().identifier().equals("this"))
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ASSIGNABLE, variableScope))
        .toList();
  }

  @Override
  protected JmlClause translateAssignableAccessibleClause(
      Formal formal,
      JmlClauseKind kind,
      VariableTranslator variableScope) {

    Optional<VariableScope> variable = variableScope.translate(formal.identifier());
    Optional<Expression> variableExpression = variable.map(VariableScope::getJmlTerm);

    Expression expr = variableExpression.orElseGet(() -> {
      System.err.println(String.format("Identifier value not found: %s", formal.identifier()));
      //TODO: Provide proper translation
      return new BooleanLiteralExpr(false);
    });

    var fp = (kind == JmlClauseKind.ACCESSIBLE && this.isAccessible(formal.argumentMode()) ||
        kind == JmlClauseKind.ASSIGNABLE && this.isAssignable(formal.argumentMode()))
            ? footprint(expr)
            : nothing();

    return new JmlSimpleExprClause(kind, null, NodeList.nodeList(), fp);
  }
}
