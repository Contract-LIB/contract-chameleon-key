package org.contract_lib.adapters;

import com.github.javaparser.ast.PackageDeclaration;
import java.io.IOException;

import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseResult;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.TypeSolverBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import org.contract_lib.adapters.result.ContractLibResult;
import org.contract_lib.adapters.translations.AbstractClassImporter;
import org.contract_lib.adapters.translations.contracts.ClassMethodContractTranslation;
import org.contract_lib.adapters.translations.contracts.ConstructorContractTranslation;
import org.contract_lib.adapters.translations.contracts.ContractArgumentsExtractor;
import org.contract_lib.adapters.translations.contracts.ImportContractTranslation;
import org.contract_lib.adapters.translations.contracts.ImportContractTranslator;
import org.contract_lib.adapters.translations.terms.BinaryExpressionTranslation;
import org.contract_lib.adapters.translations.terms.ImportTermTranslation;
import org.contract_lib.adapters.translations.terms.ImportTermTranslator;
import org.contract_lib.adapters.translations.terms.QuantifierTranslation;
import org.contract_lib.adapters.translations.terms.StandardImportTermTranslation;
import org.contract_lib.adapters.translations.ImportPattern;
import org.contract_lib.adapters.translations.types.ImportTypeTranslation;
import org.contract_lib.adapters.translations.types.ImportTypeTranslator;
import org.contract_lib.adapters.translations.types.PrimitiveTypeTranslation;
import org.contract_lib.contract_chameleon.adapters.ImportAdapter.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.contract_lib.ast.Abstraction;
import org.contract_lib.lang.contract_lib.ast.Contract;
import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.ast.Datatype;
import org.contract_lib.lang.contract_lib.ast.SortDec.Def;

public class SimpleKeyImportTranslator {

  private final ChameleonMessageManager messageManager;
  private final JavaParser javaParser;

  private final List<Abstraction> abstractions;
  private final List<Contract> contracts;
  private final List<Datatype> datatypes;
  private final List<Def> sorts;

  private final List<ImportPattern> importPatterns;

  private final List<ImportTypeTranslation> importTypeTranslations;
  private final List<ImportContractTranslation> importContractTranslations;
  private final List<ImportTermTranslation> importTermTranslations;

  public SimpleKeyImportTranslator(
      ChameleonMessageManager manager) {

    TypeSolver typesolver = new TypeSolverBuilder()
        .withCurrentJRE()
        .build();

    JavaSymbolSolver solver = new JavaSymbolSolver(typesolver);
    ParserConfiguration config = new ParserConfiguration();
    config.setSymbolResolver(solver);
    config.setKeepJmlDocs(false);
    config.setProcessJml(true);
    config.setStoreTokens(true);

    this.javaParser = new JavaParser(config);
    this.messageManager = manager;

    this.abstractions = new ArrayList<>();
    this.contracts = new ArrayList<>();
    this.datatypes = new ArrayList<>();
    this.sorts = new ArrayList<>();

    this.importPatterns = new ArrayList<>();
    this.importTermTranslations = new ArrayList<>();
    this.importContractTranslations = new ArrayList<>();
    this.importTypeTranslations = new ArrayList<>();

    // Types
    this.importTypeTranslations.addAll(PrimitiveTypeTranslation.getAll());

    ImportTypeTranslator typeTranslator = new ImportTypeTranslator(this.importTypeTranslations);

    // Terms

    ImportTermTranslator termTranslator = new ImportTermTranslator(
        this.importTermTranslations,
        typeTranslator);

    this.importTermTranslations.addAll(StandardImportTermTranslation.getAll());
    this.importTermTranslations.addAll(BinaryExpressionTranslation.getAll());
    this.importTermTranslations.addAll(QuantifierTranslation.getAll());

    // Contract arguemtns

    ContractArgumentsExtractor contractArgumentsExtractor = new ContractArgumentsExtractor(typeTranslator);

    // Contracts
    this.importContractTranslations
        .add(new ConstructorContractTranslation(contractArgumentsExtractor, termTranslator));
    this.importContractTranslations
        .add(new ClassMethodContractTranslation(contractArgumentsExtractor, termTranslator));

    ImportContractTranslator importContractTranslator = new ImportContractTranslator(
        this.importContractTranslations);

    // Class - Patterns

    this.importPatterns.add(new AbstractClassImporter(typeTranslator, importContractTranslator));
  }

  public List<TranslationResult> translate(List<Path> paths) {
    paths.stream()
        .forEach(this::translateJavaFile);

    return createResults();
  }

  public List<TranslationResult> createResults() {
    return List.of(new ContractLibResult(
        ".",
        "clib_specification",
        new ContractLibAst(
            this.datatypes,
            this.abstractions,
            this.sorts,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            contracts,
            new ArrayList<>())));
  }

  // - Translation of JML Specification

  private void translateJavaFile(Path path) {
    if (path.toString().endsWith(".java")) {
      try {
        ParseResult<CompilationUnit> cu = javaParser.parse(path);
        System.err.println(cu.getProblems());
        cu.ifSuccessful(this::translateCompilationUnit);
      } catch (IOException e) {
        System.err.println(e);
      }
    } else {
      System.err.println(String.format("File '%s' found, but this ending is not supported.", path));
    }
  }

  private void translateCompilationUnit(CompilationUnit cu) {
    Optional<PackageDeclaration> pa = cu.getPackageDeclaration();
    Optional<String> packageName = pa.map(PackageDeclaration::getNameAsString);

    cu.getTypes().stream()
        .forEach(d -> this.translateTopLevelDeclaration(packageName, d));
  }

  private void translateTopLevelDeclaration(Optional<String> packageName, TypeDeclaration<?> declaration) {
    if (declaration.isClassOrInterfaceDeclaration()) {
      declaration.ifClassOrInterfaceDeclaration(d -> this.matchPattern(packageName, d));
    } else {
      System.err.println("This type of content cannot be handle yet: " + declaration);
    }
  }

  private void matchPattern(Optional<String> packageName, ClassOrInterfaceDeclaration declaration) {
    this.importPatterns.forEach(p -> p.translate(packageName, this.abstractions, this.contracts, declaration));
  }

  // - Translation of additional KeY files

  private void translateKeyFile() {
    //TODO: translate the specifications done in KeY
  }

  private <R> void errorConsumer(R input) {
    //TODO: handle error properly
    System.err.println("This type of content cannot be handle yet: " + input);
  }
}
