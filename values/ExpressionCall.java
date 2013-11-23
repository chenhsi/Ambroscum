package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionCall extends Expression
{
	private Function func; // not sure how func is being implemented yet
	private List<Expression> list;
	
	public ExpressionCall(String code)
	{
		
	}
	
	public Value evaluate(IdentifierMap values)
	{
		return null;
//		return list.get(0).evaluate(values).call("_" + func, list.get(1).evaluate(values));
	}
}