package org.contract_lib.adapters.translations.functions;

import java.util.List;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

@AutoService(FuncProvider.class)
public record MapFuncTranslations() implements FuncProvider {

  static final Sort CLIB_MAP_TYPE = new Sort.Type("Map");
  static final Sort CLIB_GENERIC_KEY_TYPE = new Sort.Type("K");
  static final Sort CLIB_GENERIC_VALUE_TYPE = new Sort.Type("V");
  static final Sort CLIB_BOOL_TYPE = new Sort.Type("Bool");
  static final Sort CLIB_INT_TYPE = new Sort.Type("Int");

  static final Type JML_GENERIC_KEY_TYPE = new ClassOrInterfaceType(null, new SimpleName("K"), null);
  static final Type JML_GENERIC_VALUE_TYPE = new ClassOrInterfaceType(null, new SimpleName("V"), null);
  static final Type JML_MAP_TYPE = new ClassOrInterfaceType(null, new SimpleName("\\map"), null);
  static final Type JML_BOOL_TYPE = PrimitiveType.booleanType();
  static final Type JML_INT_TYPE = PrimitiveType.intType();

  public List<FuncTranslation> getAll() {
    return List.of(
        new FuncTranslation.ConstantTranslation(
            "map.empty",
            new NameExpr("\\map_empty"),
            JML_MAP_TYPE,
            CLIB_MAP_TYPE),

        new FuncTranslation.MethodCall(
            "map.singleton",
            "\\dl_mapSingleton",
            List.of(CLIB_GENERIC_KEY_TYPE, CLIB_GENERIC_VALUE_TYPE),
            List.of(JML_GENERIC_KEY_TYPE, JML_GENERIC_VALUE_TYPE),
            JML_MAP_TYPE,
            CLIB_MAP_TYPE),

        new FuncTranslation.MethodCall(
            "map.get",
            "\\dl_mapGet",
            List.of(CLIB_MAP_TYPE, CLIB_GENERIC_KEY_TYPE),
            List.of(JML_GENERIC_KEY_TYPE, JML_GENERIC_KEY_TYPE),
            JML_GENERIC_VALUE_TYPE,
            CLIB_GENERIC_VALUE_TYPE),

        new FuncTranslation.MethodCall(
            "map.inDomain",
            "\\dl_inDomain",
            List.of(CLIB_MAP_TYPE, CLIB_GENERIC_KEY_TYPE),
            List.of(JML_GENERIC_KEY_TYPE, JML_GENERIC_KEY_TYPE),
            JML_BOOL_TYPE,
            CLIB_BOOL_TYPE),

        new FuncTranslation.MethodCall(
            "map.set",
            "\\dl_mapUpdate",
            List.of(CLIB_MAP_TYPE, CLIB_GENERIC_KEY_TYPE, CLIB_GENERIC_VALUE_TYPE),
            List.of(JML_GENERIC_KEY_TYPE, JML_GENERIC_KEY_TYPE, JML_GENERIC_VALUE_TYPE),
            JML_GENERIC_KEY_TYPE,
            CLIB_MAP_TYPE));

  }
}
