package org.contract_lib.contract_chameleon.adapters;

import java.util.List;

import org.contract_lib.contract_chameleon.Adapter;
import org.contract_lib.contract_chameleon.SharedContextManager.InterfaceProvidedContext;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;

public abstract class TranslationAdapter extends Adapter {

  /**
   * Performs the translation.
   */
  public abstract void performTranslation();

  /** A list of checker adapter that this translation adapter requires.
   * <p>
   * Note: the adapter might be run, without running the checker adapters first.
   * The checker adapter only serve the purpose, 
   * that the provided input is valid (e.g. well-defined).
   */
  public List<String> checkerAdapter() {
    return List.of();
  }

  @Override
  public List<Class<? extends InterfaceProvidedContext>> argumentContextsFromInterface() {
    return List.of(
        SourcePathsContext.class,
        ResultDirectoryContext.class);
  }

  @Override
  public final void perform() {
    this.performTranslation();
  }
}
