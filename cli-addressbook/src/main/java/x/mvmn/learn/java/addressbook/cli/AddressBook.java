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
import x.mvmn.learn.java.addressbook.impl.model.MutablePhoneNumberImpl;
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
		menuItems.add(new MenuItem("Add person", ctx -> {
			System.out.print("Enter person name: ");
			String input = ctx.getInput().nextLine().trim();
			if (!input.isEmpty()) {
				String prefix = null;
				if (input.matches("[a-zA-Z]+\\.")) {
					int idxOfDot = input.indexOf(".");
					prefix = input.substring(0, idxOfDot + 1);
					input = input.substring(idxOfDot + 1).trim();
				}
				String firstName = null;
				String middleName = null;
				String lastName = input;
				if (input.contains(" ")) {
					int lastIdxOfSpace = input.lastIndexOf(" ");
					lastName = input.substring(lastIdxOfSpace);
					input = input.substring(0, lastIdxOfSpace).trim();
				}
				if (input.contains(" ")) {
					int idxOfSpace = input.indexOf(" ");
					middleName = input.substring(idxOfSpace);
					firstName = input.substring(0, idxOfSpace);
				} else {
					firstName = input;
				}
				MutablePersonImpl person = MutablePersonImpl.builder().prefix(prefix).firstName(firstName).middleName(middleName)
						.lastName(lastName).build();
				long id = addressBookService.savePerson(person);
				System.out.println("Person saved with ID " + id + ":\n " + renderPerson(person));
			}
			return false;
		}));

		List<MenuItem> editPersonMenuItems = new ArrayList<>();
		editPersonMenuItems.add(new MenuItem("Done editing", ctx -> true));
		editPersonMenuItems.add(new MenuItem("Change first name", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			System.out.print("Enter value: ");
			String input = ctx.getInput().nextLine();
			if (!input.trim().isEmpty()) {
				Person person = addressBookService.getPerson(personId).setFirstName(input.trim());
				addressBookService.savePerson(person);
				System.out.println(renderPerson(person));
			}

			return false;
		}));
		editPersonMenuItems.add(new MenuItem("Change last name", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			System.out.print("Enter value: ");
			String input = ctx.getInput().nextLine();
			if (!input.trim().isEmpty()) {
				Person person = addressBookService.getPerson(personId).setLastName(input.trim());
				addressBookService.savePerson(person);
				System.out.println(renderPerson(person));
			}
			return false;
		}));
		editPersonMenuItems.add(new MenuItem("Change middle name", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			System.out.print("Enter value: ");
			String input = ctx.getInput().nextLine();
			if (input.trim().isEmpty()) {
				input = null;
			} else {
				input = input.trim();
			}
			Person person = addressBookService.getPerson(personId).setMiddleName(input);
			addressBookService.savePerson(person);
			System.out.println(renderPerson(person));
			return false;
		}));
		editPersonMenuItems.add(new MenuItem("Change prefix", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			System.out.print("Enter value: ");
			String input = ctx.getInput().nextLine();
			if (input.trim().isEmpty()) {
				input = null;
			} else {
				input = input.trim();
			}
			Person person = addressBookService.getPerson(personId).setPrefix(input);
			addressBookService.savePerson(person);
			System.out.println(renderPerson(person));
			return false;
		}));

		List<MenuItem> phoneNumbersMenuItems = new ArrayList<>();
		phoneNumbersMenuItems.add(new MenuItem("Done", ctx -> true));
		phoneNumbersMenuItems.add(new MenuItem("List phone numbers", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			addressBookService.getPhoneNumbers(personId).stream()
					.map(pn -> " [" + pn.getId() + "] " + pn.getCountryCode() + "" + pn.getNumber()).forEach(System.out::println);
			return false;
		}));
		phoneNumbersMenuItems.add(new MenuItem("Add phone number", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			System.out.print("Enter phone number: ");
			String input = ctx.getInput().nextLine();
			if (!input.trim().isEmpty() && input.trim().matches("[0-9]+")) {
				addressBookService.addPhoneNumber(personId, new MutablePhoneNumberImpl().setNumber(Long.parseLong(input.trim())));
			} else {
				System.out.println("Invalid phone number");
			}

			return false;
		}));
		phoneNumbersMenuItems.add(new MenuItem("Delete phone number", ctx -> {
			long personId = (Long) ctx.getAttribute("personId");
			addressBookService.getPhoneNumbers(personId).stream()
					.map(pn -> " [" + pn.getId() + "] " + pn.getCountryCode() + "" + pn.getNumber()).forEach(System.out::println);
			System.out.print("Enter phone number ID: ");
			String input = ctx.getInput().nextLine();
			if (input.trim().matches("[0-9]+")) {
				long phoneNumberId = Long.parseLong(input.trim());
				addressBookService.deletePhoneNumber(personId, phoneNumberId);
			} else {
				System.out.println("Invalid phone number ID");
			}
			return false;
		}));
		editPersonMenuItems.add(new MenuItem("Manage phone numbers", ctx -> {
			boolean done = false;
			do {
				done = new MenuActionSubmenu(new MenuDefinition(phoneNumbersMenuItems)).perform(ctx);
			} while (!done);
			return false;
		}));
		List<MenuItem> addressesMenuItems = new ArrayList<>();
		addressesMenuItems.add(new MenuItem("Done", ctx -> true));
		editPersonMenuItems.add(new MenuItem("Manage addresses", ctx -> {
			boolean done = false;
			do {
				done = new MenuActionSubmenu(new MenuDefinition(addressesMenuItems)).perform(ctx);
			} while (!done);
			return false;
		}));

		menuItems.add(new MenuItem("Edit person", ctx -> {
			addressBookService.listAllPersons().stream().map(this::renderPerson).map(v -> " " + v).forEach(System.out::println);
			System.out.print("Enter person ID to edit: ");
			String input = ctx.getInput().nextLine();
			Person person = null;
			if (input.trim().matches("[0-9]+")) {
				long personId = Long.parseLong(input.trim());
				person = addressBookService.getPerson(personId);
			}
			if (person != null) {
				System.out.println(renderPerson(person));
				ctx.setAttribute("personId", person.getId());
				boolean done = false;
				do {
					done = new MenuActionSubmenu(new MenuDefinition(editPersonMenuItems)).perform(ctx);
				} while (!done);
				ctx.removeAttribute("personId");
			} else {
				System.out.println("Person not found.");
			}

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
}
