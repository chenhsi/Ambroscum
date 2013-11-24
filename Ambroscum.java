/**
 * The entry point to the program.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;

public class Ambroscum
{
	public static void main(String[] args) throws IOException
	{
/*		try {
			if (args.length == 1)
				Interpreter.interpret(args[0]);
		} catch (IOException ex) {
			ex.printStackTrace();
		}*/
//		Interpreter.interpret();
		Interpreter.interpret(new File("tests/tests.ambr"));
	}
}
