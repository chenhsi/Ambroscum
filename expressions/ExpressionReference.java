package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.NotDereferenceableException;
import ambroscum.values.Value;
import ambroscum.values.FunctionDeclaration;

import java.util.*;

public class ExpressionReference extends Expression
{
	private Expression primary;
	private Expression secondary;

	public ExpressionReference(Expression base, TokenStream stream)
	{
		primary = base;
		//secondary = Expression.singleExpression(stream);
		if (true)
			throw new UnsupportedOperationException("I don't get the fields in this class :(");
	}

	@Override
	public Value evaluate(IdentifierMap values)
	{
		Value first = primary.evaluate(values);
		if (first instanceof FunctionDeclaration)
			throw new NotDereferenceableException("Cannot dot reference a function");
		return first.dereference(secondary.toString());
	}

	public void setValue(Value value, IdentifierMap values)
	{
		Value first = primary.evaluate(values);
		if (first instanceof FunctionDeclaration)
			throw new NotDereferenceableException("Cannot dot reference a function");
		first.setDereference(secondary.toString(), value);
	}
	
	@Override
	public String toString()
	{
		return primary.toString() + "." + secondary.toString();
	}
}