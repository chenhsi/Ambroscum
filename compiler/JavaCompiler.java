/**
 * The compiler. Currently compiles Ambroscum files to Java files, in a single
 * Main.java file.
 * 
 * @author Chen-Hsi Steven Bi
 * @version 1.0
 */

package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class JavaCompiler
{
	private static final boolean optimizeLocally = true; // should be a safe optimization, i.e. does not introduce errors or change behavior
														 // actually, a necessary optimization - removes lines after jump statements that javac hates
	private static final boolean propogateConstants = false; // causes errors when there are multiple scopes
	
	private static PrintWriter out;
	
	/**
	 * Reads data from an Ambroscum file, and compiles it into a Java file.
	 *
	 * @param	file			the Ambroscum file to read from and compile
	 */
	public static void compile(File input, PrintWriter out) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(input);

		Block block = new Block(null, stream, 0);
		if (optimizeLocally)
			block = (Block) block.localOptimize();
		if (propogateConstants)
		{
			// finding declarations
			Map<String, Expression> lastDeclarations = new HashMap<String, Expression> ();
			block.setDeclarations(lastDeclarations, true);
			if (optimizeLocally)
				block = (Block) block.localOptimize();
		}

		out.println("import java.util.*;");
		out.println();
		for (Line line : block.getLines())
			functionDeclaration(line);
		out.println();
		out.println("public class Main {");
		out.println("\tpublic static void main(String[] args) {");
		out.println("\t\tVariableMap map = new VariableMap();");
		out.println("\t\tMap<String, VariableMap> scopes = new HashMap<>();");
		for (Line line : block.getLines())
		{
			process(line, 2);
			compile(line, 2);
		}
		out.println("\t}");
		out.println("}");
		out.flush();
		out.close();
	}
	
	private static void functionDeclaration(Line line)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					functionDeclaration(subLine);
				break;
			case "IfLine":
				for (Block block : ((IfLine) line).getClauses())
					functionDeclaration(block);
				break;
			case "WhileLine":
				functionDeclaration(((WhileLine) line).getBlock());
				functionDeclaration(((WhileLine) line).getThenBlock());
				break;
			case "ForLine":
				functionDeclaration(((ForLine) line).getLoopBlock());
				functionDeclaration(((ForLine) line).getThenBlock());
				break;
			case "DefLine":
				out.println("class _tl" + line.getID() + " extends Function {");
				out.println("\tpublic _tl" + line.getID() + "(VariableMap parentMap) {");
				out.print("\t\tsuper(parentMap");
				for (String str : ((DefLine) line).getParams())
					out.print(", \"" + str + "\"");
				out.print(");\n");
				out.println("\t}");
				out.println("\tprotected Value call(VariableMap map) {");
				Block block = ((DefLine) line).getBlock();
				if (block != null)
					for (Line subLine : block.getLines())
					{
						process(subLine, 2);
						compile(subLine, 2);
					}
				if (!block.endsWithReturn())
					out.println("\treturn null;");
				out.println("\t}");
				out.println("}");
				if (block != null)
					for (Line subLine : block.getLines())
						functionDeclaration(subLine);
				break;
		}
	}
	
	// should probably be moved into compile(Line)
	private static void process(Line line, int indentation)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				break;
			case "AssertLine":
				break;
			case "AssignmentLine":
				break;
			case "PrintLine":
				break;
			case "IfLine":
				break;
			case "WhileLine":
				break;
			case "ForLine":
				break;
			case "DefLine":
				break;
			case "BreakLine":
				break;
			case "ContinueLine":
				break;
			case "ReturnLine":
				break;
			case "CallLine":
				break;
		}
	}
	
	private static void compile(Line line, int indentation)
	{
		if (line == null)
			return;
		Block block, thenBlock;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
				{
					process(subLine, indentation);
					compile(subLine, indentation);
				}
				break;
			case "AssertLine":
				AssertLine asAssert = (AssertLine) line;
				process(asAssert.getTest(), indentation);
				printIndentation(indentation);
				out.print("if (((BooleanValue) ");
				compile(asAssert.getTest());
				out.print(").value) {\n");
				if (asAssert.getErrorMessage() != null)
					process(asAssert.getErrorMessage(), indentation + 1);
				printIndentation(indentation);
				out.print("assert false");
				if (asAssert.getErrorMessage() != null)
				{
					out.print(" : ");
					compile(asAssert.getErrorMessage());
				}
				printIndentation(indentation + 1);
				out.print(";\n");
				printIndentation(indentation);
				out.println("}");
				break;
			case "AssignmentLine":
				AssignmentLine assign = (AssignmentLine) line;
				int lastIndex = assign.getAssignTargets().size();
				for (int i = 0; i < lastIndex; i++)
				{
					Expression target = assign.getAssignValues().get(i);
					process(target, indentation);
					printIndentation(indentation);
					out.print("Value _" + i + "tl" + assign.getID() + " = ");
					compile(target);
					out.print(";\n");
				}
				ExpressionOperator assignType = assign.getAssignType();
				for (int i = 0; i < lastIndex; i++)
				{
					Expression target = assign.getAssignTargets().get(i);
					process(target, indentation);
					printIndentation(indentation);
					if (target instanceof ExpressionIdentifier)
					{
						if (((ExpressionIdentifier) target).getParent() != null)
							throw new UnsupportedOperationException();
						out.print("map.put(\"" + ((ExpressionIdentifier) target).getReference() + "\", ");
						if (assignType == null)
							out.print("_" + i + "tl" + assign.getID());
						else
						{
							compile(target);
							out.print(".operator(\"" + assignType + "\", _" + i + "tl" + assign.getID() + ")");
						}
						out.print(");\n");
					}
					else if (target instanceof ExpressionReference)
					{
						out.print("((AmbroscumList) ");
						compile(((ExpressionReference) target).getPrimary());
						out.print(").set((IntValue) ");
						compile(((ExpressionReference) target).getSecondary());
						out.print(", ");
						if (assignType == null)
							out.print("_" + i + "tl" + assign.getID());
						else
						{
							compile(target);
							out.print(".operator(\"" + assignType + "\", _" + i + "tl" + assign.getID() + ")");
						}
						out.print(");\n");
					}
				}
				break;
			case "PrintLine":
				PrintLine print = (PrintLine) line;
				for (Expression expr : print.getPrintExpressions())
					process(expr, indentation);
				boolean first = true;
				for (Expression expr : print.getPrintExpressions())
				{
					printIndentation(indentation);
					out.print("System.out.print");
					out.print("(");
					if (!first)
						out.print("\" \" + ");
					first = false;
					compile(expr);
					out.print(");\n");
				}
				if (print.isPrintNewline())
				{
					printIndentation(indentation);
					out.println("System.out.println();");
				}
				break;
			case "IfLine":
				List<Expression> conditions = ((IfLine) line).getConditions();
				List<Block> clauses = ((IfLine) line).getClauses();
				
				printIndentation(indentation);
				out.println("boolean _tl" + line.getID() + " = true;");
				
				for (int i = 0; i < conditions.size(); i++)
				{
					printIndentation(indentation);
					out.println("if (_tl" + line.getID() + ") {");
					process(conditions.get(i), indentation + 1);
					printIndentation(indentation + 1);
					out.print("if (((BooleanValue) ");
					compile(conditions.get(i));
					out.print(").value) {\n");
					printIndentation(indentation + 2);
					out.println("_tl" + line.getID() + " = false;");
					compile(clauses.get(i), indentation + 2);
					printIndentation(indentation + 1);
					out.println("}");
					printIndentation(indentation);
					out.println("}");
				}
				if (clauses.size() > conditions.size())
				{
					printIndentation(indentation);
					out.println("if (_tl" + line.getID() + ") {");
					process(clauses.get(clauses.size() - 1), indentation + 1);
					compile(clauses.get(clauses.size() - 1), indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				break;
			case "WhileLine":
				Expression condition = ((WhileLine) line).getCondition();
				block = ((WhileLine) line).getBlock();
				thenBlock = ((WhileLine) line).getThenBlock();
				
				printIndentation(indentation);
				out.println("boolean _tl" + line.getID() + ";");
				printIndentation(indentation);
				out.println("while (true) {");
				printIndentation(indentation + 1);
				out.println("_tl" + line.getID() + " = true;");
				process(condition, indentation + 1);
				printIndentation(indentation + 1);
				out.print("if (");
				compile(condition);
				out.print(" == BooleanValue.FALSE) break;\n");
				printIndentation(indentation + 1);
				out.println("_tl" + line.getID() + " = false;");
				compile(block, indentation + 1);
				printIndentation(indentation);
				out.println("}");
				
				if (thenBlock != null)
				{
					printIndentation(indentation);
					out.println("if (_tl" + line.getID() + ") {");
					compile(thenBlock, indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				break;
			case "ForLine":
				ExpressionIdentifier var = ((ForLine) line).getIterVariable();
				Expression iterable = ((ForLine) line).getIterable();
				block = ((ForLine) line).getLoopBlock();
				thenBlock = ((ForLine) line).getThenBlock();
				
				if (thenBlock == null)
				{
					process(iterable, indentation);
					printIndentation(indentation);
					out.print("for (Value _tl" + line.getID() + " : (AmbroscumList) ");
					compile(iterable);
					out.print(") {\n");
					printIndentation(indentation + 1);
					out.println("map.put(\"" + var.getReference() + "\", _tl" + line.getID() + ");");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				else
				{
					printIndentation(indentation);
					out.println("boolean _1tl" + line.getID() + ";");
					process(iterable, indentation);
					printIndentation(indentation);
					out.print("Iterator<Value> _2tl" + line.getID() + " = ((AmbroscumList) ");
					compile(iterable);
					out.print(").iterator();\n");
					printIndentation(indentation);
					out.println("while (true) {");
					printIndentation(indentation + 1);
					out.println("_1tl" + line.getID() + " = true;");
					printIndentation(indentation + 1);
					out.println("if (!_2tl" + line.getID() + ".hasNext()) break;");
					printIndentation(indentation + 1);
					out.println("_1tl" + line.getID() + " = false;");
					printIndentation(indentation + 1);
					out.println("map.put(\"" + var.getReference() + "\", _2tl" + line.getID() + ".next());");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.println("}");
					printIndentation(indentation);
					out.println("if (_1tl" + line.getID() + ") {");
					compile(thenBlock, indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				break;
			case "DefLine":
				printIndentation(indentation);
				out.println("map.put(\"" + ((DefLine) line).getName() + "\", new _tl" + line.getID() + "(map));");
				break;
			case "BreakLine":
				printIndentation(indentation);
				out.println("break;");
				break;
			case "ContinueLine":
				printIndentation(indentation);
				out.println("continue;");
				break;
			case "ReturnLine":
				Expression returnExpr = ((ReturnLine) line).getReturnExpr();
				if (returnExpr == null)
				{
					printIndentation(indentation);
					out.println("return null;");
				}
				else
				{
					process(returnExpr, indentation);
					printIndentation(indentation);
					out.print("return ");
					compile(returnExpr);
					out.print(";\n");
				}
				break;
			case "CallLine":
				process(((CallLine) line).getCall(), indentation);
				printIndentation(indentation);
				compile(((CallLine) line).getCall());
				out.print(";\n");
				break;
			default:
				System.err.println("Unsupported line: " + line);
		}
	}
	
	private static void process(Expression expr, int indentation)
	{
		switch (expr.getClass().getSimpleName())
		{
			case "ExpressionLiteral":
				printIndentation(indentation);
				out.print("Value _te" + expr.getID() + " = ");
				Value v = ((ExpressionLiteral) expr).getValue();
				switch (v.getClass().getSimpleName())
				{
					case "NullValue":
						out.print("null");
						break;
					case "BooleanValue":
						out.print("BooleanValue.from(" + ((BooleanValue) v).getValue() + ")");
						break;
					case "IntValue":
						out.print("IntValue.from(" + ((IntValue) v).getValue() + ")");
						break;
					case "StringValue":
						out.print("StringValue.from(" + v.repr() + ")");
						break;
				}
				out.print(";\n");
				break;
			case "ExpressionIdentifier":
				Expression possParent = ((ExpressionIdentifier) expr).getParent();
				if (possParent != null)
					process(possParent, indentation);
				break;
			case "ExpressionReference":
				ExpressionReference cast = (ExpressionReference) expr;
				process(cast.getPrimary(), indentation);
				process(cast.getSecondary(), indentation);
				if (cast.getSecondaryRight() != null)
					process(cast.getSecondaryRight(), indentation);
				break;
			case "ExpressionList":
				printIndentation(indentation);
				for (Expression subexpr : ((ExpressionList) expr).getExpressions())
					process(subexpr, indentation);
				out.print("AmbroscumList _e" + expr.getID() + " = new AmbroscumList();\n");
				for (Expression subexpr : ((ExpressionList) expr).getExpressions())
				{
					printIndentation(indentation);
					out.print("_e" + expr.getID() + ".add(");
					compile(subexpr);
					out.print(");\n");
				}
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				process(call.getFunction(), indentation);
				for (Expression operand : call.getOperands())
					process(operand, indentation);
				break;
			case "ExpressionOperator":
				break;
			case "ExpressionIncrement":
				process(((ExpressionIncrement) expr).getBaseExpression(), indentation);
				break;
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				process(ternary.getCond(), indentation);
				process(ternary.getTrueCase(), indentation);
				process(ternary.getFalseCase(), indentation);
				break;
		}
	}
	
	private static void compile(Expression expr)
	{
		switch (expr.getClass().getSimpleName())
		{
			case "ExpressionLiteral":
				out.print("_te" + expr.getID());
				break;
			case "ExpressionIdentifier":
				Expression possParent = ((ExpressionIdentifier) expr).getParent();
				if (possParent == null)
					out.print("map.get(\"");
				else
				{
					out.print("scopes.get(");
					compile(possParent);
					out.print(").get(\"");
				}
				out.print(((ExpressionIdentifier) expr).getReference());
				out.print("\")");
				break;
			case "ExpressionReference":
				ExpressionReference ref = (ExpressionReference) expr;
				out.print("((AmbroscumList) ");
				compile(ref.getPrimary());
				if (ref.getSecondaryRight() == null)
				{
					out.print(").get((IntValue) ");
					compile(ref.getSecondary());
				}
				else
				{
					out.print(").subList((IntValue) ");
					compile(ref.getSecondary());
					out.print(", (IntValue) ");
					compile(ref.getSecondaryRight());
				}
				out.print(")");
				break;
			case "ExpressionList":
				out.print("_e" + expr.getID());
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				out.print("(");
				if (call.getFunction() instanceof ExpressionOperator)
				{
					String type = ((ExpressionOperator) call.getFunction()).getValue().getOperandType();
					compile(call.getOperands().get(0));
					out.print(".operator(\"");
					compile(call.getFunction());
					out.print("\"");
					if (call.getOperands().size() > 1)
					{
						out.print(", ");
						compile(call.getOperands().get(1));
					}
					out.print(")");
				}
				else
				{
					out.print("(Function) ");
					compile(call.getFunction());
					out.print(").call(");
					boolean first = true;
					for (Expression operand : call.getOperands())
					{
						if (!first)
							out.print(", ");
						first = false;
						compile(operand);
					}
				}
				out.print(")");
				break;
			case "ExpressionOperator":
				out.print(expr.toString());
				break;
			case "ExpressionIncrement":
				throw new UnsupportedOperationException();
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				out.print("(((BooleanValue) ");
				compile(ternary.getCond());
				out.print(").value ? ");
				compile(ternary.getTrueCase());
				out.print(" : ");
				compile(ternary.getFalseCase());
				out.print(")");
				break;
			
		}
	}
	
	private static void printIndentation(int indentation)
	{
		for (int i = 0; i < indentation; i++)
			out.print("\t");
	}
	
	private static void assignHelper(Expression target, Expression value)
	{
		if (target instanceof ExpressionIdentifier)
		{
			if (((ExpressionIdentifier) target).getParent() != null)
				throw new UnsupportedOperationException();
			out.print("map.put(\"" + ((ExpressionIdentifier) target).getReference() + "\", ");
			compile(value);
			out.print(");\n");
		}
		else if (target instanceof ExpressionReference)
		{
			out.print("((AmbroscumList) ");
			compile(((ExpressionReference) target).getPrimary());
			out.print(").set((IntValue) ");
			compile(((ExpressionReference) target).getSecondary());
			out.print(", ");
			compile(value);
			out.print(");\n");
		}
	}
	
	private static void printHelper(Expression target, String value)
	{
		if (target instanceof ExpressionIdentifier)
		{
			if (((ExpressionIdentifier) target).getParent() != null)
				throw new UnsupportedOperationException();
			out.print("map.put(\"" + ((ExpressionIdentifier) target).getReference() + "\", ");
			out.print(value);
			out.print(");\n");
		}
		if (target instanceof ExpressionReference)
		{
			ExpressionReference ref = (ExpressionReference) target;
			if (ref.getSecondaryRight() == null)
			{
				out.print("((AmbroscumList) ");
				compile(((ExpressionReference) target).getPrimary());
				out.print(").set((IntValue) ");
				compile(((ExpressionReference) target).getSecondary());
				out.print(", ");
				out.print(value);
				out.print(");\n");
			}
			else
			{
				// need to delete old elements first
				out.print("((AmbroscumList) ");
				compile(((ExpressionReference) target).getPrimary());
				out.print(").addAll((IntValue) ");
				compile(((ExpressionReference) target).getSecondary());
				out.print(", (AmbroscumList) ");
				out.print(value);
				out.print(");\n");
				throw new UnsupportedOperationException();
			}
		}
	}
}