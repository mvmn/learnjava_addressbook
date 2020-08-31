package x.mvmn.learn.java.addressbook.cli.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import x.mvmn.learn.java.addressbook.cli.MenuAction;

@Data
@AllArgsConstructor
public class MenuDefinition {

	@Data
	@AllArgsConstructor
	public static class MenuItem {
		private String name;
		private MenuAction action;

	}

	private List<MenuItem> menuItems;
}
