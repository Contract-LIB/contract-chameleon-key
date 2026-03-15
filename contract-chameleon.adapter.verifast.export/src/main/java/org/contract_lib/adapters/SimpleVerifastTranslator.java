package org.contract_lib.adapters;

import java.nio.file.Path;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.ast.Abstraction;
import org.contract_lib.lang.contract_lib.ast.Argument;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.contract_lib.tools.ClassNameExtractor;
import org.contract_lib.lang.contract_lib.tools.JavaMethodSignaturExtractor;

import org.contract_lib.lang.verifast.ast.VeriFastMethod;
import org.contract_lib.lang.verifast.ast.VeriFastPredicate;
import org.contract_lib.lang.verifast.ast.VeriFastSpec;
import org.contract_lib.lang.verifast.ast.VeriFastClass;
import org.contract_lib.lang.verifast.ast.VeriFastConstructor;
import org.contract_lib.lang.verifast.ast.VeriFastType;
import org.contract_lib.lang.verifast.ast.VeriFastExpression;
import org.contract_lib.lang.verifast.ast.VeriFastJavaExpression;
import org.contract_lib.lang.verifast.ast.VeriFastJavaStatement;
import org.contract_lib.lang.verifast.ast.VeriFastArgument;
import org.contract_lib.lang.verifast.ast.VeriFastContract;

import org.contract_lib.adapters.result.JarSpecFile;
import org.contract_lib.adapters.result.JarSrcFile;
import org.contract_lib.adapters.result.JavaFile;
import org.contract_lib.adapters.result.JavaSpecFile;
import org.contract_lib.adapters.result.HelperFile;

import org.contract_lib.adapters.translation.HelperTranslator;
import org.contract_lib.adapters.translation.TypeTranslator;

import org.contract_lib.adapters.translation.AbstractionTranslation;
import org.contract_lib.adapters.translation.PredicateTranslator;

import org.contract_lib.adapters.translation.TermTranslator;
import org.contract_lib.adapters.translation.TypeTranslation;
import org.contract_lib.adapters.translation.TermTranslation;
import org.contract_lib.adapters.translation.TermTranslationProvider;
import org.contract_lib.adapters.translation.QuantorTranslation;
import org.contract_lib.adapters.translation.quantifier.ExistentialQuantorTranslation;
import org.contract_lib.adapters.translation.quantifier.AllIntQuantorTranslation;

/// This class only supprts one abstraction at a time
/// It was mainly build for experimental purposes to find out,
/// what are suitable abstractions and interfaces used in a translation.
public class SimpleVerifastTranslator {

  private static final String RESULT_NAME = "result";
  private static final String NULL = "null";

  private ChameleonMessageManager messageManager;

  private final TypeTranslator typeTranslator;
  private final HelperTranslator helperTranslator;
  private final PredicateTranslator predicateTranslator;

  private final List<TranslationResult> results;
  private final List<String> javaSpecFiles;
  private final List<String> javaFiles;

  /// Access the name of the implementation class given an class identifer.
  private final Map<String, String> getImplementationClassName;

  /// Available methods of the abstract class.
  private final Map<String, List<VeriFastMethod>> getAbstractJavaClassMethods;

  /// Available methods of the implementation class.
  private final Map<String, List<VeriFastMethod>> getImplClassMethods;

  /// Available methods of the java specification.
  private final Map<String, List<VeriFastMethod>> getSpecClassMethods;

  /// Available constructors of the implementation class.
  private final Map<String, List<VeriFastConstructor>> getImplClassConstructors;

  private final List<TermTranslation> termTranslations;
  private final List<QuantorTranslation> quantorTranslations;

