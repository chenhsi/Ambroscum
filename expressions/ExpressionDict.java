package ambroscum.expressions;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;
import ambroscum.values.DictValue;
import java.util.*;

public class ExpressionDict extends Expression {
	
	private ArrayList<Expression> keyList, valueList;
	
	public ExpressionDict(Token opener, TokenStream stream) {
		// Expects a stream of form "expression : expression, expression : expression, expression : expression}"
		keyList = new ArrayList<>();
		valueList = new ArrayList<>();
		if (!stream.getFirst().toString().equals("}"))
		{
			while (true)
			{
				keyList.add(Expression.interpret(stream));
				Token colon = stream.removeFirst(); // Remove the colon that separates the key and the value
				if (colon != Token.COLON)
					throw new SyntaxError("Expected a colon delimiter in assignment");
				valueList.add(Expression.interpret(stream));
				
				Token comma = stream.removeFirst(); // Remove the comma
				if (comma == Token.NEWLINE)
					throw new SyntaxError("Unexpected end of line when parsing expression");
				if (comma.toString().equals("}"))
					break; // End of list
				if (comma != Token.COMMA)
					throw new SyntaxError("Expected a comma delimiter in assignment");
			}
		} else {
			stream.removeFirst();
		}
	}

	public Value evaluate(IdentifierMap values) {
		ArrayList<Value> keyVals = new ArrayList<>(), valVals = new ArrayList<>();
		for (int i = 0; i < keyList.size(); i++) {
			keyVals.add(keyList.get(i).evaluate(values));
			valVals.add(valueList.get(i).evaluate(values));
		}
		return new DictValue(keyVals, valVals);
	}

	public String toString() {
		return null;
	}
	
	@Override
	public Expression localOptimize()
	{
		for (int i = 0; i < valueList.size(); i++)
			valueList.set(i, valueList.get(i).localOptimize());
		return this;
	}
}