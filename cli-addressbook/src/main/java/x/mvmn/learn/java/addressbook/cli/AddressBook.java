package x.mvmn.learn.java.addressbook.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import x.mvmn.learn.java.addressbook.api.model.Person;
import x.mvmn.learn.java.addressbook.cli.model.MenuDefinition;
import x.mvmn.learn.java.addressbook.cli.model.MenuDefinition.MenuItem;
import x.mvmn.learn.java.addressbook.impl.model.MutablePersonImpl;
import x.mvmn.learn.java.addressbook.service.impl.file.AddressBookFileServiceImpl;

public class AddressBook {

	public static void main(String args[]) throws IOException {
		System.out.println(IOUtils.toString(AddressBook.class.getResourceAsStream("/splashscreen.txt"), StandardCharsets.UTF_8));
		AddressBook addressBook = new AddressBook();
		try (Scanner scan = new Scanner(System.in)) {
			addressBook.run(scan);
		}
	}

	private final File dataFolder;
	private final AddressBookFileServiceImpl addressBookService;

	public AddressBook() {
		dataFolder = new File(new File(System.getProperty("user.home")), ".addrbook");
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		addressBookService = new AddressBookFileServiceImpl(new ObjectMapper(), dataFolder, "adbk");
	}

	public void run(Scanner scan) {
		List<MenuItem> menuItems = new ArrayList<>();
		menuItems.add(new MenuItem("Exit", ctx -> true));
		menuItems.add(new MenuItem("List persons", ctx -> {
			addressBookService.listAllPersons().stream().map(this::renderPerson).map(v -> " " + v).forEach(System.out::println);
			return false;
		}));
		menuItems.add(new MenuItem("Delete person", ctx -> {
			System.out.print("Enter person ID: ");

			String input = ctx.getInput().nextLine();
			if (input.trim().matches("[0-9]+")) {
				long personId = Long.parseLong(input.trim());
				if (addressBookService.deletePerson(personId)) {
					System.out.println("Person with ID " + personId + " deleted.");
				} else {
					System.out.println("Person not found by ID " + personId);
				}
			} else {
				System.out.println("Not a valid person ID");
			}

			return false;
		}));

		boolean exit = false;
		do {
			exit = new MenuActionSubmenu(new MenuDefinition(menuItems)).perform(new MenuContext(scan));
		} while (!exit);
	}

	private String renderPerson(Person person) {
		StringBuilder result = new StringBuilder();
		result.append("[").append(person.getId()).append("] ");
		if (person.getPrefix() != null && !person.getPrefix().trim().isEmpty()) {
			result.append(person.getPrefix().trim()).append(" ");
		}
		result.append(person.getFirstName().trim()).append(" ");
		if (person.getMiddleName() != null && !person.getMiddleName().trim().isEmpty()) {
			result.append(person.getMiddleName().trim()).append(" ");
		}
		result.append(person.getLastName().trim());

		return result.toString().trim();
	}

	private boolean doCommand(int command, Scanner scanner) {
		switch (command) {
			case 1:
				addressBookService.listAllPersons().stream().map(p -> p.getId() + ": " + p.getFirstName() + " " + p.getLastName())
						.forEach(System.out::println);
			break;
			case 2:
			break;
			case 3:
				System.out.print("\nFirst name: ");
				String fn = scanner.next();
				System.out.print("\nLast name: ");
				String ln = scanner.next();
				System.out.print("\nMiddle name: ");
				String mn = scanner.next();
				System.out.print("\nPrefix name: ");
				String pfx = scanner.next();
				MutablePersonImpl person = MutablePersonImpl.builder().prefix(pfx).firstName(fn).middleName(mn).lastName(ln).build();
				addressBookService.savePerson(person);
			break;
			case 4:
			break;
			case 5:
				System.out.print("\nEnter ID of person to delete: ");
				int id = scanner.nextInt();
				if (addressBookService.deletePerson(id)) {
					System.out.println("Delete successful.");
				} else {
					System.out.println("Person not found.");
				}
			break;
			case 6:
				return true;
			default:
		}
		return false;
	}
}
