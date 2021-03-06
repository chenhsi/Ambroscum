package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;
import ambroscum.values.ListValue;
import java.util.*;

public class ExpressionList extends Expression {
	
	private Expression[] expressions;
	
	public ExpressionList(Token opener, TokenStream stream) {
		// Expects a stream of form "expression, expression, expression]"
		ArrayList<Expression> exprsList = new ArrayList<Expression>();
		if (!stream.getFirst().toString().equals("]")) {
			while (true) {
				exprsList.add(Expression.interpret(stream));
				Token comma = stream.removeFirst(); // Remove the comma
				if (comma == Token.NEWLINE)
					throw new SyntaxError("Unexpected end of line when parsing expression");
				if (comma.toString().equals("]"))
					break; // End of list
				if (comma != Token.COMMA)
					throw new SyntaxError("Expected a comma delimiter in assignment");
			}
			
			expressions = new Expression[exprsList.size()];
			exprsList.toArray(expressions);
		} else {
			stream.removeFirst();
			expressions = new Expression[0];
		}
	}

	public Value evaluate(IdentifierMap values) {
		Value[] valuesList = new Value[expressions.length];
		for (int i = 0; i < valuesList.length; i++) {
			valuesList[i] = expressions[i].evaluate(values);
		}
		return new ListValue(valuesList);
	}

	public String toString() {
		return Arrays.toString(expressions);
	}

	// unsafe, could be fixed later	
	public Expression[] getExpressions()
	{
		return expressions;
	}
	
	@Override
	public boolean hasSideEffects()
	{
		for (Expression expr : expressions)
			if (expr.hasSideEffects())
				return true;
		return false;
	}
	
	@Override
	public Expression localOptimize()
	{
		for (int i = 0; i < expressions.length; i++)
			expressions[i] = expressions[i].localOptimize();
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations)
	{
		for (Expression expr : expressions)
			expr.setDeclarations(declarations);
	}
}