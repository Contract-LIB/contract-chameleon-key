package org.contract_lib.adapters.translations;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.jml.clauses.JmlContract;
import com.github.javaparser.ast.type.Type;

public record ContractTranslation(
    NodeList<JmlContract> jmlContracts,
    NodeList<Expression> arguments,
    NodeList<Parameter> parameters,
    Type returnType) {
}
