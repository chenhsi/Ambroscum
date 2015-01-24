package ambroscum.compiler;

import java.util.*;

public class Instruction
{
	BasicBlock block;
	String line;
	List<String> variablesUsed;
	InstructionType type;
	
	Map<String, Instruction> preDeclarations;
	Map<String, Instruction> postDeclarations;
	Set<String> preLiveVariables;
	Set<String> postLiveVariables;
	
	public Instruction(String str, BasicBlock block)
	{
		this.block = block;
		line = str;
		variablesUsed = new LinkedList<String> ();
		int index = line.indexOf(" = ");
		if (index != -1)
		{
			String[] substrs;
//			if (line.contains("\""))
//			{
//				List<String> parts = new ArrayList<> ();
//				int curr = index + 3;
//				throw new UnsupportedOperationException();
//			}
//			else
				substrs = line.substring(index + 3).split(" ");
			if (substrs[0].equals("paramvalue") || substrs[0].equals("returnvalue"))
				type = InstructionType.SPECIALASSIGNMENT;
			else if (substrs.length > 1)
				type = InstructionType.CALCULATION;
			else if (substrs[0].startsWith("*_func"))
				type = InstructionType.ASSIGNMENT;
			else
			{
				type = InstructionType.ASSIGNMENT;
				if (line.charAt(0) == '*')
					variablesUsed.add(line.substring(1, index));
			}
			for (String substr : substrs)
				if (identifier(substr))
				{
					if (substr.charAt(0) == '*')
						variablesUsed.add(substr.substring(1));
					else
						variablesUsed.add(substr);
				}
		}
		else if (line.startsWith("jumpunless"))
		{
			variablesUsed.add(line.substring(11, line.lastIndexOf(" ")));
			type = InstructionType.JUMP;
		}
		else if (line.startsWith("jump"))
			type = InstructionType.JUMP;
		else if (line.startsWith("param"))
		{
			String paramValue = line.substring(line.lastIndexOf(" ") + 1);
			if (identifier(paramValue))
				variablesUsed.add(paramValue);
			type = InstructionType.FUNCTIONPARAM;
		}
		else if (line.startsWith("call"))
		{
			String func = line.substring(5);
			variablesUsed.add(func);
			type = InstructionType.FUNCTIONCALL;
		}
		else if (line.startsWith("return"))
		{
			String returnValue = line.substring(7);
			if (!returnValue.equals("null"))
				variablesUsed.add(returnValue);
			type = InstructionType.FUNCTIONRETURN;
		}
		else
			throw new UnsupportedOperationException(line + " not recognized");
	}
	