  public SimpleVerifastTranslator(Path path, ChameleonMessageManager messageManager) {
    this.messageManager = messageManager;
    this.results = new ArrayList<>();

    this.javaSpecFiles = new ArrayList<>();
    this.javaFiles = new ArrayList<>();

    this.getImplementationClassName = new HashMap<>();
    this.getAbstractJavaClassMethods = new HashMap<>();
    this.getImplClassMethods = new HashMap<>();
    this.getSpecClassMethods = new HashMap<>();
    this.getImplClassConstructors = new HashMap<>();

    this.helperTranslator = new HelperTranslator(
        this.messageManager,
        HelperTranslator.DEFAULT_HELPER);

    ServiceLoader<TypeTranslation> loadedTypes = ServiceLoader.load(TypeTranslation.class);
    ServiceLoader<TermTranslationProvider> loadedTerms = ServiceLoader.load(TermTranslationProvider.class);

    this.typeTranslator = new TypeTranslator(loadedTypes.stream().map(Provider::get).toList());

    this.predicateTranslator = new PredicateTranslator(
        this.messageManager,
        this.typeTranslator);

    this.quantorTranslations = List.of(
        new ExistentialQuantorTranslation(),
        new AllIntQuantorTranslation());

    this.termTranslations = new ArrayList<>();

    loadedTerms.stream().map(Provider::get)
        .map(TermTranslationProvider::getAll)
        .forEach(this.termTranslations::addAll);

    String fileName = path.getFileName().toString().replaceAll("(\\.|/)", "_");

    HelperFile helperFile = helperTranslator.getFile();
    javaSpecFiles.add(helperFile.fileName() + ".javaspec");
    this.results.add(helperFile);

    //Add jar-specification
    this.results.add(
        new JarSpecFile(
            fileName,
            javaSpecFiles));

    //Add source definition
    this.results.add(
        new JarSrcFile(
            List.of(fileName + ".jar"),
            javaFiles));
  }

  public List<TranslationResult> translateContractLibAstProvider(ContractLibAst ast) {
    // Add abstract classes
    ast.abstractions()
        .stream()
        .forEach(this::translateAbstractionJavaFile);

    // Add implentation classes
    ast.abstractions()
        .stream()
        .forEach(this::translateAbstractionImplementation);

    // Add contracts
    ast.contracts()
        .stream()
        .forEach(this::translateContract);

    return results;
  }

  public List<TranslationResult> translateContractLibAstApplicant(ContractLibAst ast) {
    // Add abstract java specifications
    ast.abstractions()
        .stream()
        .forEach(this::translateAbstractionJavaSpec);

    // Add contracts
    ast.contracts()
        .stream()
        .forEach(this::translateContract);

    return results;
  }

  private void addAbstractionType(String name) {
    this.typeTranslator.addTranslation(new AbstractionTranslation(
        new Sort.Type(name),
        new VeriFastType.VeriFastClass(name, List.of())));
  }

  private void translateAbstractionJavaFile(Abstraction abstraction) {
    ClassNameExtractor nameExtractor = new ClassNameExtractor(abstraction, messageManager);

    String name = nameExtractor.getClassName();
    String packageName = nameExtractor.getPackageName();
    String directoryName = nameExtractor.getDirectoryName();
    String identifier = nameExtractor.getClassIdentifier();

    List<VeriFastPredicate> predicates = predicateTranslator.translatePredicateDef(
        name,
        abstraction.datatypeDec().constructors().get(0));

    List<VeriFastMethod> methods = new ArrayList<>();

    VeriFastClass vfc = new VeriFastClass(
        name,
        predicates,
        new ArrayList<>(),
        methods,
        true,
        Optional.empty());

    VeriFastSpec spec = new VeriFastSpec(
        packageName,
        vfc);

    JavaFile file = new JavaFile(
        directoryName,
        name,
        spec);

    System.err.println("Abstraction for: " + identifier);
    addAbstractionType(name);
    javaFiles.add(directoryName + "/" + name + ".java");
    results.add(file);
    getAbstractJavaClassMethods.put(identifier, methods);
  }

