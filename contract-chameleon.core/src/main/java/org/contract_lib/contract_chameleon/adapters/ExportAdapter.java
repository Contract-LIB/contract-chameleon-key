
package org.contract_lib.contract_chameleon.adapters;

import java.nio.file.Path;
import java.util.Optional;

import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext;
import org.contract_lib.contract_chameleon.contexts.SourcePathsContext;
import org.contract_lib.contract_chameleon.contexts.ResultDirectoryContext.Dir;

public abstract class ExportAdapter extends TranslationAdapter {

  private ResultDirectoryContext result;

  @Override
  public final void performTranslation() {

    Optional<SourcePathsContext> sourcesContext = getContext(SourcePathsContext.class);
    Optional<ResultDirectoryContext> resultContext = getContext(ResultDirectoryContext.class);
    if (sourcesContext.isEmpty()) {
      getMessageContext().logError("Interface: ExportAdapters requires SourcePathsContext.");
      return;
    }
    if (resultContext.isEmpty()) {
      getMessageContext().logError("Interface: ExportAdapters requires ResultDirectoryContext required.");
      return;
    }
    this.result = resultContext.get();

    if (sourcesContext.get().getPaths().size() == 1) {
      this.performForPath(sourcesContext.get().getPaths().getFirst(), this.result.getResultDirectory());
    } else {
      sourcesContext.get().getPaths().forEach(this::performAddingSubdir);
    }
  }

  final void performAddingSubdir(Path p) {
    String filename = p.getFileName().toString();
    Dir finalDir = result.getResultDirectory().addSubDirectories(filename);
    //NOTE: As there might be multiple Contract-LIB files that have the same name, this will just override I think.
    performForPath(p, finalDir);
  }

  /** Given a path to a single ContractLIB file, creates a translation in the result directory.
   *
   * @param p the path to the ContractLIB file.
   * @param finalDir the directory where the results should be written.
   */
  public abstract void performForPath(Path p, Dir finalDir);

}
