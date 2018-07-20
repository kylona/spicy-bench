package cg.omp.trace;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Trace extends ArrayList<Event> {
  /**
   * Creates a trace object from a log of a trace
   * @param fileName The name of a file which contains an executed trace of an OMP program
   */
  public Trace(String fileName) {
    parseFile(fileName);
  }

  private void parseFile(String fileName) {
//    try {
//      Scanner scanner = new Scanner(new FileInputStream(""));
//    } catch (FileNotFoundException e) {
//      System.err.println("The ")
//    }
  }
}
