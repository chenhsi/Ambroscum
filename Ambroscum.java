/**
 * The entry point to the program.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;

public class Ambroscum
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
//		Interpreter.interpret();
		compileTest("04 sort.ambr");
	}
	
	private static void compileTest(String testName) throws IOException, InterruptedException
	{
		try
		{
			Compiler.compile(new File("tests/" + testName), new PrintWriter(new BufferedWriter(new FileWriter("temp/Main.java"))));
		}
		catch (IOException ex)
		{
			System.err.println("Failed in IO");
			throw ex;
		}
		catch (Exception ex)
		{
			System.err.println("Failed when compiling .ambr file");
			throw ex;
		}
		
		String s;
		BufferedReader stdInput;
		
		System.out.println("\nNow attempting to compile .java file\n");
		
		try
		{
			Process compiler = new ProcessBuilder("javac", "-cp", "temp", "temp/Main.java").redirectErrorStream(true).start();
			stdInput = new BufferedReader(new InputStreamReader(compiler.getInputStream()));
			s = null;
			while ((s = stdInput.readLine()) != null)
				System.out.println(s);
			compiler.waitFor();
		}
		catch (Exception ex)
		{
			System.err.println("Error when compiling .java file");
			throw ex;
		}
		
		System.out.println("\nNow attempting to execute .class file\n");
		
		try
		{
			Process executor = new ProcessBuilder("java", "-cp", "temp", "Main").redirectErrorStream(true).start();
			stdInput = new BufferedReader(new InputStreamReader(executor.getInputStream()));
			s = null;
			while ((s = stdInput.readLine()) != null)
				System.out.println(s);
			executor.waitFor();
		}
		catch (Exception ex)
		{
			System.err.println("Error when executing .class file");
			throw ex;
		}
	}
}
