package org.contract_lib.lang.key.tools;

import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

import org.contract_lib.lang.key.ast.KeyAst;
import org.contract_lib.lang.key.ast.KeySort;
import org.contract_lib.lang.key.ast.KeyFunction;
import org.contract_lib.lang.key.ast.KeyDatatype;
import org.contract_lib.lang.key.ast.KeyDatatypeConstructor;
import org.contract_lib.lang.key.ast.KeyArgument;

public class KeyPrinter {

  private Writer writer;
  private ChameleonMessageManager manager;
  private int indentation;

  private static final int DEFAULT_INDENTATION = 4;

  private static final String SPACE = " ";
  private static final String BLOCK_OPEN = "{";
  private static final String BLOCK_CLOSE = "}";
  private static final String BRACKET_OPEN = "(";
  private static final String BRACKET_CLOSE = ")";
  private static final String VERTICAL_SEP = "|";
  private static final String COLON = ",";
  private static final String EQUAL = "=";
  private static final String SEMICOLON = ";";

  private static final String COMMAND_JAVA_SOURCES = "\\javaSource";
  private static final String COMMAND_CHOOSE_CONTRACT = "\\chooseContract";
  private static final String COMMAND_SORTS = "\\sorts";
  private static final String COMMAND_FUNCTIONS = "\\functions";
  private static final String COMMAND_DATATYPS = "\\datatypes";
  private static final String COMMAND_RULES = "\\rules";

  private static final String MODIFIER_UNIQUE = "\\unique";

  public KeyPrinter(Writer writer, ChameleonMessageManager manager) {
    this.writer = writer;
    this.indentation = 0;
    this.manager = manager;
  }

  public void print(KeyAst ast) throws IOException {
    if (!ast.sorts().isEmpty()) {
      printSorts(ast.sorts());
    }
    if (!ast.functions().isEmpty()) {
      printFunctions(ast.functions());
    }
    if (!ast.datatypes().isEmpty()) {
      printDatatypes(ast.datatypes());
    }
  }

  public void printDatatypes(List<KeyDatatype> dts) {
    printIndentation();
    print(COMMAND_DATATYPS);
    print(SPACE);
    printBlock(() -> dts.forEach((d) -> this.printDatatype(d)));
  }

  public void printDatatype(KeyDatatype dt) {
    printIndentation();
    print(dt.datatype().<String>perform(this::name, this::name));
    print(SPACE);
    print(EQUAL);
    print(SPACE);
    print(getConstructors(dt.constructors()));
    print(SEMICOLON);
    printNewLine();
  }

  private String getConstructors(List<KeyDatatypeConstructor> constrs) {
    return constrs
        .stream()
        .map(this::getConstructor)
        .collect(Collectors.joining(SPACE + VERTICAL_SEP + SPACE));
  }

  private String getConstructor(KeyDatatypeConstructor c) {
    if (c.arguments().isEmpty()) {
      return c.name();
    }
    return c.name() + BRACKET_OPEN + getArguments(c.arguments()) + BRACKET_CLOSE;
  }

  private String getArguments(List<KeyArgument> args) {
    return args
        .stream()
        .map(this::getArgument)
        .collect(Collectors.joining(COLON + SPACE));
  }

  private String getArgument(KeyArgument arg) {
    return arg.type().perform(this::name, this::name) + SPACE + arg.name();
  }

  public void printSorts(List<KeySort> sorts) {
    printIndentation();
    print(COMMAND_SORTS);
    print(SPACE);
    printBlock(() -> sorts.forEach((s) -> this.printSort(s)));
  }

  public void printSort(KeySort sort) {
    printIndentation();
    print(sort.<String>perform(this::name, this::name));
    print(SEMICOLON);
    printNewLine();
  }

  public String name(KeySort.Internal internalSort) {
    return internalSort.name();
  }

  public String name(KeySort.Custom customSort) {
    return customSort.name();
  }

  public void printFunctions(List<KeyFunction> functions) {
    printIndentation();
    print(COMMAND_FUNCTIONS);
    print(SPACE);
    printBlock(() -> functions.forEach((f) -> this.printFunction(f)));
  }

  public void printParameters(List<KeySort> parameters) {
    String joinedParameters = parameters
        .stream()
        .map((s) -> s.perform(this::name, this::name))
        .collect(Collectors.joining(COLON + SPACE));

    print(joinedParameters);
  }

  public void printFunction(KeyFunction func) {
    printIndentation();
    func.perform(this::printUniqueFunction, this::printDefaultFunction);
    print(SEMICOLON);
    printNewLine();
  }

  public Void printUniqueFunction(KeyFunction.UniqueFunction funcDec) {
    print(MODIFIER_UNIQUE);
    print(SPACE);
    return printDefaultFunction(funcDec.function());
  }

  public Void printDefaultFunction(KeyFunction.DefaultFunction funcDec) {
    print(funcDec.returnType().<String>perform(this::name, this::name));
    print(SPACE);
    print(funcDec.name());
    if (!funcDec.parameter().isEmpty()) {
      print(BRACKET_OPEN);
      printParameters(funcDec.parameter());
      print(BRACKET_CLOSE);
    }
    return null;
  }

  public void printSources(String sourcesPath) {
    printIndentation();
    print(COMMAND_JAVA_SOURCES);
    print(SPACE);
    print(sourcesPath);
    print(SEMICOLON);
    printNewLine();
  }

  public void printChooseContract() {
    printIndentation();
    print(COMMAND_CHOOSE_CONTRACT);
    printNewLine();
  }

  public void printNewLine() {
    print(System.lineSeparator());
  }

  @FunctionalInterface
  private interface BlockContent {
    void print();
  }

  private void print(String text) {
    try {
      this.writer.write(text);
    } catch (IOException e) {
      //TODO: Add error handling
    }
  }

  private void printBlock(BlockContent bc) {
    print(SPACE);
    print(BLOCK_OPEN);
    printNewLine();
    increaseIndentation();
    bc.print();
    decreaseIndentation();
    print(BLOCK_CLOSE);
    printNewLine();
  }

  private void increaseIndentation() {
    indentation += DEFAULT_INDENTATION;
  }

  private void decreaseIndentation() {
    indentation -= DEFAULT_INDENTATION;
  }

  private void printIndentation() {
    print(SPACE.repeat(indentation));
  }
}
