package org.contract_lib.adapters.translations;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.ast.type.Type;

import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

public interface TypeTranslation {
  Sort getClibSort();

  Type getJmlType(Sort sort);

  KeySort getKeySort(Sort sort);

  //TODO: add method for default value (false, 0, null, …)
  //Expression getDefaultExampleValue();

  /// the return type must match the return value of @getFootprintInvariant
  boolean hasFootprint();

  /// Provides the list of helpers, that must be ensured as invariants for the given field.
  List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab);

  /// None, when this is a value type, and all of it's components are value types.
  /// Some, when either they type itself has a \locset,
  /// or parts of the components have a \locset.
  /// The expression describes, that all \locsets are part of this.
  Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab);

  //public final record TypeTranslation(
  //    Sort clibSort,
  //    ReferenceType jmlType) {
  //}
}
