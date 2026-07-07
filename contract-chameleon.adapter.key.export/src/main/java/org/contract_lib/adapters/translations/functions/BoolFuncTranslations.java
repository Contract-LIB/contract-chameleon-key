package org.contract_lib.adapters.translations.functions;

import java.util.List;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

import static org.contract_lib.adapters.translations.FuncTranslation.UnaryTranslation;
import static org.contract_lib.adapters.translations.FuncTranslation.BinaryTranslation;
import static org.contract_lib.adapters.translations.FuncTranslation.ConstantTranslation;

@AutoService(FuncProvider.class)
public record BoolFuncTranslations() implements FuncProvider {

  static final Sort CLIB_BOOLEAN_TYPE = new Sort.Type("Bool");
  static final Type JML_BOOLEAN_TYPE = PrimitiveType.booleanType();

  public List<FuncTranslation> getAll() {
    return List.of(
        new ConstantTranslation("true",
            new BooleanLiteralExpr(true),
            JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE),
        new ConstantTranslation("false",
            new BooleanLiteralExpr(false),
            JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE),
        new UnaryTranslation(
            "not",
            UnaryExpr.Operator.LOGICAL_COMPLEMENT,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),
        new BinaryTranslation(
            "xor",
            BinaryExpr.Operator.XOR,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),
        new BinaryTranslation(
            "or",
            BinaryExpr.Operator.OR,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),
        new BinaryTranslation(
            "and",
            BinaryExpr.Operator.AND,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),
        new BinaryTranslation(
            "=>",
            BinaryExpr.Operator.IMPLICATION,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),

        // Move '=' and 'distinct' to a generic type
        new BinaryTranslation(
            "=",
            BinaryExpr.Operator.EQUALS,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE),
        new BinaryTranslation(
            "distinct",
            BinaryExpr.Operator.NOT_EQUALS,
            JML_BOOLEAN_TYPE, JML_BOOLEAN_TYPE,
            CLIB_BOOLEAN_TYPE, CLIB_BOOLEAN_TYPE)
    //TODO: Add trinary, functions (chainable, pairwise, operators)
    );
  }
}
