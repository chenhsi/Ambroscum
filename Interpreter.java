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
	private static void interactive(TokenStream stream, IdentifierMap identifiers)
	{
		while (true)
		{
			try
			{
				// Standard REPL here
				// The localOptimize call shouldn't be necessary usually,
				// but probably still useful for complex stuff e.g. functions
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
	 * session. Allows for optional command-line style arguments
	 *
	 * @param	file			the Ambroscum file to read from and evaluate
	 * @param	thenInteract	whether to begin interacitve input afterwards
	 * @param	args	pre-set arguments
	 */
	public static void interpret(File file, boolean thenInteract, String... args) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(file);
		IdentifierMap identifiers = evalArgs(args);
		new Block(null, stream, 0).evaluate(identifiers);
		if (thenInteract)
		{
			stream.makeInteractive();
			interactive(stream, identifiers);
		}
	}

	/**
	 * Starts a command-line interactive Ambroscum session, with optional
	 * command-line style arguments
	 *
	 * @param	args	pre-set arguments
	 */
	public static void interpret(String... args)
	{
		interactive(TokenStream.interactiveInput(), evalArgs(args));
	}

	private static IdentifierMap evalArgs(String... args)
	{
		IdentifierMap identifiers = new IdentifierMap();
		if (args.length > 0)
		{
			Value[] argValues = new Value[args.length];
			for (int i = 0; i < args.length; i++)
				argValues[i] = StringValue.fromString(args[i]);
	    	identifiers.set("args", new ListValue(argValues));
		}
	    return identifiers;
	}
}
