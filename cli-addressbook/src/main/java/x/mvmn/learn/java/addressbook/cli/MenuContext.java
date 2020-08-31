package x.mvmn.learn.java.addressbook.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MenuContext {

	private final Scanner input;
	private final Map<String, Object> attributes = new HashMap<>();

	public MenuContext(Scanner input) {
		this.input = input;
	}

	public Scanner getInput() {
		return input;
	}

	public MenuContext setAttribute(String key, Object value) {
		attributes.put(key, value);
		return this;
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}
}
