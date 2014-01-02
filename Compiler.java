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
	/**
	 * Reads data from an Ambroscum file, and compiles it into a Java file.
	 *
	 * @param	file			the Ambroscum file to read from and compile
	 */
	public static void compile(File file) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(file);

		System.out.println("import java.util.*;");
		System.out.println();
		System.out.println("public class Main {");
		System.out.println("\tpublic static void main(String[] args) {");
		while (stream.hasNext())
		{
			Line line = Line.interpret(null, stream, 0);
			line.localOptimize();
			process(line, 2);
			compile(line, 2);
		}
		System.out.println("\t}");
		System.out.println("}");
	}
	
	private static void process(Line line, int indentation)
	{
		switch (line.getClass().getSimpleName())
		{
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
		}
	}
	
	private static void compile(Line line, int indentation)
	{
		switch (line.getClass().getSimpleName())
		{
			case "AssertLine":
				AssertLine asAssert = (AssertLine) line;
				printIndentation(indentation);
				System.out.print("assert ");
				compile(asAssert.getTest());
				if (asAssert.getErrorMessage() != null)
				{
					System.out.print(" : ");
					compile(asAssert.getErrorMessage());
				}
				System.out.print(";\n");
				break;
			case "AssignmentLine":
				AssignmentLine assign = (AssignmentLine) line;
				for (int i = 0; i < assign.getAssignValues().size(); i++)
				{
					printIndentation(indentation);
					System.out.print("Object _" + assign.getAssignTargets().get(i).getID() + " = ");
					compile(assign.getAssignValues().get(i));
					System.out.print(";\n");
				}
				for (int i = 0; i < assign.getAssignTargets().size(); i++)
				{
					printIndentation(indentation);
					compile(assign.getAssignTargets().get(i));
					System.out.print(" = " + assign.getAssignTargets().get(i).getID());
					System.out.print(";\n");
				}
				
					
				// not currently dealing with assignments with operators
				break;
			case "PrintLine":
				PrintLine print = (PrintLine) line;
				printIndentation(indentation);
				System.out.print("System.out.print");
				if (print.isPrintNewline())
					System.out.print("ln");
				System.out.print("(");
				boolean first = true;
				for (Expression expr : print.getPrintExpressions())
				{
					if (!first)
						System.out.print(" + ");
					first = false;
					compile(expr);
				}
				System.out.print(");\n");
				break;
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
				System.out.print("List<Object> _" + expr.getID() + " = new ArrayList<Object> ());\n");
				for (Expression subexpr : ((ExpressionList) expr).getExpressions())
				{
					printIndentation(indentation);
					System.out.print("_" + expr.getID() + ".add(");
					compile(subexpr);
					System.out.print(");\n");
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
						System.out.print("null");
						break;
					case "BooleanValue":
						System.out.print(((BooleanValue) v).getValue());
						break;
					case "IntValue":
						System.out.print(((IntValue) v).getValue());
						break;
					case "StringValue":
						System.out.print(v.repr());
						break;
				}
				break;
			case "ExpressionIdentifier":
				Expression possParent = ((ExpressionIdentifier) expr).getParent();
				if (possParent != null)
				{
					compile(possParent);
					System.out.print(".");
				}
				System.out.print(((ExpressionIdentifier) expr).getReference());
				break;
			case "ExpressionReference":
				ExpressionReference cast = (ExpressionReference) expr;
				compile(cast.getPrimary());
				System.out.print(".get(");
				compile(cast.getSecondary());
				System.out.print(")");
				break;
			case "ExpressionList":
				System.out.print("_" + expr.getID());
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
			case "ExpressionCall":
				ExpressionCall call = (ExpressionCall) expr;
				if (call.getFunction() instanceof ExpressionOperator)
				{
					System.out.print("(");
					if (call.getOperands().size() == 1)
					{
						compile(call.getFunction());
						System.out.print(" ");
						compile(call.getOperands().get(0));
					}
					else
					{
						compile(call.getOperands().get(0));
						System.out.print(" ");
						compile(call.getFunction());
						System.out.print(" ");
						compile(call.getOperands().get(1));
					}
					System.out.print(")");
				}
				else
				{
					compile(call.getFunction());
					System.out.print("(");
					boolean first = true;
					for (Expression operand : call.getOperands())
					{
						if (!first)
							System.out.print(", ");
						first = false;
						compile(operand);
					}
					System.out.print(")");
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
				System.out.print(asStr);
				break;
			case "ExpressionIncrement":
				throw new UnsupportedOperationException();
			case "ExpressionTernary":
				ExpressionTernary ternary = (ExpressionTernary) expr;
				compile(ternary.getCond());
				System.out.print(" ? ");
				compile(ternary.getTrueCase());
				System.out.print(" : ");
				compile(ternary.getFalseCase());
				break;
			
		}
	}
	
	private static void printIndentation(int indentation)
	{
		for (int i = 0; i < indentation; i++)
			System.out.print("\t");
	}
}
