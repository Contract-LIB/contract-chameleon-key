package org.contract_lib.adapters.translations.default_translators;

import static com.github.javaparser.ast.jml.clauses.JmlClauseKind.ASSIGNABLE;
import static com.github.javaparser.ast.jml.clauses.JmlClauseKind.ENSURES;
import static com.github.javaparser.ast.jml.clauses.JmlClauseKind.REQUIRES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.adapters.translations.ContractTranslation;
import org.contract_lib.adapters.translations.ContractTranslator;
import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.adapters.translations.VariableScope;
import org.contract_lib.adapters.translations.VariableTranslator;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.contract_chameleon.error.ChameleonReportable;
import org.contract_lib.lang.contract_lib.ast.ArgumentMode;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.Formal;
import org.contract_lib.lang.contract_lib.ast.PrePostPair;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.jml.clauses.JmlClause;
import com.github.javaparser.ast.jml.clauses.JmlClauseKind;
import com.github.javaparser.ast.jml.clauses.JmlContract;
import com.github.javaparser.ast.jml.clauses.JmlSimpleExprClause;
import com.github.javaparser.ast.stmt.Behavior;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

public class DefaultContractTranslator implements ContractTranslator {

  ChameleonMessageManager messageManager;

  private final TypeTranslator typeTranslator;
  private final TermTranslator termTranslator;

  private final String footprintName;
  private final String jmlResultLabel;
  private final String contractLibResultLabel;
  private final String contractLibThisLabel;
  private final String jmlThisLabel;

  public DefaultContractTranslator(
      TypeTranslator typeTranslator,
      TermTranslator termTranslator,
      String footprintName,
      String contractLibResultLabel,
      String jmlResultLabel,
      String contractLibThisLabel,
      String jmlThisLabel,
      ChameleonMessageManager messageManager) {
    this.typeTranslator = typeTranslator;
    this.termTranslator = termTranslator;
    this.footprintName = footprintName;
    this.contractLibResultLabel = contractLibResultLabel;
    this.jmlResultLabel = jmlResultLabel;
    this.contractLibThisLabel = contractLibThisLabel;
    this.jmlThisLabel = jmlThisLabel;
    this.messageManager = messageManager;
  }

  public ContractTranslation translate(Contract contract) {

    VariableScopeManager variableScope = getParameterScope(contract.formals());

    Optional<Type> returnT = variableScope.getReturnType();
    //TODO: Check that owner matches
    Optional<Type> ownerType = variableScope.getOwnerType();

    Type returnType = returnT.orElseGet(VoidType::new);

    List<ExpressionPair> clausePairs = contract.pairs()
        .stream()
        .map(t -> this.translateExprPair(t, variableScope))
        .collect(Collectors.toList());

    JmlSimpleExprClause preClause = new JmlSimpleExprClause()
        .setExpression(joinPreContracts(clausePairs))
        .setKind(REQUIRES);

    JmlSimpleExprClause postClause = new JmlSimpleExprClause()
        .setExpression(joinPostContracts(clausePairs))
        .setKind(ENSURES);

    List<JmlClause> disPreClause = disjuntClauses(
        REQUIRES,
        contract.formals(),
        this::isInPrecondition,
        variableScope);

    List<JmlClause> disPostClause = disjuntClauses(
        ENSURES,
        contract.formals(),
        this::isInPostcondition,
        variableScope);

    List<JmlClause> accessibleClause = translateAccessible(
        contract.formals(),
        variableScope);
    List<JmlClause> assignableClause = translateAssignable(
        contract.formals(),
        variableScope);

    Optional<JmlClause> nothingClauses = nothingClauses(contract.formals(), variableScope);

    NodeList<JmlClause> clauses = new NodeList<>();

    clauses.add(preClause);
    clauses.addAll(disPreClause);
    clauses.add(postClause);
    clauses.addAll(disPostClause);

    //add ensures clauses for new created object (invariant, fresh) 
    objectCreated(contract.formals(), variableScope).ifPresent(clauses::addAll);

    //allows all parameters that are `INOUT`, to have new objects created in their footprint
    newElementsFreshClause(contract.formals(), variableScope).ifPresent(clauses::add);

    if (variableScope.getReturnType().isPresent()) {
      clauses.addAll(accessibleClause);
    }
    clauses.addAll(assignableClause);

    if (assignableClause.isEmpty()) {
      nothingClauses.ifPresent(clauses::add);
    }

    JmlContract jmlContract = new JmlContract()
        .setBehavior(Behavior.NORMAL)
        .setClauses(clauses);

    NodeList<JmlContract> contracts = NodeList.nodeList(jmlContract);

    List<Parameter> parameters = variableScope.getParameters();

    List<Expression> args = parameters.stream().map(p -> new NameExpr(p.getNameAsString()))
        .collect(Collectors.toList());

    ContractTranslation contractTranslation = new ContractTranslation(
        contracts,
        NodeList.nodeList(args),
        NodeList.nodeList(parameters),
        returnType);

    return contractTranslation;
  }

