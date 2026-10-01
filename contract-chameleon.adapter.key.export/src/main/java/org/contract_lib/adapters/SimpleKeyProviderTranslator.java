package org.contract_lib.adapters;

import java.io.Writer;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.printer.DefaultPrettyPrinter;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;

import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import com.github.javaparser.ast.jml.body.JmlFieldDeclaration;
import com.github.javaparser.ast.jml.body.JmlClassAccessibleDeclaration;
import com.github.javaparser.ast.jml.body.JmlClassExprDeclaration;

import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;

import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.ast.Abstraction;
import org.contract_lib.lang.contract_lib.ast.Constructor;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.Term;
import org.contract_lib.lang.contract_lib.tools.JavaMethodSignaturExtractor;
import org.contract_lib.lang.contract_lib.ast.SelectorDec;
import org.contract_lib.lang.contract_lib.ast.DatatypeDec;

import org.contract_lib.lang.contract_lib.modifier.IdentifierSubstitution;

import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.adapters.translations.types.AbstractionTranslation;
import org.contract_lib.adapters.translations.types.LogicTypeTranslation;
import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.adapters.translations.ContractTranslation;
import org.contract_lib.adapters.translations.ContractTranslator;
import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslator;
import org.contract_lib.adapters.translations.KeyTranslations;
import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.default_translators.DefaultContractTranslator;
import org.contract_lib.adapters.translations.default_translators.DefaultFuncTranslator;
import org.contract_lib.adapters.translations.default_translators.DefaultIndexFabric;
import org.contract_lib.adapters.translations.default_translators.DefaultSortTranslator;
import org.contract_lib.adapters.translations.default_translators.DefaultTermTranslator;
import org.contract_lib.adapters.translations.functions.FieldAccessTranslation;

/// This class only supprts one abstraction at a time
/// It was mainly build for experimental purposes to find out,
/// what are suitable abstractions and interfaces used in a translation.
public class SimpleKeyProviderTranslator {

  private static final String IMPLEMENTATION_SUFFIX = "Impl";
  private static String FOOTPRINT_NAME = "fp";
  private static String RESULT_LABEL_CONTRACT_LIB = "result";
  private static String RESULT_LABEL_JML = "\\result";
  private static String THIS_LABEL_CONTRACT_LIB = "this";
  private static String THIS_LABEL_JML = "this";

  private ChameleonMessageManager messageManager;

  //Translator for sorts
  private final TypeTranslator sortTranslator;
  //Translator for func symbols
  private final FuncTranslator funcTranslator;
  //The used KeY translator (ADT representation)
  private final KeyTranslations keyTranslator;
  //The used term translator
  private final TermTranslator termTranslator;
  //The used contract translator
  private final ContractTranslator contractTranslator;

  //This map stores the abstract class definitions generated from the abstractions 
  private Map<String, ClassOrInterfaceDeclaration> abstractAbstractions = new HashMap<>();

  //This map stores the class implementation blueprints generated from the abstractions 
  private Map<String, ClassOrInterfaceDeclaration> abstractionImpementations = new HashMap<>();

  private List<TranslationResult> results = new ArrayList<>();

  private Set<DetailLevel> detailLevel = new HashSet<>();

  public SimpleKeyProviderTranslator(
      ChameleonMessageManager manager) {

    this.messageManager = manager;

    this.sortTranslator = new DefaultSortTranslator(new HashMap<>());
    this.funcTranslator = new DefaultFuncTranslator(new HashMap<>());

    //NOTE: This allows to explicitly introduce keywords, where they are assumed default in KeY
    //this.detailLevel.add(DetailLevel.INSTANCE_ACCESSIBLE);
    //this.detailLevel.add(DetailLevel.INSTANCE_GHOST);
    //this.detailLevel.add(DetailLevel.INSTANCE_INVARIANT);
    //this.detailLevel.add(DetailLevel.INSTANCE_FOOTPRINT);

    this.keyTranslator = new KeyTranslations(
        manager,
        sortTranslator,
        //NOTE: This allows the change from datatype type translation to sort
        KeyTranslations.ToDatatypeTranslation::new
    //KeyTranslations.ToSortTranslation::new
    );

    ServiceLoader<TypeTranslation> typeLoader = ServiceLoader.load(
        TypeTranslation.class,
        ClassLoader.getSystemClassLoader());

    //NOTE: Additional `AbstractionTranslations` are created when abstraction is found
    typeLoader.forEach(sortTranslator::store);

    ServiceLoader<FuncProvider> funcLoader = ServiceLoader.load(
        FuncProvider.class,
        ClassLoader.getSystemClassLoader());

    funcLoader.forEach(f -> funcTranslator.store(f.getAll()));

    this.termTranslator = new DefaultTermTranslator(
        this.sortTranslator,
        this.funcTranslator);

    this.contractTranslator = new DefaultContractTranslator(
        this.sortTranslator,
        this.termTranslator,
        FOOTPRINT_NAME,
        RESULT_LABEL_CONTRACT_LIB,
        RESULT_LABEL_JML,
        THIS_LABEL_CONTRACT_LIB,
        THIS_LABEL_JML,
        this.messageManager);
  }

