package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.NotDereferenceableException;
import ambroscum.values.Value;
import ambroscum.values.ObjectValue;
import ambroscum.values.FunctionValue;

import java.util.*;

public class ExpressionIdentifier extends Expression
{
	private Expression parent;
	private String identifier;

	public ExpressionIdentifier(Token token)
	{
		if (!IdentifierMap.isValidIdentifier(token.toString()))
			throw new SyntaxError("Invalid identifier: " + token);
		identifier = token.toString();
	}
	
	public ExpressionIdentifier(Expression parent, Token token)
	{
		this(token);
		this.parent = parent;
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		if (parent == null)
			return values.get(identifier);
		else
		{
			Value parentValue = parent.evaluate(values);
			if (parentValue instanceof FunctionValue)
				throw new NotDereferenceableException(parent + " is not a dereferenceable object");
			else
				return ((ObjectValue) parentValue).dereference(identifier);
		}
	}

	public void setValue(Value value, IdentifierMap values)
	{
		if (parent == null)
			values.add(identifier, value);
		else
		{
			Value parentValue = parent.evaluate(values);
			if (parentValue instanceof FunctionValue)
				throw new NotDereferenceableException(parent + " is not a dereferenceable object");
			else
				((ObjectValue) parentValue).setDereference(identifier, value);
		}
		
	}
	
	public Expression getParent()
	{
		return parent;
	}
	public String getReference()
	{
		return identifier;
	}
	
	@Override
	public String toString()
	{
		if (parent == null)
			return identifier;
		else
			return parent.toString() + "." + identifier;
	}

	private	Expression source;
	
	@Override
	public boolean hasSideEffects()
	{
		return false;
	}
	
	@Override
	public Expression localOptimize()
	{
		Expression sourceTrace = source;
		while (sourceTrace instanceof ExpressionIdentifier)
			sourceTrace = ((ExpressionIdentifier) sourceTrace).source;
		if (sourceTrace instanceof ExpressionLiteral)
			return sourceTrace;
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations)
	{
		if (parent != null)
			throw new UnsupportedOperationException("Complicated references don't support value propogation yet");
		if (declarations.containsKey(identifier) && declarations.get(identifier) != null)
			source = declarations.get(identifier);
	}
}