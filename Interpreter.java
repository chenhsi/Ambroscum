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

public class Interpreter
{
	/**
	 * Starts a command-line interactive Ambroscum session.
	 */
	public static void interpret()
	{
		interactive(TokenStream.interactiveInput(), new IdentifierMap(null));
	}
	
	private static void interactive(TokenStream stream, IdentifierMap identifiers)
	{
		while (true)
		{
			try
			{
				Line line = Line.interpret(stream, 0);
				if (!line.expectsBlock())
					line.evaluate(identifiers);
				else
				{
					Block block = readBlock(stream, line, 1);
					line.setBlock(block);
					line.evaluate(identifiers);
				}
				System.out.println();
			}
			catch (AmbroscumError ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private static Block readBlock(TokenStream stream, Line root, int indentation) {
		ArrayList<Line> newBlock = new ArrayList<>();
		Line lineLine;
		do {
			lineLine = Line.interpret(stream, indentation);
			if (lineLine instanceof ElseLine) {
				if (root instanceof IfLine) {
					Block block = readBlock(stream, lineLine, indentation);
					lineLine.setBlock(block);
					((IfLine) root).setElseClause((ElseLine) lineLine);
					break;
				}
			} else if (lineLine.expectsBlock()) {
				Block block = readBlock(stream, lineLine, indentation + 1);
				lineLine.setBlock(block);
			}
			newBlock.add(lineLine);
		} while (!(lineLine instanceof EndLine));
		return new Block(newBlock);
	}
	
	public static void interpret(File file, boolean thenInteract) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(file);
		IdentifierMap identifiers = new IdentifierMap(null);
		while (stream.hasNext())
		{
			Line line = Line.interpret(stream, 0);
			if (!line.expectsBlock())
				line.evaluate(identifiers);
			else
			{
				Block block = readBlock(stream, line, 1);
				line.setBlock(block);
				line.evaluate(identifiers);
			}
		}
		if (thenInteract)
		{
			stream.makeInteractive();
			interactive(stream, identifiers);
		}
	}
}
