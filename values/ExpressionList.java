package ambroscum.values;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;
import java.util.*;

public class ExpressionList extends Expression {
	
	private Expression[] expressions;
	
	public ExpressionList(Token opener, TokenStream stream) {
		ArrayList<Expression> exprsList = new ArrayList<Expression>();
		while (true) {
			exprsList.add(Expression.interpret(stream));
			if (stream.size() > 0) {
				Token comma = stream.removeFirst(); // Remove the comma
				if (comma.toString().equals("]")) {
					// End of list
					break;
				}
				if (comma != Token.COMMA) {
					throw new SyntaxError("Expected a comma delimiter in assignment");
				}
			} else {
				throw new SyntaxError("Unexpected end of expression");
			}
		}
		
		expressions = new Expression[exprsList.size()];
		exprsList.toArray(expressions);
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
}