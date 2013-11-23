// represents a specific block of code that might be run multiple times and (more importantly) that has its own scope
// could represent stuff ranging from a function to a for loop

/**
 * Represents a block of code with its own scope.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import ambroscum.lines.Line;

public class Scope
{
	private IdentifierMap idmap;
	private Line[] code;
}
