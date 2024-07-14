import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;

/**
 * ***********************************************************************
 * UNCLASSIFIED This class is used to create a logger. The only common logging
 * is achieved with tempLog which writes to c:\temp\temp.log. The temp.log file
 * is more or less used in place of writing to the console since as a Cameo
 * plugin that cannot be done.
 * 
 * @author D99510
 * *********************************************************************** */
public class Logger {
  private static Logger commonLogger = null;
  private static Logger tempLogger = null;
  private String mFileName = null;
  FileWriter logFile = null;
  boolean autoNewLine = true;
  static int LogLevel = 1; // 1 is the lowest level, 0 turns all logging off
  boolean echoStdout = false;
  static boolean loggingSuspended = false;

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Static temp logger used for programming debugging purpose,
   * static makes it shared among all different plugins using it. Note : not
   * thread safe
   * 
   * @param s - the string to log
   * @author D99510
   * *********************************************************************** */
  public static void templog(String s) {
    templog(s,true);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Static temp logger used for programming debugging purpose,
   * static makes it shared among all different plugins using it. Note : not
   * thread safe
   * 
   * @param s - the string to log
   * @param newLine - flag indicating if newline will be appended
   * @author D99510
   * *********************************************************************** */
  public static void templog(String s, boolean newLine) {

    if (tempLogger == null) {
      tempLogger = new Logger();
      tempLogger.setName(5, "C:\\temp\\temp.log");
    }
    if (!newLine)
      tempLogger.setAutoNewLine(false);
    tempLogger.log(s);
    if (!newLine)
      tempLogger.setAutoNewLine(true);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Static common logger used for general programmer logging
   * purpose, static makes it shared among all different plugins using it Note :
   * not thread safe
   * 
   * @param s - the string to log
   * @author D99510
   * *********************************************************************** */
  public static void commonlog(String s) {
    commonlog(s,true);
  }  

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Static common logger used for general programmer logging
   * purpose, static makes it shared among all different plugins using it Note :
   * not thread safe
   * 
   * @param s - the string to log
   * @param newLine - flag indicating if newline will be appended
   * @author D99510
   * *********************************************************************** */
  public static void commonlog(String s, boolean newLine) {

    if (commonLogger == null) {
      commonLogger = new Logger();
      commonLogger.setName(5, "C:\\temp\\common.log");
    }
    if (!newLine)
      commonLogger.setAutoNewLine(false);
    commonLogger.log(s);
    if (!newLine)
      commonLogger.setAutoNewLine(true);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Sets the logger to also echo what is logged to the console
   * 
   * @param echo - turns on or off the echo
   * @author D99510
   * *********************************************************************** */
  public void setEcho(boolean echo) {
    log("setting echo to " + echo);
    echoStdout = echo;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Sets the current level that will be logged.
   * 
   * @param level - the level to log at (higher usually means more logging)
   * @author D99510
   * *********************************************************************** */
  public void setLevel(int level) {
    log(1, "Changing logging level to " + level);
    LogLevel = level;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Gets the current level that will be logged.
   * 
   * @return level - the level to log at (higher usually means more logging)
   * @author D99510
   * *********************************************************************** */
  public int getLevel() {
    return LogLevel;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Sets the name for the logfile Note: path separators should be
   * "\\"
   * 
   * @param fileName - the name of the file to log to
   * @author D99510
   * *********************************************************************** */
  public void setName(String fileName)  {

    if (!fileName.isEmpty()) {
      try {
        logFile = new FileWriter(fileName);
      } catch (IOException e) {
        String eStr = e.getLocalizedMessage();
        eStr += "\nLogging to " + fileName + " suspended";
        JOptionPane.showMessageDialog(null, eStr);
        loggingSuspended = true;
      }
      mFileName = fileName;
    }
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Sets the name for the logfile Note: path separators should be
   * "\\"
   * 
   * @param level    - the initial logging level when the name is set
   * @param fileName - the name of the file to log to
   * @author D99510
   * *********************************************************************** */
  public void setName(int Level, String fileName) {
    setName(fileName);
    setLevel(Level);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Gets the name for the logfile
   * 
   * @return fileName - the name of the file to log to
   * @author D99510
   * *********************************************************************** */
  public String getName() {
    return mFileName;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Sets whether a newline should be automatically added to the
   * string after it is logged
   * 
   * @param anl - true to auto new line, false to not do this horrendous atrocity
   * @author D99510
   * *********************************************************************** */
  public void setAutoNewLine(boolean anl) {
    autoNewLine = anl;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Gets whether an atrocious newline is horrendously added to the
   * end of the string when logged.
   * 
   * @return true if heinous act of adding a new line, false if heinousness is
   *         lacking
   * @author D99510
   * *********************************************************************** */
  public boolean getAutoNewLine() {
    return autoNewLine;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Flushes the log so the last thing logged is not lost if there
   * is an exception. Also it allows the "tail" funciton to chase the end of file.
   * @author D99510
   * *********************************************************************** */
  public void Flush() {
    try {
      logFile.flush();
      logFile.close();
      logFile = new FileWriter(mFileName, true);
    } catch (IOException e) {
    }
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Logs the string to the log
   * 
   * @param msg - the message to log
   * @author D99510
   * *********************************************************************** */
  public void log(String msg) {
    if (loggingSuspended)
      return;
    
    if (msg == null)
      msg = " Trying to log null string";

    if (LogLevel > 0) {
      if (logFile != null) {
        try {
          logFile.write(msg);
          if (autoNewLine)
            logFile.write(System.lineSeparator());// newLine();
          Flush();
          if (echoStdout) {
            if (autoNewLine)
              System.out.println(msg);
            else
              System.out.print(msg);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Logs the exception to the log
   * 
   * @param exc - the exception to log
   * @author D99510
   * *********************************************************************** */
  public void log(Exception exc) {
    if (loggingSuspended)
      return;
    
    if (LogLevel > 0) {
      if (logFile != null) {
        try {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          exc.printStackTrace(pw);
          logFile.write("General Exception Logger=======");
          logFile.write(System.lineSeparator());// newLine();
          if (exc.getMessage() != null)
            logFile.write(exc.getMessage());
          logFile.write(System.lineSeparator());// newLine();
          logFile.write(sw.toString());
          logFile.write(System.lineSeparator());// newLine();
          Flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Logs the throwable to the log
   * 
   * @param msg - the throwable to log
   * @author D99510
   * *********************************************************************** */
  public void log(Throwable t) {
    if (loggingSuspended)
      return;
    
    if (LogLevel > 0) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      t.printStackTrace();
      pw.flush();
      sw.flush();
      log(sw.toString());
    }
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Conditionally logs the string to the log depending on the
   * current log level.
   * 
   * @param level - the level needed for logging
   * @param msg   - the message to log
   * @author D99510
   * *********************************************************************** */
  public void log(int level, String msg) {
    if (level <= LogLevel)
      log(msg);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Closes the log
   * 
   * @param keep - true to keep the log, false to delete the log
   * @author D99510
   * *********************************************************************** */
  public void close(boolean keep) {
    if (logFile != null)
      try {
        logFile.close();
        if (!keep && !mFileName.isEmpty()) {
          File f = new File(mFileName);
          f.delete();
        }
      } catch (IOException e) {
        System.out.println(e.getMessage() + " closing log file.");
      }
    logFile = null;
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - Closes the log
   * 
   * @author D99510
   * *********************************************************************** */
  public void close() {
    close(true);
  }

  /**
   * ***********************************************************************
   * UNCLASSIFIED - finalize() implementation, ensures the log is saved if
   * something happens like
   * 
   * @param keep - true to keep the log, false to delete the log
   * @author D99510
   * *********************************************************************** */
  public void finalize() {
    close(true);
  }
}

Logger l = new Logger();
;