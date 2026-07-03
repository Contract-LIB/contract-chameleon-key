package org.contract_lib.adapters.translations;

import org.contract_lib.lang.contract_lib.ast.Contract;

public interface ContractTranslator {
  public ContractTranslation translate(Contract contract);
}
