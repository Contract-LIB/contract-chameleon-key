
package org.contract_lib.adapters.translations.custom_functions;

import java.util.List;

import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.adapters.translations.FuncTranslator;
import org.contract_lib.adapters.translations.KeyTranslations;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Function;
import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.SortedVar;
import com.github.javaparser.ast.type.Type;

/// Strategy for translating functions.
///
/// For declarations fallback on KeYFunctionsStrategy.
/// For definitions this strategy used `JML`
public final class JMLFunctionsStrategy implements CustomFunctionTranslationStrategy {

  private final KeYFunctionsStrategy functionsStrategy;
  private final TypeTranslator typeTranslator;
  private final FuncTranslator funcTranslator;

  private final Void defs;

  public JMLFunctionsStrategy(
      KeyTranslations keyTranslations,
      TypeTranslator typeTranslator,
      FuncTranslator funcTranslator
      Term) {
    this.functionsStrategy = new KeYFunctionsStrategy(keyTranslations, typeTranslator);
    this.typeTranslator = typeTranslator;
    this.funcTranslator = funcTranslator;

  }

  @Override
  public Void addDec(Function.FunctionDec dec) {
    functionsStrategy.addDec(dec);
    return null;
  }

  @Override
  public Void addDef(Function.FunctionDef def) {

    Type res = typeTranslator.translate(def.result()).getJmlType(def.result());

    List<Sort> args = def.arguments()
        .stream()
        .map(SortedVar::sort).toList();
    List<Type> par = args.stream().map(a -> typeTranslator.translate(a).getJmlType(a))
        .toList();

    funcTranslator.store(List.of(
        new FuncTranslation.MethodCall(
            def.name().identifier(),
            fixIdentifier(def.name().identifier()),
            args,
            par,
            res,
            def.result())));

    return null;
  }

  @Override
  public void store() {
    System.err.println("JML Function Strategy does not store the translations.");

    functionsStrategy.store();
  }

  @Override
  public String getIdentifier(String clibId) {
    return "clib_" + fixIdentifier(clibId);
  }

  private final String fixIdentifier(String id) {
    return id
        .replace(".", "_")
        .replace("-", "_");

  }
}