  private void translateAbstractionJavaSpec(Abstraction abstraction) {
    ClassNameExtractor nameExtractor = new ClassNameExtractor(abstraction, messageManager);

    String name = nameExtractor.getClassName();
    String packageName = nameExtractor.getPackageName();
    String directoryName = nameExtractor.getDirectoryName();
    String identifier = nameExtractor.getClassIdentifier();

    List<VeriFastPredicate> predicates = predicateTranslator.translatePredicateDef(
        name,
        abstraction.datatypeDec().constructors().get(0));
    List<VeriFastMethod> methods = new ArrayList<>();

    VeriFastClass vfc = new VeriFastClass(
        name,
        predicates,
        new ArrayList<>(),
        methods,
        true,
        Optional.empty());

    VeriFastSpec spec = new VeriFastSpec(
        packageName,
        vfc);

    JavaSpecFile file = new JavaSpecFile(
        packageName,
        name,
        spec);

    System.err.println("Abstraction for: " + identifier);

    addAbstractionType(name);
    javaSpecFiles.add(directoryName + "/" + name + ".javaspec");
    results.add(file);
    this.getSpecClassMethods.put(identifier, methods);
  }

  private void translateAbstractionImplementation(Abstraction abstraction) {
    ClassNameExtractor nameExtractor = new ClassNameExtractor(abstraction, messageManager);

    String name = nameExtractor.getClassName();
    String implName = nameExtractor.getImplClassName();
    String packageName = nameExtractor.getPackageName();
    String directoryName = nameExtractor.getDirectoryName();
    String identifier = nameExtractor.getClassIdentifier();

    List<VeriFastPredicate> predicates = predicateTranslator.translatePredicateDef(
        implName,
        abstraction.datatypeDec().constructors().get(0));

    List<VeriFastMethod> methods = new ArrayList<>();
    List<VeriFastConstructor> constructors = new ArrayList<>();

    VeriFastClass vfc = new VeriFastClass(
        implName,
        predicates,
        constructors,
        methods,
        false,
        Optional.of(name));

    VeriFastSpec spec = new VeriFastSpec(
        packageName,
        vfc);

    JavaFile file = new JavaFile(
        packageName,
        implName,
        spec);

    System.err.println("Abstraction for: " + identifier);

    addAbstractionType(name);

    javaFiles.add(directoryName + "/" + implName + ".java");
    results.add(file);

    this.getImplementationClassName.put(identifier, implName);
    this.getImplClassMethods.put(identifier, methods);
    this.getImplClassConstructors.put(identifier, constructors);
  }

