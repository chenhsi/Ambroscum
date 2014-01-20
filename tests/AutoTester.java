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
	
	public static void main(String[] args) throws Exception {
		Scanner input = new Scanner(System.in);
		System.out.println("1 to run Interpreter tests, 2 to run compiler tests");
		boolean isInterpreter = input.next().equals("1");
		
		File testsFolder = new File(TEST_DIRECTORY);
		File[] testFiles = testsFolder.listFiles();
		for (File file : testFiles) {
			String fileName = file.getName();
			if (fileName.lastIndexOf(TEST_EXTENSION) < fileName.length() - TEST_EXTENSION.length()) {
				// This is not a test file
				continue;
			}
			
			if (isInterpreter) {
				interpreterTest(file);
			} else {
				compilerTest(file);
			}
		}
	}
	
	private static void interpreterTest(File file) throws IOException {
		String fileName = file.getName();
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
		
		Scanner correctFileScanner = new Scanner(new File(file.getPath() + CORRECT_EXTENSION));
		Scanner testOutputScanner = new Scanner(testOutputStr);
		
		Object[] result = compare(testOutputScanner, correctFileScanner, hadError);
		
		boolean correct = (boolean) result[0];
		testOutputStr = (String) result[1];
		String correctOutputStr = (String) result[2];
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
	private static void compilerTest(File file) throws IOException {
		// Compile the file
		// Execute the compiled file
		// Interpret the original script
		// Compare the two results
		// Output the comparison
	}
	// Output:
	// Object[] {boolean isCorrect, String testOutput, String correctOutput}
	private static Object[] compare(Scanner testOutputScanner, Scanner correctFileScanner, boolean hadError) {
		StringBuilder correctOutputStrBldr = new StringBuilder();
		StringBuilder testOutputStrBldr = new StringBuilder();

		boolean correct = true;
		while (testOutputScanner.hasNextLine()) {
			String correctLine = correctFileScanner.hasNextLine() ? correctFileScanner.nextLine() : null;
			if (correctLine != null) {
				correctOutputStrBldr.append(correctLine + "\n");
				String testLine = testOutputScanner.nextLine();
				testOutputStrBldr.append(testLine + "\n");
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
			} else {
				// Not enough output - incorrect
				correct = false;
				break;
			}
		}
		// Test output had extra stuff in it
		while (!hadError && testOutputScanner.hasNextLine()) {
			correct = false;
			testOutputStrBldr.append(testOutputScanner.nextLine() + "\n");
		}
		
		return new Object[] {correct, testOutputStrBldr.toString().replaceAll(System.getProperty("line.separator"), "\n").trim(), correctOutputStrBldr.toString().replaceAll(System.getProperty("line.separator"), "\n").trim()};
	}
}
