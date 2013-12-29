package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;
import ambroscum.values.ListValue;
import ambroscum.values.DictValue;

import java.util.*;

public class ExpressionReference extends Expression {
	private Expression primary;
	private Expression secondary;
	private ReferenceType type;
	// If this is set, primary is always null and type is always NONE
	private String baseReference;
	// Set if and only if this.type == DOT
	// If set, then secondary = null
	private String dotReference;

	public ExpressionReference(Token token)
	{
		if (!IdentifierMap.isValidIdentifier(token.toString()))
			throw new SyntaxError("Invalid identifier: " + token);
		baseReference = token.toString();
		type = ReferenceType.NONE;
	}
	
	public ExpressionReference(Expression base, TokenStream stream)
	{
		ReferenceType type = stream.removeFirst().toString().equals(".") ? ReferenceType.DOT : ReferenceType.BRACKET;
		Expression secondary = Expression.singleExpression(stream);
		if (true)
			throw new UnsupportedOperationException("I don't get the fields in this class :(");
		if (type == ReferenceType.BRACKET)
		{
			Token nextToken = stream.removeFirst();
			if (!nextToken.toString().equals("]"))
				throw new SyntaxError("Expecting close brace, found " + nextToken);
		}
	}

	@Override
	public Value evaluate(IdentifierMap values) {
		switch (type) {
			case NONE:
				if (baseReference != null)
					return values.get(baseReference);
				else
					return primary.evaluate(values);
			case BRACKET:
				Value outerList = primary.evaluate(values);
				if (outerList instanceof ListValue) {
					return ((ListValue) outerList).get(secondary.evaluate(values));
				} else if (outerList instanceof DictValue) {
					return ((DictValue) outerList).get(secondary.evaluate(values));
				}
				throw new SyntaxError("Cannot index: " + outerList);
			case DOT:
				return primary.evaluate(values).dereference(dotReference);
		}
		return null;
	}

	public void setValue(Value value, IdentifierMap values) {
		switch (type) {
			case NONE:
				if (baseReference != null)
					values.add(baseReference, value);
				else
					throw new SyntaxError("Cannot set the value of " + primary);
				break;
			case BRACKET:
				Value outerList = primary.evaluate(values);
				if (outerList instanceof ListValue) {
					((ListValue) outerList).set(secondary.evaluate(values), value);
				} else if (outerList instanceof DictValue) {
					((DictValue) outerList).set(secondary.evaluate(values), value);
				} else {
					throw new SyntaxError("Cannot use brackets to index a non-list: " + outerList);
				}
				break;
			case DOT:
				primary.evaluate(values).setDereference(dotReference, value);
				break;
			default:
				throw new UnsupportedOperationException("Fancy stuff doesn't work yet.");
		}
	}
	
	@Override
	public String toString() {
		if (primary == null) {
			return baseReference;
		}
		StringBuilder sb = new StringBuilder(primary.toString());
		if (type != ReferenceType.NONE)
			sb.append(type.open).append(secondary.toString()).append(type.close);
		return sb.toString();
	}
	
	private static enum ReferenceType {
		NONE("", ""), DOT(".", ""), BRACKET("[", "]");
		
		String open;
		String close;
		
		ReferenceType(String o, String c) {
			open = o;
			close = c;
		}
	}
}