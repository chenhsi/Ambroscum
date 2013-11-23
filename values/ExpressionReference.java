package ambroscum.values;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import java.util.*;

public class ExpressionReference extends Expression
{
	private ExpressionReference primary;
	private Expression secondary;
	private ReferenceType type;
	private String baseReference;

	private ExpressionReference() {}
	private ExpressionReference(ExpressionReference p, TokenStream stream)
	{
		primary = p;
		Token peek = stream.size() > 0 ? stream.getFirst() : null;
		String peekStr = peek != null ? peek.toString() : null;
		if ("[".equals(peekStr))
		{
			stream.removeFirst();
			type = ReferenceType.BRACKET;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("]"))
				throw new SyntaxError("Missing close bracket");
		}
		else if ("{".equals(peekStr))
		{
			stream.removeFirst();
			type = ReferenceType.BRACE;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("}"))
				throw new SyntaxError("Missing close brace");
		} else {
			type = ReferenceType.NONE;
		}
	}

	@Override
	public Value evaluate(IdentifierMap values)
	{
		switch (type) {
			case NONE:
				return values.get(baseReference);
			case BRACKET:
				Value outerList = primary.evaluate(values);
				if (outerList instanceof ListValue) {
					return ((ListValue) outerList).get(secondary.evaluate(values));
				}
				throw new SyntaxError("Cannot use brackets to index a non-list: " + outerList);
			case BRACE:
				return null;
		}
		return null;
	}

	public void setValue(Value value, IdentifierMap values)
	{
		switch (type)
		{
			case NONE:
				values.set(baseReference, value);
				break;
			case BRACKET:
				Value outerList = values.get(primary.toString());
				if (outerList instanceof ListValue) {
					((ListValue) outerList).set(secondary.evaluate(values), value);
				} else
					throw new SyntaxError("Cannot use brackets to index a non-list: " + outerList);
				break;
			default:
				throw new UnsupportedOperationException("Fancy stuff doesn't work yet.");
		}
	}
	
	@Override
	public String toString()
	{
		if (primary == null) {
			return baseReference;
		}
		StringBuilder sb = new StringBuilder(primary.toString());
		if (type != ReferenceType.NONE)
			sb.append(type.open).append(secondary.toString()).append(type.close);
		return sb.toString();
	}
	
	public static ExpressionReference createExpressionReference(Token start, TokenStream stream) {
		// I will admit that this works mostly because of magic. And demi-recursion, which is effectively the same thing.
		ExpressionReference outerRef = new ExpressionReference();
		String baseReference = start.toString();
		outerRef.baseReference = baseReference;
		outerRef.type = ReferenceType.NONE;
		
		while (true) {
			if (stream.size() > 0) {
				String first = stream.getFirst().toString();
				// If the references continue
				if ("[".equals(first) || "{".equals(first)) {
					// This reads in the next thing (e.g. "[fancy expression stuff]")
					// and creates a new ExpressionReference
					outerRef = new ExpressionReference(outerRef, stream);
					continue;
				}
			}
			// Essentially, if we don't read in another [0] thing, we're done here.
			break;
		}
		return outerRef;
	}

	private static enum ReferenceType
	{
		NONE("", ""), BRACKET("[", "]"), BRACE("{", "}");
		
		String open;
		String close;
		
		ReferenceType(String o, String c)
		{
			open = o;
			close = c;
		}
	}
}