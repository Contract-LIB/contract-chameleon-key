package org.contract_lib.adapters;

import java.io.Writer;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.github.javaparser.printer.DefaultPrettyPrinter;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Behavior;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;

import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import com.github.javaparser.ast.jml.body.JmlFieldDeclaration;
import com.github.javaparser.ast.jml.body.JmlClassAccessibleDeclaration;
import com.github.javaparser.ast.jml.body.JmlClassExprDeclaration;

import com.github.javaparser.ast.jml.clauses.JmlClauseKind;
import com.github.javaparser.ast.jml.clauses.JmlSimpleExprClause;
import com.github.javaparser.ast.jml.clauses.JmlContract;
import com.github.javaparser.ast.jml.clauses.JmlClause;

import com.github.javaparser.ast.jml.expr.JmlQuantifiedExpr;

import static com.github.javaparser.ast.jml.clauses.JmlClauseKind.REQUIRES;
import static com.github.javaparser.ast.jml.clauses.JmlClauseKind.ENSURES;

import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.google.common.base.Predicate;

import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.ast.Abstraction;
import org.contract_lib.lang.contract_lib.ast.Constructor;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.Term;
import org.contract_lib.lang.contract_lib.tools.JavaMethodSignaturExtractor;
import org.contract_lib.lang.contract_lib.ast.SelectorDec;
import org.contract_lib.lang.contract_lib.ast.Symbol;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Formal;
import org.contract_lib.lang.contract_lib.ast.PrePostPair;
import org.contract_lib.lang.contract_lib.ast.SortedVar;
import org.contract_lib.lang.contract_lib.ast.Quantor;
import org.contract_lib.lang.contract_lib.ast.ArgumentMode;
import org.contract_lib.lang.contract_lib.ast.DatatypeDec;

import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.types.AbstractionTranslation;
import org.contract_lib.adapters.translations.types.LogicTypeTranslation;
import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.adapters.translations.KeyTranslations;

import org.contract_lib.adapters.translations.VariableScope;
import org.contract_lib.adapters.translations.functions.FieldAccessTranslation;
import static org.contract_lib.adapters.translations.VariableScope.VariableTranslator;

/// This class only supprts one abstraction at a time
/// It was mainly build for experimental purposes to find out,
/// what are suitable abstractions and interfaces used in a translation.
public class SimpleKeyProviderTranslator {

  private static final String IMPLEMENTATION_SUFFIX = "Impl";

  private ChameleonMessageManager messageManager;
  private KeyTranslations keyTranslator;

  public SimpleKeyProviderTranslator(
      ChameleonMessageManager manager) {
    this.messageManager = manager;

    this.keyTranslator = new KeyTranslations(
        manager,
        sortTranslator,
        KeyTranslations.ToDatatypeTranslation::new
    //KeyTranslations.ToSortTranslation::new
    );

    ServiceLoader<TypeTranslation> typeLoader = ServiceLoader.load(
        TypeTranslation.class,
        ClassLoader.getSystemClassLoader());

    //NOTE: AbstractionTranslations are created when abstraction is found
    typeLoader.forEach(sortTranslator::store);

    ServiceLoader<FuncProvider> funcLoader = ServiceLoader.load(
        FuncProvider.class,
        ClassLoader.getSystemClassLoader());

    System.err.println(funcLoader.stream().count());

    funcLoader.forEach(f -> funcTranslator.store(f.getAll()));
  }

  record SimpleResult(
      String packageName,
      String className,
      CompilationUnit cu) implements TranslationResult {

    public String directoryName() {
      return packageName;
    }

    public String fileName() {
      return className;
    }

    public String fileEnding() {
      return ".java";
    }

    public void write(Writer writer) throws IOException {
      PrinterConfiguration config = new DefaultPrinterConfiguration();
      DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
      writer.write(printer.print(cu));
    }
  }

  private record SortTranslator(
      Map<String, TypeTranslation> map) implements TypeTranslation.TypeTranslator {
    public void store(TypeTranslation trans) {
      //TODO: Do error check if translation is ambigous
      this.map().put(trans.getClibSort().getName(), trans);
    }

    public TypeTranslation translate(Sort sort) {
      TypeTranslation translation = this.map().get(sort.getName());
      if (translation == null) {
        //TODO: Report proper error
        System.err.println(String.format("ERROR: The type '%s' could not be translated.", sort));
        return null;
      }
      return translation;
    }
  }

