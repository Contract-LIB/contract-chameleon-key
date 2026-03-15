
package org.contract_lib.contract_chameleon.contexts;

import org.contract_lib.contract_chameleon.AdapterId;
import org.contract_lib.contract_chameleon.SharedContextManager;

public final class AdapterContext implements
    SharedContextManager.InterfaceProvidedContext,
    SharedContextManager.SettableContext,
    SharedContextManager.DefaultContext {

  private AdapterId id;

  public AdapterContext(AdapterId id) {
    this.id = id;
  }

  public AdapterId getAdapterIdentifer() {
    return id;
  }
}
