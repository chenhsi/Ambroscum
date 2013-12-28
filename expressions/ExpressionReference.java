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

	private ExpressionReference() {}
	private ExpressionReference(ExpressionReference p, TokenStream stream) {
		primary = p;
		String peekStr = stream.getFirst().toString();
		if ("[".equals(peekStr)) {
			stream.removeFirst();
			type = ReferenceType.BRACKET;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("]"))
				throw new SyntaxError("Missing close bracket");
		} else if (".".equals(peekStr)) {
			stream.removeFirst();
			type = ReferenceType.DOT;
			dotReference = stream.removeFirst().toString();
		} else
			type = ReferenceType.NONE;
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
	
	public static ExpressionReference createExpressionReference(Token token)
	{
		if (!IdentifierMap.isValidIdentifier(token.toString()))
			throw new SyntaxError("Invalid identifier: " + token);
		ExpressionReference expr = new ExpressionReference();
		expr.baseReference = token.toString();
		expr.type = ReferenceType.NONE;
		return expr;
	}
	
	public static ExpressionReference createExpressionReference(Expression base, TokenStream stream)
	{
		ExpressionReference expr = new ExpressionReference();
		ReferenceType type = stream.removeFirst().toString().equals(".") ? ReferenceType.DOT : ReferenceType.BRACKET;
		Expression secondary = Expression.singleExpression(stream);
		if (true)
			throw new UnsupportedOperationException("I don't get the fields in this class :(");
		if (type == ReferenceType.BRACKET)
		{
			Token nextToken = stream.removeFirst();
			if (!nextToken.toString().equals("]"))
				throw new SyntaxError("Expecting close brace , found " + nextToken);
		}
		return expr;
	}
	
	public static ExpressionReference createExpressionReference2(Token start, TokenStream stream) {
		ExpressionReference outerRef = new ExpressionReference();
		String baseReference = start.toString();
		outerRef.baseReference = baseReference;
		outerRef.type = ReferenceType.NONE;
		
		return createExpressionReferenceHelper(outerRef, stream);
	}
	public static ExpressionReference createExpressionReference2(Expression base, TokenStream stream) {
		ExpressionReference outerRef = new ExpressionReference();
		outerRef.primary = base;
		outerRef.type = ReferenceType.NONE;
		
		return createExpressionReferenceHelper(outerRef, stream);
	}
	/**
	 * Creates an ExpressionReference that cannot have complex references (i.e. must be of the form variable_name).
	 *
	 * @param stream The TokenStream, with the first token being "variable_name"
	 */
	public static ExpressionReference createSimpleExpressionReference(TokenStream stream) {
		Token next = stream.removeFirst();
		if (!next.toString().matches("[A-Za-z0-9_]+")) {
			throw new SyntaxError("Invalid variable name: " + next.toString());
		}
		ExpressionReference ref = new ExpressionReference();
		ref.baseReference = next.toString();
		ref.type = ReferenceType.NONE;
		if (stream.hasNext()) {
			next = stream.getFirst();
			if (next != Token.NEWLINE && (next.toString().equals("[") || next.toString().equals("."))) {
				throw new SyntaxError("Unexpected reference after variable declaration");
			}
		}
		return ref;
	}
	private static ExpressionReference createExpressionReferenceHelper(ExpressionReference outerRef, TokenStream stream) {
		Token next = stream.getFirst();
		while (next != Token.NEWLINE && (next.toString().equals("[") || next.toString().equals("."))) {
			// If the references continue
			// This reads in the next thing (e.g. "[fancy expression stuff]")
			// and creates a new ExpressionReference
			outerRef = new ExpressionReference(outerRef, stream);
			next = stream.getFirst();
		}
		return outerRef;
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