  public List<TranslationResult> translateContractLibAst(
      ContractLibAst ast,
      boolean provider) {

    results.add(keyTranslator.translate(ast));
    keyTranslator.getSorts().stream()
        .map(LogicTypeTranslation::new)
        .forEach(sortTranslator::store);

    funcTranslator.store(keyTranslator.getCons());

    //Creates the list of all provided abstractions
    ast.abstractions()
        .stream()
        .forEach(a -> translateAbstraction(a, provider));

    //Populates all abstractions with their contracts
    ast.contracts()
        .stream()
        .forEach(this::translateContract);

    //TODO: create example main class

    return results;
  }

  private SimpleName getName(SelectorDec selector) {
    return new SimpleName(selector.symbol().identifier());
  }

  protected void annotateMethodDecl(CallableDeclaration<?> decl) {
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
        //.addModifier(Modifier.DefaultKeyword.JML_INSTANCE)
        .addModifier(Modifier.DefaultKeyword.JML_GHOST);

    addModifierIfRequired(DetailLevel.INSTANCE_GHOST, fieldDec);

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
    IndexFabric fab = new DefaultIndexFabric();

    translation.getHelper(
        new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), getName(selector)),
        selector.sort(),
        sortTranslator,
        fab)
        .stream()
        .map(e -> addModifierIfRequired(DetailLevel.INSTANCE_INVARIANT,
            new JmlClassExprDeclaration(
                NodeList.nodeList(), //JML Tags
                NodeList.nodeList(), //Modifier
                new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
                null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
                e)
                .addModifier(Modifier.DefaultKeyword.PUBLIC)))
        //.addModifier(Modifier.DefaultKeyword.JML_INSTANCE))
        .forEach((i) -> dec.addMember(i));

    //Footprint invariants for ghost fields that are reference types.
    IndexFabric footprintIndexfab = new DefaultIndexFabric();

    translation.getFootprintInvariant(
        new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName(selector.symbol().identifier())),
        selector.sort(),
        sortTranslator,
        footprintIndexfab).ifPresent(
            footprintInv -> dec.addMember(
                addModifierIfRequired(DetailLevel.INSTANCE_INVARIANT,
                    new JmlClassExprDeclaration(
                        NodeList.nodeList(), //JML Tags
                        NodeList.nodeList(), //Modifier
                        new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
                        null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
                        footprintInv)
                        .addModifier(Modifier.DefaultKeyword.PUBLIC))));
    //.addModifier(Modifier.DefaultKeyword.JML_INSTANCE)));
  }

  protected void addAbstractionFootprint(ClassOrInterfaceDeclaration dec) {
    FieldDeclaration fieldDec = new FieldDeclaration(
        NodeList.nodeList(),
        new ClassOrInterfaceType(
            null,
            new SimpleName("\\locset"),
            null),
        //new JmlLogicType(JmlLogicType.Primitive.SET), 
        FOOTPRINT_NAME)
        .setPublic(true)
        //.addModifier(Modifier.DefaultKeyword.JML_INSTANCE)
        .addModifier(Modifier.DefaultKeyword.JML_GHOST);

    addModifierIfRequired(DetailLevel.INSTANCE_FOOTPRINT, fieldDec);

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
                new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName(FOOTPRINT_NAME)))))
        .addModifier(Modifier.DefaultKeyword.PUBLIC);

    addModifierIfRequired(DetailLevel.INSTANCE_ACCESSIBLE, footprintInv);

    dec.addMember(footprintInv);

    // All ghost fields of this class are part of the footprint footprint
    JmlClassExprDeclaration footprintThisInv = new JmlClassExprDeclaration(
        NodeList.nodeList(), //JML Tags
        NodeList.nodeList(), //Modifier
        new SimpleName("invariant"), // kind //TODO: this is also ignored, but invariant at least printed.
        null, //new SimpleName("name"), //name //TODO: this is not supported yet in printing??
        new MethodCallExpr(
            null, // scope
            new SimpleName("\\subset"),
            NodeList.nodeList(
                new ThisExpr(),
                new FieldAccessExpr(new ThisExpr(), NodeList.nodeList(), new SimpleName(FOOTPRINT_NAME)))))
        .addModifier(Modifier.DefaultKeyword.PUBLIC);

    addModifierIfRequired(DetailLevel.INSTANCE_ACCESSIBLE, footprintInv);

    dec.addMember(footprintThisInv);
  }

  protected void annotateClassOrInterfaceDecl(ClassOrInterfaceDeclaration decl) {
  }

  protected void addAccessibleDef(ClassOrInterfaceDeclaration dec, List<SelectorDec> selector) {

    // All invariants have to relay on footprint
    JmlClassAccessibleDeclaration accessInv = new JmlClassAccessibleDeclaration(
        NodeList.nodeList(),
        NodeList.nodeList(),
        new NameExpr(new SimpleName("\\inv")),
        NodeList.nodeList(
            new NameExpr(new SimpleName(FOOTPRINT_NAME))),
        null //Measured by
    )
        .addModifier(Modifier.DefaultKeyword.PUBLIC);

    addModifierIfRequired(DetailLevel.INSTANCE_ACCESSIBLE, accessInv);

    dec.addMember(accessInv);
  }

  protected void addImplementationFootprint(ClassOrInterfaceDeclaration dec, Abstraction abstraction) {
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
  }

  private void createAbstractClass(
      Abstraction abstraction,
      String packageName,
      String className) {
    // Abstract Class Definition
    CompilationUnit abstractCompUnit = new CompilationUnit();
    abstractCompUnit.setPackageDeclaration(packageName);

    ClassOrInterfaceDeclaration abstractClassDeclaration = abstractCompUnit
        .addClass(className)
        .setPublic(true)
        .setAbstract(true);
    annotateClassOrInterfaceDecl(abstractClassDeclaration);

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
    annotateClassOrInterfaceDecl(classImpl);

    addImplementationFootprint(classImpl, abstraction);

    abstractionImpementations.put(getIdentifier(packageName, className), classImpl);

    SimpleResult resImpl = new SimpleResult(
        packageName,
        className + IMPLEMENTATION_SUFFIX,
        implComplUnit);

    classImpl
        .addOrphanComment(new LineComment(String.format(" TODO: Implement '%s.%s.body'.\n", packageName, className)));

    results.add(resImpl);
  }

  private String getIdentifier(String packageName, String className) {
    return packageName + "." + className;
  }

  private void translateAbstraction(
      Abstraction abstraction, boolean provider) {
    //TODO: Extract package name from abstraction or report warning
    String packageName = packageName(abstraction);
    String className = className(abstraction);

    System.err.println("Abstr Dec: " + getIdentifier(packageName, className));

    createAbstractClass(abstraction, packageName, className);
    if (provider)
      createImplementationClass(abstraction, packageName, className);
  }

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

  public void translateContract(Contract contract) {

    JavaMethodSignaturExtractor methodSignaturExtractor = new JavaMethodSignaturExtractor(contract, messageManager);

    System.err.println("INFO: Only contracts that are part of abstractions are supported at the moment.");

    String classIdentifier = methodSignaturExtractor.ownerClassIdentifier();
    String methodIdentifier = methodSignaturExtractor.methodName();

    ContractTranslation contractTranslation = contractTranslator.translate(contract);

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

        IdentifierSubstitution substitution = new IdentifierSubstitution(
            RESULT_LABEL_CONTRACT_LIB,
            THIS_LABEL_CONTRACT_LIB,
            messageManager);
        Contract constructorContract = substitution.applyContract(contract);
        ContractTranslation constructorContractTranslation = contractTranslator.translate(constructorContract);

        EmptyStmt em = new EmptyStmt();

        em.setLineComment(methodSignaturExtractor.getDefaultMethodBody());

        NodeList<Statement> nl = NodeList.nodeList(em);
        BlockStmt body = new BlockStmt(nl);

        CallableDeclaration<?> constructorDecl = classImpl.addConstructor()
            .setParameters(NodeList.nodeList(constructorContractTranslation.parameters()))
            .setBody(body)
            .setContracts(constructorContractTranslation.jmlContracts());
        annotateMethodDecl(constructorDecl);

        ObjectCreationExpr obc = new ObjectCreationExpr(
            null,
            new ClassOrInterfaceType(
                null,
                classImpl.getNameAsString()),
            constructorContractTranslation.arguments());
        returnStmt = new ReturnStmt(obc);
      }

      NodeList<Statement> nl = NodeList.nodeList(returnStmt);
      BlockStmt body = new BlockStmt(nl);

      CallableDeclaration<?> methodDeclAbstr = abstractClassDeclaration
          .addMethod(methodIdentifier)
          .setBody(body)
          .setType(contractTranslation.returnType())
          .setParameters(NodeList.nodeList(contractTranslation.parameters()))
          .setPublic(true)
          .setStatic(true)
          .setContracts(contractTranslation.jmlContracts());
      annotateMethodDecl(methodDeclAbstr);

    } else {
      CallableDeclaration<?> methodDeclAbstr = abstractClassDeclaration
          .addMethod(methodIdentifier)
          .setBody(null)
          .setType(contractTranslation.returnType())
          .setParameters(NodeList.nodeList(contractTranslation.parameters()))
          .setPublic(true)
          .setAbstract(true)
          .setContracts(contractTranslation.jmlContracts());

      annotateMethodDecl(methodDeclAbstr);

      //TODO: set default value when return type != void
      Statement returnStmt = new ReturnStmt();
      returnStmt.setLineComment(methodSignaturExtractor.getDefaultMethodBody());

      NodeList<Statement> nl = NodeList.nodeList(returnStmt);

      BlockStmt blueprintStatement = new BlockStmt(nl);

      if (classImpl != null) {
        CallableDeclaration<?> methodDeclImpl = classImpl
            .addMethod(methodIdentifier)
            .setType(contractTranslation.returnType())
            .setParameters(contractTranslation.parameters())
            .setBody(blueprintStatement)
            .setPublic(true);
        annotateMethodDecl(methodDeclImpl);
      }
    }
  }

  <N extends Node & NodeWithModifiers<N>> N addModifierIfRequired(DetailLevel modifier, N declaration) {
    if (this.detailLevel.contains(modifier)) {
      declaration.addModifier(modifier.getKeyword());
    }
    return declaration;
  }

  /// Which keywords should added to the specification, even if they are the default
  private enum DetailLevel {
    INSTANCE_FOOTPRINT,
    INSTANCE_GHOST,
    INSTANCE_ACCESSIBLE,
    INSTANCE_INVARIANT;

    Modifier.DefaultKeyword getKeyword() {
      return switch (this) {
        case DetailLevel.INSTANCE_FOOTPRINT,
            DetailLevel.INSTANCE_GHOST,
            DetailLevel.INSTANCE_ACCESSIBLE,
            DetailLevel.INSTANCE_INVARIANT ->
          Modifier.DefaultKeyword.JML_INSTANCE;
      };
    }
  }

  //TODO: Move to own file
  record SimpleResult(
      String packageName,
      String className,
      CompilationUnit cu) implements TranslationResult {
    @Override
    public Optional<String> extendSubDirectory() {
      return Optional.ofNullable(packageName);
    }

    public String fileName() {
      return className;
    }

    public String fileEnding() {
      return ".java";
    }

    public void writeTo(Writer writer) throws IOException {
      PrinterConfiguration config = new DefaultPrinterConfiguration();
      DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
      writer.write(printer.print(cu));
    }
  }
}
