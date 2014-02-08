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
import ambroscum.lines.IfLine;
import ambroscum.lines.Line;
import ambroscum.parser.TokenStream;
import ambroscum.values.Value;
import ambroscum.values.StringValue;
import ambroscum.values.ListValue;

public class Interpreter
{
	/**
	 * Starts a command-line interactive Ambroscum session.
	 */
	public static void interpret()
	{
		interactive(TokenStream.interactiveInput(), new IdentifierMap());
	}
	
	private static void interactive(TokenStream stream, IdentifierMap identifiers)
	{
		while (true)
		{
			try
			{
				Line l = Line.interpret(null, stream, 0);
				l.localOptimize().evaluate(identifiers);
				System.out.println();
			}
			catch (AmbroscumError ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Reads data from an Ambroscum file, then optionally begins an interactive
	 * session.
	 *
	 * @param	file			the Ambroscum file to read from and evaluate
	 * @param	thenInteract	whether to begin interacitve input afterwards
	 */
	public static void interpret(File file, boolean thenInteract) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(file);
		IdentifierMap identifiers = new IdentifierMap();
		new Block(null, stream, 0).evaluate(identifiers);
		if (thenInteract)
		{
			stream.makeInteractive();
			interactive(stream, identifiers);
		}
	}
	
	public static void interpret(File file, boolean thenInteract, String[] args) throws FileNotFoundException {
		TokenStream stream = TokenStream.readFile(file);
		IdentifierMap identifiers = new IdentifierMap();
		Value[] argValues = new Value[args.length];
		for (int i = 0; i < args.length; i++) {
			argValues[i] = StringValue.fromString(args[i]);
	    }
	    identifiers.set("args", new ListValue(argValues));
		new Block(null, stream, 0).evaluate(identifiers);
		if (thenInteract) {
			stream.makeInteractive();
			interactive(stream, identifiers);
		}
	}
	public static void interpret(String[] args) {
		IdentifierMap identifiers = new IdentifierMap();
		Value[] argValues = new Value[args.length];
		for (int i = 0; i < args.length; i++) {
			argValues[i] = StringValue.fromString(args[i]);
	    }
	    identifiers.set("args", new ListValue(argValues));
		interactive(TokenStream.interactiveInput(), identifiers);
	}
}
