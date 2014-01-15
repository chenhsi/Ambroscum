package ambroscum.compiler;

import java.util.*;

public class Instruction
{
	BasicBlock block;
	String line;
	List<String> variablesUsed;
	InstructionType type;
	
	Map<String, Instruction> preDeclarations = new HashMap<> ();
	Map<String, Instruction> postDeclarations = new HashMap<> ();
	Set<String> preLiveVariables = new HashSet<> ();
	Set<String> postLiveVariables = new HashSet<> ();
	
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
		else if (line.startsWith("jump"))
			type = InstructionType.JUMP;
		else if (line.startsWith("jumpunless"))
		{
			variablesUsed.add(line.substring(11, line.lastIndexOf(" ")));
			type = InstructionType.JUMP;
		}
		else if (line.startsWith("param"))
		{
			variablesUsed.add(line.substring(6));
			type = InstructionType.FUNCTIONPARAM;
		}
		else if (line.startsWith("call"))
		{
			String substr = line.substring(5, line.lastIndexOf(" "));
			variablesUsed.add(substr);
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
	
	void print()
	{
//			System.out.println("\t\tPre-Declarations: " + preDeclarations);
			System.out.println("\t\tPre-Live Variables: " + preLiveVariables);
//			System.out.println("\t\tReferenced Variables: " + variablesUsed);
			System.out.println("\t" + line);
//			System.out.println("\t\tPost-Declarations: " + postDeclarations);
	}
	
	void optimize()
	{
		boolean optimized = false;
		if (type == InstructionType.ASSIGNMENT || type == InstructionType.CALCULATION || type == InstructionType.FUNCTIONRETURN)
			for (String str : variablesUsed)
			{
				Instruction decl = preDeclarations.get(str);
				if (decl == null || decl.block != this.block || decl.type != InstructionType.ASSIGNMENT)
					continue;
				String rest = decl.line.substring(decl.line.indexOf(" = ") + 3);
				line = line.replaceAll(str, rest);
				variablesUsed.remove(str);
				if (identifier(rest))
					variablesUsed.add(rest);
				optimized = true;
				break;
			}
// this "optimization" reverses other optimizations done; I don't remember why I thought it would be useful
/*			if (!optimized && line.indexOf(" = ") != -1 && variablesUsed.size() == 1 && line.endsWith(" = " + variablesUsed.get(0)))
		{
			Instruction decl = preDeclarations.get(variablesUsed.get(0));
			if (decl != null && decl.block == this.block && decl.line.split(" ").length == 5)
			{
				line = line.substring(0, line.indexOf(" = ")) + decl.line.substring(decl.line.indexOf(" = "));
				variablesUsed.remove(0);
				optimized = true;
			}
		}*/
		if (optimized)
			optimize();
	}
	
	public String toString()
	{
		return "inst:\"" + line + "\"";
	}
	
	private static boolean identifier(String str)
	{
		char c = str.charAt(0);
		if (c == '_' || c == '*')
			return true;
		if (!Character.isLetter(c))
			return false;
		if (str.equals("true") || str.equals("false") || str.equals("paramvalue") || str.equals("returnvalue") || str.equals("print"))
			return false;
		return true;
	}
}

enum InstructionType
{
	CALCULATION(true), ASSIGNMENT(true), FUNCTIONCALL(false), FUNCTIONPARAM(false), FUNCTIONRETURN(false), SPECIALASSIGNMENT(true), JUMP(false);
	
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