  protected VariableScopeManager getParameterScope(List<Formal> formals) {
    VariableScopeManager variableScope = new VariableScopeManager(this.typeTranslator);

    for (Formal f : formals) {
      variableScope.add(f);
    }

    return variableScope;
  }

  protected boolean isInPrecondition(ArgumentMode am) {
    return am.equals(ArgumentMode.IN) | am.equals(ArgumentMode.INOUT);
  }

  protected boolean isInPostcondition(ArgumentMode am) {
    return true;
  }

  protected boolean isFootprintAssignable(ArgumentMode am) {
    return am.equals(ArgumentMode.INOUT);
  }

  protected boolean isAssignable(ArgumentMode am) {
    return am.equals(ArgumentMode.INOUT) || am.equals(ArgumentMode.OUT);
  }

  protected boolean isAccessible(ArgumentMode am) {
    return true;
  }

  protected boolean filterResult(Formal formal) {
    return !formal.identifier().identifier().equals("result");
  }

  protected boolean filterThis(Formal formal) {
    return !formal.identifier().identifier().equals("this");
  }

  protected boolean isReference(Formal formal, VariableTranslator variableScope) {
    Optional<VariableScope> variable = variableScope.translate(formal.identifier());
    VariableScope expr = variable.orElseGet(() -> {
      System.err.println(String.format("ERROR: Identifier value not found: %s", formal.identifier()));
      //TODO: Provide proper translation
      return new VariableScopeElement(null, null, null, null, false);
    });
    return expr.hasFootprint();
  }

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

