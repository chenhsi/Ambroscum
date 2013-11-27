/**
 * Runs test cases from the /tests folder.
 *
 * USAGE: java AutoTester
 * Place test files in the /tests directory.
 * Test file containing code should be called *.ambr; the correct output should be in *.ambr.correct.
 * 
 * @author Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.tests;

import java.io.*;
import java.util.*;
import ambroscum.Interpreter;

public class AutoTester {
	
	public static final String TEST_DIRECTORY = System.getProperty("user.dir") + "/tests",
								TEST_EXTENSION = ".ambr",
								CORRECT_EXTENSION = ".correct",
								ERROR_PREFIX = "ERROR: ";
	
	public static void main(String[] args) throws IOException {
		PrintStream systemOut = System.out;
		
		File testsFolder = new File(TEST_DIRECTORY);
		File[] testFiles = testsFolder.listFiles();
		for (File file : testFiles) {
			String fileName= file.getName();
			if (fileName.lastIndexOf(TEST_EXTENSION) < fileName.length() - TEST_EXTENSION.length()) {
				// This is not a test file
				continue;
			}
			
			PipedOutputStream pipeOut = new PipedOutputStream();
			PipedInputStream pipeIn = new PipedInputStream(pipeOut);
			System.setOut(new PrintStream(pipeOut));

			// A hack for now.
			boolean hadError = false;
			try {
				Interpreter.interpret(file, false);
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
				hadError = true;
			}
			
			StringBuilder testOutput = new StringBuilder();
			while (pipeIn.available() > 0) {
				int read = pipeIn.read();
				testOutput.append((char) read);
			}
			String testOutputStr = testOutput.toString().replaceAll(System.getProperty("line.separator"), "\n").trim();
			
			StringBuilder correctOutputStrBldr = new StringBuilder();
			
			Scanner correctFileScanner = new Scanner(new File(file.getPath() + CORRECT_EXTENSION));
			Scanner testOutputScanner = new Scanner(testOutputStr);
			
			boolean correct = true;
			while (correctFileScanner.hasNextLine()) {
				String correctLine = correctFileScanner.nextLine();
				correctOutputStrBldr.append(correctLine + "\n");
				String testLine = testOutputScanner.hasNextLine() ? testOutputScanner.nextLine() : null;
				// Not enough output - incorrect
				if (testLine == null) {
					correct = false;
					break;
				}
				// Check if this line was supposed to be an error.
				if (correctLine.indexOf(ERROR_PREFIX) == 0) {
					String expectedError = correctLine.substring(ERROR_PREFIX.length());
					// Either not an error, or the wrong error
					if (testLine.indexOf(expectedError) != 0) {
						correct = false;
					}
					break;
				}
				// Incorrect output
				if (!correctLine.equals(testLine)) {
					correct = false;
					break;
				}
			}
			
			// Read in the rest of the correct output
			while (correctFileScanner.hasNextLine()) {
				correctOutputStrBldr.append(correctFileScanner.nextLine() + "\n");
			}
			
			String correctOutputStr = correctOutputStrBldr.toString().replaceAll(System.getProperty("line.separator"), "\n").trim();
			if (correct) {
				System.err.println("Tests passed for " + fileName);
			} else {
				System.err.println("Incorrect output for " + fileName);
				System.err.println("-----------");
				System.err.println(testOutputStr);
				System.err.println("-----------");
				System.err.println(correctOutputStr);
			}
			System.err.println("======================");
		}
	}
}
