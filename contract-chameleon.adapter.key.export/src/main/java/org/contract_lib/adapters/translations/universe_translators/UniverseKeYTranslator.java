package org.contract_lib.adapters.translations.universe_translators;

import java.util.List;

import org.contract_lib.adapters.translations.ContractTranslator;
import org.contract_lib.adapters.translations.TermTranslator;
import org.contract_lib.adapters.translations.TypeTranslator;
import org.contract_lib.adapters.translations.default_translators.DefaultKeYTranslator;
import org.contract_lib.adapters.translations.default_translators.SimpleResult;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.TranslationResult;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.lang.contract_lib.ast.Abstraction;
import org.contract_lib.lang.contract_lib.ast.SelectorDec;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.jml.body.JmlClassAccessibleDeclaration;

public class UniverseKeYTranslator extends DefaultKeYTranslator {

  public UniverseKeYTranslator(ChameleonMessageManager messageManager) {
    super(messageManager);
  }

  @Override
  protected TypeTranslator typeTranslatorFabric() {
    return super.typeTranslatorFabric();

    // TODO Replace Ref & Abstraction translation

    /*TODO: probably it it best to add this directly to the type translations
     
    @Override
    public List<Parameter> getParameters() {
    return super.getParameters().stream()
        .map(p -> p.getType().isReferenceType()
            ? p.addAnnotation("Payload")
            : p)
        .toList();
    }
    
    protected Type getReturnT() {
    return super.getReturnType()
        .map(t -> {
          if (t.isReferenceType()) {
            t.annotations().add(annotation(isStatic ? "Peer" : "Payload"));
          }
          return t;
        }).orElseGet(VoidType::new);
    }
    */
  }

  @Override
  protected ContractTranslator contractTranslatorFabric(
      TypeTranslator typeTranslator,
      TermTranslator termTranslator,
      ChameleonMessageManager messageManager) {
    return new UniverseContractTranslator(
        typeTranslator,
        termTranslator,
        FOOTPRINT_NAME,
        RESULT_LABEL_CONTRACT_LIB,
        RESULT_LABEL_JML,
        THIS_LABEL_CONTRACT_LIB,
        THIS_LABEL_JML,
        messageManager);
  }

  public static void addImport(List<TranslationResult> results, String importString) {
    results.stream()
        .filter(c -> c instanceof SimpleResult)
        .forEach(c -> {
          SimpleResult unit = (SimpleResult) c;
          unit.cu().addImport(importString);
        });
  }

  private static MethodCallExpr footprint() {
    return new MethodCallExpr(
        null,
        new SimpleName("\\dl_createdRepfp"),
        NodeList.nodeList(new ThisExpr()));
  }

  @Override
  protected void annotateMethodDecl(CallableDeclaration<?> decl) {
    if (!decl.isStatic())
      decl.addMarkerAnnotation("RepOnly");
  }

  @Override
  protected void addAbstractionFootprint(ClassOrInterfaceDeclaration dec) {
  }

  @Override
  protected void annotateClassOrInterfaceDecl(ClassOrInterfaceDeclaration decl) {
    decl.addMarkerAnnotation("DSI");
  }

  @Override
  protected void addAccessibleDef(ClassOrInterfaceDeclaration dec, List<SelectorDec> selector) {
    // All invariants have to relay on footprint
    JmlClassAccessibleDeclaration accessInv = new JmlClassAccessibleDeclaration(
        NodeList.nodeList(),
        NodeList.nodeList(),
        new NameExpr(new SimpleName("\\inv")),
        NodeList.nodeList(footprint()),
        null);

    dec.addMember(accessInv
        .addModifier(Modifier.DefaultKeyword.PUBLIC)
        .addModifier(Modifier.DefaultKeyword.JML_INSTANCE));
  }

  @Override
  protected void addImplementationFootprint(ClassOrInterfaceDeclaration dec, Abstraction abstraction) {
  }
}
