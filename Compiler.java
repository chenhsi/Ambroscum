/**
 * The compiler. Currently compiles Ambroscum files to Java files, in a single
 * Main.java file.
 * 
 * @author Chen-Hsi Steven Bi
 * @version 1.0
 */

package ambroscum;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class Compiler
{
	private static final boolean optimizeLocally = true; // should be a safe optimization, i.e. does not introduce errors or change behavior
	private static final boolean propogateConstants = true; // causes errors when there are multiple scopes
	private static final boolean variableLiveness = false; // causes errors when there are multiple scopes
	
	private static PrintWriter out;
	
	/**
	 * Reads data from an Ambroscum file, and compiles it into a Java file.
	 *
	 * @param	file			the Ambroscum file to read from and compile
	 */
	public static void compile(File input, PrintWriter output) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(input);
		out = output;

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
		if (variableLiveness)
		{
			// probably not going to implement this until after basic blocks are created
			// doing this with SSA form seems much easier
		}

		out.println("import java.util.*;");
		out.println();
		for (Line line : block.getLines())
			functionDeclarations(line);
		out.println();
		out.println("public class Main {");
		out.println("\tpublic static void main(String[] args) {");
		out.println("\t\tVariableMap map = new VariableMap();");
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
	
	private static void functionDeclarations(Line line)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					functionDeclarations(subLine);
				break;
			case "IfLine":
				for (Block block : ((IfLine) line).getClauses())
					functionDeclarations(block);
				break;
			case "WhileLine":
				functionDeclarations(((WhileLine) line).getBlock());
				break;
			case "DefLine":
				out.println("class _l" + line.getID() + " extends Function {");
				out.println("\tpublic _l" + line.getID() + "(VariableMap parentMap) {");
				out.print("\t\tsuper(parentMap");
				for (String str : ((DefLine) line).getParams())
					out.print(", \"" + str + "\"");
				out.print(");\n");
				out.println("\t}");
				out.println("\tprotected Object call(VariableMap map) {");
				Block block = ((DefLine) line).getBlock();
				boolean lastReturn = false;
				if (block != null)
					for (Line subLine : block.getLines())
					{
						process(subLine, 2);
						compile(subLine, 2);
						lastReturn = subLine instanceof ReturnLine;
					}
				if (!lastReturn)
					out.println("\treturn null;");
				out.println("\t}");
				out.println("}");
				if (block != null)
					for (Line subLine : block.getLines())
						functionDeclarations(subLine);
				break;
		}
	}
	
	private static void process(Line line, int indentation)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					process(subLine, indentation);
				break;
			case "AssertLine":
				AssertLine asAssert = (AssertLine) line;
				process(asAssert.getTest(), indentation);
				if (asAssert.getErrorMessage() != null)
					process(asAssert.getErrorMessage(), indentation);
				break;
			case "AssignmentLine":
				AssignmentLine assign = (AssignmentLine) line;
				for (Expression expr : assign.getAssignTargets())
					process(expr, indentation);
				for (Expression expr : assign.getAssignValues())
					process(expr, indentation);
				break;
			case "PrintLine":
				for (Expression expr : ((PrintLine) line).getPrintExpressions())
					process(expr, indentation);
				break;
			case "IfLine":
				for (Expression expr : ((IfLine) line).getConditions())
					process(expr, indentation);
				for (Block block : ((IfLine) line).getClauses())
					process(block, indentation);
				break;
			case "WhileLine":
				process(((WhileLine) line).getCondition(), indentation);
				process(((WhileLine) line).getBlock(), indentation);
				process(((WhileLine) line).getThenBlock(), indentation);
				break;
			case "DefLine":
				break;
			case "BreakLine":
				break;
			case "ContinueLine":
				break;
			case "ReturnLine":
				process(((ReturnLine) line).getReturnExpr(), indentation);
				break;
			case "CallLine":
				process(((CallLine) line).getCall(), indentation);
				break;
		}
	}
	
	private static void compile(Line line, int indentation)
	{
		if (line == null)
			return;
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					compile(subLine, indentation);
				break;
			case "AssertLine":
				AssertLine asAssert = (AssertLine) line;
				printIndentation(indentation);
				out.print("assert (boolean) ");
				compile(asAssert.getTest());
				if (asAssert.getErrorMessage() != null)
				{
					out.print(" : ");
					compile(asAssert.getErrorMessage());
				}
				out.print(";\n");
				break;
			case "AssignmentLine":
				AssignmentLine assign = (AssignmentLine) line;
				int lastIndex = assign.getAssignTargets().size() - 1;
				for (int i = 0; i < lastIndex; i++)
				{
					printIndentation(indentation);
					out.print("Object _e" + assign.getAssignTargets().get(i).getID() + " = ");
					compile(assign.getAssignValues().get(i));
					out.print(";\n");
				}
				printIndentation(indentation);
				printHelper(assign.getAssignTargets().get(lastIndex), assign.getAssignValues().get(lastIndex));
				for (int i = 0; i < lastIndex; i++)
				{
					printIndentation(indentation);
					printHelper(assign.getAssignTargets().get(i), "_e" + assign.getAssignTargets().get(i).getID());
				}
				// not currently dealing with assignments with operators
				break;
			case "PrintLine":
				PrintLine print = (PrintLine) line;
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
				out.print("if ((boolean) ");
				compile(conditions.get(0));
				out.print(") {\n");
				compile(clauses.get(0), indentation + 1);
				printIndentation(indentation);
				out.print("}\n");
				
				for (int i = 1; i < conditions.size(); i++)
				{
					printIndentation(indentation);
					out.print("else if ((boolean) ");
					compile(conditions.get(i));
					out.print(") {\n");
					compile(clauses.get(i), indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				if (clauses.size() > conditions.size())
				{
					printIndentation(indentation);
					out.println("else {");
					compile(clauses.get(clauses.size() - 1), indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				break;
			case "WhileLine":
				Expression condition = ((WhileLine) line).getCondition();
				Block block = ((WhileLine) line).getBlock();
				Block thenBlock = ((WhileLine) line).getThenBlock();
				
				if (thenBlock == null)
				{
					printIndentation(indentation);
					out.print("while ((boolean) ");
					compile(condition);
					out.print(") {\n");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				else
				{
					printIndentation(indentation);
					out.println("boolean _l" + line.getID() + ";");
					printIndentation(indentation);
					out.println("while (true) {");
					printIndentation(indentation);
					out.println("\t_l" + line.getID() + " = true;");
					printIndentation(indentation);
					out.print("\tif ((boolean) ");
					compile(condition);
					out.print(") break;\n");
					printIndentation(indentation);
					out.println("\t_l" + line.getID() + " = false;");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.println("}");
					printIndentation(indentation);
					out.println("if (_l" + line.getID() + ") {");
					compile(thenBlock, indentation + 1);
					printIndentation(indentation);
					out.println("}");
				}
				break;
			case "DefLine":
				printIndentation(indentation);
				out.println("map.put(\"" + ((DefLine) line).getName() + "\", new _l" + line.getID() + "(map));");
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
				printIndentation(indentation);
				out.print("return ");
				Expression expr = ((ReturnLine) line).getReturnExpr();
				if (expr != null)
					compile(expr);
				else
					out.print("null");
				out.print(";\n");
				break;
			case "CallLine":
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
				break;
			case "ExpressionList":
				printIndentation(indentation);
				for (Expression subexpr : ((ExpressionList) expr).getExpressions())
					process(subexpr, indentation);
				out.print("List _e" + expr.getID() + " = new ArrayList ();\n");
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
				throw new UnsupportedOperationException();
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
				Value v = ((ExpressionLiteral) expr).getValue();
				switch (v.getClass().getSimpleName())
				{
					case "NullValue":
						out.print("null");
						break;
					case "BooleanValue":
						out.print(((BooleanValue) v).getValue());
						break;
					case "IntValue":
						out.print(((IntValue) v).getValue());
						break;
					case "StringValue":
						out.print(v.repr());
						break;
				}
				break;
			case "ExpressionIdentifier":
				Expression possParent = ((ExpressionIdentifier) expr).getParent();
				out.print("map.get(\"");
				if (possParent != null)
				{
					throw new UnsupportedOperationException();
//					compile(possParent);
//					out.print(".");
				}
				out.print(((ExpressionIdentifier) expr).getReference());
				out.print("\")");
				break;
			case "ExpressionReference":
				ExpressionReference cast = (ExpressionReference) expr;
				out.print("((List) ");
				compile(cast.getPrimary());
				out.print(").get((int) ");
				compile(cast.getSecondary());
				out.print(")");
				break;
			case "ExpressionList":
				out.print("_e" + expr.getID());
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				if (call.getFunction() instanceof ExpressionOperator)
				{
					String type = ((ExpressionOperator) call.getFunction()).getValue().getOperandType();
					out.print("(");
					if (call.getOperands().size() == 1)
					{
						compile(call.getFunction());
						out.print(" ((" + type + ") ");
						compile(call.getOperands().get(0));
						out.print(")");
					}
					else
					{
						out.print("((" + type + ") ");
						compile(call.getOperands().get(0));
						out.print(") ");
						compile(call.getFunction());
						out.print(" ((" + type + ") ");
						compile(call.getOperands().get(1));
						out.print(")");
					}
					out.print(")");
				}
				else
				{
					out.print("((Function) ");
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
					out.print(")");
				}
				break;
			case "ExpressionOperator":
				String asStr = expr.toString();
				if (asStr.equals("and"))
					asStr = "&&";
				else if (asStr.equals("or"))
					asStr = "||";
				else if (asStr.equals("not"))
					asStr = "!";
				else if (asStr.equals("="))
					asStr = "==";
				out.print(asStr);
				break;
			case "ExpressionIncrement":
				throw new UnsupportedOperationException();
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				out.print("((boolean) ");
				compile(ternary.getCond());
				out.print(" ? ");
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
	
	private static void printHelper(Expression target, Expression value)
	{
		if (target instanceof ExpressionIdentifier)
		{
			if (((ExpressionIdentifier) target).getParent() != null)
				throw new UnsupportedOperationException();
			out.print("map.put(\"" + ((ExpressionIdentifier) target).getReference() + "\", ");
			compile(value);
			out.print(");\n");
		}
		if (target instanceof ExpressionReference)
		{
			out.print("((List) ");
			compile(((ExpressionReference) target).getPrimary());
			out.print(").set((int) ");
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
			out.print("((List) ");
			compile(((ExpressionReference) target).getPrimary());
			out.print(").set((int) ");
			compile(((ExpressionReference) target).getSecondary());
			out.print(", ");
			out.print(value);
			out.print(");\n");
		}
	}
}