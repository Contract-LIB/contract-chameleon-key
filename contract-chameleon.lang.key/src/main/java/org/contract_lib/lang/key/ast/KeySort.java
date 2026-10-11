package org.contract_lib.lang.key.ast;

import java.util.List;
import java.util.function.Function;

public interface KeySort {

  <R> R perform(
      Function<KeySort.Internal, R> internal,
      Function<KeySort.Custom, R> custom,
      Function<KeySort.Parametric, R> parametric);

  public record Internal(
      String name) implements KeySort {

    public static List<KeySort.Internal> getAll() {
      return List.of(
          getInt(),
          getBoolean(),
          getSeq());
    }

    public static KeySort.Internal getInt() {
      return new KeySort.Internal("int");
    }

    public static KeySort.Internal getBoolean() {
      return new KeySort.Internal("boolean");
    }

    public static KeySort.Internal getSeq() {
      return new KeySort.Internal("Seq");
    }

    public <R> R perform(
        Function<KeySort.Internal, R> internal,
        Function<KeySort.Custom, R> custom,
        Function<KeySort.Parametric, R> parametric) {
      return internal.apply(this);
    }
  }

  public record Custom(
      String name) implements KeySort {

    public <R> R perform(
        Function<KeySort.Internal, R> internal,
        Function<KeySort.Custom, R> custom,
        Function<KeySort.Parametric, R> parametric) {
      return custom.apply(this);
    }
  }

  public record Parametric(
      String name,
      List<KeySort> parameters) implements KeySort {

    public <R> R perform(
        Function<KeySort.Internal, R> internal,
        Function<KeySort.Custom, R> custom,
        Function<KeySort.Parametric, R> parametric) {
      return parametric.apply(this);
    }
  }
}
