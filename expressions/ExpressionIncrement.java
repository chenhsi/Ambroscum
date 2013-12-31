// this name is terrible

package ambroscum.expressions;

import ambroscum.IdentifierMap;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import ambroscum.errors.SyntaxError;

public class ExpressionIncrement extends ExpressionCall
{
	private Expression baseExpr;
	private Expression opExpr;
	private boolean prefix;
	
	public ExpressionIncrement(Expression expr, boolean increment, boolean prefix)
	{
		super(null); // extending ExpressionCall mainly for typing purposes - nothing of ExpressionCall's should ever be used
		if (!(expr instanceof ExpressionReference || expr instanceof ExpressionIdentifier))
			throw new SyntaxError("Only references can be " + (increment ? "in" : "de") + "cremented: " + expr);
		this.prefix = prefix;
		baseExpr = expr;
		opExpr = new ExpressionCall(new ExpressionOperator(increment ? "+" : "-"), expr, new ExpressionLiteral(IntValue.fromInt(1)));
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		Value setValue = opExpr.evaluate(values);
		Value returnValue = (prefix ? setValue : baseExpr.evaluate(values));
		if (baseExpr instanceof ExpressionIdentifier)
			((ExpressionIdentifier) baseExpr).setValue(setValue, values);
		if (baseExpr instanceof ExpressionReference)
			((ExpressionReference) baseExpr).setValue(setValue, values);
		return returnValue;
	}
	
	@Override
	public String toString()
	{
		throw new UnsupportedOperationException();
	}
}