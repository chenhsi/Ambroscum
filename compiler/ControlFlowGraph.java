package ambroscum.compiler;

import java.io.InputStream;
import java.util.*;

public class ControlFlowGraph
{
	public static void analyze(InputStream stream)
	{
		Scanner scanner = new Scanner(stream);
		Map<String, BasicBlock> labels = new HashMap<> ();
		BasicBlock start = new BasicBlock();
		BasicBlock curr = start;
		while (scanner.hasNext())
		{
			String str = scanner.nextLine();
			if (str.startsWith("label") || str.startsWith("jumpunless"))
			{
				BasicBlock next = new BasicBlock();
				connect(curr, next);
				curr = next;
				curr.instructions.add(str);
				if (str.startsWith("label"))
					labels.put(str.substring(6), curr);
				continue;
			}
			else if (str.startsWith("jump") || str.startsWith("return"))
			{
				curr.instructions.add(str);
				curr = new BasicBlock();
				continue;
			}
			else if (str.startsWith("call"))
			{
				throw new UnsupportedOperationException();
			}
			else
				curr.instructions.add(str);
		}
	}

	static void connect(BasicBlock parent, BasicBlock child)
	{
		parent.children.add(child);
		child.parents.add(parent);
	}
	
	static class BasicBlock
	{
		List<String> instructions = new LinkedList<String> ();
		Set<BasicBlock> parents = new HashSet<> ();
		Set<BasicBlock> children = new HashSet<> ();
	}
}