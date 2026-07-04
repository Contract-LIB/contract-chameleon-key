
package org.contract_lib.adapters.translations.functions;

import java.util.List;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

import static org.contract_lib.adapters.translations.FuncTranslation.ConstantTranslation;

@AutoService(FuncProvider.class)
public record RefFuncTranslations() implements FuncProvider {

  static final Sort CLIB_GENERIC_TYPE = new Sort.Type("A");
  static final Sort CLIB_REF_TYPE = new Sort.ParametricType("Ref", List.of(CLIB_GENERIC_TYPE));
  static final Type JML_GENERIC_TYPE = new ClassOrInterfaceType(null, new SimpleName("A"), null);

  public List<FuncTranslation> getAll() {
    return List.of(
        new ConstantTranslation("null",
            new NullLiteralExpr(),
            JML_GENERIC_TYPE,
            CLIB_REF_TYPE));
  }
}
