// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.error.*;
import ambroscum.values.*;
import ambroscum.parser.*;

public class AssignmentLine extends Line
{
	private ExpressionReference[] assignIDs; // list of the ids being set to
	private Expression[] expressions;

	AssignmentLine(TokenStream stream)
	{
		ArrayList<ExpressionReference> assignIDsList = new ArrayList<ExpressionReference>();
		while (true) {
			assignIDsList.add(new ExpressionReference(stream));
			if (stream.size() > 0) {
				Token first = stream.getFirst();
				if (Token.COMMA == first) {
					stream.removeFirst();
				} else if ("=".equals(first.toString())) {
					stream.removeFirst();
					break;
				}
			} else {
				throw new SyntaxError("Expected assignment operation");
			}
		}
		
		ArrayList<Expression> exprsList = new ArrayList<Expression>();
		while (true) {
			exprsList.add(Expression.interpret(stream));
			if (stream.size() > 0) {
				Token first = stream.getFirst();
				if (Token.COMMA == first) {
					stream.removeFirst();
				} else if (Token.NEWLINE == first) {
					stream.removeFirst();
					break;
				}
			} else {
				throw new SyntaxError("Expected value to assign");
			}
		}
		
		assignIDs = new ExpressionReference[assignIDsList.size()];
		assignIDsList.toArray(assignIDs);
		expressions = new Expression[exprsList.size()];
		exprsList.toArray(expressions);
		
		if (expressions.length != assignIDs.length)
			throw new RuntimeException("need to find better exceptions"); // comment to be easier to find
	}

	public void evaluate(IdentifierMap values)
	{
		for (int i = 0; i < expressions.length; i++) {
			assignIDs[i].setValue(expressions[i].evaluate(values), values);
		}
	}
}
