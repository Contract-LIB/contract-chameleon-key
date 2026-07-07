package org.contract_lib.adapters.translations.functions;

import java.util.List;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

import static org.contract_lib.adapters.translations.FuncTranslation.BinaryTranslation;

@AutoService(FuncProvider.class)
public record IntFuncTranslations() implements FuncProvider {

  static final Sort CLIB_INT_TYPE = new Sort.Type("Int");
  static final Sort CLIB_BOOL_TYPE = new Sort.Type("Bool");
  static final Type JML_INT_TYPE = PrimitiveType.intType();
  static final Type JML_BOOL_TYPE = PrimitiveType.booleanType();

  public List<FuncTranslation> getAll() {
    return List.of(
        //TODO: Move '=' and 'distinct' to generic definion 
        /*
        new UnaryTranslation(
            "-",
            UnaryExpr.Operator.LOGICAL_COMPLEMENT,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        */

        //TODO: Add unary minus!!
        new BinaryTranslation(
            "=",
            BinaryExpr.Operator.EQUALS,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE),
        new BinaryTranslation(
            "distinct",
            BinaryExpr.Operator.NOT_EQUALS,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE),
        new BinaryTranslation(
            "+",
            BinaryExpr.Operator.PLUS,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        new BinaryTranslation(
            "-",
            BinaryExpr.Operator.MINUS,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        new BinaryTranslation(
            "*",
            BinaryExpr.Operator.MULTIPLY,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        new BinaryTranslation(
            "div",
            BinaryExpr.Operator.DIVIDE,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        new BinaryTranslation(
            "mod",
            BinaryExpr.Operator.REMAINDER,
            JML_INT_TYPE, JML_INT_TYPE,
            CLIB_INT_TYPE, CLIB_INT_TYPE),
        new BinaryTranslation(
            "<",
            BinaryExpr.Operator.LESS,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE),
        new BinaryTranslation(
            "<=",
            BinaryExpr.Operator.LESS_EQUALS,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE),
        new BinaryTranslation(
            ">",
            BinaryExpr.Operator.GREATER,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE),
        new BinaryTranslation(
            ">=",
            BinaryExpr.Operator.GREATER_EQUALS,
            JML_INT_TYPE, JML_BOOL_TYPE,
            CLIB_INT_TYPE, CLIB_BOOL_TYPE));
  }
}
