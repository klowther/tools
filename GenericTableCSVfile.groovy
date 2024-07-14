import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 ***********************************************************************
 * UNCLASSIFIED This class is used to process a CSV file produced from a generic
 * table.
 * 
 * @author D99510
 */
public class GenericTableCSVfile {
  ArrayList<String[]> lines = new ArrayList<String[]>();
  String filename = "";
  public enum sOptions {localcsv, opencsv, supercsv }; 

  String byteOrderMark = "ï»¿";  

  /**
   ***********************************************************************
   * UNCLASSIFIED gets the name of the file
   * 
   * @return the filename
   * @author D99510
   */
  public String getFilename() {
    return filename;
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Gets the file reader skipping the BOM if one exists
   * 
   * @param fName - the full path to file.
   * @return the file reader or null if error
   * @author D99510
   */
  public FileReader getReader(String fName) {
    FileReader fr = null;
    try {
      fr = new FileReader(fName);
      String tst = "";
      tst += (char) fr.read();
      tst += (char) fr.read();
      tst += (char) fr.read();
      if (!tst.equals("ï»¿")) {
        fr.close();
        fr = new FileReader(fName);
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return fr;
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Processes the CSV file and stores the lines in arrays of strings
   * 
   * @param filename - the name of the CSV file
   * @author D99510
   */
  private void setupLocal(String filename) throws IOException {
    File csvFile;
    csvFile = new File(filename);
    
    try {
      FileReader fr = new FileReader(csvFile);
      BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
      String line;
      line = br.readLine(); 
      while (line != null) {
        String[] sdata = CSVUtils.splitCSV(line);
        sdata = trimLine(sdata);
        lines.add(trimLine(sdata));
        line = br.readLine();
      }
      br.close();
    } catch (Exception e) {
      return;
    }
    
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Handles the line if it has a byte order mark in it
   * 
   * @param filename - the name of the CSV file
   * @author D99510
   */
  String[] trimLine(String[] str) {
    int len = str.length;
    int start = 0;
    int end = len - 1;
    if (str[len - 1].equals("")) {
      end = len - 2;
    }
    if (str[0].contains(byteOrderMark))
      str[0] = str[0].replace(byteOrderMark, "");
    
    len = end - start + 1;
      
    String[] rv = new String[len];
    int index = 0;
    for (int i = start; i <= end; i++) {
      rv[index] = str[i];
      index++;
    }
    return rv;
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Processes the CSV file and stores the lines in arrays of strings
   * 
   * @param filename - the name of the CSV file
   * @author D99510
   */
  private void setupOpenCSV(String filename) throws IOException {
    FileReader fr = getReader(filename);
    if (fr != null) {
      try (CSVReader csvReader = new CSVReader(fr)) {
        lines.addAll(csvReader.readAll());
      }
    }
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Returns an arraylist of the lines which are stored in arrays of
   * strings(tokens)
   * 
   * @return the lines from the CSV file
   * @author D99510
   */
  public ArrayList<String[]> getLines() {
    return lines;
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Returns an an array of strings representing the tokens
   * 
   * @param - the line number
   * @return the line from the CSV file
   * @author D99510
   */
  public String[] getLine(int lineNumber) {
    if (lineNumber < 0 || lineNumber > lines.size() - 1)
      return null;
    return lines.get(lineNumber);
  }

  /**
   ***********************************************************************
   * UNCLASSIFIED Sets the filename for the CSV file. 
   * !!!!THIS MUST BE CALLED BEFORE USING AN INSTANCE OF THIS CLASS
   * 
   * @param filename - the name of the CSV file
   * @author D99510
   * @return 
   * @throws IOException 
   */
  public void setFileName(String filename) throws IOException {
    setupOpenCSV(filename);
  }
}

GenericTableCSVfile gtf = new GenericTableCSVfile();
gtf
