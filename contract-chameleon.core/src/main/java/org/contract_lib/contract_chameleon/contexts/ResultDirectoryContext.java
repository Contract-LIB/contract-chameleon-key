package org.contract_lib.contract_chameleon.contexts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.contract_lib.contract_chameleon.AdapterId;
import org.contract_lib.contract_chameleon.SharedContextManager;

/** Context to the context where the results should be generated.
 * <p>
 * You can access the {@link Dir} where the results should be written via {@link ResultDirectoryContext#getResultDirectory()}.
 * On the {@link Dir} you can then write a file ({@link Dir#writeFile(String, String, WritableResult)},
 * write a {@link TranslationResult} ({@link Dir#writeResult(TranslationResult)})
 * or add subdirectories ({@link Dir#addSubDirectories(String...)}).
 * <p>
 * Depending on your interface,
 * the {@link AdapterId} of current adapter is used as the name of the result directory,
 * or a custom path if such is provided by the user.
 * <p>
 * This allows to run multiple adapters,
 * without worring about conflics in the generated files,
 * as by default each adapter only writes to its own directory.
 */
public class ResultDirectoryContext implements SharedContextManager.InterfaceProvidedContext {

  private static final String CANT_OVERRRIDE_MESSAGE = "File at %s does already exist, change overrride mode to override the file!";
  private static final String OVERRRIDE_MESSAGE = "File at %s does already exist, will be overridden!";

  private final SharedContextManager sharedContextManager;
  private final Optional<String> resultDirectory;
  private final OverrideMode overrideMode;

  /// Default constructor when no custom path is set for the result directory.
  public ResultDirectoryContext(SharedContextManager sharedContextManager, OverrideMode overrideMode) {
    this(sharedContextManager, null, overrideMode);
  }

  /// Set the path of the result directory to a custom path.
  public ResultDirectoryContext(
      SharedContextManager sharedContextManager,
      String resultDirectory,
      OverrideMode overrideMode) {
    this.sharedContextManager = sharedContextManager;
    this.resultDirectory = Optional.ofNullable(resultDirectory);
    this.overrideMode = overrideMode;
  }

  /** Access a path to a directory where the results should be stored.
   * <p>
   * If no custom path was set by the provider, 
   * the working directory appended with the adapter identifier is 
   * @return the path under which all results should be stored.
   */
  public Dir getResultDirectory() {
    String adapterId = sharedContextManager
        .getDefaultContext(AdapterContext.class)
        .getAdapterIdentifer()
        .getAdapterName();

    Path resultPath = this.resultDirectory
        .map(Paths::get)
        .orElseGet(() -> Paths.get(".", adapterId));

    return new Dir(resultPath);
  }

  /** A directory where a result can be written to.
   */
  public final class Dir {
    private Path path;

    public Dir(Path path) {
      this.path = path;
    }

    public void writeResult(TranslationResult result) {

      if (Files.isDirectory(path)) {
        sharedContextManager
            .getMessageContext()
            .logInfo(String.format("Directory at %s does already exist.", path));
      } else {
        try {
          Files.createDirectories(path);
        } catch (IOException e) {
          sharedContextManager
              .getMessageContext()
              .logException(e);
        }
      }
      writeFile(result.fileName(), result.fileEnding(), result::writeTo);
    }

    /** Write a result to the file.
     * 
     * @param filename The filename to write to.
     * @param fileEnding The file endig. Should start with a '.'.
     * @param content The content that should be written.
    */
    public void writeFile(String filename, String fileEnding, WritableResult content) {
      Path path = Paths.get(this.path.toString(), filename + fileEnding);

      // Handle overrride mode when file already exists.
      if (Files.exists(path)) {
        switch (overrideMode) {
          case NO_OVERRIDE -> {
            sharedContextManager
                .getMessageContext()
                .logError(String.format(CANT_OVERRRIDE_MESSAGE, path));
            return;
          }
          case OVERRIDE_WARN ->
            sharedContextManager
                .getMessageContext()
                .logWarning(String.format(OVERRRIDE_MESSAGE, path));
          case OVERRIDE ->
            sharedContextManager
                .getMessageContext()
                .logWarning(String.format(OVERRRIDE_MESSAGE, path));
        }
      }

      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        content.writeTo(writer);
      } catch (IOException e) {
        sharedContextManager
            .getMessageContext()
            .logException(e);
      }
    }

    public Dir addSubDirectories(String... components) {
      Path path = Paths.get(this.path.toString(), components);
      return new Dir(path);
    }
  }

  /// Functional interface of content that can be written.
  @FunctionalInterface
  public interface WritableResult {
    /** Write handler how the content should be written to the file.
     * 
     * @param writer the object to which the result should be written to. 
     * @throws IOException if there occures an {@link IOException} in the writing process.
     */
    void writeTo(Writer writer) throws IOException;
  }

  /// Extend interface for written content, especially for complete results.
  public interface TranslationResult extends WritableResult {
    /** The name of the directory where the file should be stored.
     * <p>
     * Leave empty, if you want to place it at the directory where the result should be written. 
     */
    Optional<String> extendSubDirectory();

    /** The name under which the file should be stored. 
     */
    String fileName();

    /** The fileending of the file stored.
    * <p>
    * Should start with a dot, like {@code .clib}.
    */
    String fileEnding();
  }

  /// Different mode how existing files in the result directory are handled.
  public enum OverrideMode {
    /// No file is overridden, and if it would be the case an error is produced.
    NO_OVERRIDE,
    /// Files are overridden, and if so an waning is produced.
    OVERRIDE_WARN,
    /// Files are overridden.
    OVERRIDE;
  }
}
