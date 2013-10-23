package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionReference extends Expression
{
	private Value primary;
	private ReferenceType type;
	private Object reference;
	
	
	public ExpressionReference(String code)
	{
		
	}
	
	public Value evaluate(IdentifierMap values)
	{
		throw new UnsupportedOperationException();
	}
	
	private enum ReferenceType
	{
		NONE, DOT, BRACKET, BRACE
	}
}