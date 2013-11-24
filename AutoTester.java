/**
 * Runs test cases from the /tests folder.
 * 
 * @author Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;
import java.util.*;

public class AutoTester {
	
	public static final String TEST_DIRECTORY = "tests",
								CORRECT_EXTENSION = ".correct";
	
	public static void main(String[] args) throws IOException {
		PrintStream systemOut = System.out;
		
		File testsFolder = new File(TEST_DIRECTORY);
		File[] testFiles = testsFolder.listFiles();
		for (File file : testFiles) {
			PipedOutputStream pipeOut = new PipedOutputStream();
			PipedInputStream pipeIn = new PipedInputStream(pipeOut);
			System.setOut(new PrintStream(pipeOut));
			
			String fileName =file.getName();
			if (fileName.lastIndexOf(CORRECT_EXTENSION) != -1) {
				// This is not a test file; it is a correct results file
				continue;
			}
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