    return new JmlSimpleExprClause(kind, null, NodeList.nodeList(),
        new FieldAccessExpr(
            expr,
            //new NameExpr(formal.identifier().identifier()),
            NodeList.nodeList(),
            new SimpleName(footprintName)));
  }

  protected List<JmlClause> translateAccessible(
      List<Formal> formals,
      VariableTranslator variableScope) {

    List<JmlClause> accessible = formals
        .stream()
        .filter((f) -> this.isAccessible(f.argumentMode()))
        .filter(this::filterResult) //do not put result statement to clause
        .filter((f) -> this.isReference(f, variableScope)) //do not put result statement to clause
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ACCESSIBLE, variableScope))
        .collect(Collectors.toList());

    return accessible;
  }

  protected List<JmlClause> translateAssignable(
      List<Formal> formals,
      VariableTranslator variableScope) {

    List<JmlClause> assignable = formals
        .stream()
        .filter((f) -> this.isFootprintAssignable(f.argumentMode()))
        .filter(this::filterResult) //do not put result statement to clause
        .filter((f) -> this.isReference(f, variableScope)) //do not put result statement to clause
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ASSIGNABLE, variableScope))
        .collect(Collectors.toList());

    return assignable;
  }

  protected List<JmlClause> translateConstructor(
      List<Formal> formals,
      VariableTranslator variableScope) {

    List<JmlClause> assignable = formals
        .stream()
        .filter((f) -> this.isFootprintAssignable(f.argumentMode()))
        .filter(this::filterThis) //do not put result statement to clause
        .filter((f) -> this.isReference(f, variableScope)) //do not put result statement to clause
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ASSIGNABLE, variableScope))
        .collect(Collectors.toList());

    return assignable;
  }

  protected Optional<JmlClause> nothingClauses(
      List<Formal> formals,
      VariableTranslator variableScope) {

    if (formals
        .stream()
        .filter((f) -> this.isAssignable(f.argumentMode()))
        .filter((f) -> this.isReference(f, variableScope))
        .findAny()
        .isEmpty()) {
      return Optional.of(
          new JmlSimpleExprClause(ASSIGNABLE, null, NodeList.nodeList(),
              new NameExpr(new SimpleName("\\strictly_nothing"))));
    } else if (formals
        .stream()
        .filter((f) -> this.isAssignable(f.argumentMode()))
        .filter(this::filterResult) //do not put result statement to clause
        .filter(this::filterThis) //do not put result statement to clause
        .filter((f) -> this.isReference(f, variableScope))
        .findAny()
        .isEmpty()) {
      return Optional.of(
          new JmlSimpleExprClause(ASSIGNABLE, null, NodeList.nodeList(),
              new NameExpr(new SimpleName("\\nothing"))));
    } else {
      return Optional.empty();
    }

  }

  protected List<JmlClause> disjuntClauses(
      JmlClauseKind kind,
      List<Formal> formals,
      Predicate<ArgumentMode> filter,
      VariableTranslator variableScope) {

    List<Expression> arguments = formals.stream()
        .filter((f) -> filter.test(f.argumentMode()))
        .filter((f) -> isReference(f, variableScope))
        .map(Formal::identifier)
        .map(variableScope::translate)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(VariableScope::getJmlTerm)
        .collect(Collectors.toList());

    List<Expression> pairArgument = new ArrayList<>(arguments);
    List<JmlClause> res = new ArrayList<>();
    if (pairArgument.isEmpty()) {
      return res;
    }
    for (Expression a : arguments) {
      pairArgument.removeFirst();
      for (Expression b : pairArgument) {
        res.add(
            new JmlSimpleExprClause(kind,
                null,
                NodeList.nodeList(),
                createDisjunctClause(a, b)));
      }
    }

    return res;
  }

  protected Optional<JmlClause> newElementsFreshClause(
      List<Formal> formals,
      VariableTranslator variableScope) {

    List<Expression> changeableFootprint = formals.stream()
        .filter((f) -> isReference(f, variableScope))
        .filter((f) -> isAssignable(f.argumentMode()))
        .map(Formal::identifier)
        .map(variableScope::translate)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(VariableScope::getJmlTerm)
        .toList();

    if (changeableFootprint.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new JmlSimpleExprClause(ENSURES,
        null,
        NodeList.nodeList(),
        new MethodCallExpr(null, new SimpleName("\\new_elems_fresh"),
            NodeList.nodeList(createUnionClause(changeableFootprint)))));
  }

  protected MethodCallExpr createDisjunctClause(Expression a, Expression b) {
    return new MethodCallExpr(null, new SimpleName("\\disjoint"),
        NodeList.nodeList(
            new FieldAccessExpr(a, footprintName),
            new FieldAccessExpr(b, footprintName)));
  }

  //NOTE: requries at leas one element
  protected Expression createUnionClause(List<Expression> expressions) {
    if (expressions.size() == 1) {
      return new FieldAccessExpr(expressions.getFirst(), footprintName);
    }
    Expression first = expressions.removeFirst();
    return new MethodCallExpr(null, new SimpleName("\\union"),
        NodeList.nodeList(
            new FieldAccessExpr(first, footprintName),
            createUnionClause(expressions)));
  }

  // - Object Creation

  protected Optional<List<JmlClause>> objectCreated(List<Formal> formals, VariableTranslator variableScope) {
    return formals.stream()
        .filter((f) -> isReference(f, variableScope))
        .filter((f) -> ArgumentMode.OUT.equals(f.argumentMode()))
        //.map(Formal::identifier)
        //.map(variableScope::translate)
        .findAny()
        .map(this::getObjectCreatedLabel);
  }

  private List<JmlClause> getObjectCreatedLabel(Formal formal) {
    if (formal.identifier().identifier().equals(this.contractLibResultLabel)) {
      return staticObjectCreated();
    } else if (formal.identifier().identifier().equals(this.contractLibThisLabel)) {
      return constructorObjectCreated();
    } else {
      this.messageManager.report(new ChameleonReportable() {
        @Override
        public String getMessage() {
          return String.format("Only `result` is allowed as identifier for an out parameter. Got: %s",
              formal.identifier().identifier());
        }
      });

      return List.of();
    }
  }

  private List<JmlClause> constructorObjectCreated() {
    NameExpr resultLabel = new NameExpr(jmlThisLabel);
    return List.of(
        ensuresInvariantClause(resultLabel),
        ensuresFreshClause(resultLabel));
  }

  private List<JmlClause> staticObjectCreated() {
    NameExpr resultLabel = new NameExpr(jmlResultLabel);
    return List.of(
        ensuresInvariantClause(resultLabel),
        ensuresFreshClause(resultLabel),
        ensuresNotNullClause(resultLabel));
  }

  private JmlClause ensuresNotNullClause(NameExpr name) {
    return new JmlSimpleExprClause(ENSURES, null,
        NodeList.nodeList(),
        new BinaryExpr(new NullLiteralExpr(), name, Operator.NOT_EQUALS));
  }

  private JmlClause ensuresInvariantClause(NameExpr name) {
    return new JmlSimpleExprClause(ENSURES, null,
        NodeList.nodeList(),
        new MethodCallExpr(null, new SimpleName("\\invariant_for"),
            NodeList.nodeList(name)));
  }

  private JmlClause ensuresFreshClause(NameExpr name) {
    return new JmlSimpleExprClause(ENSURES, null,
        NodeList.nodeList(),
        new MethodCallExpr(null, new SimpleName("\\fresh"),
            NodeList.nodeList(new FieldAccessExpr(name, footprintName))));
  }

  // - Contract Pair Translation

  protected ExpressionPair translateExprPair(PrePostPair pair, VariableTranslator scope) {
    IndexFabric preFab = new DefaultIndexFabric();
    IndexFabric postFab = new DefaultIndexFabric();
    //TODO: deep copy of scope?
    return new ExpressionPair(
        termTranslator.translateTerm(pair.pre(), scope, preFab),
        termTranslator.translateTerm(pair.post(), scope, postFab));
  }

  protected Expression joinPreContracts(List<ExpressionPair> contracts) {
    if (contracts.size() == 0) {
      return new BooleanLiteralExpr(true);
    }
    if (contracts.size() == 1) {
      return contracts.getFirst().pre();
    }

    return contracts
        .stream()
        .map(ExpressionPair::pre)
        .collect(Collectors.reducing(new BooleanLiteralExpr(false), this::mergeOr));
  }

  protected Expression joinPostContracts(List<ExpressionPair> contracts) {

    if (contracts.size() == 0) {
      return new BooleanLiteralExpr(true);
    }
    if (contracts.size() == 1) {
      return contracts.getFirst().post();
    }

    return contracts
        .stream()
        .map(this::createPostCond)
        .collect(Collectors.reducing(new BooleanLiteralExpr(true), this::mergeAnd));
  }

  protected Expression createPostCond(ExpressionPair pair) {
    return mergeExpression(
        pair.pre(),
        pair.post(),
        BinaryExpr.Operator.IMPLICATION);
  }

  protected Expression mergeExpression(Expression left, Expression right, BinaryExpr.Operator op) {
    return new BinaryExpr(new EnclosedExpr(left), new EnclosedExpr(right), op);
  }

  protected Expression mergeOr(Expression left, Expression right) {
    return mergeExpression(left, right, BinaryExpr.Operator.OR);
  }

  protected Expression mergeAnd(Expression left, Expression right) {
    return mergeExpression(left, right, BinaryExpr.Operator.OR);
  }

  protected static record ExpressionPair(
      Expression pre,
      Expression post) {
  }
}
