package org.contract_lib.lang.contract_lib.ast;

public record Abstraction(
    SortDec.Def identifier,
    DatatypeDec datatypeDec) implements ContractLibAstElement {
}
