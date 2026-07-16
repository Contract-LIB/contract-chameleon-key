package org.contract_lib.adapters.translations.functions;

import java.util.List;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

@AutoService(FuncProvider.class)
public record SetFuncTranslations() implements FuncProvider {

  static final Sort CLIB_SET_TYPE = new Sort.Type("Set");
  static final Sort CLIB_GENERIC_TYPE = new Sort.Type("A");
  static final Sort CLIB_BOOL_TYPE = new Sort.Type("Bool");
  static final Sort CLIB_INT_TYPE = new Sort.Type("Int");

  static final Type JML_SET_TYPE = new ClassOrInterfaceType(null, new SimpleName("\\map"), null);
  static final Type JML_GENERIC_TYPE = new ClassOrInterfaceType(null, new SimpleName("A"), null);
  static final Type JML_BOOL_TYPE = PrimitiveType.booleanType();
  static final Type JML_INT_TYPE = PrimitiveType.intType();

  public List<FuncTranslation> getAll() {
    return List.of(
        new FuncTranslation.MethodCall(
            "set.empty",
            "\\dl_finiteSetEmpty",
            List.of(),
            List.of(),
            JML_SET_TYPE,
            CLIB_SET_TYPE),

        new FuncTranslation.MethodCall(
            "set.singleton",
            "\\dl_finiteSetSingleton",
            List.of(CLIB_GENERIC_TYPE),
            List.of(JML_GENERIC_TYPE),
            JML_SET_TYPE,
            CLIB_SET_TYPE),

        new FuncTranslation.MethodCall(
            "set.union",
            "\\dl_finiteSetUnion",
            List.of(CLIB_SET_TYPE, CLIB_SET_TYPE),
            List.of(JML_SET_TYPE, JML_SET_TYPE),
            JML_SET_TYPE,
            CLIB_SET_TYPE),

        new FuncTranslation.MethodCall(
            "set.inter",
            "\\dl_finiteSetInter",
            List.of(CLIB_SET_TYPE, CLIB_SET_TYPE),
            List.of(JML_SET_TYPE, JML_SET_TYPE),
            JML_SET_TYPE,
            CLIB_SET_TYPE),

        new FuncTranslation.MethodCall(
            "set.minus",
            "\\dl_finiteSetMinus",
            List.of(CLIB_SET_TYPE, CLIB_SET_TYPE),
            List.of(JML_SET_TYPE, JML_SET_TYPE),
            JML_SET_TYPE,
            CLIB_SET_TYPE),

        new FuncTranslation.MethodCall(
            "set.member",
            "\\dl_finiteSetMember",
            List.of(CLIB_SET_TYPE, CLIB_GENERIC_TYPE),
            List.of(JML_SET_TYPE, JML_GENERIC_TYPE),
            JML_BOOL_TYPE,
            CLIB_BOOL_TYPE),
        new FuncTranslation.MethodCall(
            "set.subset",
            "\\dl_finiteSetSubset",
            List.of(CLIB_SET_TYPE, CLIB_SET_TYPE),
            List.of(JML_SET_TYPE, JML_SET_TYPE),
            JML_BOOL_TYPE,
            CLIB_BOOL_TYPE),
        new FuncTranslation.MethodCall(
            "set.disjunct",
            "\\dl_finiteSetDisjunct",
            List.of(CLIB_SET_TYPE, CLIB_SET_TYPE),
            List.of(JML_SET_TYPE, JML_SET_TYPE),
            JML_BOOL_TYPE,
            CLIB_BOOL_TYPE),

        new FuncTranslation.MethodCall(
            "set.card",
            "\\dl_finiteSetCard",
            List.of(CLIB_SET_TYPE),
            List.of(JML_SET_TYPE),
            JML_INT_TYPE,
            CLIB_INT_TYPE));
  }
}
