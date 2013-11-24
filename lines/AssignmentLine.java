// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.*;
import ambroscum.parser.*;

public class AssignmentLine extends Line
{
	private ExpressionReference[] assignIDs; // list of the ids being set to
	private Expression[] expressions;

	AssignmentLine(TokenStream idStream, TokenStream valueStream)
	{
		ArrayList<ExpressionReference> assignIDsList = new ArrayList<ExpressionReference>();
		while (true) {
			assignIDsList.add(ExpressionReference.createExpressionReference(idStream.removeFirst(), idStream));
			if (idStream.size() > 0) {
				Token comma = idStream.removeFirst(); // Remove the comma
				if (comma != Token.COMMA) {
					throw new SyntaxError("Expected a comma delimiter in assignment");
				}
			} else {
				break;
			}
		}

		ArrayList<Expression> exprsList = new ArrayList<Expression>();
		while (true) {
			exprsList.add(Expression.interpret(valueStream));
			if (valueStream.size() > 0) {
				Token first = valueStream.getFirst();
				if (Token.COMMA == first) {
					valueStream.removeFirst();
				} else if (Token.NEWLINE == first) {
					valueStream.removeFirst();
					break;
				} else {
					throw new SyntaxError("Expected a comma delimiter or newline in assignment");
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
			throw new SyntaxError("Invalid assignment line: " + Arrays.toString(assignIDs) + " = " + Arrays.toString(expressions));
	}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}

	@Override
	public void evaluate(IdentifierMap values)
	{
		Value[] targetVals = new Value[assignIDs.length];
		for (int i = 0; i < expressions.length; i++) {
			targetVals[i] = expressions[i].evaluate(values);
		}
		for (int i = 0; i < expressions.length; i++) {
			assignIDs[i].setValue(targetVals[i], values);
		}
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString(assignIDs) + " = " + Arrays.toString(expressions);
	}
}
