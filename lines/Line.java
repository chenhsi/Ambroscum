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
	public abstract Block.ExitStatus evaluate(IdentifierMap values);
	public abstract boolean expectsBlock();	
	public abstract void setBlock(Block b);

	public static Line interpret(TokenStream stream, int indentation)
	{
		for (int i = 0; i < indentation; i++)
		{
			Token tab = stream.removeFirst();
			if (tab != Token.TAB)
			{
				if (i == indentation - 1)
				{
					if (tab.toString().equals("else"))
					{
						Token temp = stream.removeFirst();
						if (temp != Token.COLON)
							throw new SyntaxError("Expected colon after else");
						temp = stream.removeFirst();
						if (temp != Token.NEWLINE)
							throw new SyntaxError("Unexpected token after else:" + temp);
						return new ElseLine();
					}
					else if (tab.toString().equals("end"))
					{
						Token temp = stream.removeFirst();
						if (temp != Token.NEWLINE)
							throw new SyntaxError("Unexpected token after end:" + temp);
						return new EndLine();
					}
					throw new SyntaxError("Missing indentation");
				}
				else
					throw new SyntaxError("Missing indentation");
			}
		}
		Token token = stream.removeFirst();
		if (token == Token.NEWLINE)
			return Line.interpret(stream, indentation);
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
			return new IfLine(stream, indentation);
		if (token.toString().equals("while"))
			return new WhileLine(stream, indentation);
		if (token.toString().equals("def"))
			return new DefLine(stream, indentation);
		List<Token> newStream = new LinkedList<Token>();
		newStream.add(token);
		while (true)
		{
			Token next = stream.removeFirst();
			newStream.add(next);
			if (next.toString().equals("="))
				return new AssignmentLine(TokenStream.readAsStream(newStream), stream);
			if (next == Token.NEWLINE)
				return new CallLine(TokenStream.readAsStream(newStream));
		}
	}
}
