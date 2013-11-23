package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionTree extends Expression
{
	private String operator;
	private List<Expression> list;
	
	public ExpressionTree(String code)
	{
		
	}
	
	public Value evaluate(IdentifierMap values)
	{
		return null;
		// currently falsely assumes that there's always exactly 2 operators
		// also assumes that the operator is the direct name of the function
//		return list.get(0).evaluate().call("_" + operator, list.get(1).evaluate());
	}
}