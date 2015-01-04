package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class MIPSCompiler
{
	public static void compile(File input, PrintWriter out) throws IOException
	{
		ControlFlowGraph graph = ILCompiler.compile(input);
		
		out.println(".data");
		out.println("  testString: .asciiz \"Hello World\\n\"");
		out.println();
		out.println(".text");
		out.println("  la $a0 testString");
		out.println("  li $v0 4");
		out.println("  syscall");
		out.println("  li $v0 10");
		out.println("  syscall");
		
		out.flush();
		out.close();
	}
}