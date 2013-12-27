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
	public static void interpret(File file) throws FileNotFoundException
	{
		TokenStream stream = TokenStream.readFile(file);

		System.out.println("import java.util.*;");
		System.out.println();
		System.out.println("public class Main {");
		System.out.println("\tpublic static void main(String[] args) {");
		while (stream.hasNext())
			compile(Line.interpret(null, stream, 0), 2);
		System.out.println("\t}");
		System.out.println("}");
	}
	
	private static void compile(Line line, int indentation)
	{
		for (int i = 0; i < indentation; i++)
			System.out.print("\t");
		switch (line.getClass().getSimpleName())
		{
			case "PrintLine":
				PrintLine print = (PrintLine) line;
				System.out.print("System.out.print");
				if (print.isPrintNewline())
					System.out.print("ln");
				System.out.print("(");
				boolean first = true;
				for (Expression expr : ((PrintLine) line).getPrintExpressions())
				{
					if (!first)
						System.out.print(" + ");
					first = false;
					compile(expr);
				}
				System.out.println(")");
				break;
		}
	}
	
	private static void compile(Expression expr)
	{
		switch (line.getClass().getSimpleName())
		{
			case "ExpressionLiteral":
				Value v = ((ExpressionLiteral) expr).getValue();
				switch (v.getClass().getSimpleName())
				{
					case "NullValue":
						System.out.print(null);
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
			case "ExpressionReference":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionList":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionDict":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionCall":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionOperator":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionIncrement":
				throw new UnsupportedOperationException();
				break;
			case "ExpressionTernary":
				throw new UnsupportedOperationException();
				break;
			
		}
	}
}
