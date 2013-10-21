// represents a specific block of code that might be run multiple times and (more importantly) that has its own scope
// could represent stuff ranging from a function to a for loop

package ambroscum;

public class Scope
{
	private IdentifierMap idmap;
	private Line[] code;
}