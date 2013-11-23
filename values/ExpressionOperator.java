package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.Token;

public class ExpressionOperator extends Expression
{
	private String op;
	
	public ExpressionOperator(Token token)
	{
		op = "_" + token.toString();
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		switch (op)
		{
			case "+": 
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return op;
	}
}