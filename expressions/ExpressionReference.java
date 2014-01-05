package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.AmbroscumError;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.OptimizedException;
import ambroscum.errors.NotDereferenceableException;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import ambroscum.values.ListValue;
import ambroscum.values.DictValue;
import ambroscum.values.FunctionValue;

import java.util.*;

public class ExpressionReference extends Expression
{
	private Expression primary;
	private Expression secondary;
	private Expression secondaryRight;

	public ExpressionReference(Expression base, Expression reference)
	{
		primary = base;
		secondary = reference;
	}

	public ExpressionReference(Expression base, Expression sliceLeft, Expression sliceRight)
	{
		primary = base;
		secondary = sliceLeft;
		secondaryRight = sliceRight;
	}

	@Override
	public Value evaluate(IdentifierMap values)
	{
		Value first = primary.evaluate(values);
		if (first instanceof FunctionValue)
			throw new NotDereferenceableException("Cannot dot reference a function");
		else if (first instanceof ListValue)
		{
			if (secondaryRight == null)
				return ((ListValue) first).get(secondary.evaluate(values));
			else
				return ((ListValue) first).getRange(secondary.evaluate(values), secondaryRight.evaluate(values));
		}
		else if (first instanceof DictValue)
		{
			if (secondaryRight != null)
				throw new SyntaxError("Dictionaries cannot be sliced");
			return ((DictValue) first).get(secondary.evaluate(values));
		}
		else
			throw new UnsupportedOperationException("this should prob be converted to an operator of some sort");
		
	}

	public void setValue(Value value, IdentifierMap values)
	{
		Value first = primary.evaluate(values);
		if (first instanceof FunctionValue)
			throw new NotDereferenceableException("Cannot dot reference a function");
		else if (first instanceof ListValue)
			((ListValue) first).set(secondary.evaluate(values), value);
		else if (first instanceof DictValue)
			((DictValue) first).set(secondary.evaluate(values), value);
		else
			throw new UnsupportedOperationException("this should prob be converted to an operator of some sort");
	}
	
	public Expression getPrimary()
	{
		return primary;
	}
	public Expression getSecondary()
	{
		return secondary;
	}
	public Expression getSecondaryRight() // this name is terrible
	{
		return secondaryRight;
	}
	
	@Override
	public String toString()
	{
		return primary.toString() + "[" + secondary.toString() + "]";
	}
	
	@Override
	public boolean hasSideEffects()
	{
		return primary.hasSideEffects() || secondary.hasSideEffects();
	}
	
	@Override
	public Expression localOptimize()
	{
		primary = primary.localOptimize();
		secondary = secondary.localOptimize();
		if (secondary instanceof ExpressionLiteral)
		{
			Value reference = ((ExpressionLiteral) secondary).getValue();
			if (primary instanceof ExpressionList && !((ExpressionList) primary).hasSideEffects())
			{
				try
				{
					if (!(reference instanceof IntValue))
						throw new SyntaxError("Expected int for list index");
					return ((ExpressionList) primary).getExpressions()[(int) ((IntValue) reference).getValue()];
				}
				catch (AmbroscumError e)
				{
					throw new OptimizedException(e);
				}
			}
		}
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations)
	{
		primary.setDeclarations(declarations);
		secondary.setDeclarations(declarations);
	}
}