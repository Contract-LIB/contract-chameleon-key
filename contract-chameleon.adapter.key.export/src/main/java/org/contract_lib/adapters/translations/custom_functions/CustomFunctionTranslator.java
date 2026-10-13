
package org.contract_lib.adapters.translations.custom_functions;

import java.util.ArrayList;
import java.util.List;

import org.contract_lib.adapters.translations.FuncTranslator;
import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Function;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.SortedVar;

import com.github.javaparser.ast.type.Type;

public final class CustomFunctionTranslator {

  private final TypeTranslator typeTranslator;
  private final FuncTranslator functionTranslator;

  private final CustomFunctionTranslationStrategy storeStrategy;

  public CustomFunctionTranslator(
      TermTranslator termTranslator,
      TypeTranslator typeTranslator,
      FuncTranslator functionTranslator,
      CustomFunctionTranslationStrategy storeStrategy) {
    this.typeTranslator = typeTranslator;
    this.functionTranslator = functionTranslator;
    this.storeStrategy = storeStrategy;
  }

  public void addFunction(Function func) {
    this.storeStrategy.addFunc(func);
    func.perform(
        this::addFunctionDec,
        this::addFunctionDef);
  }

  private Void addFunctionDec(Function.FunctionDec dec) {
    String functionId = dec.name().identifier();

    List<Type> jmlParameterType = dec.arguments()
        .stream()
        .map(a -> typeTranslator.translate(a).getJmlType(a)).toList();

    Type jmlResultType = typeTranslator
        .translate(dec.result()).getJmlType(dec.result());

    // Make translation available to the translation of functions.
    functionTranslator.store(List.of(
        new CustomFunctionTranslation(
            functionId,
            storeStrategy.getIdentifier(functionId),
            dec.arguments(),
            dec.result(),
            jmlParameterType,
            jmlResultType)));

    return null;
  }

  private Void addFunctionDef(Function.FunctionDef def) {

    String functionId = def.name().identifier();
    List<Sort> signature = def.arguments().stream().map(SortedVar::sort).toList();

    List<Type> jmlParameterType = signature
        .stream()
        .map(a -> typeTranslator.translate(a).getJmlType(a)).toList();

    Type jmlResultType = typeTranslator
        .translate(def.result()).getJmlType(def.result());

    // Make translation available to the translation of functions.
    functionTranslator.store(List.of(
        new CustomFunctionTranslation(
            functionId,
            storeStrategy.getIdentifier(functionId),
            signature,
            def.result(),
            jmlParameterType,
            jmlResultType)));

    return null;
  }
}
