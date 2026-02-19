package org.contract_lib.lang.contract_lib.generator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.stream.Stream;

import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.contract_chameleon.error.ChameleonMessageGroup;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.generator.ContractLibGenerator;

class CreateAstFromSourceTest {

  static Stream<Arguments> positiveDefinitions() {
    return Stream.of(
        Arguments.of("ast/DeclareAbstraction.clib", 3, 0, 0),
        Arguments.of("ast/DeclareDatatype.clib", 0, 3, 0),
        Arguments.of("ast/DeclareSort.clib", 0, 0, 4));
  }

  @ParameterizedTest
  @MethodSource("positiveDefinitions")
  void testDeclarations(
      String filePath,
      int nAbstractions,
      int nDatatypes,
      int nSorts) throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
    assertNotNull(in, "Input stream not created from resource.");

    CharStream charStream = CharStreams.fromStream(in);
    ChameleonMessageManager messageManager = new ChameleonMessageManager();

    ContractLibGenerator generator = new ContractLibGenerator(messageManager);
    ContractLibAst ast = generator.generate(filePath, charStream);
    System.err.println(ast);

    assertFalse(messageManager::errorFound, "There was an message generated.");
    assertNotNull(ast, "AST could not be created.");

    assertEquals(nAbstractions, ast.abstractions().size(), "Wrong numner of abstractions found.");
    assertEquals(nDatatypes, ast.datatypes().size(), "Wrong numner of abstractions found.");
    assertEquals(nSorts, ast.sorts().size(), "Wrong numner of sorts found.");
  }

  static Stream<Arguments> definitionErrors() {
    return Stream.of(
        Arguments.of("ast/InvalidTopLevel.clib", 2, 2, 3));
  }

  @ParameterizedTest
  @MethodSource("definitionErrors")
  void testInvalidDeclarations(
      String filePath,
      int nAbstractions,
      int nDatatypes,
      int nSorts) throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
    assertNotNull(in, "Input stream not created from resource.");

    CharStream charStream = CharStreams.fromStream(in);
    ChameleonMessageManager messageManager = new ChameleonMessageManager();

    ContractLibGenerator generator = new ContractLibGenerator(messageManager);
    ContractLibAst ast = generator.generate(filePath, charStream);

    System.err.println(ast);
    messageManager.writeStdErr();

    assertTrue(
        messageManager::errorFound,
        "An error is expected.");

    //TODO: Check number of exceptions
    //TODO: Report error when bracket is missing
    //TODO: Better locations of syntax errors / not the occurence of next Token, but where the construct is incompleat

    assertNotNull(ast, "AST could not be created.");
    assertEquals(nAbstractions, ast.abstractions().size(), "Wrong numner of abstractions found.");
    assertEquals(nDatatypes, ast.datatypes().size(), "Wrong numner of abstractions found.");
    assertEquals(nSorts, ast.sorts().size(), "Wrong numner of sorts found.");

  }
}
