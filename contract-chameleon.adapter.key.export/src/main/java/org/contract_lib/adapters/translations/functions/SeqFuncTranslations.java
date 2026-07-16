package org.contract_lib.adapters.translations.functions;

import java.util.List;

import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

import org.contract_lib.adapters.translations.FuncProvider;
import org.contract_lib.adapters.translations.FuncTranslation;
import org.contract_lib.lang.contract_lib.ast.Sort;

@AutoService(FuncProvider.class)
public record SeqFuncTranslations() implements FuncProvider {

  static final Sort CLIB_SEQ_TYPE = new Sort.Type("Seq");
  static final Sort CLIB_GENERIC_TYPE = new Sort.Type("A");
  static final Sort CLIB_BOOL_TYPE = new Sort.Type("Bool");
  static final Sort CLIB_INT_TYPE = new Sort.Type("Int");

  static final Type JML_GENERIC_TYPE = new ClassOrInterfaceType(null, new SimpleName("A"), null);
  static final Type JML_SEQ_TYPE = new ClassOrInterfaceType(null, new SimpleName("seq"), null);
  static final Type JML_BOOL_TYPE = PrimitiveType.booleanType();
  static final Type JML_INT_TYPE = PrimitiveType.intType();

  public List<FuncTranslation> getAll() {
    return List.of(
        new FuncTranslation.ConstantTranslation(
            "seq.empty",
            new NameExpr("\\seq_empty"),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),

        new FuncTranslation.MethodCall(
            "seq.unit",
            "\\seq_singleton",
            List.of(CLIB_GENERIC_TYPE),
            List.of(JML_GENERIC_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),
        new FuncTranslation.MethodExpr(
            "seq.at",
            (s) -> {
              NodeList<Expression> n = NodeList.nodeList(s);
              n.add(new IntegerLiteralExpr("1"));
              return new MethodCallExpr(null, "\\dl_seqSub", n);
            },
            List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
            List.of(JML_SEQ_TYPE, JML_INT_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),
        new FuncTranslation.MethodExpr(
            "seq.extract",
            (s) -> new MethodCallExpr(null, "\\dl_seqSub", NodeList.nodeList(s)),
            List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE, CLIB_INT_TYPE),
            List.of(JML_SEQ_TYPE, JML_INT_TYPE, JML_INT_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),

        // - Sequence queries 
        new FuncTranslation.MethodExpr(
            "seq.len",
            (s) -> new FieldAccessExpr(
                new EnclosedExpr(new CastExpr(new ClassOrInterfaceType(null, new SimpleName("\\seq"), null), s.get(0))),
                "length"),
            List.of(CLIB_SEQ_TYPE),
            List.of(JML_SEQ_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),

        new FuncTranslation.MethodExpr(
            "seq.nth",
            (s) -> new ArrayAccessExpr(s.get(0), s.get(1)),
            List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
            List.of(JML_SEQ_TYPE, JML_INT_TYPE),
            JML_GENERIC_TYPE,
            CLIB_GENERIC_TYPE),

        new FuncTranslation.MethodExpr(
            "seq.indexof",
            (s) -> new MethodCallExpr(null, "\\dl_seqIndexOf", NodeList.nodeList(s)),
            List.of(CLIB_SEQ_TYPE, CLIB_GENERIC_TYPE),
            List.of(JML_SEQ_TYPE, JML_GENERIC_TYPE),
            JML_INT_TYPE,
            CLIB_INT_TYPE),

        /*
        // - Sequence boolean query 
        new FuncTranslation.MethodCall(
        "seq.contains",
        "\\seq_--",
        List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
        List.of(JML_SEQ_TYPE, JML_INT_TYPE),
        JML_SEQ_TYPE,
        CLIB_SEQ_TYPE),
        new FuncTranslation.MethodCall(
        "seq.prefixof",
        "\\seq_--",
        List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
        List.of(JML_SEQ_TYPE, JML_INT_TYPE),
        JML_SEQ_TYPE,
        CLIB_SEQ_TYPE),
        new FuncTranslation.MethodCall(
        "seq.suffixof",
        "\\seq_--",
        List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
        List.of(JML_SEQ_TYPE, JML_INT_TYPE),
        JML_SEQ_TYPE,
        CLIB_SEQ_TYPE),
        */

        // - Sequence mutation function

        new FuncTranslation.MethodExpr(
            "seq.++",
            (s) -> new MethodCallExpr(null, "\\seq_concat", NodeList.nodeList(s)),
            List.of(CLIB_SEQ_TYPE, CLIB_SEQ_TYPE),
            List.of(JML_SEQ_TYPE, JML_SEQ_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE),
        new FuncTranslation.MethodExpr(
            "seq.update",
            (s) -> new MethodCallExpr(null, "\\dl_seqUpd", NodeList.nodeList(s)),
            List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE, CLIB_GENERIC_TYPE),
            List.of(JML_SEQ_TYPE, JML_INT_TYPE, JML_GENERIC_TYPE),
            JML_SEQ_TYPE,
            CLIB_SEQ_TYPE));
    /*
    new FuncTranslation.MethodCall(
        "seq.replace",
        "\\seq_--",
        List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
        List.of(JML_SEQ_TYPE, JML_INT_TYPE),
        JML_SEQ_TYPE,
        CLIB_SEQ_TYPE),
    new FuncTranslation.MethodCall(
        "seq.replace_all",
        "\\seq_--",
        List.of(CLIB_SEQ_TYPE, CLIB_INT_TYPE),
        List.of(JML_SEQ_TYPE, JML_INT_TYPE),
        JML_SEQ_TYPE,
        CLIB_SEQ_TYPE));
        */
  }
}
