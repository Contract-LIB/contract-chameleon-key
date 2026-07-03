package org.contract_lib.adapters.translations;

import java.util.Optional;

import org.contract_lib.lang.contract_lib.ast.Symbol;

public interface VariableTranslator {
  Optional<VariableScope> translate(Symbol symbol);
}
