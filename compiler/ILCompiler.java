package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class ILCompiler
{
	private static List<String> instructions;
	private static Map<String, List<String>> functions;
	
	public static ControlFlowGraph compile(File input) throws IOException
	{
		TokenStream stream = TokenStream.readFile(input);

		Block block = (Block) new Block(null, stream, 0);

		functions = new HashMap<> ();
		for (Line line : block.getLines())
			functionDeclarations(line);

		instructions = new LinkedList<> ();
		for (Line line : block.getLines())
			compile(line, null, null);
//		instructions.add("return null");

		ControlFlowGraph graph = new ControlFlowGraph(instructions, functions);
		graph.optimize();
		
		return graph;
	}
	
	private static void functionDeclarations(Line line)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "BlockLine":
				for (Line subline : ((Block) line).getLines())
					functionDeclarations(subline);
				break;
			case "AssignmentLine":
				break;
			case "PrintLine":
				break;
			case "BreakLine":
				break;
			case "ContinueLine":
				break;
			case "ReturnLine":
				break;
			case "AssertLine":
				break;
			case "CallLine":
				break;
			case "IfLine":
				for (Block block : ((IfLine) line).getClauses())
					functionDeclarations(block);
				break;
			case "WhileLine":
				functionDeclarations(((WhileLine) line).getBlock());
				functionDeclarations(((WhileLine) line).getThenBlock());
				break;
			case "ForLine":
				functionDeclarations(((ForLine) line).getLoopBlock());
				functionDeclarations(((ForLine) line).getThenBlock());
				break;
			case "DefLine":
				instructions = new LinkedList<String> ();
				Block block = ((DefLine) line).getBlock();
				functionDeclarations(block);
				List<String> params = ((DefLine) line).getParams();
				for (int i = params.size() - 1; i >= 0; i--)
					instructions.add(params.get(i) + " = paramvalue");
				for (Line subline : block.getLines())
					compile(subline, null, null);
				instructions.add("return null");
				functions.put("_tl" + line.getID(), instructions);
				break;
			case "ClassLine":
				throw new UnsupportedOperationException();
		}
	}
	
	private static void compile(Line line, String breakTarget, String continueTarget)
	{
		String str;
		if (line == null)
			return;
		int id = line.getID();
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					compile(subLine, breakTarget, continueTarget);
				break;
			case "AssignmentLine":
				List<Expression> assignTargets = ((AssignmentLine) line).getAssignTargets();
				List<Expression> assignValues = ((AssignmentLine) line).getAssignValues();
				for (int i = 0; i < assignValues.size(); i++)
				{
					ExpressionOperator op = ((AssignmentLine) line).getAssignType();
					if (op == null)
						instructions.add("_" + i + "_1tl" + id + " = " + compile(assignValues.get(i)));
					else
					{
						String rightHalf = compile(assignTargets.get(i)) + " " + compile(op) + " " + compile(assignValues.get(i));
						instructions.add("_" + i + "_1tl" + id + " = " + rightHalf);
					}
				}
				for (int i = 0; i < assignValues.size(); i++)
				{
					Expression target = assignTargets.get(i);
					if (target instanceof ExpressionIdentifier)
					{
						if (((ExpressionIdentifier) target).getParent() != null)
							throw new UnsupportedOperationException();
						else
							instructions.add(((ExpressionIdentifier) target).getReference() + " = _" + i + "_1tl" + id);
					}
					else if (target instanceof ExpressionReference)
					{
						ExpressionReference ref = (ExpressionReference) target;
						if (ref.getSecondaryRight() != null)
							throw new UnsupportedOperationException();
						// assumes list
						instructions.add("_" + i + "_2tl" + id + " = 1 + " + compile(ref.getSecondary()));
						instructions.add("_" + i + "_3tl" + id + " = 4 * _" + i + "_2tl" + id);
						instructions.add("_" + i + "_4tl" + id + " = _" + i + "_3tl" + id + " + " + compile(ref.getPrimary()));
						instructions.add("*_" + i + "_4tl" + id + " = _" + i + "_1tl" + id);
					}
					else
						throw new UnsupportedOperationException();
				}
				break;
			case "PrintLine":
				PrintLine printLine = (PrintLine) line;
				boolean firstPrint = true;
				for (Expression expr : printLine.getPrintExpressions())
				{
					if (!firstPrint)
					{
						instructions.add("param \" \"");
						instructions.add("call print 1");
					}
					firstPrint = false;
					instructions.add("param " + compile(expr));
					instructions.add("call print 1");
				}
				if (printLine.isPrintNewline())
				{
					instructions.add("param \"\\\\n\"");
					instructions.add("call print 1");
				}
				break;
			case "BreakLine":
				instructions.add("jump " + breakTarget);
				break;
			case "ContinueLine":
				instructions.add("jump " + continueTarget);
				break;
			case "ReturnLine":
				Expression returnExpr = ((ReturnLine) line).getReturnExpr();
				if (returnExpr != null)
					instructions.add("return " + compile(returnExpr));
				else
					instructions.add("return null");
				break;
			case "AssertLine":
				// tempted to simply assume no errors and continue on
				// or, maybe special error block?
				throw new UnsupportedOperationException();
			case "IfLine":
				IfLine ifLine = (IfLine) line;
				List<Expression> conditions = ifLine.getConditions();
				List<Block> blocks = ifLine.getClauses();
				for (int i = 0; i < conditions.size(); i++)
				{
					str = compile(conditions.get(i));
					instructions.add("jumpunless " + str + " _" + i + "tl" + id);
					compile(blocks.get(i), breakTarget, continueTarget);
					instructions.add("jump _tl" + id);
					instructions.add("label _" + i + "tl" + id);
				}
				if (blocks.size() > conditions.size())
					compile(blocks.get(blocks.size() - 1), breakTarget, continueTarget);
				instructions.add("label _tl" + id);
				break;
			case "WhileLine":
				WhileLine whileLine = (WhileLine) line;
				Expression condition = whileLine.getCondition();
				Block mainBlock = whileLine.getBlock();
				Block thenBlock = whileLine.getThenBlock();
				instructions.add("label _1tl" + id);
				str = compile(condition);
				instructions.add("jumpunless " + str + " _2tl" + id);
				compile(mainBlock, "_3tl" + id, "_1tl" + id);
				instructions.add("jump _1tl" + id);
				instructions.add("label _2tl" + id);
				compile(thenBlock, breakTarget, continueTarget);
				instructions.add("label _3tl" + id);
				break;
			case "ForLine":
				ForLine forLine = (ForLine) line;
				instructions.add("_1tl" + id + " = " + compile(forLine.getIterable())); // pointer to array
				instructions.add("_2tl" + id + " = *_1tl" + id); // has the length of the array
				instructions.add("_3tl" + id + " = 4 * _2tl" + id);
				instructions.add("_4tl" + id + " = _1tl" + id + " + _3tl" + id); // pointer to last array element
				instructions.add("label _5tl" + id); // start of loop
				instructions.add("_6tl" + id + " = _1tl" + id + " < _4tl" + id); // condition for if loop should end
				instructions.add("jumpunless _6tl" + id + " _7tl" + id); // jump if loop has ended
				instructions.add("_1tl" + id + " = 4 + _1tl" + id); // move pointer in array
				instructions.add(forLine.getIterVariable() + " = *_1tl" + id); // get the actual value
				compile(forLine.getLoopBlock(), "_8tl" + id, "_5tl" + id);
				instructions.add("jump _5tl" + id);
				instructions.add("label _7tl" + id);
				compile(forLine.getThenBlock(), breakTarget, continueTarget);
				instructions.add("label _8tl" + id);
				break;
			case "DefLine":
				throw new UnsupportedOperationException();
			case "ClassLine":
				throw new UnsupportedOperationException();
			case "CallLine":
				throw new UnsupportedOperationException();
		}
	}
	
	private static String compile(Expression expr)
	{
		String str;
		switch (expr.getClass().getSimpleName())
		{
			case "ExpressionLiteral":
				Value v = ((ExpressionLiteral) expr).getValue();
				str = "_te" + expr.getID();
				switch (v.getClass().getSimpleName())
				{
					case "NullValue":
						throw new UnsupportedOperationException();
					case "BooleanValue":
						instructions.add(str + " = " + ((BooleanValue) v).getValue());
						break;
					case "IntValue":
						instructions.add(str + " = " + ((IntValue) v).getValue());
						break;
					case "StringValue":
						instructions.add(str + " = \"" + ((StringValue) v).getValue() + "\"");
						break;
					default:
						throw new UnsupportedOperationException();
				}
				return str;
			case "ExpressionIdentifier":
				ExpressionIdentifier var = (ExpressionIdentifier) expr;
				if (var.getParent() != null)
					throw new UnsupportedOperationException();
				else
					instructions.add("_te" + expr.getID() + " = " + var.getReference());
				return "_te" + expr.getID();
			case "ExpressionReference":
				// assuming lists and not dicts
				ExpressionReference ref = (ExpressionReference) expr;
				instructions.add("_1te" + expr.getID() + " = " + compile(ref.getSecondary()) + " + 1");
				instructions.add("_2te" + expr.getID() + " = _1te" + expr.getID() + " * 4");
				instructions.add("_3te" + expr.getID() + " = " + compile(ref.getPrimary()) + " + _2te" + expr.getID());
				instructions.add("_te" + expr.getID() + " = *_3te" + expr.getID());
				return "_te" + expr.getID();
			case "ExpressionList":
				Expression[] array = ((ExpressionList) expr).getExpressions();
				instructions.add("call malloc " + (array.length + 1) * 4);
				instructions.add("_te" + expr.getID() + " = returnvalue");
				instructions.add("*_te" + expr.getID() + " = " + array.length);
				for (int i = 0; i < array.length; i++)
				{
					str = compile(array[i]);
					instructions.add("_" + i + "te" + expr.getID() + " = _te" + expr.getID() + " + " + (i + 1) * 4);
					instructions.add("*_" + i + "te" + expr.getID() + " = " + str);
				}
				return "_te" + expr.getID();
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				if (call.getFunction() instanceof ExpressionOperator)
				{
					List<Expression> exprs = call.getOperands();
					str = "_te" + expr.getID();
					if (exprs.size() == 1)
					{
						String op = compile(call.getFunction());
						if (op.equals("-"))
							instructions.add(str + " = 0 - " + compile(exprs.get(0)));
						else
							instructions.add(str + " = " + op + " " + compile(exprs.get(0)));
					}
					else if (exprs.size() == 2)
						instructions.add(str + " = " + compile(exprs.get(0)) + " " + compile(call.getFunction()) + " " + compile(exprs.get(1)));
					else
						throw new AssertionError();
					return str;
				}
				else
				{
					for (Expression subexpr : call.getOperands())
					{
						str = compile(subexpr);
						instructions.add("param " + str);
					}
					str = compile(call.getFunction());
					instructions.add("call " + str + " " + call.getOperands().size());
					instructions.add("_te" + expr.getID() + " = returnvalue");
					return "_te" + expr.getID();
				}
			case "ExpressionOperator":
				return expr.toString();
			case "ExpressionIncrement":
				ExpressionIncrement asIncr = (ExpressionIncrement) expr;
				String base = compile(asIncr.getBaseExpression());
				instructions.add(base + " = " + compile(asIncr.getIncrementExpression()));
				throw new UnsupportedOperationException();
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				str = compile(ternary.getCond());
				instructions.add("jumpunless " + str + " _1te" + expr.getID());
				str = compile(ternary.getTrueCase());
				instructions.add("_te" + expr.getID() + " = " + str);
				instructions.add("jump _2te" + expr.getID());
				instructions.add("label _1te" + expr.getID());
				str = compile(ternary.getFalseCase());
				instructions.add("_te" + expr.getID() + " = " + str);
				instructions.add("label _2te" + expr.getID());
				return "_te" + expr.getID();
			default:
				throw new UnsupportedOperationException();
		}
	}
}