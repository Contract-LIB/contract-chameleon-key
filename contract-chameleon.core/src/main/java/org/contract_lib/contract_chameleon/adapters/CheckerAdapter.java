
package org.contract_lib.contract_chameleon.adapters;

import org.contract_lib.contract_chameleon.Adapter;

public abstract class CheckerAdapter extends Adapter {

  /**
   * Performs the checks on the provided source files.
   * 
   * @return If the check was successful {@value CheckerAdapterResult#SUCCESS} otherwise {@value CheckerAdapterResult#FAILURE}.
   */
  public abstract CheckerAdapterResult performCheck();

  @Override
  public final void perform() {
    // Execution if the checker is run as a normal adapter.
    switch (this.performCheck()) {
      case SUCCESS -> this.getMessageContext().logInfo("Checker successful.");
      case FAILURE -> this.getMessageContext().logInfo("Checker failed.");
    }
  }

  public enum CheckerAdapterResult {
    SUCCESS,
    FAILURE;
  }
}
