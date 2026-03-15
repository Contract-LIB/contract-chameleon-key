
package org.contract_lib.contract_chameleon.contexts;

import org.contract_lib.contract_chameleon.AdapterId;
import org.contract_lib.contract_chameleon.SharedContextManager;
import org.contract_lib.contract_chameleon.error.ChameleonException;
import org.contract_lib.contract_chameleon.error.ChameleonFileReportable;
import org.contract_lib.contract_chameleon.error.ChameleonMessageManager;
import org.contract_lib.contract_chameleon.error.ChameleonReportable;

/** The {@link MessageContext} is responsible for handeling log and error messages.
 * <p>
 * The idea of the creation of error messages in contract-chameleon is,
 * that it does not crash on the first problem,
 * but collects all problems,
 * until it cannot continue the translation in all possible locations.
 * As a consequence each run should generate a rich list of meaningful errors,
 * that can be fixed, before running contract-chameleon again.
 * <p>
 * It differentiates between different log messages:
 * <ul>
 * <li> <b>Exception</b>: Exceptions that are not caused by the translations itself and rather by the system (e.g. file access, permissions, …).
 * <li> <b>Error</b>: Error in the translation. Any translation produced should be considered invalid, or no translation can be produced.
 * <li> <b>Warning</b>: There might be cases that (different) adapters are not able to handle the provided input.
 * <li> <b>Info</b>: Message to the user, what contract-chameleon is duing.
 * <li> <b>Debug</b>: Detailed messages about the translation stepps. Useful for debugging.
 * </ul>
 * <p>
 * It groups the error messages by their origin (file, line, position), if such a location can be determined.
 * <p>
 * It provides information about the adapter that caused the message, if such information is provided.
 * <p>
 * It provides information about the stage of the translation process, if such information is provided.
 * <p>
 * It supports different interfaces, how the logging is presented to the user.
 * {@link MessageContext.ContextLogger} gives access to the logged objects itself,
 * and can implement its own logic or mapping on how objects should be logged.
 * {@link MessageContext.StringLogger} just gets all information in one {@link String},
 * that should be printed by the logger.
 * <p>
 * You can configure a message context by different log levels:
 * <ol>
 * <li> <b>Only Errors</b>: Only logs exception and error messages.
 * <li> <b>Warn</b>: Logs exception, error, and warning messages.
 * <li> <b>Info (Default)</b> Logs exception, error, warning and info messages.
 * <li> <b>Details</b> Logs exception, error, warning and info messages (also prints hints where available).
 * <li> <b>Debug</b> Logs exception, error, warning, info and debug messages.
 * </ol>
 */
public final class MessageContext implements
    SharedContextManager.InterfaceProvidedContext,
    SharedContextManager.DefaultContext {

  //TODO: Decide about the proper interface of this context.

  // TODO: Remove dependencies on message manager. Only this class should manage all messages.
  private ChameleonMessageManager manager;
  private ContextLogger logger;

  public MessageContext(
      ContextLogger contextLogger) {
    this.logger = contextLogger;
    this.manager = new ChameleonMessageManager();
  }

  public MessageContext(StringLogger stringLogger) {
    this.logger = new ContextToStringLogger(stringLogger);
    this.manager = new ChameleonMessageManager();
  }

  /** Log a complete message for a known origin.
   * 
   * @param adapter The adapter that caused this message.
   * @param stage The state where the message was caused.
   * @param type The type of the message.
   * @param location The location where the message origns.
   * @param message The message itself.
   * @param detail A detaild description (or a hint).
   */
  public void log(
      AdapterId adapter,
      Stage stage,
      MessageType type,
      Location origin,
      String message,
      String detail) {
    this.manager.report(new ChameleonReportable() {
      @Override
      public String getMessage() {
        return String.format("%s (%s,%s) in %s: %s %n%n %s",
            type.toString(),
            adapter.getAdapterName(),
            stage.getStageName(),
            origin.locationIdentifier(),
            message,
            detail);
      }
    });
  }

  /** Log a complete message for a known location in a file.
   * 
   * @param adapter The adapter that caused this message.
   * @param stage The state where the message was caused.
   * @param type The type of the message.
   * @param fileLocation The location where the message origns.
   * @param message The message itself.
   * @param detail A detaild description (or a hint).
   */
  public void log(
      AdapterId adapter,
      Stage stage,
      MessageType type,
      FileLocation fileLocation,
      String message,
      String hint) {
    this.manager.report(new ChameleonReportable() {
      @Override
      public String getMessage() {
        return String.format("%s (%s,%s) in %s (%d, %d): %s %n%n %s",
            type.toString(),
            adapter.getAdapterName(),
            stage.getStageName(),
            fileLocation.fileIdentifier(),
            fileLocation.line(),
            fileLocation.charPos(),
            message,
            hint);
      }
    });
  }

  public void logException(Exception exception) {
    this.manager.report(new ChameleonException(exception));
  }

  public void logError(String string) {
    this.manager.report(new ChameleonReportable() {
      @Override
      public String getMessage() {
        return "ERROR: " + string;
      }
    });
  }

  public void logWarning(String string) {
    this.manager.report(new ChameleonReportable() {
      @Override
      public String getMessage() {
        return "WARNING: " + string;
      }
    });
  }

  public void logInfo(String string) {
    this.manager.report(new ChameleonReportable() {
      @Override
      public String getMessage() {
        return "Info: " + string;
      }
    });
  }

  public void logFile(MessageType type, String locationIdentifier, int line, int pos, String message) {
    this.manager.report(new ChameleonFileReportable() {
      @Override
      public String messageType() {
        return type.toString();
      }

      @Override
      public String getLocationIdentifier() {
        return locationIdentifier;
      }

      @Override
      public int getLine() {
        return line;
      }

      @Override
      public int getCharIndex() {
        return pos;
      }

      @Override
      public String getMessage() {
        return message;
      }
    });
  }

  /** Log all messages in a structured way, using the provided logger.
   * <p>
   * This method should be called by the interface after all adapters are run.
   */
  public void log() {
    //TODO: fallback on the old message manager interface to manage messages.
    this.manager.getMessages()
        .forEachOrdered((m) -> this.logger.log(m.getMessage()));
  }

  public ChameleonMessageManager getMessageManager() {
    return manager;
  }

  public interface ContextLogger {
    //TODO: This interface is incomplete.
    void log(String string);
  }

  public interface StringLogger {
    void log(String string);
  }

  public enum MessageType {
    EXEPTION,
    ERROR,
    WARNING,
    INFO,
    DEBUG;
  }

  public enum LogLevel {
    ONLY_ERRORS,
    WARN,
    INFO,
    DETAILS,
    DEBUG;
  }

  public interface Stage {
    String getStageName();
  }

  public interface Location {
    String locationIdentifier();
  }

  public interface FileLocation {
    String fileIdentifier();

    int line();

    int charPos();
  }

  /// Private logger, if no custom context logger is provided.
  private final class ContextToStringLogger implements ContextLogger {
    private StringLogger logger;

    ContextToStringLogger(StringLogger logger) {
      this.logger = logger;
    }

    @Override
    public void log(String string) {
      logger.log(string);
    }
  }
}
