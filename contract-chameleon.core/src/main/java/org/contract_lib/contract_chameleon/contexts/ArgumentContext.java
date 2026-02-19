package org.contract_lib.contract_chameleon.contexts;

import org.contract_lib.contract_chameleon.SharedContextManager.SharedContext;
import org.contract_lib.contract_chameleon.SharedContextManager.SharedContextProvider;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

public class ArgumentContext implements SharedContext {

  public ArgumentContext() {

  }

  public class Provider implements SharedContextProvider<ArgumentContext> {
    public Provider(String[] args) {

    }

    @Override
    public Class<ArgumentContext> getContext() {
      return ArgumentContext.class;
    }

    @Override
    public ArgumentContext createContext(ChameleonMessageManager messageManager) {
      return new ArgumentContext();
    }
  }

}
