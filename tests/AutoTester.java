/**
 * Runs test cases from the /tests folder.
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
								CORRECT_EXTENSION = ".correct";
	
	public static void main(String[] args) throws IOException {
		PrintStream systemOut = System.out;
		
		File testsFolder = new File(TEST_DIRECTORY);
		File[] testFiles = testsFolder.listFiles();
		System.out.println(System.getProperty("user.dir"));
		for (File file : testFiles) {
			String fileName= file.getName();
			if (fileName.lastIndexOf(TEST_EXTENSION) < fileName.length() - TEST_EXTENSION.length()) {
				// This is not a test file
				continue;
			}
			
			PipedOutputStream pipeOut = new PipedOutputStream();
			PipedInputStream pipeIn = new PipedInputStream(pipeOut);
			System.setOut(new PrintStream(pipeOut));
			
			Interpreter.interpret(file);
			
			Scanner correctFile = new Scanner(new File(file.getPath() + CORRECT_EXTENSION));
			StringBuilder correctOutput = new StringBuilder();
			while (correctFile.hasNextLine()) {
				correctOutput.append(correctFile.nextLine() + "\n");
			}
			
			StringBuilder testOutput = new StringBuilder();
			while (pipeIn.available() > 0) {
				int read = pipeIn.read();
				testOutput.append((char) read);
			}
			String correctOutputStr = correctOutput.toString().replaceAll(System.getProperty("line.separator"), "\n");
			String testOutputStr = testOutput.toString().replaceAll(System.getProperty("line.separator"), "\n");
			
			if (correctOutputStr.equals(testOutputStr)) {
				System.err.println("Tests passed for " + fileName);
			} else {
				System.err.println("Incorrect output for " + fileName);
				System.err.println("-----------");
				System.err.println(testOutputStr);
				System.err.println("-----------");
				System.err.println(correctOutputStr);
				System.err.println("======================");
			}
		}
	}
}
