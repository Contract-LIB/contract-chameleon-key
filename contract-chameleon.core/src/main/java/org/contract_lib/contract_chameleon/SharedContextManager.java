package org.contract_lib.contract_chameleon;

import java.util.HashMap;
import java.util.Optional;

import org.contract_lib.contract_chameleon.error.ChameleonInfo;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.contract_chameleon.error.ChameleonReportable;

/** A manager object to access shared contexts from different adapters.
 * <p>
 * These contexts are non destructive,
 * and work as a cache.
 * You might extend a context with new data,
 * but never must delete context created.
 * <p>
 * Moreover,
 * every adapter is responsible for the creation of its required contexts,
 * so it is best to have a shared provider class SharedContextManager.SharedContextProvider.
 */
public final class SharedContextManager {

  private boolean allwaysRecreate;
  private ChameleonMessageManager messageManager;

  public SharedContextManager(ChameleonMessageManager messageManager) {
    this(messageManager, false);
  }

  public SharedContextManager(ChameleonMessageManager messageManager, boolean allwaysRecreate) {
    this.allwaysRecreate = allwaysRecreate;
    this.messageManager = messageManager;
  }

  private HashMap<Class<?>, SharedContext> contextCache = new HashMap<>();

  // The providers that can be used to create a context.
  private HashMap<Class<?>, SharedContextProvider<? extends SharedContext>> providerMap = new HashMap<>();

  /** Add a provider to the list of available providers.
   * <p>
   * This method is called by the interface for each provider supported.
   * The providers must not be able to generate the context when this method is called.
   * However, when the adapter is performed they must be able to provide their context.
   * 
   * @param <C> The context that is supported.
   * @param provider The provider 
   */
  public <C extends UserProvidedContext> void putProvider(SharedContextProvider<C> provider) {
    if (providerMap.containsKey(provider.getContext())) {
      this.messageManager.report(new ChameleonReportable() {
        @Override
        public String getMessage() {
          return String.format(
              "There already is a SharedContextProvider for %s. The duplicate provider '%s' is ignored.",
              provider.getContext(), provider.getClass());
        }
      });
      return;
    }
    // Add the context to the cache.
    providerMap.put(provider.getContext(), provider);
  }

  /*
    // Provider creates context
    o_provider.ifPresentOrElse(
        (c) -> this.createAndStore(c),
        () -> this.messageManager.report(new ChameleonReportable() {
          @Override
          public String getMessage() {
            return "There is no user provider for the context '%s'.";
          };
        }));
  
    return Optional.ofNullable((C) contextCache.get(expectedContext));
  }
  public <C extends UserProvidedContext & MergableContext<C>> SharedContextProvider<C> getProvider(
      Class<C> expectedContext) {
  
  }
  */

  /** Access a shared context, that can be created by a provider.
   * 
   * @param <C> The type of context to be accessed.
   * @param provider The provider to use, if the context was not created before.
   * @return The shared context requested, the optional is empty if there was an error in the context creation.
   */
  public <C extends SharedContext> Optional<C> getContext(SharedContextProvider<C> provider) {
    if (allwaysRecreate) {
      return this.createAndStore(provider);
    }
    Optional<C> cacheContext = Optional.ofNullable((C) contextCache.get(provider.getContext()))
        .or(() -> this.createAndStore(provider));

    return cacheContext;
  }

  /** Access context provided via the interface.
   * <p>
   * A user context might be required by the {@link Adapter#argumentContextsFromInterface()} for a specific adapter.
   * This context must be created by the user interface provider.
   *
   * @param <C> The type of context to be accessed.
   * @param expectedContext
   * @return
   */
  public <C extends UserProvidedContext> Optional<C> getContext(Class<C> expectedContext) {
    return this.getProvider(expectedContext)
        .flatMap(this::getContext);
  }

  private <C extends SharedContext> Optional<C> createAndStore(SharedContextProvider<C> provider) {
    Optional<C> context = Optional.ofNullable(provider.createContext(this.messageManager));
    context.ifPresentOrElse((c) -> this.contextCache.put(provider.getContext(), c),
        () -> this.messageManager.report(new ChameleonReportable() {
          @Override
          public String getMessage() {
            return String.format("Could not create context '%s' from provider '%s'.", provider.getClass());
          };
        }));

    return context;
  }

  /** Access a previously stored provider.
   * <p>
   * These stored providers can be required by an adapter,
   * and called automatically by a user interface.
   * <p>
   * The user interface must store the providers view {@link #putProvider(SharedContextProvider)}.
   * @param <C> The type of the expected context.
   * @param expectedContext The context that is expected.
   * @return An optional, containing the context or empty, if the context could not be provided by the interface.
   */
  public <C extends UserProvidedContext> Optional<SharedContextProvider<C>> getProvider(Class<C> expectedContext) {
    // Identify the provider that is responsible for creating the context.
    Optional<SharedContextProvider<C>> o_provider = Optional
        .ofNullable((SharedContextProvider<C>) providerMap.get(expectedContext));
    return o_provider;
  }

  public interface SharedContext {
  }

  public interface UserProvidedContext extends SharedContext {
  }

  public interface MergableContext<C extends SharedContext> extends SharedContext {
    /** Merges two contexts and creates a new context from it.
     * <p>
     * In the case the context already exists, possibly provided by a different provider,
     * the contexts are merged and a new context is returned by this method.
     * This returned context combines the two provided contexts.
     *
     * @param first the first context to merge.
     * @param second the second context to merge.
     * @return the context of both contexts merged.
     */
    public C merge(C first, C second);
  }

  public interface SharedContextProvider<C extends SharedContext> {
    /** The identifier under which the shared context is stored.
     * <p>
     * This property normally defaults to {@code return <concrete type of C>.class;}.
     */
    public Class<C> getContext();

    /** Creates a new shared context.
     * <p>
     * After the object is created it is considered immutable.
     *
     * @param messageManager a message manager for printing messages to the user.
     * @return an instance of the shared context.
     */
    public C createContext(ChameleonMessageManager messageManager);
  }
}
