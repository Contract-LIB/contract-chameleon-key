package org.contract_lib.adapters.translations;

import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;
import java.util.function.Function;

import org.contract_lib.lang.contract_lib.ast.ContractLibAst;
import org.contract_lib.lang.contract_lib.ast.Datatype;
import org.contract_lib.lang.contract_lib.ast.Constructor;
import org.contract_lib.lang.contract_lib.ast.SelectorDec;
import org.contract_lib.lang.contract_lib.ast.FunctionDec;
import org.contract_lib.lang.contract_lib.ast.Constant;
import org.contract_lib.lang.contract_lib.ast.SortDec;
import org.contract_lib.lang.contract_lib.ast.Sort;

import org.contract_lib.lang.key.ast.KeyAst;
import org.contract_lib.lang.key.ast.KeySort;
import org.contract_lib.lang.key.ast.KeyFunction;
import org.contract_lib.lang.key.ast.KeyDatatype;
import org.contract_lib.lang.key.ast.KeyDatatypeConstructor;
import org.contract_lib.lang.key.ast.KeyArgument;

import org.contract_lib.lang.key.tools.KeyPrinter;
import org.contract_lib.adapters.translations.functions.LogicTypeFuncTranslation;
import org.contract_lib.contract_chameleon.adapters.ExportAdapter.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;

public class KeyTranslations {

  record KeyResult(
      KeyAst keyAst,
      ChameleonMessageManager manager) implements TranslationResult {

    public String directoryName() {
      return ".";
    }

    public String fileName() {
      return "key_provider";
    }

    public String fileEnding() {
      return ".key";
    }

    public void write(Writer writer) throws IOException {
      KeyPrinter printer = new KeyPrinter(writer, manager);
      printer.print(keyAst);
      printer.printNewLine();
      printer.printSources("\".\"");
      printer.printNewLine();
      printer.printChooseContract();
    }
  }

  private ChameleonMessageManager messageManager;

  public KeyTranslations(
      ChameleonMessageManager messageManager,
      TypeTranslation.TypeTranslator translator,
      Function<KeyTranslations, DatatypeTranslation> suppl) {
    this.messageManager = messageManager;
    this.sorts = new ArrayList<>();
    this.functions = new ArrayList<>();
    this.datatypes = new ArrayList<>();
    this.translator = translator;
    this.dtTranslation = suppl.apply(this);
    this.ltft = new ArrayList<>();

    defaultSorts();
  }

  private void defaultSorts() {
    KeySort.Custom set = new KeySort.Custom("set");
    KeySort.Custom any = new KeySort.Custom("any");
    KeySort i = KeySort.Internal.getInt();
    KeySort b = KeySort.Internal.getBoolean();
    this.sorts.add(set);
    this.functions.add(new KeyFunction.DefaultFunction(set, "finiteSetUnion", List.of(set, set)));
    this.functions.add(new KeyFunction.DefaultFunction(set, "finiteSetInter", List.of(set, set)));
    this.functions.add(new KeyFunction.DefaultFunction(set, "finiteSetMinus", List.of(set, set)));
    this.functions.add(new KeyFunction.DefaultFunction(b, "finiteSetMember", List.of(set, any)));
    this.functions.add(new KeyFunction.DefaultFunction(b, "finiteSetSubset", List.of(set, set)));
    this.functions.add(new KeyFunction.DefaultFunction(b, "finiteSetDisjunct", List.of(set, set)));
    this.functions.add(new KeyFunction.DefaultFunction(set, "finiteSetEmpty", List.of()));
    this.functions.add(new KeyFunction.DefaultFunction(set, "finiteSetSingleton", List.of(any)));
    this.functions.add(new KeyFunction.DefaultFunction(i, "finiteSetCard", List.of(set)));
  }

  private DatatypeTranslation dtTranslation;

  private List<LogicTypeFuncTranslation> ltft;

  private List<KeySort.Custom> sorts;
  private List<KeyFunction> functions;
  private List<KeyDatatype> datatypes;
  private TypeTranslation.TypeTranslator translator;