  private record FuncTranslator(
      Map<String, FuncTranslation> map) implements FuncTranslation.FuncTranslator {

    public void store(List<FuncTranslation> translations) {
      //TODO: Error check if ambigous
      for (FuncTranslation trans : translations) {
        this.map().put(trans.getClibIdentifier().identifier().identifier(), trans);
      }
    }

    public FuncTranslation translate(Term.Identifier.IdentifierValue identifier) {
      FuncTranslation t = this.map().get(identifier.identifier().identifier());
      if (t == null) {
        System.err.println(String.format("ERROR: Translations for the identifier '%s' not found.", identifier));
        return null;
      }
      return t;
    }
  }

  //TODO: This is just a most simple implementation, might be reworked, but serves it purpose for now
  private final class VariableScopeManager implements VariableScope.VariableTranslator {

    Map<String, VariableScope> map = new HashMap<>();
    Optional<Type> returnType = Optional.empty();
    Optional<Type> ownerType = Optional.empty();
    List<Parameter> parameters = new ArrayList<>();

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
            new NameExpr("\\result"),
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
  }

  private record VariableScopeElement(
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

  private final class IndexFab implements TypeTranslation.IndexFabric {
    private char i = 'a';

    public SimpleName getNextIndex() {
      char res = i;
      i++;
      if (i > 'z') {
        //TODO: report Error
        i = 'a';
      }
      return new SimpleName(String.valueOf(res));
    }
  }

  //Map storing available translations for sorts
  SortTranslator sortTranslator = new SortTranslator(new HashMap<>());

  //Map storing availabel translations for func symbols
  FuncTranslator funcTranslator = new FuncTranslator(new HashMap<>());

  //This map stores the abstract class definitions generated from the abstractions 
  Map<String, ClassOrInterfaceDeclaration> abstractAbstractions = new HashMap<>();

  //This map stores the class implementation blueprints generated from the abstractions 
  Map<String, ClassOrInterfaceDeclaration> abstractionImpementations = new HashMap<>();

  List<TranslationResult> results = new ArrayList<>();

  List<TranslationResult> translateContractLibAstApplicant(ContractLibAst ast) {

    results.add(keyTranslator.translate(ast));
    keyTranslator.getSorts().stream()
        .map(LogicTypeTranslation::new)
        .forEach(sortTranslator::store);

    funcTranslator.store(keyTranslator.getCons());

    //Creates the list of all provided abstractions
    ast.abstractions()
        .stream()
        .forEach(this::translateAbstractionApplicant);

    //Populates all abstractions with their contracts
    ast.contracts()
        .stream()
        .forEach(this::translateContract);

    //TODO: create example main class

    return results;
  }

  List<TranslationResult> translateContractLibAstProvider(ContractLibAst ast) {

    results.add(keyTranslator.translate(ast));
    keyTranslator.getSorts().stream()
        .map(LogicTypeTranslation::new)
        .forEach(sortTranslator::store);

    funcTranslator.store(keyTranslator.getCons());

    //Creates the list of all provided abstractions
    ast.abstractions()
        .stream()
        .forEach(this::translateAbstractionProvider);

    //Populates all abstractions with their contracts
    ast.contracts()
        .stream()
        .forEach(this::translateContract);

    return results;
  }

  private SimpleName getName(SelectorDec selector) {
    return new SimpleName(selector.symbol().identifier());
  }

  private boolean abstractionBuilder(
      Abstraction abstraction,
      ClassOrInterfaceDeclaration dec) {
    DatatypeDec dt = abstraction.datatypeDec();

    if (dt.constructors().size() != 1) {
      System.err.println("Only datatypes with one constructor are allowed at the moment.");
      return false;
    }
    Constructor cons = dt.constructors().get(0);

    addAbstractionFootprint(dec);
    addAccessibleDef(dec, cons.selectors());

    addGhostFields(dec, cons.selectors());

    return true;
  }

  private void addGhostFields(ClassOrInterfaceDeclaration dec, List<SelectorDec> selectors) {
    selectors
        .stream()
        .forEach(s -> addGhostField(dec, s));
  }

  private void addGhostField(ClassOrInterfaceDeclaration dec, SelectorDec selector) {

    TypeTranslation translation = sortTranslator.translate(selector.sort());

    FieldDeclaration fieldDec = new FieldDeclaration(
        NodeList.nodeList(),
        translation.getJmlType(selector.sort()),
        selector.symbol().identifier())
        .setPublic(true)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE)
        .addModifier(Modifier.DefaultKeyword.JML_GHOST);

    JmlFieldDeclaration jmlFieldDec = new JmlFieldDeclaration(
        NodeList.nodeList(),
        fieldDec);

    dec.addMember(jmlFieldDec);

    funcTranslator.store(List.of(new FieldAccessTranslation(
        new Term.Identifier.IdentifierValue(selector.symbol()),
        null, //TODO: Add types
        null,
        null,
        null)));

    //Helper
    IndexFab fab = new IndexFab();

    translation.getHelper(
        new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), getName(selector)),
        selector.sort(),
        sortTranslator,
        fab)
        .stream()
        .map(e -> new JmlClassExprDeclaration(
            NodeList.nodeList(), //JML Tags
            NodeList.nodeList(), //Modifier
            new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
            null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
            e)
            .addModifier(Modifier.DefaultKeyword.PUBLIC)
            .addModifier(Modifier.DefaultKeyword.JML_INSTANCE))
        .forEach((i) -> dec.addMember(i));

    //Footprint invariants for ghost fields that are reference types.
    IndexFab footprintIndexfab = new IndexFab();

    translation.getFootprintInvariant(
        new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName(selector.symbol().identifier())),
        selector.sort(),
        sortTranslator,
        footprintIndexfab).ifPresent(
            footprintInv -> dec.addMember(
                new JmlClassExprDeclaration(
                    NodeList.nodeList(), //JML Tags
                    NodeList.nodeList(), //Modifier
                    new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
                    null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
                    footprintInv)
                    .addModifier(Modifier.DefaultKeyword.PUBLIC)
                    .addModifier(Modifier.DefaultKeyword.JML_INSTANCE)));
  }

  private void addAbstractionFootprint(ClassOrInterfaceDeclaration dec) {

    FieldDeclaration fieldDec = new FieldDeclaration(
        NodeList.nodeList(),
        new ClassOrInterfaceType(
            null,
            new SimpleName("\\locset"),
            null),
        //new JmlLogicType(JmlLogicType.Primitive.SET), 
        "footprint")
        .setPublic(true)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE)
        .addModifier(Modifier.DefaultKeyword.JML_GHOST);
    //.addModifier(Modifier.DefaultKeyword.JML_MODEL);

    JmlFieldDeclaration jmlFieldDec = new JmlFieldDeclaration(
        NodeList.nodeList(),
        fieldDec);

    dec.addMember(jmlFieldDec);

    // All ghost fields of this class are part of the footprint footprint
    JmlClassExprDeclaration footprintInv = new JmlClassExprDeclaration(
        NodeList.nodeList(), //JML Tags
        NodeList.nodeList(), //Modifier
        new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
        null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\subset"),
            NodeList.nodeList(
                new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName("*")),
                new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName("footprint")))))
        .addModifier(Modifier.DefaultKeyword.PUBLIC)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE);

    dec.addMember(footprintInv);

  }

  private void addAccessibleDef(ClassOrInterfaceDeclaration dec, List<SelectorDec> selector) {

    /*
    // The footprint invariant may only depend on itself
    JmlClassAccessibleDeclaration accessFootprint = new JmlClassAccessibleDeclaration(
        NodeList.nodeList(),
        NodeList.nodeList(),
        new NameExpr(new SimpleName("footprint")),
        NodeList.nodeList(
            new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName("footprint"))),
        null //Measured by
    )
        .addModifier(Modifier.DefaultKeyword.PUBLIC)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE);
    
    dec.addMember(accessFootprint);
    */

    // All invariants have to relay on footprint
    JmlClassAccessibleDeclaration accessInv = new JmlClassAccessibleDeclaration(
        NodeList.nodeList(),
        NodeList.nodeList(),
        new NameExpr(new SimpleName("\\inv")),
        NodeList.nodeList(
            new NameExpr(new SimpleName("footprint"))),
        null //Measured by
    )
        .addModifier(Modifier.DefaultKeyword.PUBLIC)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE);

    dec.addMember(accessInv);
  }

  private void addImplementationFootprint(ClassOrInterfaceDeclaration dec, Abstraction abstraction) {
    DatatypeDec dt = abstraction.datatypeDec();
    if (dt.constructors().size() != 1) {
      System.err.println("Only datatypes with one constructor are allowed.");
      return;
    }
    Constructor cons = dt.constructors().get(0);

    List<SelectorDec> sel = cons.selectors();

    NodeList<Expression> components = new NodeList<>(
        sel.stream().map(selector -> new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), getName(selector)))
            .collect(Collectors.toList()));

    //TODO: Add footprints of components that are reference types, not value types.

    /*
    JmlRepresentsDeclaration represents = new JmlRepresentsDeclaration(
        NodeList.nodeList(),
        NodeList.nodeList(),
        new Name("footprint"),
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\set_union"),
            components))
        .addModifier(Modifier.DefaultKeyword.PRIVATE);
    dec.addMember(represents);
    */
  }

  private void createAbstractClass(
      Abstraction abstraction,
      String packageName,
      String className) {
    // Abstract Classe Definition
    CompilationUnit abstractCompUnit = new CompilationUnit();
    abstractCompUnit.setPackageDeclaration(packageName);

    ClassOrInterfaceDeclaration abstractClassDeclaration = abstractCompUnit
        .addClass(className)
        .setPublic(true)
        .setAbstract(true);

    if (!abstractionBuilder(abstraction, abstractClassDeclaration)) {
      System.err.println("Abort abstraction translation.");
      return;
    }

    sortTranslator.store(new AbstractionTranslation(className));
    abstractAbstractions.put(getIdentifier(packageName, className), abstractClassDeclaration);

    SimpleResult resAbs = new SimpleResult(
        packageName,
        className,
        abstractCompUnit);

    results.add(resAbs);
  }

  private void createImplementationClass(
      Abstraction abstraction,
      String packageName,
      String className) {
    // Implementation Classe Definition
    CompilationUnit implComplUnit = new CompilationUnit();
    implComplUnit.setPackageDeclaration(packageName);

    ClassOrInterfaceType parentType = new ClassOrInterfaceType(className);

    ClassOrInterfaceDeclaration classImpl = implComplUnit
        .addClass(className + IMPLEMENTATION_SUFFIX)
        .setPublic(true)
        .addExtendedType(parentType);

    addImplementationFootprint(classImpl, abstraction);

    abstractionImpementations.put(getIdentifier(packageName, className), classImpl);

    SimpleResult resImpl = new SimpleResult(
        packageName,
        className + IMPLEMENTATION_SUFFIX,
        implComplUnit);

    results.add(resImpl);
  }

  private void translateAbstractionApplicant(
      Abstraction abstraction) {
    //TODO: Extract package name from abstraction or report warning
    String packageName = packageName(abstraction);
    String className = className(abstraction);

    System.err.println("Abstr Dec: " + getIdentifier(packageName, className));

    createAbstractClass(abstraction, packageName, className);
  }

  private String getIdentifier(String packageName, String className) {
    return packageName + "." + className;
  }

  private void translateAbstractionProvider(Abstraction abstraction) {

    //TODO: Extract package name from abstraction or report warning
    String packageName = packageName(abstraction);
    String className = className(abstraction);

    System.err.println("Abstr Dec: " + getIdentifier(packageName, className));

    createAbstractClass(abstraction, packageName, className);
    createImplementationClass(abstraction, packageName, className);
  }

  private ExpressionPair translateExprPair(PrePostPair pair, VariableTranslator scope) {
    IndexFab preFab = new IndexFab();
    IndexFab postFab = new IndexFab();
    //TODO: deep copy of scope?
    return new ExpressionPair(
        translateTerm(pair.pre(), scope, preFab),
        translateTerm(pair.post(), scope, postFab));
  }

  private Expression joinPreContracts(List<ExpressionPair> contracts) {
    return contracts
        .stream()
        .map(ExpressionPair::pre)
        .collect(Collectors.reducing(new BooleanLiteralExpr(false), this::mergeOr));
  }

  private Expression joinPostContracts(List<ExpressionPair> contracts) {
    return contracts
        .stream()
        .map(this::createPostCond)
        .collect(Collectors.reducing(new BooleanLiteralExpr(true), this::mergeAnd));
  }

  private Expression createPostCond(ExpressionPair pair) {
    return mergeExpression(
        pair.pre(),
        pair.post(),
        BinaryExpr.Operator.IMPLICATION);
  }

  private Expression mergeExpression(Expression left, Expression right, BinaryExpr.Operator op) {
    return new BinaryExpr(new EnclosedExpr(left), new EnclosedExpr(right), op);
  }

  private Expression mergeOr(Expression left, Expression right) {
    return mergeExpression(left, right, BinaryExpr.Operator.OR);
  }

  //TODO: Merge as separate clauses
  private Expression mergeAnd(Expression left, Expression right) {
    return mergeExpression(left, right, BinaryExpr.Operator.OR);
  }

  //TODO: Compute those in one Function, with checks 
  private String packageName(Abstraction abstraction) {
    //TODO: To proper error testing
    String[] split = abstraction.identifier().name().identifier().split("\\.");
    String[] copy = Arrays.copyOf(split, split.length - 1);

    return String.join(".", copy);
  }

  private String className(Abstraction abstraction) {
    //TODO: To proper error testing
    String[] split = abstraction.identifier().name().identifier().split("\\.");
    return split[split.length - 1];
  }

  private VariableScopeManager getParameterScope(List<Formal> formals) {
    VariableScopeManager variableScope = new VariableScopeManager();

    for (Formal f : formals) {
      variableScope.add(f);
    }

    return variableScope;
  }

  private boolean isInPrecondition(ArgumentMode am) {
    return am.equals(ArgumentMode.IN) | am.equals(ArgumentMode.INOUT);
  }

  private boolean isInPostcondition(ArgumentMode am) {
    return true;
  }

  private boolean isAssignable(ArgumentMode am) {
    return am.equals(ArgumentMode.OUT) | am.equals(ArgumentMode.INOUT);
  }

  private boolean isAccessible(ArgumentMode am) {
    return true;
  }

  private boolean filterResult(Formal formal) {
    return !formal.identifier().identifier().equals("result");
  }

  private boolean isReference(Formal formal, VariableTranslator variableScope) {
    Optional<VariableScope> variable = variableScope.translate(formal.identifier());
    VariableScope expr = variable.orElseGet(() -> {
      System.err.println(String.format("ERROR: Identifier value not found: %s", formal.identifier()));
      //TODO: Provide proper translation
      return new VariableScopeElement(null, null, null, null, false);
    });
    return expr.hasFootprint();
  }

  private JmlClause translateAssignableAccessibleClause(
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
            new SimpleName("footprint")));
  }

  private List<JmlClause> translateAccessible(
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

  private List<JmlClause> translateAssignable(
      List<Formal> formals,
      VariableTranslator variableScope) {

    List<JmlClause> assignable = formals
        .stream()
        .filter((f) -> this.isAssignable(f.argumentMode()))
        .filter(this::filterResult) //do not put result statement to clause
        .filter((f) -> this.isReference(f, variableScope)) //do not put result statement to clause
        .map((s) -> this.translateAssignableAccessibleClause(s, JmlClauseKind.ASSIGNABLE, variableScope))
        .collect(Collectors.toList());

    return assignable;
  }

  private List<JmlClause> disjuntClauses(
      JmlClauseKind kind,
      List<Formal> formals,
      Predicate<ArgumentMode> filter,
      VariableTranslator variableScope) {

    List<Expression> arguments = formals.stream()
        .filter((f) -> filter.apply(f.argumentMode()))
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

  private MethodCallExpr createDisjunctClause(Expression a, Expression b) {
    return new MethodCallExpr(null, new SimpleName("\\disjoint"),
        NodeList.nodeList(
            new FieldAccessExpr(a, "footprint"),
            new FieldAccessExpr(b, "footprint")));
  }

  private Optional<JmlClause> objectCreated(List<Formal> formals, VariableTranslator variableScope) {

    return formals.stream()
        .filter((f) -> isReference(f, variableScope))
        .filter((f) -> ArgumentMode.OUT.equals(f.argumentMode()))
        .map(Formal::identifier)
        .map(variableScope::translate)
        .findAny()
        .map((f) -> new JmlSimpleExprClause(ENSURES, null,
            NodeList.nodeList(),
            //new MethodCallExpr(null, new SimpleName("\\new_elems_fresh"),
            new MethodCallExpr(null, new SimpleName("\\fresh"),
                NodeList.nodeList(new FieldAccessExpr(new NameExpr("\\result"), "footprint")))));

  }

  public void translateContract(Contract contract) {

    JavaMethodSignaturExtractor methodSignaturExtractor = new JavaMethodSignaturExtractor(contract, messageManager);

    System.err.println("INFO: Only contracts that are part of abstractions are supported at the moment.");

    String classIdentifier = methodSignaturExtractor.ownerClassIdentifier();
    String methodIdentifier = methodSignaturExtractor.methodName();

    VariableScopeManager variableScope = getParameterScope(contract.formals());

    Optional<Type> returnT = variableScope.returnType;
    //TODO: Check that owner matches
    Optional<Type> ownerType = variableScope.ownerType;

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

    List<JmlClause> accessibleClause = translateAccessible(contract.formals(), variableScope);
    List<JmlClause> assignableClause = translateAssignable(contract.formals(), variableScope);

    NodeList<JmlClause> clauses = new NodeList<>();

    clauses.add(preClause);
    clauses.addAll(disPreClause);
    clauses.add(postClause);
    clauses.addAll(disPostClause);
    objectCreated(contract.formals(), variableScope).ifPresent(clauses::add);
    clauses.addAll(accessibleClause);
    clauses.addAll(assignableClause);

    JmlContract jmlContract = new JmlContract()
        .setBehavior(Behavior.NORMAL)
        .setClauses(clauses);

    NodeList<JmlContract> contracts = NodeList.nodeList(jmlContract);

    //TODO: declare default case, or allow annotation under which class the contract should be declared.
    ClassOrInterfaceDeclaration abstractClassDeclaration = abstractAbstractions.get(classIdentifier);

    if (abstractClassDeclaration == null) {
      System.err.println(String.format("ERROR: The contract '%s', does not belong to any class, and is not translated.",
          methodIdentifier));
    }

    ClassOrInterfaceDeclaration classImpl = abstractionImpementations.get(classIdentifier);

    if (methodSignaturExtractor.isStatic()) {
      System.err.println("Static constructor method found");
      Statement returnStmt;
      if (classImpl == null) {
        returnStmt = new ReturnStmt("null");
        returnStmt.setLineComment("NOTE: This should be never called, as it is only the interface!");
      } else {

        List<Expression> args = variableScope.parameters.stream().map(p -> new NameExpr(p.getNameAsString()))
            .collect(Collectors.toList());
        EmptyStmt em = new EmptyStmt();

        em.setLineComment("TODO: To implement by the user.");

        NodeList<Statement> nl = NodeList.nodeList(em);
        BlockStmt body = new BlockStmt(nl);
        classImpl.addConstructor()
            .setParameters(NodeList.nodeList(variableScope.parameters))
            .setBody(body)
            .setContracts(contracts);

        ObjectCreationExpr obc = new ObjectCreationExpr(
            null,
            new ClassOrInterfaceType(null, classImpl.getNameAsString()), NodeList.nodeList(args));
        returnStmt = new ReturnStmt(obc);
      }

      NodeList<Statement> nl = NodeList.nodeList(returnStmt);
      BlockStmt body = new BlockStmt(nl);

      MethodDeclaration methodDeclAbstr = abstractClassDeclaration
          .addMethod(methodIdentifier)
          .setBody(body)
          .setType(returnType)
          .setParameters(NodeList.nodeList(variableScope.parameters))
          .setPublic(true)
          .setStatic(true)
          .setContracts(contracts);

    } else {
      MethodDeclaration methodDeclAbstr = abstractClassDeclaration
          .addMethod(methodIdentifier)
          .setBody(null)
          .setType(returnType)
          .setParameters(NodeList.nodeList(variableScope.parameters))
          .setPublic(true)
          .setAbstract(true)
          .setContracts(contracts);

      //TODO: set default value when return type != void
      Statement returnStmt = new ReturnStmt();
      returnStmt.setLineComment("TODO: To implement by the user.");

      NodeList<Statement> nl = NodeList.nodeList(returnStmt);

      BlockStmt blueprintStatement = new BlockStmt(nl);

      if (classImpl != null) {
        MethodDeclaration methodDeclImpl = classImpl
            .addMethod(methodIdentifier)
            .setType(returnType)
            .setParameters(NodeList.nodeList(variableScope.parameters))
            .setBody(blueprintStatement)
            .setPublic(true);
      }
    }
  }

  Expression translateTerm(
      Term term,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
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

  Expression translateTermSpecConstant(
      Term.SpecConstant specCons,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    System.err.println(String.format("Specific constants as are not supported yet: %s", specCons));
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  Expression translateTermIdentifierAs(
      Term.Identifier.IdentifierAs idAs,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    System.err.println(String.format("As Terms are not supported yet: %s", idAs));
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  Expression translateTermIdentifierValue(
      Term.Identifier.IdentifierValue idV,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    Optional<VariableScope> variable = variableScope.translate(idV.identifier());
    Optional<Expression> variableExpression = variable.map(VariableScope::getJmlTerm);

    return variableExpression.orElseGet(() -> {
      FuncTranslation trns = funcTranslator.translate(idV);
      if (trns == null) {
        System.err.println(funcTranslator.map.keySet());
        System.err.println(String.format("Identifier value not found: %s", idV));
        return new BooleanLiteralExpr(false);
      }
      return trns.getJmlTerm(List.of());
    });
  }

  Expression translateTermMethodApplication(
      Term.MethodApplication methAppl,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
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

  Expression translateTermOld(
      Term.Old oldTerm,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    Expression content = translateTerm(oldTerm.argument(), variableScope, indexFabric);

    return new MethodCallExpr(
        null,
        "\\old",
        NodeList.nodeList(content));
  }

  Expression translateTermBooleanLiteral(
      Term.BooleanLiteral booleanLit,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    return new BooleanLiteralExpr(booleanLit.value());
  }

  Expression translateTermNumeralLiteral(
      Term.NumberLiteral numeralLit,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    //TODO: Provide proper translation
    //One might to differentiate between (concider Type!)
    // - binary / hexadecimal numeral -> LongLiteral? / IntLiteral?, 
    // - decimal -> double / float
    return new DoubleLiteralExpr(numeralLit.value());
  }

  Expression translateTermLetBinding(
      Term.LetBinding letBind,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    System.err.println("Let expressions are not supported yet.");
    //TODO: Provide proper translation
    return new BooleanLiteralExpr(false);
  }

  JmlQuantifiedExpr.JmlBinder translateBinder(Quantor quantor) {
    return switch (quantor) {
      case Quantor.ALL -> JmlQuantifiedExpr.JmlDefaultBinder.FORALL;
      case Quantor.EXISTS -> JmlQuantifiedExpr.JmlDefaultBinder.EXISTS;
    };
  }

  Expression translateQuantorBinding(
      Term.QuantorBinding quantBind,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
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

  Expression translateMatchBinding(
      Term.MatchBinding matchBind,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    //TODO: Provide proper translation
    System.err.println("Match expressions are not supported yet.");
    return new BooleanLiteralExpr(false);
  }

  Expression translateAttributes(
      Term.Attributes attributes,
      VariableTranslator variableScope,
      IndexFab indexFabric) {
    //TODO: Provide proper error 
    System.err.println("Attributes are not supported yet and are ignored.");
    return translateTerm(attributes.term(), variableScope, indexFabric);
  }

  private record ExpressionPair(
      Expression pre,
      Expression post) {
  }
}
