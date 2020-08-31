package x.mvmn.learn.java.addressbook.cli;

import lombok.AllArgsConstructor;
import x.mvmn.learn.java.addressbook.cli.model.MenuDefinition;
import x.mvmn.learn.java.addressbook.cli.model.MenuDefinition.MenuItem;

@AllArgsConstructor
public class MenuActionSubmenu implements MenuAction {

	private MenuDefinition subMenu;

	@Override
	public boolean perform(MenuContext context) {
		int choice = -1;
		while (choice < 1 || choice > subMenu.getMenuItems().size()) {
			System.out.println("\nPlease choose command:");
			for (int i = 1; i <= subMenu.getMenuItems().size(); i++) {
				MenuItem menuItem = subMenu.getMenuItems().get(i - 1);
				System.out.println(" (" + i + ") " + menuItem.getName());
			}
			System.out.print("\n> ");
			String input = context.getInput().nextLine();
			choice = input.trim().matches("[0-9]+") ? Integer.parseInt(input.trim()) : -1;
		}
		return subMenu.getMenuItems().get(choice - 1).getAction().perform(context);
	}
}
