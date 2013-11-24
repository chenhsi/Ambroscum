/**
 * Represents a single line of code.
 *
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import java.util.Iterator;

public abstract class Line
{
	public abstract void evaluate(IdentifierMap values);
	public abstract boolean expectsBlock();	
	public abstract void setBlock(Block b);

	public static Line evalAsLine(TokenStream stream, int indentation)
	{
		System.out.println("START " + stream + " INDENTATION " + indentation);
		for (int i = 0; i < indentation; i++)
		{
			Token tab = stream.removeFirst();
			if (tab != Token.TAB)
			{
				if (i == indentation - 1)
				{
					if (!tab.toString().equals("end"))
						throw new SyntaxError("Missing indentation");
					Token temp = stream.removeFirst();
					if (temp != Token.NEWLINE)
						throw new SyntaxError("Unexpected token after end:" + temp);
					return new EndLine();
				}
				else
					throw new SyntaxError("Missing indentation");
			}
		}
		System.out.println("UP HERE " + stream);
		Token token = stream.removeFirst();
		token = Token.getToken(token.toString().trim());
		if (token == Token.TAB)
			throw new SyntaxError("Unexpected indentation");
		if (token.toString().equals("assert"))
			return new AssertLine(stream);
		if (token.toString().equals("print") || token.toString().equals("println"))
			return new PrintLine(stream, token.toString().length() == 7);
		if (token.toString().equals("break"))
			return new BreakLine(stream);
		if (token.toString().equals("continue"))
			return new ContinueLine(stream);
		if (token.toString().equals("return"))
			return new ReturnLine(stream);
		if (token.toString().equals("if"))
			return new IfLine(stream);
		if (token.toString().equals("def"))
			return new DefLine(stream, indentation);
		System.out.println("DOWN HERE \"" + stream + "\"");
		TokenStream newStream = new TokenStream();
		newStream.add(token);
		while (true) {
			Token next = stream.removeFirst();
			if (next.toString().equals("="))
				return new AssignmentLine(newStream, stream);
			else if (next == Token.NEWLINE) {
				throw new UnsupportedOperationException("Call expressions not implemented!");
			} else {
				newStream.add(next);
			}
		}

		//throw new UnsupportedOperationException();
	}
}
