import java.util.*;

class _l2 extends Function {
	public _l2(VariableMap parentMap) {
		super(parentMap, "array");
	}
	protected Object call(VariableMap map) {
		map.put("i", 0);
		while ((boolean) (((int) map.get("i")) < ((int) 9))) {
			map.put("s", map.get("i"));
			map.put("j", (((int) map.get("i")) + ((int) 1)));
			while ((boolean) (((int) map.get("j")) < ((int) 9))) {
				if ((boolean) (((int) ((List) map.get("array")).get((int) map.get("j"))) < ((int) ((List) map.get("array")).get((int) map.get("s"))))) {
					map.put("s", map.get("j"));
				}
				else {
				}
				map.put("j", (((int) map.get("j")) + ((int) 1)));
			}
			Object _e35 = ((List) map.get("array")).get((int) map.get("i"));
			((List) map.get("array")).set((int) map.get("i"), ((List) map.get("array")).get((int) map.get("s")));
			((List) map.get("array")).set((int) map.get("s"), _e35);
			map.put("i", (((int) map.get("i")) + ((int) 1)));
		}
	return null;
	}
}

public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		map.put("sort", new _l2(map));
		List _e51 = new ArrayList ();
		_e51.add(1);
		_e51.add(6);
		_e51.add(3);
		_e51.add(5);
		_e51.add(9);
		_e51.add(7);
		_e51.add(2);
		_e51.add(8);
		_e51.add(4);
		map.put("i", _e51);
		((Function) map.get("sort")).call(map.get("i"));
		System.out.print(map.get("i"));
	}
}