	void optimize()
	{
//		System.out.println("Optimizing self: " + this);
		boolean optimized = false;
		
		while (type == InstructionType.ASSIGNMENT)
		{
			String[] parts = line.split(" = ");
			// Right now, don't know how to optimize memory accesses
			// (since the memory could have been changed by something in between)
			if (parts[1].charAt(0) == '*')
				break;
			parts[0] += " = ";
			String rest = null;
			Instruction decl = preDeclarations.get(parts[1]);
			
			// Found the exact value
			if (decl != null && decl.block == this.block && decl.type == InstructionType.ASSIGNMENT)
				rest = decl.line.substring(decl.line.indexOf(" = ") + 3);
			else
				break;
			
			// Memory accesses must be done at proper time, unless I can
			// somehow guarantee that the memory doesn't get changed in-between
			if (rest.charAt(0) == '*')
				break;
			
			line = parts[0] + rest;
			variablesUsed.remove(parts[1]);
			if (rest.charAt(0) == '*')
				variablesUsed.add(rest.substring(1));
			else if (identifier(rest))
				variablesUsed.add(rest);
			optimized = true;
			break;
		}
		
		while (type != InstructionType.ASSIGNMENT)
		{
			for (String str : variablesUsed)
			{
				Instruction decl = preDeclarations.get(str);
				if (decl == null || decl.block != this.block || decl.type != InstructionType.ASSIGNMENT)
					continue;
				String rest = decl.line.substring(decl.line.indexOf(" = ") + 3);
				if (rest.charAt(0) == '*' && !rest.startsWith("*_func"))
					continue;
				line = line.replaceAll(str, rest);
				variablesUsed.remove(str);
				if (identifier(rest))
					variablesUsed.add(rest);
				optimized = true;
				break;
			}
			break;
		}

		if (line.startsWith("jumpunless"))
		{
			// Optimizations on the jump condition
			String jumpCond = line.substring(11, line.lastIndexOf(" "));
			if (!identifier(jumpCond))
			{
				if (!jumpCond.equals("true") && !jumpCond.equals("false"))
					throw new AssertionError(); // Could also be a syntax error, but assertions for now
				optimized = true;
				if (jumpCond.equals("true")) // Never jump
				{
					type = InstructionType.NOP;
					line = "nop";
					block.jumpBlock.parents.remove(block);
					block.jumpBlock = null;
				}
				else // jumpCond is false, always jump
				{
					line = "jump" + line.substring(line.lastIndexOf(" "));
					block.nextBlock.parents.remove(block);
					block.nextBlock = null;
				}
			}
		}

		if (!optimized && type == InstructionType.CALCULATION && variablesUsed.size() == 0)
		{
			String[] parts = line.split(" ");
			if (parts.length == 4)
			{
				if (parts[2].equals("-"))
				{
					if (!rawType(parts[3]).equals("int"))
						throw new AssertionError();
					line = parts[0] + " = " + -Integer.parseInt(parts[3]);
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
				else if (parts[2].equals("not"))
				{
					if (!rawType(parts[3]).equals("boolean"))
						throw new AssertionError();
					line = parts[0] + " = " + !Boolean.parseBoolean(parts[3]);
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
				else
					throw new UnsupportedOperationException();
			}
			else if (parts.length == 5)
			{
				String leftType = rawType(parts[2]);
				String rightType = rawType(parts[4]);
				if (leftType.equals("int") && rightType.equals("int"))
				{
					int left = Integer.parseInt(parts[2]);
					int right = Integer.parseInt(parts[4]);
					switch (parts[3])
					{
						case "+":
							line = parts[0] + " = " + (left + right);
							break;
						case "-":
							line = parts[0] + " = " + (left - right);
							break;
						case "*":
							line = parts[0] + " = " + (left * right);
							break;
						case "/":
							line = parts[0] + " = " + (left / right);
							break;
						case "%":
							line = parts[0] + " = " + (left % right);
							break;
						case ">":
							line = parts[0] + " = " + (left > right);
							break;
						case "<":
							line = parts[0] + " = " + (left < right);
							break;
						case ">=":
							line = parts[0] + " = " + (left >= right);
							break;
						case "<=":
							line = parts[0] + " = " + (left <= right);
							break;
						case "=":
							line = parts[0] + " = " + (left == right);
							break;
						case "!=":
							line = parts[0] + " = " + (left != right);
							break;
						default:
							throw new UnsupportedOperationException(line);
					}
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
				else if (leftType.equals("boolean") && rightType.equals("boolean"))
				{
					boolean left = Boolean.parseBoolean(parts[2]);
					boolean right = Boolean.parseBoolean(parts[4]);
					switch (parts[3])
					{
						case "and":
							line = parts[0] + " = " + (left && right);
							break;
						case "or":
							line = parts[0] + " = " + (left || right);
							break;
						default:
							throw new UnsupportedOperationException(line);
					}
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
				else if (leftType.equals("string") || rightType.equals("string"))
				{
					switch (parts[3])
					{
						case "+":
							line = parts[0] + " = " + (parts[2] + parts[4]);
							break;
						default:
							throw new UnsupportedOperationException(line);
					}
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
				else
					throw new UnsupportedOperationException(line);
			}
			else
				throw new AssertionError("Operators must be either unary or binary: " + line);
		}
//		System.out.println("After self-optimizing: " + this);
		if (optimized)
			optimize();
	}
	
	void print()
	{
//		System.out.println("\t\tPre-Declarations: " + preDeclarations);
//		System.out.println("\t\tPre-Live Variables: " + preLiveVariables);
//		System.out.println("\t\tReferenced Variables: " + variablesUsed);
		System.out.println("\t" + line);
//		System.out.println("\t\tPost-Live Variables: " + postLiveVariables);
//		System.out.println("\t\tPost-Declarations: " + postDeclarations);
	}
	
	public String toString()
	{
		return "inst:\"" + line + "\"";
	}
	
	private static final Set<String> identifiers = new HashSet<String> ();
	static
	{
		identifiers.add("true"); identifiers.add("false");
		identifiers.add("paramvalue"); identifiers.add("returnvalue");
		identifiers.add("and"); identifiers.add("or"); identifiers.add("not");
		identifiers.add("print"); identifiers.add("malloc");
	}
	
	public static boolean identifier(String str)
	{
		char c = str.charAt(0);
		if (c == '_' || c == '*' && str.length() > 1)
			return true;
		if (!Character.isLetter(c))
			return false;
		if (identifiers.contains(str))
			return false;
		return true;
	}
	
	private static String rawType(String value)
	{
		if (value.equals("true") || value.equals("false"))
			return "boolean";
		try
		{
			Integer.parseInt(value);
			return "int";
		}
		catch (NumberFormatException ex)
		{
			return "string";
		}
	}
}

enum InstructionType
{
	CALCULATION(true), ASSIGNMENT(true), FUNCTIONCALL(false), FUNCTIONPARAM(false), FUNCTIONRETURN(false), SPECIALASSIGNMENT(true), JUMP(false), NOP(false);
	
	private boolean assignmentType;
	
	private InstructionType(boolean assignmentType)
	{
		this.assignmentType = assignmentType;
	}
	
	public boolean isAssignment()
	{
		return assignmentType;
	}
}