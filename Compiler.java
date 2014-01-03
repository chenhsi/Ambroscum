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
		out.println("public class Main {");
		out.println("\tpublic static void main(String[] args) {");
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
	
	private static void process(Line line, int indentation)
	{
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
				{
					process(expr, indentation);
					if (expr instanceof ExpressionIdentifier && ((ExpressionIdentifier) expr).getParent() == null && !declare(((ExpressionIdentifier) expr).getReference()))
					{
						printIndentation(indentation);
						out.print("Object " + ((ExpressionIdentifier) expr).getReference() + ";\n");
					}
				}
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
		}
	}
	
	private static void compile(Line line, int indentation)
	{
		switch (line.getClass().getSimpleName())
		{
			case "Block":
				for (Line subLine : ((Block) line).getLines())
					compile(subLine, indentation);
				break;
			case "AssertLine":
				AssertLine asAssert = (AssertLine) line;
				printIndentation(indentation);
				out.print("assert ");
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
					out.print("Object _" + assign.getAssignTargets().get(i).getID() + " = ");
					compile(assign.getAssignValues().get(i));
					out.print(";\n");
				}
				printIndentation(indentation);
				compile(assign.getAssignTargets().get(lastIndex));
				out.print(" = ");
				compile(assign.getAssignValues().get(lastIndex));
				out.print(";\n");
				for (int i = 0; i < lastIndex; i++)
				{
					printIndentation(indentation);
					compile(assign.getAssignTargets().get(i));
					out.print(" = _" + assign.getAssignTargets().get(i).getID());
					out.print(";\n");
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
					out.print("System.out.println();\n");
				}
				break;
			case "IfLine":
				List<Expression> conditions = ((IfLine) line).getConditions();
				List<Block> clauses = ((IfLine) line).getClauses();
				
				printIndentation(indentation);
				out.print("if (");
				compile(conditions.get(0));
				out.print(") {\n");
				compile(clauses.get(0), indentation + 1);
				printIndentation(indentation);
				out.print("}\n");
				
				for (int i = 1; i < conditions.size(); i++)
				{
					printIndentation(indentation);
					out.print("else if (");
					compile(conditions.get(i));
					out.print(") {\n");
					compile(clauses.get(i), indentation + 1);
					printIndentation(indentation);
					out.print("}\n");
				}
				if (clauses.size() > conditions.size())
				{
					printIndentation(indentation);
					out.print("else {");
					compile(clauses.get(clauses.size() - 1), indentation + 1);
					printIndentation(indentation);
					out.print("}\n");
				}
				break;
			case "WhileLine":
				Expression condition = ((WhileLine) line).getCondition();
				Block block = ((WhileLine) line).getBlock();
				Block thenBlock = ((WhileLine) line).getThenBlock();
				
				if (thenBlock == null)
				{
					printIndentation(indentation);
					out.print("while (");
					compile(condition);
					out.print(") {\n");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.print("}\n");
				}
				else
				{
					printIndentation(indentation);
					out.print("boolean _" + condition.getID() + ";\n");
					printIndentation(indentation);
					out.print("while (true) {\n");
					printIndentation(indentation);
					out.print("\t_" + condition.getID() + " = true;\n");
					printIndentation(indentation);
					out.print("\tif (");
					compile(condition);
					out.print(") break;");
					printIndentation(indentation);
					out.print("\t_" + condition.getID() + " = false;\n");
					compile(block, indentation + 1);
					printIndentation(indentation);
					out.print("}\n");
					printIndentation(indentation);
					out.print("if (_" + condition.getID() + ") {\n");
					compile(thenBlock, indentation + 1);
					printIndentation(indentation);
					out.print("}\n");
				}
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
				out.print("List<Object> _" + expr.getID() + " = new ArrayList<Object> ();\n");
				for (Expression subexpr : ((ExpressionList) expr).getExpressions())
				{
					printIndentation(indentation);
					out.print("_" + expr.getID() + ".add(");
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
				if (possParent != null)
				{
					compile(possParent);
					out.print(".");
				}
				out.print(((ExpressionIdentifier) expr).getReference());
				break;
			case "ExpressionReference":
				ExpressionReference cast = (ExpressionReference) expr;
				compile(cast.getPrimary());
				out.print(".get(");
				compile(cast.getSecondary());
				out.print(")");
				break;
			case "ExpressionList":
				out.print("_" + expr.getID());
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				if (call.getFunction() instanceof ExpressionOperator)
				{
					out.print("(");
					if (call.getOperands().size() == 1)
					{
						compile(call.getFunction());
						out.print(" ");
						compile(call.getOperands().get(0));
					}
					else
					{
						compile(call.getOperands().get(0));
						out.print(" ");
						compile(call.getFunction());
						out.print(" ");
						compile(call.getOperands().get(1));
					}
					out.print(")");
				}
				else
				{
					compile(call.getFunction());
					out.print("(");
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
				compile(ternary.getCond());
				out.print(" ? ");
				compile(ternary.getTrueCase());
				out.print(" : ");
				compile(ternary.getFalseCase());
				break;
			
		}
	}
	
	private static void printIndentation(int indentation)
	{
		for (int i = 0; i < indentation; i++)
			out.print("\t");
	}
	
	private static final Set<String> declared = new HashSet<String> (); // only works without scopes
	private static boolean declare(String str)
	{
		if (declared.contains(str))
			return true;
		declared.add(str);
		return false;
	}
}
