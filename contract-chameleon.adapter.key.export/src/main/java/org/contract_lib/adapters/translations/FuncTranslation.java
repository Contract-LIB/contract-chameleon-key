package org.contract_lib.adapters.translations;

import java.util.List;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

import com.github.javaparser.ast.type.Type;

import org.contract_lib.lang.contract_lib.ast.Sort;
import org.contract_lib.lang.contract_lib.ast.Symbol;
import org.contract_lib.lang.contract_lib.ast.Term;

public interface FuncTranslation {
  Term.Identifier.IdentifierValue getClibIdentifier();

  Expression getJmlTerm(List<Expression> parameters);

  List<Sort> getClibParameterTypes();

  List<Type> getJmlParameterTypes();

  Sort getClibResultType();

  Type getJmlResultType();

  @FunctionalInterface
  public interface MethodExprInterface {
    Expression apply(List<Expression> expressions);
  }

  public record MethodExpr(
      String methodOperator,
      MethodExprInterface exprInterface,
      List<Sort> clibArgumentSorts,
      List<Type> jmlArgumentTypes,
      Type jmlResultType,
      Sort cLibResultType) implements FuncTranslation {

    public Term.Identifier.IdentifierValue getClibIdentifier() {
      return new Term.Identifier.IdentifierValue(new Symbol(this.methodOperator));
    }

    public Expression getJmlTerm(List<Expression> parameters) {
      if (parameters.size() != clibArgumentSorts.size()) {
        //TODO: Throw proper error
        System.err.println(
            String.format("Arguments to not match for '%s', given: %s",
                methodOperator,
                parameters));
      }

      return exprInterface.apply(parameters);
    }

    public List<Sort> getClibParameterTypes() {
      return clibArgumentSorts;
    }

    public List<Type> getJmlParameterTypes() {
      return jmlArgumentTypes;
    }

    public Sort getClibResultType() {
      return cLibResultType;
    }

    public Type getJmlResultType() {
      return jmlResultType;
    }
  }

  public record MethodCall(
      String methodOperator,
      String jmlMethodName,
      List<Sort> clibArgumentSorts,
      List<Type> jmlArgumentTypes,
      Type jmlResultType,
      Sort cLibResultType) implements FuncTranslation {

    public Term.Identifier.IdentifierValue getClibIdentifier() {
      return new Term.Identifier.IdentifierValue(new Symbol(this.methodOperator));
    }

    public Expression getJmlTerm(List<Expression> parameters) {
      if (parameters.size() != clibArgumentSorts.size()) {
        //TODO: Throw proper error
        System.err.println(
            String.format("Arguments to not match for '%s', given: %s",
                methodOperator,
                parameters));
      }

      return new MethodCallExpr(
          null, //scope
          jmlMethodName,
          NodeList.nodeList(parameters));
    }

    public List<Sort> getClibParameterTypes() {
      return clibArgumentSorts;
    }

    public List<Type> getJmlParameterTypes() {
      return jmlArgumentTypes;
    }

    public Sort getClibResultType() {
      return cLibResultType;
    }

    public Type getJmlResultType() {
      return jmlResultType;
    }
  }

  public record ConstantTranslation(
      String constantCLibOperator,
      Expression constantJmlExpr,
      Type jmlResultType,
      Sort cLibResultType) implements FuncTranslation {

    public Term.Identifier.IdentifierValue getClibIdentifier() {
      return new Term.Identifier.IdentifierValue(new Symbol(this.constantCLibOperator));
    }

    public Expression getJmlTerm(List<Expression> parameters) {
      if (parameters.size() != 0) {
        //TODO: Throw proper error
        System.err.println(
            String.format("A constant '%s' must only not have any parameter, given: %s",
                constantCLibOperator,
                parameters));
      }
      return constantJmlExpr;
    }

    public List<Sort> getClibParameterTypes() {
      return List.of();
    }

    public List<Type> getJmlParameterTypes() {
      return List.of();
    }

    public Sort getClibResultType() {
      return cLibResultType;
    }

    public Type getJmlResultType() {
      return jmlResultType;
    }
  }

  public record UnaryTranslation(
      String unaryCLibOperator,
      UnaryExpr.Operator unaryJmlOperator,
      Type jmlType,
      Type jmlResultType,
      Sort cLibType,
      Sort cLibResultType) implements FuncTranslation {

    public Term.Identifier.IdentifierValue getClibIdentifier() {
      return new Term.Identifier.IdentifierValue(new Symbol(this.unaryCLibOperator));
    }

    public Expression getJmlTerm(List<Expression> parameters) {
      if (parameters.size() != 1) {
        //TODO: Throw proper error
        System.err.println(
            String.format("A unary operator '%s' must only have one parameter, given: %s",
                unaryCLibOperator,
                parameters));
      }
      Expression expr = parameters.get(0);
      return new UnaryExpr(
          new EnclosedExpr(expr),
          this.unaryJmlOperator);
    }

    public List<Sort> getClibParameterTypes() {
      return List.of(
          cLibType);
    }

    public List<Type> getJmlParameterTypes() {
      return List.of(
          jmlType);
    }

    public Sort getClibResultType() {
      return cLibResultType;
    }

    public Type getJmlResultType() {
      return jmlResultType;
    }
  }

  public record BinaryTranslation(
      String binaryCLibOperator,
      BinaryExpr.Operator binaryJmlOperator,
      Type jmlType,
      Type jmlResultType,
      Sort cLibType,
      Sort cLibResultType) implements FuncTranslation {

    public Term.Identifier.IdentifierValue getClibIdentifier() {
      return new Term.Identifier.IdentifierValue(new Symbol(this.binaryCLibOperator));
    }

    public Expression getJmlTerm(List<Expression> parameters) {
      if (parameters.size() != 2) {
        //TODO: Throw proper error
        String.format("A binary operator '%s' must only have one parameter, given: %s", binaryCLibOperator, parameters);
      }
      Expression left = parameters.get(0);
      Expression right = parameters.get(1);
      return new BinaryExpr(
          new EnclosedExpr(left),
          new EnclosedExpr(right),
          this.binaryJmlOperator);
    }

    public List<Sort> getClibParameterTypes() {
      return List.of(
          cLibType,
          cLibType);
    }

    public List<Type> getJmlParameterTypes() {
      return List.of(
          jmlType,
          jmlType);
    }

    public Sort getClibResultType() {
      return cLibResultType;
    }

    public Type getJmlResultType() {
      return jmlResultType;
    }
  }
}
