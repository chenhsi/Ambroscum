/**
 * The interpreter itself. Parses input files and executes the code.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.io.*;
import java.util.*;
import ambroscum.errors.AmbroscumError;
import ambroscum.lines.Block;
import ambroscum.lines.EmptyLine;
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
		boolean firstLine = true;
		String line;
		while (true) {
			try {
				if (!firstLine)
					System.out.println();
				firstLine = false;
				System.out.print(">>> ");
				line = in.nextLine() + "\n";
				TokenStream tokens = Tokenizer.tokenize(line);
				Line lineLine = Line.evalAsLine(tokens, 0);
				if (!lineLine.expectsBlock()) {
					System.out.println("Interpret as " + lineLine);
					lineLine.evaluate(identifiers);
				} else {
					Block block = new Block(Block.OUTER_BLOCK);
					while (block != Block.OUTER_BLOCK) {
						line = in.nextLine() + "\n";
						tokens = Tokenizer.tokenize(line);
						Line subLine = Line.evalAsLine(tokens, block.getIndentation());
						block = block.readLines(tokens);
					}
					lineLine.setBlock(block);
				}
			} catch (AmbroscumError ex) {
				ex.printStackTrace();
			}
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
