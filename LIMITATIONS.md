# Limitations in the translations

## Well-definedness

- To make a translation, the `Contract-LIB` specification must be well-defined.
- Recursive Functions are not well-defined in `KeY`
  (compare [KeY issue 3155](https://github.com/KeYProject/key/issues/3155)).

## KeY specific

- Opaque References (`Ref<T>`) might hurt the well encapsulated property,
  when their state is accessed.
  (This can be checked with `Universe Types`.)

## Functional Limitations

- There is no support for generic classes / methods.
- Not all functions of the default theories are supported by the translation.
- `let` and `match` statements are not supported by the translation.
