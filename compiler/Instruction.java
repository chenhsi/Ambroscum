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
			String[] substrs = line.substring(index + 3).split(" ");
			if (substrs.length > 1)
				type = InstructionType.CALCULATION;
			else if (substrs[0].equals("paramvalue") || substrs[0].equals("returnvalue"))
				type = InstructionType.SPECIALASSIGNMENT;
			else
				type = InstructionType.ASSIGNMENT;
			for (String substr : substrs)
				if (identifier(substr))
					variablesUsed.add(substr);
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
			if (identifier(line.substring(6)))
				variablesUsed.add(line.substring(6));
			type = InstructionType.FUNCTIONPARAM;
		}
		else if (line.startsWith("call"))
		{
			String func = line.substring(5, line.lastIndexOf(" "));
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
		for (String str : variablesUsed)
		{
			if (str.charAt(0) == '*')
				continue;
			Instruction decl = preDeclarations.get(str);
			if (decl == null || decl.block != this.block || decl.type != InstructionType.ASSIGNMENT)
				continue;
			String rest = decl.line.substring(decl.line.indexOf(" = ") + 3);
			if (rest.charAt(0) == '*')
				continue;
			line = line.replaceAll(str, rest);
			variablesUsed.remove(str);
			if (identifier(rest))
				variablesUsed.add(rest);
			optimized = true;
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
			try
			{
				int left = Integer.parseInt(parts[2]);
				int right = Integer.parseInt(parts[4]);
				boolean change = true;
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
						change = false;
						break;
				}
				if (change)
				{
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
			}
			catch (NumberFormatException ex)
			{
				boolean change = true;
				if (parts[2].equals("not"))
				{
					boolean unary = Boolean.parseBoolean(parts[3]);
					line = parts[0] + " = " + !unary;
				}
				else
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
							change = false;
							break;
					}
				}
				if (change)
				{
					type = InstructionType.ASSIGNMENT;
					optimized = true;
				}
			}
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
		System.out.println("\t\tPost-Declarations: " + postDeclarations);
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