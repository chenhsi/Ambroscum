import java.util.*;

class _l35 extends Function {
	public _l35(VariableMap parentMap) {
		super(parentMap, "x");
	}
	protected Object call(VariableMap map) {
		if ((boolean) (((int) map.get("x")) < ((int) 0))) {
			return -1;
		}
		else if ((boolean) (((Object) map.get("x")) == ((Object) 0))) {
			return 0;
		}
		else {
			return 1;
		}
	return null;
	}
}

public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		System.out.print(1);
		System.out.println();
		System.out.print(1);
		System.out.println();
		Object _e9 = 1;
		map.put("y", 2);
		map.put("x", _e9);
		map.put("x", 5);
		System.out.print(5);
		System.out.print(" " + 2);
		System.out.println();
		System.out.print(1000);
		System.out.println();
		map.put("test", new _l35(map));
		System.out.print(((Function) map.get("test")).call(-4));
		System.out.println();
		System.out.print(((Function) map.get("test")).call(5));
		System.out.println();
		System.out.print(((Function) map.get("test")).call(0));
		System.out.println();
		System.out.print((((int) ((Function) map.get("test")).call(((Function) map.get("test")).call(1))) + ((int) ((Function) map.get("test")).call(((Function) map.get("test")).call(-1)))));
		System.out.println();
		System.out.print(5);
		System.out.println();
		System.out.print(2);
		System.out.println();
		System.out.print(true);
		System.out.println();
	}
}
