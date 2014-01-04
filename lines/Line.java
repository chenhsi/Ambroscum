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
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionOperator;
import ambroscum.values.Value;

public abstract class Line
{
	private static int lineCounter = 0;
	private final int lineID;

	protected Line parent;
	
	protected Line(Line p)
	{
		parent = p;
		lineID = ++lineCounter;
	}
	
	public int getID()
	{
		return lineID;
	}
	
	protected void setReturnValue(Value v)
	{
		if (parent == null)
			throw new SyntaxError("Cannot have a return line outside of a function declaration");
		parent.setReturnValue(v);
	}
	
	public Line localOptimize()
	{
		return this;
	}

	// ultimately, this should be overwritten by every subclass, and thus be abstract
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
//		throw new UnsupportedOperationException();
	}
	
	public abstract Block.ExitStatus evaluate(IdentifierMap values);

	public static Line interpret(Line parent, TokenStream stream, int indentation)
	{
		for (int i = 0; i < indentation; i++)
		{
			Token tab = stream.getFirst();
			if (tab != Token.TAB)
			{
				if (tab == Token.NEWLINE)
				{
					// if this is an blank line (other than tabs), reset loop counter and continue on next line
					stream.removeFirst();
					i = -1;
					continue;
				}
				else if (i == indentation - 1)
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
		if (stream.getFirst() == Token.EOF)
			return new EndLine(parent);
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
		Stack<Enclosure> enclosures = new Stack<Enclosure>();
		while (true)
		{
			Token next = stream.removeFirst();
			newStream.add(next);
			if (next.toString().endsWith("=") && enclosures.size() == 0)
				return new AssignmentLine(parent, TokenStream.readAsStream(newStream), stream);
			if (next == Token.NEWLINE)
				return new CallLine(parent, TokenStream.readAsStream(newStream));
			// Hacky way of avoiding equals equality vs. equals assignment
			Enclosure e = Enclosure.getEnclosure(next.toString());
			if (e != null) {
				if (enclosures.size() != 0 && e == enclosures.peek()) {
					enclosures.pop();
				} else {
					enclosures.add(e);
				}
			}
		}
	}
	
	public enum Enclosure {
		PARENTHESES("(", ")"),
		QUOTATION("\"", "\""),
		BRACKETS("[", "]"),
		BRACES("{", "}");
		
		private String start, end;
		
		Enclosure(String s, String e) {
			start = s;
			end = e;
		}
		
		public String getEnd() {
			return end;
		}
		
		public static Enclosure getEnclosure(String str) {
			for (Enclosure e : Enclosure.values()) {
				if (e.start.equals(str) || e.end.equals(str)) {
					return e;
				}
			}
			return null;
		}
	}
}
