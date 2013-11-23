/**
 * The interpreter itself. Parses input files and executes the code.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;
import java.util.*;
import ambroscum.lines.Line;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Tokenizer;

public class Interpreter
{
	
	private static IdentifierMap identifiers;
	
	static {
		identifiers = new IdentifierMap(null);
	}
	
	/**
	 * Starts a command-line interactive Ambroscum session.
	 */
	public static void interpret() {
		Scanner in = new Scanner(System.in);
		String line;
		while (true) {
			System.out.print("\n>>> ");
			line = in.nextLine() + "\n";
			TokenStream tokens = Tokenizer.tokenize(line);
			Line lineLine = Line.evalAsLine(tokens);
			lineLine.evaluate(identifiers);
		}
	}
/*	public static void interpret(String filename) throws IOException {
		interpret(new File(filename));
	}
	public static void interpret(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		do {
			line = reader.readLine();
			Line lineLine = Line.evalAsLine(line, null);
		} while(line != null);
	}*/
}