  public TranslationResult translate(ContractLibAst ast) {

    ast.datatypes().stream().forEach(dtTranslation::translateDatatype);

    //TODO: Add datatypes to sort translator
    //TODO: Define order, how the sorts are read
    //TODO: Add constructors to sort translator

    KeyAst keyAst = new KeyAst(
        new ArrayList<>(sorts),
        new ArrayList<>(functions),
        new ArrayList<>(datatypes));

    return new KeyResult(
        keyAst,
        messageManager);
  }

  public List<FuncTranslation> getCons() {
    return new ArrayList<>(ltft);
  }

  public List<KeySort.Custom> getSorts() {
    List<KeySort.Custom> sorts = new ArrayList<>(this.sorts);
    datatypes.stream().map(KeyDatatype::datatype).forEach(sorts::add);
    return sorts;
  }

  private KeySort translateKeySort(Sort s) {

    TypeTranslation translation = translator.translate(s);
    if (translation != null) {
      return translation.getKeySort(s);
    }
    return new KeySort.Custom(s.getName());
  }

  private KeySort translateSelector(SelectorDec sdec) {
    //TODO: Convert Interface to Optional
    return translateKeySort(sdec.sort());
  }

  private void translateFunctionDec(FunctionDec funcDec) {
    //TODO: print error not supported yet
    System.err.println("Translation of function with key are not supported yet.");
  }

  private void translateConstant(Constant c) {
    //TODO: print error not supported yet
    System.err.println("Translation constants with key are not supported yet.");
  }

  private void translateSortDecDef(SortDec.Def sd) {
    //TODO: print error not supported yet
    System.err.println("Translation Sorts with key are not supported yet.");
  }

  private void translateSortDecParameter(SortDec.Parameter sp) {
    //TODO: print error not supported yet
    System.err.println("Translation of Sort Parameters with key are not supported yet.");
  }

  public interface DatatypeTranslation {
    void translateDatatype(Datatype dt);
  }

  public record ToSortTranslation(
      KeyTranslations keyTranslations) implements DatatypeTranslation {

    public void translateDatatype(Datatype dt) {
      String name = dt.identifier().name().identifier();

      //TODO: Parameters are not represented in key, type casting has to be done through the application functions
      KeySort.Custom sort = new KeySort.Custom(name);
      keyTranslations.sorts.add(sort);
      dt.dtDec().constructors().forEach((c) -> this.translateConstructor(sort, c));
    }

    private void translateConstructor(KeySort.Custom returnSort, Constructor c) {
      String name = c.identifier().identifier();

      List<KeySort> parameters = c.selectors()
          .stream()
          .map(keyTranslations::translateSelector)
          .collect(Collectors.toList());

      keyTranslations.ltft.add(
          new LogicTypeFuncTranslation(
              returnSort, name));

      keyTranslations.functions.add(new KeyFunction.UniqueFunction(
          new KeyFunction.DefaultFunction(returnSort, name, parameters)));
    }
  }

  public record ToDatatypeTranslation(
      KeyTranslations keyTranslations) implements DatatypeTranslation {
    public void translateDatatype(Datatype dt) {
      String name = dt.identifier().name().identifier();

      KeySort.Custom sort = new KeySort.Custom(name);
      List<KeyDatatypeConstructor> constructors = dt.dtDec().constructors()
          .stream()
          .map(this::translateConstructor)
          .collect(Collectors.toList());
      KeyDatatype datatype = new KeyDatatype(
          sort,
          constructors);

      constructors.forEach(c -> {
        keyTranslations.ltft.add(
            new LogicTypeFuncTranslation(
                datatype.datatype(), c.name()));
      });

      keyTranslations.datatypes.add(datatype);
    }

    public KeyDatatypeConstructor translateConstructor(Constructor c) {
      String name = c.identifier().identifier();

      List<KeyArgument> args = c.selectors()
          .stream()
          .map(this::translateSelector)
          .collect(Collectors.toList());

      return new KeyDatatypeConstructor(
          name,
          args);
    }

    public KeyArgument translateSelector(SelectorDec sdec) {
      KeySort sort = keyTranslations.translateKeySort(sdec.sort());
      String name = sdec.symbol().identifier();
      return new KeyArgument(
          sort,
          name);
    }
  }
}
