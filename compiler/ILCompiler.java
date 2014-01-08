package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class ILCompiler
{
	private static PrintWriter output;
	
	public static void compile(File input) throws IOException
	{
		TokenStream stream = TokenStream.readFile(input);

		Block block = (Block) new Block(null, stream, 0);

		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream();
		in.connect(out);
		output = new PrintWriter(out);
		

		for (Line line : block.getLines())
			compile(line);
		for (Line line : block.getLines())
			functionDeclarations(line);
		
		ControlFlowGraph.analyze(in);
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
				Block block = ((DefLine) line).getBlock();
				functionDeclarations(block);
				output.println("label _tl" + line.getID());
				List<String> params = ((DefLine) line).getParams();
				for (int i = params.size() - 1; i >= 0; i--)
					output.println("getparam " + params.get(i));
				for (Line subline : block.getLines())
					compile(subline);
				output.println("return null");
			case "ClassLine":
				throw new UnsupportedOperationException();
		}
	}
	
	private static void compile(Line line)
	{
		String str;
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					compile(subLine);
				break;
			case "AssignmentLine":
				List<Expression> assignTargets = ((AssignmentLine) line).getAssignTargets();
				List<Expression> assignValues = ((AssignmentLine) line).getAssignValues();
				for (int i = 0; i < assignValues.size(); i++)
					output.println("_" + i + "tl" + line.getID() + " = " + compile(assignValues.get(i)));
				for (int i = 0; i < assignValues.size(); i++)
				{
					Expression target = assignTargets.get(i);
					if (target instanceof ExpressionIdentifier)
					{
						if (((ExpressionIdentifier) target).getParent() != null)
							throw new UnsupportedOperationException();
						else
						{
							output.println(((ExpressionIdentifier) target).getReference() + " = _" + i + "tl" + line.getID());
							continue;
						}
					}
					if (target instanceof ExpressionReference)
						throw new UnsupportedOperationException();
					throw new UnsupportedOperationException();
				}
				break;
			case "PrintLine":
				PrintLine printLine = (PrintLine) line;
				for (Expression expr : printLine.getPrintExpressions())
					output.println("param " + compile(expr));
				if (printLine.isPrintNewline())
				{
					output.println("_tl" + line.getID() + " = \"\\n\"");
					output.println("param _1tl" + line.getID());
					output.println("call print " + (printLine.getPrintExpressions().size() + 1));
				}
				else
					output.println("call print " + printLine.getPrintExpressions().size());
				output.println("returnvalue _2tl" + line.getID());
				break;
			case "BreakLine":
				throw new UnsupportedOperationException();
			case "ContinueLine":
				throw new UnsupportedOperationException();
			case "ReturnLine":
				str = compile(((ReturnLine) line).getReturnExpr());
				output.println("return " + str);
				break;
			case "AssertLine":
				throw new UnsupportedOperationException();
			case "IfLine":
				IfLine ifLine = (IfLine) line;
				List<Expression> conditions = ifLine.getConditions();
				List<Block> blocks = ifLine.getClauses();
				for (int i = 0; i < conditions.size(); i++)
				{
					str = compile(conditions.get(i));
					output.println("jumpunless " + str + " _" + i + "tl" + line.getID());
					compile(blocks.get(i));
					output.println("jump _tl" + line.getID());
					output.println("label _" + i + "tl" + line.getID());
				}
				if (blocks.size() > conditions.size())
					compile(blocks.get(blocks.size() - 1));
				output.println("label _tl" + line.getID());
				break;
			case "WhileLine":
				WhileLine whileLine = (WhileLine) line;
				Expression condition = whileLine.getCondition();
				Block mainBlock = whileLine.getBlock();
				Block thenBlock = whileLine.getThenBlock();
				output.println("label _1tl" + line.getID());
				str = compile(condition);
				output.println("jumpunless " + str + " _2tl" + line.getID());
				compile(mainBlock);
				output.println("jump _1tl" + line.getID());
				output.println("label _2tl" + line.getID());
				compile(thenBlock);
				output.println("label _3tl" + line.getID());
				break;
			case "ForLine":
				throw new UnsupportedOperationException();
			case "DefLine":
				DefLine defLine = (DefLine) line;
				output.println("function " + defLine.getName() + " " + defLine.getID());
				break;
			case "ClassLine":
				throw new UnsupportedOperationException();
			case "CallLine":
				compile(((CallLine) line).getCall());
				break;
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
						output.println(str + " = " + ((BooleanValue) v).getValue());
						break;
					case "IntValue":
						output.println(str + " = " + ((IntValue) v).getValue());
						break;
					case "StringValue":
						output.println(str + " = " + ((StringValue) v).getValue());
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
					output.println("_te" + expr.getID() + " = " + var.getReference());
				return "_te" + expr.getID();
			case "ExpressionReference":
				throw new UnsupportedOperationException();
			case "ExpressionList":
				throw new UnsupportedOperationException();
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				if (call.getFunction() instanceof ExpressionOperator)
				{
					List<Expression> exprs = call.getOperands();
					if (exprs.size() == 1)
						output.println("_te" + expr.getID() + " = " + compile(call.getFunction()) + " " + compile(exprs.get(0)));
					else if (exprs.size() == 2)
						output.println("_te" + expr.getID() + " = " + compile(exprs.get(0)) + " " + compile(call.getFunction()) + " " + compile(exprs.get(1)));
					else
						throw new AssertionError();
					return "_te" + expr.getID();
				}
				else
				{
					for (Expression subexpr : call.getOperands())
					{
						str = compile(subexpr);
						output.println("param " + str);
					}
					str = compile(call.getFunction());
					output.println("call " + str + " " + call.getOperands().size());
					output.println("returnvalue _te" + expr.getID());
					return "_te" + expr.getID();
				}
			case "ExpressionOperator":
				return expr.toString();
			case "ExpressionIncrement":
				throw new UnsupportedOperationException();
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				str = compile(ternary.getCond());
				output.println("unlessjump " + str + " _1te" + expr.getID());
				str = compile(ternary.getTrueCase());
				output.println("_te" + expr.getID() + " = " + str);
				output.println("jump _2te" + expr.getID());
				output.println("label _1te" + expr.getID());
				str = compile(ternary.getFalseCase());
				output.println("_te" + expr.getID() + " = " + str);
				output.println("label _2te" + expr.getID());
				return str;
			default:
				throw new UnsupportedOperationException();
		}
	}
}