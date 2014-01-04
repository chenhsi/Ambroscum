import java.util.*;


public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		while ((boolean) true) {
			System.out.print("1");
			System.out.println();
			break;
		}
		map.put("i", 3);
		while ((boolean) (((int) map.get("i")) < ((int) 7))) {
			map.put("i", (((int) map.get("i")) + ((int) 1)));
			System.out.print(map.get("i"));
			System.out.println();
		}
		boolean _l17;
		while (true) {
			_l17 = true;
			if (!((boolean) (((int) map.get("i")) < ((int) 15)))) break;
			_l17 = false;
			map.put("i", (((int) map.get("i")) + ((int) 1)));
			if ((boolean) (((Object) map.get("i")) == ((Object) 11))) {
				break;
			}
			else {
			}
			System.out.print(map.get("i"));
			System.out.println();
		}
		if (_l17) {
			System.out.print("nope");
			System.out.println();
		}
		boolean _l29;
		while (true) {
			_l29 = true;
			if (!((boolean) (((int) map.get("i")) < ((int) 15)))) break;
			_l29 = false;
			System.out.print(map.get("i"));
			System.out.println();
			if ((boolean) (((Object) map.get("i")) == ((Object) 16))) {
				break;
			}
			else {
			}
			map.put("i", (((int) map.get("i")) + ((int) 1)));
		}
		if (_l29) {
			System.out.print("here");
			System.out.println();
		}
		List _e48 = new ArrayList ();
		_e48.add(15);
		_e48.add(16);
		_e48.add(18);
		map.put("lst", _e48);
		for (Object _l42 : (List) map.get("lst")) {
			map.put("i", _l42);
			System.out.print(map.get("i"));
			System.out.println();
			((List) map.get("lst")).set((int) 2, (((int) map.get("i")) + ((int) 1)));
		}
		System.out.print(((List) map.get("lst")).get((int) 2));
		System.out.println();
		List _e69 = new ArrayList ();
		_e69.add(19);
		_e69.add(20);
		_e69.add(21);
		map.put("lst", _e69);
		boolean _la49;
		Iterator _lb49 = ((List) map.get("lst")).iterator();
		while (true) {
			_la49 = true;
			if (!_lb49.hasNext()) break;
			_la49 = false;
			map.put("i", _lb49.next());
			if ((boolean) (((int) map.get("i")) > ((int) 20))) {
				break;
			}
			else {
			}
			System.out.print(map.get("i"));
			System.out.println();
		}
		if (_la49) {
			System.out.print("nope");
			System.out.println();
		}
	}
}
