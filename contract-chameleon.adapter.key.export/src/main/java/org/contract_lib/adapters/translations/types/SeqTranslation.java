package org.contract_lib.adapters.translations.types;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;

import com.github.javaparser.ast.body.Parameter;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.type.Type;
import com.google.auto.service.AutoService;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import com.github.javaparser.ast.jml.expr.JmlQuantifiedExpr;
import com.github.javaparser.ast.jml.expr.JmlMultiCompareExpr;

import org.contract_lib.adapters.translations.IndexFabric;
import org.contract_lib.adapters.translations.TypeTranslation;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeySort;

@AutoService(TypeTranslation.class)
public class SeqTranslation implements TypeTranslation {
  public Sort getClibSort() {
    return new Sort.Type("Seq");
  }

  public Type getJmlType(Sort sort) {
    return new ClassOrInterfaceType(
        null,
        new SimpleName("\\seq"),
        null);
  }

  public KeySort getKeySort(Sort sort) {
    return KeySort.Internal.getSeq();
  }

  public boolean hasFootprint() {
    return false;
  }

  public Optional<Expression> getFootprintInvariant(
      Expression field,
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    Sort sortOfPar = getTypeOfParameter(sort).get();
    TypeTranslation translationForParameter = translator.translate(sortOfPar);

    SimpleName index = fab.getNextIndex();

    return translationForParameter.getFootprintInvariant(
        new EnclosedExpr(
            new CastExpr(
                translationForParameter.getJmlType(sortOfPar),
                new ArrayAccessExpr(
                    new EnclosedExpr(
                        new CastExpr(
                            new ClassOrInterfaceType(
                                null,
                                new SimpleName("\\seq"),
                                null),
                            field)),
                    new NameExpr(index)))),
        sortOfPar,
        translator,
        fab).map(childFootprint -> {
          JmlQuantifiedExpr e = new JmlQuantifiedExpr();// \forall int i;
          e.setBinder(JmlQuantifiedExpr.JmlDefaultBinder.FORALL);
          e.setVariables(getVariables(index));
          e.setExpressions(NodeList.nodeList(
              getSeqRange(index, field),
              childFootprint));
          return e;
        });
  }

  public Optional<Sort> matchType(Sort.Type t) {
    //TODO: Throw proper error
    return Optional.empty();
  }

  public Optional<Sort> matchParametricType(Sort.ParametricType pt) {
    if (pt.arguments().size() == 0) {
      //TODO: Throw proper error
      return Optional.empty();
    }
    return Optional.of(pt.arguments().get(0));
  }

  public Optional<Sort> getTypeOfParameter(Sort seq) {
    return seq.perform(
        this::matchType,
        this::matchParametricType);
  }

  public List<Expression> getHelper(
      Expression field, //The field is of the type given in sort Seq T
      Sort sort,
      TypeTranslator translator,
      IndexFabric fab) {
    //TODO: Do proper unwrapping
    Sort sortOfPar = getTypeOfParameter(sort).get();

    SimpleName index = fab.getNextIndex();

    TypeTranslation translationForParameter = translator.translate(sortOfPar);

    List<Expression> helperOfParameter = translationForParameter.getHelper(
        new EnclosedExpr(
            new CastExpr(
                translationForParameter.getJmlType(sortOfPar),
                new ArrayAccessExpr(
                    new EnclosedExpr(
                        new CastExpr(
                            new ClassOrInterfaceType(
                                null,
                                new SimpleName("\\seq"),
                                null),
                            field
                        //  field,
                        )),
                    new NameExpr(index)))),
        sortOfPar,
        translator,
        fab);

    //TODO: Wrap helper in for all statement using index
    List<Expression> quantHelper = helperOfParameter.stream().map((h) -> {
      JmlQuantifiedExpr e = new JmlQuantifiedExpr();// \forall int i;
      e.setBinder(JmlQuantifiedExpr.JmlDefaultBinder.FORALL);
      e.setVariables(getVariables(index));
      e.setExpressions(NodeList.nodeList(getSeqRange(index, field), h));
      return e;
    }).collect(Collectors.toList());

    Type typeOfPar = translationForParameter.getJmlType(sortOfPar);
    List<Expression> helper = new ArrayList<>();

    //Ensures that all all fields have the right type 
    JmlQuantifiedExpr qe = new JmlQuantifiedExpr();
    qe.setBinder(JmlQuantifiedExpr.JmlDefaultBinder.FORALL);
    qe.setVariables(getVariables(index));
    qe.setExpressions(NodeList.nodeList(
        getSeqRange(index, field),
        new InstanceOfExpr( // <p>[i] instanceOf <P>
            new ArrayAccessExpr(
                new EnclosedExpr(
                    new CastExpr(
                        new ClassOrInterfaceType(
                            null,
                            new SimpleName("\\seq"),
                            null),
                        field)),
                new NameExpr(index)),
            //TODO: Constructor requires ReferenceType but we can also return jml type
            //typeOfPar
            new ClassOrInterfaceType(
                null,
                new SimpleName(typeOfPar.asString()),
                null //NodeList.nodeList(): use list when generic, not supported by key yet
            ))));
    helper.add(qe);

    helper.addAll(quantHelper);

    return helper;
  }

  private NodeList<Parameter> getVariables(SimpleName index) {
    return NodeList.nodeList(new Parameter(
        PrimitiveType.intType(),
        index));
  }

  private JmlMultiCompareExpr getSeqRange(SimpleName index, Expression field) {
    return new JmlMultiCompareExpr(
        NodeList.nodeList(
            new IntegerLiteralExpr("0"),
            new NameExpr(index),
            new FieldAccessExpr(
                new EnclosedExpr(
                    new CastExpr(
                        new ClassOrInterfaceType(
                            null,
                            new SimpleName("\\seq"),
                            null),
                        field)),
                NodeList.nodeList(),
                new SimpleName("length"))),
        new JmlMultiCompareExpr.Operators(List.of(
            BinaryExpr.Operator.LESS_EQUALS,
            BinaryExpr.Operator.LESS)));
  }
}
