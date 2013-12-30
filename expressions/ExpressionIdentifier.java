package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;

import java.util.*;

public class ExpressionIdentifier extends Expression
{
	private String identifier;

	public ExpressionIdentifier(Token token)
	{
		if (!IdentifierMap.isValidIdentifier(token.toString()))
			throw new SyntaxError("Invalid identifier: " + token);
		identifier = token.toString();
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		return values.get(identifier);
	}

	public void setValue(Value value, IdentifierMap values)
	{
		values.add(identifier, value);
	}
	
	@Override
	public String toString()
	{
		return identifier;
	}
}