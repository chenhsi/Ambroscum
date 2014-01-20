/**
 * The entry point to the program.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;
import ambroscum.compiler.*;
import java.util.Arrays;

public class Ambroscum
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
//		commandLineMain(args);
		commandLineMain(new String[] {"-c", "tests/02 assignments.ambr"});
//		compileILTest(new File("tests/08 functions.ambr"));
	}
	
	/* Command line arguments
	 *    no args		interactive session
	 *    -i File		interpret file and exit session
	 *    -ii File		interpret file and start interactive session
	 *    -c File		compile file, currently to a file Main.java
	 *    -cr File 		compile file to a file Main.java, and evoke javac/java on the output file
	 */
	public static void commandLineMain(String[] args) throws IOException, InterruptedException
	{
		System.out.println(java.util.Arrays.toString(args));
		if (args.length == 0)
		{
			Interpreter.interpret();
			return;
		}
		if (args.length == 1 || args.length > 2)
			throw new IllegalArgumentException("Not supported as arguments: " + Arrays.toString(args));
		if (args[0].equals("-i"))
			Interpreter.interpret(new File(args[1]), false);
		else if (args[0].equals("-ii"))
			Interpreter.interpret(new File(args[1]), true);
		else if (args[0].equals("-c"))
			compileJavaTest(new File(args[1]), false);
		else if (args[0].equals("-cr"))
			compileJavaTest(new File(args[1]), true);
		else
			throw new IllegalArgumentException("Not supported as arguments: " + Arrays.toString(args));
	}
	
	private static void compileILTest(File file) throws IOException
	{
		try
		{
			ILCompiler.compile(file);
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
	}
	
	private static void compileJavaTest(File file, boolean execute) throws IOException, InterruptedException
	{
		try
		{
			JavaCompiler.compile(file, new PrintWriter(new BufferedWriter(new FileWriter("temp/Main.java"))));
			if (!execute)
				return;
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
			{
				if (s.startsWith("Note: "))
					continue;
				else
					throw new RuntimeException(s);
			}
			compiler.waitFor();
		}
		catch (Exception ex)
		{
			System.err.println("Error when compiling .java file:");
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
