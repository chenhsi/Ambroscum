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
import ambroscum.compiler.*;
import ambroscum.errors.AmbroscumError;

public class AutoTester {
	
	public static final String TEST_DIRECTORY = System.getProperty("user.dir") + "/tests",
								TEST_EXTENSION = ".ambr",
								CORRECT_EXTENSION = ".correct",
								ERROR_PREFIX = "ERROR: ";
	
	public static void main(String[] args) throws Exception {
		Scanner input = new Scanner(System.in);
		System.out.println("1 to run Interpreter tests, 2 to run compiler tests");
		boolean isInterpreter = true;
		
		File testsFolder = new File(TEST_DIRECTORY);
		File[] testFiles = testsFolder.listFiles();
		for (File file : testFiles) {
			try {
				String fileName = file.getName();
				int extensionIndex = fileName.lastIndexOf(TEST_EXTENSION);
				if (extensionIndex < fileName.length() - TEST_EXTENSION.length() || extensionIndex < 0) {
					// This is not a test file
					continue;
				}
				
				if (isInterpreter) {
					interpreterTest(file);
				} else {
					compilerTest(file);
				}
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				System.err.println("======================");
				continue;
			}
			System.err.println("======================");
		}
	}
	
	private static void interpreterTest(File file) throws Exception {
		String fileName = file.getName();
		PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream pipeIn = new PipedInputStream(pipeOut);
		System.setOut(new PrintStream(pipeOut));

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
		
		StringBuilder testOutputStrBuilder = null;
		try {
			testOutputStrBuilder = interpreterExecute(file);
		} catch (AmbroscumError ex) {
			// NOTE: Not sure if this is actually necessary
			hadError = true;
		}
		if (testOutputStrBuilder == null) {
			// Something went horribly wrong
			System.err.println("Something bad happened when interpreting " + fileName);
			return;
		}
		
		String testOutputStr = fixNewlines(testOutputStrBuilder.toString());
		
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
		
	}
	private static StringBuilder interpreterExecute(File file) throws Exception {
		PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream pipeIn = new PipedInputStream(pipeOut);
		System.setOut(new PrintStream(pipeOut));

		try {
			Interpreter.interpret(file, false);
		} catch (AmbroscumError ex) {
			// This is to be expected; don't do anything about it
		}
		
		StringBuilder testOutput = new StringBuilder();
		while (pipeIn.available() > 0) {
			int read = pipeIn.read();
			testOutput.append((char) read);
		}
		String testOutputStr = testOutput.toString().replaceAll(System.getProperty("line.separator"), "\n").trim();
		return testOutput;
	}
	private static String fixNewlines(String str) {
		return str.replaceAll(System.getProperty("line.separator"), "\n").trim();
	}
	private static void compilerTest(File file) throws Exception {
		// Compile to .java
		try {
			JavaCompiler.compile(file, new PrintWriter(new BufferedWriter(new FileWriter("temp/Main.java"))));
		} catch (Exception ex) {
			System.err.println("Failed to compile to Java: " + file.getName());
			ex.printStackTrace(System.err);
			return;
		}
		// Compile to .class
		String s;
		BufferedReader stdInput;
		Process compiler = new ProcessBuilder("javac", "-cp", "temp", "temp/Main.java").redirectErrorStream(true).start();
		stdInput = new BufferedReader(new InputStreamReader(compiler.getInputStream()));
		s = null;
		boolean compileFailed = false;
		StringBuilder compileMessages = new StringBuilder();
		while ((s = stdInput.readLine()) != null) {
			compileMessages.append(s + "\n");
			if (s.startsWith("Note: ")) {
				continue;
			} else {
				compileFailed = true;
			}
		}
		compiler.waitFor();
		if (compileFailed) {
			System.err.println("javac failed for " + file.getName());
			System.err.println(compileMessages);
			return;
		}
		// Execute the .class
		Process executor = new ProcessBuilder("java", "-cp", "temp;temp", "Main").redirectErrorStream(true).start();
		stdInput = new BufferedReader(new InputStreamReader(executor.getInputStream()));
		s = null;
		StringBuilder compiledOutputStrBuilder = new StringBuilder();
		while ((s = stdInput.readLine()) != null) {
			compiledOutputStrBuilder.append(s + "\n");
		}
		executor.waitFor();
		// Interpret the original script
		boolean hadError = false;
		StringBuilder interpretedOutputStrBuilder = null;
		try {
			interpretedOutputStrBuilder = interpreterExecute(file);
		} catch (AmbroscumError ex) {
			// NOTE: Not sure if this is actually necessary
			hadError = true;
		}
		if (interpretedOutputStrBuilder == null) {
			// Something went horribly wrong
			System.err.println("Something bad happened when interpreting " + file.getName());
			return;
		}
		// Compare the two results
		Scanner compiledOutputScanner = new Scanner(fixNewlines(compiledOutputStrBuilder.toString()));
		Scanner interpretedOutputScanner = new Scanner(fixNewlines(interpretedOutputStrBuilder.toString()));
		Object[] result = compare(compiledOutputScanner, interpretedOutputScanner, hadError);
		// Output the comparison
		boolean correct = (boolean) result[0];
		String testOutputStr = (String) result[1];
		String correctOutputStr = (String) result[2];
		if (correct) {
			System.err.println("Tests passed for " + file.getName());
		} else {
			System.err.println("Incorrect output for " + file.getName());
			System.err.println("-----------");
			System.err.println(testOutputStr);
			System.err.println("-----------");
			System.err.println(correctOutputStr);
		}
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
