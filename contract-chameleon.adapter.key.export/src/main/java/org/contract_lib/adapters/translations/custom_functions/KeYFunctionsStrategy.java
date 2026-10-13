
package org.contract_lib.adapters.translations.custom_functions;

import java.util.ArrayList;
import java.util.List;

import org.contract_lib.adapters.translations.KeyTranslations;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Function;
import org.contract_lib.lang.contract_lib.ast.SortedVar;
import org.contract_lib.lang.key.ast.KeyFunction;
import org.contract_lib.lang.key.ast.KeySort;

public final class KeYFunctionsStrategy implements CustomFunctionTranslationStrategy {

  private final KeyTranslations keyTranslations;
  private final TypeTranslator typeTranslator;

  private final List<Function.FunctionDec> funcDecs;
  private final List<Function.FunctionDef> funcDefs;

  public KeYFunctionsStrategy(KeyTranslations keyTranslations, TypeTranslator typeTranslator) {
    this.keyTranslations = keyTranslations;
    this.typeTranslator = typeTranslator;

    funcDecs = new ArrayList<>();
    funcDefs = new ArrayList<>();
  }

  @Override
  public Void addDec(Function.FunctionDec dec) {
    funcDecs.add(dec);

    return null;
  }

  @Override
  public Void addDef(Function.FunctionDef def) {
    funcDefs.add(def);

    return null;
  }

  @Override
  public void store() {
    funcDecs.forEach(this::storeDec);
    funcDefs.forEach(this::storeDef);
  }

  @Override
  public String getIdentifier(String clibId) {
    return "\\dl_" + fixIdentifier(clibId);
  }

  private final void storeDec(Function.FunctionDec dec) {

    KeySort res = typeTranslator.translate(dec.result()).getKeySort(dec.result());
    List<KeySort> par = dec.arguments()
        .stream()
        .map(a -> typeTranslator.translate(a).getKeySort(a))
        .toList();

    keyTranslations.addKeYFunction(new KeyFunction.DefaultFunction(
        res,
        fixIdentifier(dec.name().identifier()),
        par));
  }

  private final void storeDef(Function.FunctionDef def) {

    KeySort res = typeTranslator.translate(def.result()).getKeySort(def.result());
    List<KeySort> par = def.arguments()
        .stream()
        .map(SortedVar::sort)
        .map(a -> typeTranslator.translate(a).getKeySort(a))
        .toList();

    keyTranslations.addKeYFunction(new KeyFunction.DefaultFunction(
        res,
        fixIdentifier(def.name().identifier()),
        par));

    // TODO: add translation of term
  }

  private final String fixIdentifier(String id) {
    return id
        .replace(".", "_")
        .replace("-", "_");

  }
}
