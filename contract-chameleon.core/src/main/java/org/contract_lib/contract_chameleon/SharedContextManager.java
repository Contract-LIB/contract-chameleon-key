package org.contract_lib.contract_chameleon;

import java.util.HashMap;
import java.util.Optional;

import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

/** A manager object to access shared contexts from different adapters.
 *
 * These contexts are read only,
 * and work as a cache.
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

  private HashMap<Class<?>, SharedContext> map = new HashMap<>();

  public <C extends SharedContext> C getContext(SharedContextProvider<C> provider) {
    if (allwaysRecreate) {
      return provider.createContext(this.messageManager);
    }
    return Optional.ofNullable(map.get(provider.getContext()))
        .map((c) -> provider.getContext().cast(c))
        .orElseGet(() -> this.createAndStore(provider));
  }

  private <C extends SharedContext> C createAndStore(SharedContextProvider<C> provider) {
    C context = provider.createContext(this.messageManager);
    this.map.put(provider.getContext(), context);
    return context;
  }

  public interface SharedContext {
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
