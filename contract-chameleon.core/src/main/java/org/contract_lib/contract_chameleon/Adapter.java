package org.contract_lib.contract_chameleon;

import java.util.List;

import org.contract_lib.contract_chameleon.SharedContextManager.UserProvidedContext;

public abstract class Adapter implements AdapterId {
  public abstract void perform(String[] args);

  /** Access a context of a given type, or create the context from defined context providers.
   */
  //public <C extends SharedContext> C getContext(Class<C> type) { }

  /** The contexts that should be provided through the arguments of the interface calling the adapter.
   * <p>
   * The interface might be command line arguments,
   * some default values, or something else providing the required state.
   * In the case there can be multiple ways to create the required context,
   * all provided contexts are merged.
   * <p>
   * Defaults to a empty list of contexts.
   */
  public List<Class<? extends UserProvidedContext>> argumentContextsFromInterface() {
    return List.of();
  }
}
