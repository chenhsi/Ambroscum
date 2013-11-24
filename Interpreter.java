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
import ambroscum.errors.SyntaxError;
import ambroscum.lines.Block;
import ambroscum.lines.EndLine;
import ambroscum.lines.ElseLine;
import ambroscum.lines.IfLine;
import ambroscum.lines.Line;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Tokenizer;

public class Interpreter
{
	
	private static IdentifierMap identifiers;
	
	static
	{
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
				if (line.trim().equals(""))
					continue;
				TokenStream tokens = Tokenizer.tokenize(line);
				Line lineLine = Line.interpret(tokens, 0);
//				System.out.println("Interpret as " + lineLine);
				if (!lineLine.expectsBlock()) {
					lineLine.evaluate(identifiers);
				} else {
					Block block = readBlock(in, 1, lineLine);
					lineLine.setBlock(block);
					lineLine.evaluate(identifiers);
				}
			} catch (AmbroscumError ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static Block readBlock(Scanner in, int indentation, Line root) {
		ArrayList<Line> newBlock = new ArrayList<>();
		Line lineLine;
		do {
//			System.out.print("...");
			String line = in.nextLine() + "\n";
			TokenStream tokens = Tokenizer.tokenize(line);
			lineLine = Line.interpret(tokens, indentation);
			System.out.println("interpret as " + lineLine);
			if (lineLine instanceof ElseLine) {
				if (root instanceof IfLine) {
					Block block = readBlock(in, indentation, lineLine);
					lineLine.setBlock(block);
					((IfLine) root).setElseClause((ElseLine) lineLine);
					break;
				}
			} else if (lineLine.expectsBlock()) {
				Block block = readBlock(in, indentation + 1, lineLine);
				lineLine.setBlock(block);
			}
			newBlock.add(lineLine);
		} while (!(lineLine instanceof EndLine));
		return new Block(newBlock);
	}
	
//	public static void interpret(String filename) throws IOException {
//		interpret(new File(filename));
//	}
	public static void interpret(File file) throws IOException {
		Scanner in = new Scanner(new FileInputStream(file));
		boolean firstLine = true;
		String line;
		while (in.hasNextLine()) {
			try {
				firstLine = false;
				line = in.nextLine() + "\n";
				if (line.trim().equals(""))
					continue;
				TokenStream tokens = Tokenizer.tokenize(line);
				Line lineLine = Line.interpret(tokens, 0);
				if (!lineLine.expectsBlock()) {
					lineLine.evaluate(identifiers);
				} else {
					Block block = readBlock(in, 1, lineLine);
					lineLine.setBlock(block);
					lineLine.evaluate(identifiers);
				}
			} catch (AmbroscumError ex) {
				ex.printStackTrace();
			}
		}
	}
}
