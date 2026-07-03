
package org.contract_lib.adapters.translations.contracts;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.contract_lib.adapters.translations.terms.ImportTermTranslator;
import org.contract_lib.adapters.translations.types.ImportTypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.Formal;
import org.contract_lib.lang.contract_lib.ast.PrePostPair;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Symbol;
import org.contract_lib.lang.contract_lib.ast.Term;
import org.contract_lib.lang.contract_lib.ast.Term.BooleanLiteral;
import org.contract_lib.lang.contract_lib.ast.Term.Identifier;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.jml.clauses.JmlClause;
import com.github.javaparser.ast.jml.clauses.JmlClauseKind;
import com.github.javaparser.ast.jml.clauses.JmlContract;
import com.github.javaparser.ast.jml.clauses.JmlSimpleExprClause;
import com.github.javaparser.ast.stmt.Behavior;

public final class ConstructorContractTranslation implements ImportContractTranslation {

  private final ImportTermTranslator termTranslator;
  private final ContractArgumentsExtractor contractArgumentsExtractor;

  public ConstructorContractTranslation(
      ContractArgumentsExtractor contractArgumentsExtractor,
      ImportTermTranslator termTranslator) {
    this.termTranslator = termTranslator;
    this.contractArgumentsExtractor = contractArgumentsExtractor;
  }

  public Optional<Contract> translate(
      Sort classSort,
      String methodName,
      MethodDeclaration methodDeclaration) {
    if (!methodDeclaration.isStatic()) {
      return Optional.empty();
    }

    Optional<JmlContract> cl = Optional.ofNullable(methodDeclaration.contracts().getFirst());

    if (cl.isEmpty()) {
      System.err.println("At least one contract is required per method.");
      return Optional.empty();
    }
    if (methodDeclaration.contracts().size() > 1) {
      System.err.println("Multiple contracts per method are not supported yet.");
      return Optional.empty();
    }

    List<JmlClause> accessibleClauses = extractAccessibleClauses(cl.get());
    List<JmlClause> assignableClauses = extractAssignableClause(cl.get());

    List<Formal> formals = this.contractArgumentsExtractor.extractArguments(
        methodDeclaration.getParameters(),
        methodDeclaration.getType(),
        accessibleClauses,
        assignableClauses);

    List<PrePostPair> pairs = methodDeclaration.contracts()
        .stream()
        .map(this::translateContract)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    if (pairs.isEmpty()) {
      //TODO:
      System.err.println("No pre, post condition pair in contract.");
      Optional.empty();
    }

    System.err.println("Constructor method found: " + methodName);

    return Optional.of(
        new Contract(new Symbol(methodName), formals, pairs));
  }

  private Optional<PrePostPair> translateContract(JmlContract contract) {
    boolean success = true;
    if (!contract.behavior().equals(Behavior.NORMAL)) {
      success = false;
    }

    Term requiresTerm = contract.clauses()
        .stream()
        .filter(this::filterRequiresClauses)
        .map(this::translateClause)
        .collect(this.merge());

    Term ensuresTerm = contract.clauses()
        .stream()
        .filter(this::filterEnsuresClauses)
        .map(this::translateClause)
        .collect(this.merge());

    contract.clauses()
        .stream()
        .filter(Predicate.not(this::filterRequiresClauses))
        .filter(Predicate.not(this::filterEnsuresClauses))
        .filter(Predicate.not(this::filterAccessibleClauses))
        .filter(Predicate.not(this::filterAssignableClauses))
        .forEach(this::printUnknownClause);

    //TODO: check predicate satisfaction for arguments
    //TODO: check for fresh statements

    if (success) {
      return Optional.of(new PrePostPair(requiresTerm, ensuresTerm));
    } else {
      return Optional.empty();
    }
  }

  private Term translateClause(JmlClause clause) {
    return clause.toJmlSimpleExprClause()
        .flatMap(this::translateJmlSimpleExprClause)
        .orElseGet(() -> new BooleanLiteral(true));
  }

  private Optional<Term> translateJmlSimpleExprClause(JmlSimpleExprClause simpleExprClause) {
    return this.termTranslator.translate(simpleExprClause.expression());
  }

  private boolean filterRequiresClauses(JmlClause clause) {
    return clause.getKind().equals(JmlClauseKind.REQUIRES);
  }

  private boolean filterEnsuresClauses(JmlClause clause) {
    return clause.getKind().equals(JmlClauseKind.ENSURES);
  }

  private boolean filterAccessibleClauses(JmlClause clause) {
    return clause.getKind().equals(JmlClauseKind.ACCESSIBLE);
  }

  private boolean filterAssignableClauses(JmlClause clause) {
    return clause.getKind().equals(JmlClauseKind.ASSIGNABLE);
  }

  private void printUnknownClause(JmlClause clause) {
    System.err.println("Unknown Clause found: " + clause);
  }

  private List<JmlClause> extractAccessibleClauses(JmlContract contract) {
    return contract.clauses()
        .stream()
        .filter(this::filterAccessibleClauses)
        .collect(Collectors.toList());
  }

  private List<JmlClause> extractAssignableClause(JmlContract contract) {
    return contract.clauses()
        .stream()
        .filter(this::filterAssignableClauses)
        .collect(Collectors.toList());
  }

  public Collector<Term, ?, Boolean> check(Predicate<Term> check) {
    return Collector.of(
        () -> true,
        (b, t) -> Boolean.logicalAnd(b, check.test(t)),
        Boolean::logicalAnd);
  }

  public Collector<Term, ?, Term> merge() {
    return Collectors.collectingAndThen(Collectors.toList(),
        this::createAnd);
  }

  public Term createAnd(List<Term> terms) {
    if (terms.isEmpty()) {
      return new Term.BooleanLiteral(true);
    }
    if (terms.size() == 1) {
      return terms.get(0);
    }
    Symbol sym = new Symbol("and");
    Identifier id = new Identifier.IdentifierValue(sym);
    return new Term.MethodApplication(id, terms);
  }
}
