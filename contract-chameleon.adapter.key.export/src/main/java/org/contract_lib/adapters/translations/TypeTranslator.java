
package org.contract_lib.adapters.translations;

import java.util.List;

import org.contract_lib.lang.contract_lib.ast.Sort;

public interface TypeTranslator {
  public TypeTranslation translate(Sort sort);

  public void store(TypeTranslation translations);
}
