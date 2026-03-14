package org.contract_lib.contract_chameleon;

import java.util.List;
import java.util.Optional;

import org.contract_lib.contract_chameleon.SharedContextManager.SharedContext;
import org.contract_lib.contract_chameleon.SharedContextManager.SharedContextProvider;
import org.contract_lib.contract_chameleon.contexts.MessageContext;
import org.contract_lib.contract_chameleon.SharedContextManager.InterfaceProvidedContext;

public abstract class Adapter implements AdapterId {

  private SharedContextManager sharedContextManager;

  /// Execute the adapter.
  public abstract void perform();

  /** Extend the help message for this adapter.
   * <p>
   * The interface defines how the adapter is called and what arguments are available.
   * However, this description con provide additional information how the adapter should be used. 
   */
  public Optional<String> helpMessage() {
    return Optional.empty();
  }

  /// Short access without optional unwrap for the message context: {@code sharedContextManager.getContext(MessageContext.class)}.
  public MessageContext getMessageContext() {
    return sharedContextManager.getMessageContext();
  }

  /** Access a {@link UserProvidedContext} provided via the interface.
   * <p>
   * A user context might be required by the {@link Adapter#argumentContextsFromInterface()} for a specific adapter.
   * This context must be created by the user interface provider.
   *
   * @param <C> The type of context to be accessed.
   * @param expectedContext The class of the context that is expected. 
   * @return The shared context requested, the optional is empty if there was an error in the context creation.
   */
  public final <C extends InterfaceProvidedContext> Optional<C> getContext(Class<C> type) {
    return sharedContextManager.getContext(type);
  }

  /** Access a {@link SharedContext}, that can be created by a provider.
   * 
   * @param <C> The type of context to be accessed.
   * @param provider The provider to use, if the context was not created before.
   * @return The shared context requested, the optional is empty if there was an error in the context creation.
   */
  public final <C extends SharedContext> Optional<C> getContext(SharedContextProvider<C> provider) {
    return sharedContextManager.getContext(provider);
  }

  /** The {@link UserProvidedContext} that should be provided through the arguments of the interface calling the adapter.
   * <p>
   * The interface might be command line arguments,
   * some default values, or something else providing the required state.
   * In the case there can be multiple ways to create the required context,
   * all provided contexts are merged.
   * <p>
   * Defaults to a empty list of contexts.
   * <p>
   * This list also defines the expected arguments for the interface.
   * The interface can then decide,
   * if it requires user input or provides a default context where possible.
   */
  public List<Class<? extends InterfaceProvidedContext>> argumentContextsFromInterface() {
    return List.of();
  }
}
