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
	public static final String USAGE_MESSAGE = "Usage: ambroscum [-icRh] [file] [args]\n\nargs Command line arguments to pass to the interpreted file\n\n[no args] Basic interactive session\n-i Execute file if given, then go to interactive session (if not specified, execute the given file and exit)\n-c Compile file to arg0.java\n-R If -c was specified, invoke javac and java on the output Java file\n-h Print this help message and exit";
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
//		commandLineMain(args);
//		commandLineMain(new String[] {"-c", "tests/03 if.ambr"});
//		compileILTest(new File("tests/03 if.ambr"));
		compileMIPSTest("03 if", true);
	}
	
	/* Usage: ambroscum [-icRh] [file] [-a args]
	 *
	 * [no args] Basic interactive session
	 * -a Specify command-line arguments to pass to the interpreter (can be used by the script)
	 * -i Execute file if given, then go to interactive session (if not specified, execute the given file and exit)
	 * -c Compile file to arg0.java (overridden by -i)
	 * -R If -c was specified, invoke javac and java on the output Java file
	 * -h Print this help message and exit
	 */
	public static void commandLineMain(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			Interpreter.interpret();
		} else {
			// Process command line args
			boolean interactive = false, compile = false, compileRun = false;
			int i;
			for (i = 0; i < args.length; i++) {
				if (args[i].charAt(0) == '-') {
					if (args[i].indexOf('i') > -1) {
						interactive = true;
					}
					if (args[i].indexOf('c') > -1) {
						compile = true;
					}
					if (args[i].indexOf('R') > -1) {
						compileRun = true;
					}
					if (args[i].indexOf('h') > -1) {
						System.out.println(USAGE_MESSAGE);
						System.exit(0);
					}
				} else {
					// End of the command line flags
					break;
				}
			}
			if (!interactive && !compile && !compileRun) {
				// If no flags set
				i--;
			}
			String[] interpreterArgs = null;
			String fileName = null;
			if (i < args.length) {
				// We either have a file to execute, or command line args, or both
				if (args[i].equals("-a")) {
					interpreterArgs = new String[args.length - 1 - i];
					System.arraycopy(args, i + 1, interpreterArgs, 0, interpreterArgs.length);
				} else {
					fileName = args[i];
					i++;
					// Do we have more arguments?
					if (i < args.length && args[i].equals("-a")) {
						interpreterArgs = new String[args.length - 1 - i];
						System.arraycopy(args, i + 1, interpreterArgs, 0, interpreterArgs.length);
					}
				}
			}
			if (interactive || !compile) {
				interpreterArgs = interpreterArgs != null ? interpreterArgs : new String[] {};
				if (fileName != null)
					Interpreter.interpret(new File(fileName), interactive, interpreterArgs);
				else
					Interpreter.interpret(interpreterArgs);
			} else {
				compileJavaTest(new File(fileName), compileRun);
			}
		}
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
	
	private static void compileMIPSTest(String fileName, boolean execute) throws IOException, InterruptedException
	{
		String outputFileName = "temp/" + fileName + ".asm";
		try
		{
			MIPSCompiler.compile(new File("tests/" + fileName + ".ambr"),
//								 new PrintWriter(System.out));
								 new PrintWriter(new BufferedWriter(new FileWriter(outputFileName))));
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
		
		System.out.println("\nNow attempting to execute .asm file\n");
		
		try
		{
			Process executor = new ProcessBuilder("java", "-jar", "mars.jar", outputFileName, "nc").redirectErrorStream(true).start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(executor.getInputStream()));
			String s = null;
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