  public void translateContract(Contract contract) {

    String contractIdentifier = contract.identifier().identifier();

    System.err.println("-> Translate Contract: " + contractIdentifier);

    JavaMethodSignaturExtractor extractor = new JavaMethodSignaturExtractor(contract, messageManager);

    String owner = extractor.ownerClassIdentifier();
    Optional<Sort> resultSort = extractor.getReturnType();
    Optional<VeriFastType> resultType = resultSort.map(typeTranslator::translate);
    boolean isStatic = extractor.isStatic();

    List<VeriFastExpression> requiresPredicates = new ArrayList<>();
    List<VeriFastExpression> ensuresPredicates = new ArrayList<>();

    List<Argument> inArguments = extractor.inArguments();
    List<Argument> inoutArguments = extractor.inoutArguments();

    //Add this parameter when necessary
    extractor.thisReadOnlyArgument()
        .ifPresent(inArguments::add);

    extractor.thisMutatableArgument()
        .ifPresent(inoutArguments::add);

    //TODO: Add helper for user types!!

    predicateTranslator.clearAvailableArguments();

    TermTranslator termTranslator = new TermTranslator(
        this.termTranslations,
        this.quantorTranslations,
        this.typeTranslator,
        this.predicateTranslator,
        this.helperTranslator,
        this.messageManager);

    requiresPredicates.addAll(predicateTranslator.createPredicates(inArguments, termTranslator, true, false));
    requiresPredicates.addAll(predicateTranslator.createPredicates(inoutArguments, termTranslator, true, true));

    ensuresPredicates.addAll(predicateTranslator.createPredicates(inArguments, termTranslator, false, false));
    ensuresPredicates.addAll(predicateTranslator.createPredicates(inoutArguments, termTranslator, true, false));

    //If the return value is an abstraction, create the predicate for return 
    extractor.getReturnType()
        .map((p) -> new Argument(p, RESULT_NAME))
        .flatMap(p -> predicateTranslator.createPredicate(p, termTranslator, true, false))
        .ifPresent(ensuresPredicates::add);

    extractor.getReturnType()
        .map(this.typeTranslator::translate)
        .filter(t -> !t.isLogicType())
        .map((p) -> new VeriFastExpression.BinaryOperation("!=",
            new VeriFastExpression.Variable(RESULT_NAME),
            new VeriFastExpression.Variable(NULL)))
        .ifPresent(ensuresPredicates::add);

    TermTranslator.VeriFastPrePostExpression expressions = termTranslator.translateContractPairs(
        requiresPredicates,
        ensuresPredicates,
        contract.pairs());

    requiresPredicates.add(expressions.pre());
    ensuresPredicates.add(expressions.post());

    VeriFastJavaExpression defaultBody = new VeriFastJavaExpression.SimpleStatement(
        extractor.getDefaultMethodBody());

    List<VeriFastJavaExpression> consBody = new ArrayList<>(List.of(defaultBody));

    List<VeriFastJavaExpression> methodBody = new ArrayList<>(List.of(defaultBody));

    String implementationClass = this.getImplementationClassName.get(extractor.ownerClassIdentifier());

    List<VeriFastArgument> arguments = translateArguments(extractor.getArguments());

    List<VeriFastJavaExpression> constructorCaller = List.of(
        new VeriFastJavaExpression.Return(
            new VeriFastJavaStatement.ConstructorCall(
                new VeriFastType.VeriFastClass(implementationClass, List.of()),
                arguments)));

    VeriFastContract vfContract = new VeriFastContract(
        new VeriFastExpression.Chain(requiresPredicates),
        new VeriFastExpression.Chain(ensuresPredicates));

    List<VeriFastMethod> listImpl = this.getImplClassMethods.get(owner);
    if (!isStatic) {
      if (listImpl != null) {
        listImpl.add(new VeriFastMethod(
            vfContract,
            extractor.methodName(),
            resultType,
            arguments,
            isStatic,
            Optional.of(methodBody)));
      }
    }

    List<VeriFastConstructor> listCons = this.getImplClassConstructors.get(owner);
    if (isStatic) {
      if (listCons != null) {
        listCons.add(new VeriFastConstructor(
            vfContract,
            arguments,
            consBody));
      }
    }

    List<VeriFastMethod> listAbst = this.getAbstractJavaClassMethods.get(owner);
    if (listAbst != null) {
      listAbst.add(new VeriFastMethod(
          vfContract,
          extractor.methodName(),
          resultType,
          arguments,
          isStatic,
          isStatic ? Optional.of(constructorCaller) : Optional.empty()));
    }

    List<VeriFastMethod> listSpec = this.getSpecClassMethods.get(owner);
    if (listSpec != null) {
      listSpec.add(new VeriFastMethod(
          vfContract,
          extractor.methodName(),
          resultType,
          arguments,
          isStatic,
          Optional.empty()));
    }
  }

  public List<VeriFastArgument> translateArguments(List<Argument> parameter) {
    return parameter.stream()
        .map(this::translateArgument)
        .collect(Collectors.toList());
  }

  public VeriFastArgument translateArgument(Argument argument) {
    VeriFastType type = typeTranslator.translate(argument.sort());
    return new VeriFastArgument(
        type,
        argument.identifier());
  }
}
