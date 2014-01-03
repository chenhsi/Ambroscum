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
	public static void main(String[] args) throws IOException
	{
//		Interpreter.interpret();
		Compiler.compile(new File("tests/01 printlns.ambr"), new PrintWriter(new BufferedWriter(new FileWriter("temp/Main.java"))));
	}
}
