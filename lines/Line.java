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
import ambroscum.expressions.ExpressionOperator;
import ambroscum.values.Value;
import java.util.Iterator;

public abstract class Line
{
	protected Line parent;
	
	protected Line(Line p)
	{
		parent = p;
	}
	
	protected void setReturnValue(Value v)
	{
		parent.setReturnValue(v);
	}
	
	public abstract Block.ExitStatus evaluate(IdentifierMap values);

	public static Line interpret(Line parent, TokenStream stream, int indentation)
	{
		for (int i = 0; i < indentation; i++)
		{
			Token tab = stream.getFirst();
			if (tab != Token.TAB)
			{
				if (i == indentation - 1)
				{
					String notTab = tab.toString();
					if (notTab.equals("else") || notTab.equals("elif") || notTab.equals("then"))
						return new EndLine(parent);
					else if (notTab.equals("end"))
					{
						stream.removeFirst();
						Token temp = stream.removeFirst();
						if (temp != Token.NEWLINE)
							throw new SyntaxError("Unexpected token after end:" + temp);
						return new EndLine(parent);
					}
					throw new SyntaxError("Missing indentation");
				}
				else
					throw new SyntaxError("Missing indentation");
			}
			stream.removeFirst();
		}
		Token token = stream.removeFirst();
		if (token == Token.NEWLINE)
			return Line.interpret(parent, stream, indentation);
		if (token.toString().equals("assert"))
			return new AssertLine(parent, stream);
		if (token.toString().equals("print") || token.toString().equals("println"))
			return new PrintLine(parent, stream, token.toString().length() == 7);
		if (token.toString().equals("break"))
			return new BreakLine(parent, stream);
		if (token.toString().equals("continue"))
			return new ContinueLine(parent, stream);
		if (token.toString().equals("return"))
			return new ReturnLine(parent, stream);
		if (token.toString().equals("if"))
			return new IfLine(parent, stream, indentation);
		if (token.toString().equals("while"))
			return new WhileLine(parent, stream, indentation);
		if (token.toString().equals("for"))
			return new ForLine(parent, stream, indentation);
		if (token.toString().equals("def"))
			return new DefLine(parent, stream, indentation);
		if (token.toString().equals("else") || token.toString().equals("elif"))
			throw new SyntaxError("Unexpected " + token + " line");
		List<Token> newStream = new LinkedList<Token>();
		newStream.add(token);
		while (true)
		{
			Token next = stream.removeFirst();
			newStream.add(next);
			if (next.toString().endsWith("="))
				return new AssignmentLine(parent, TokenStream.readAsStream(newStream), stream);
			if (next == Token.NEWLINE)
				return new CallLine(parent, TokenStream.readAsStream(newStream));
		}
	}
}
