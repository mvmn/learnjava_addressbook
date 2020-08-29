package x.mvmn.learn.java.addressbook.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import x.mvmn.learn.java.addressbook.service.impl.file.AddressBookFileServiceImpl;

public class Launcher {

	private File file = new File(new File(System.getProperty("user.home")), ".addrbook");
	private AddressBookFileServiceImpl adbService = new AddressBookFileServiceImpl(new ObjectMapper(), file, null);

	public Launcher() {
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static void main(String args[]) throws IOException {
		try (Scanner scan = new Scanner(System.in)) {

			System.out.println(IOUtils.toString(Launcher.class.getResourceAsStream("/splashscreen.txt"), StandardCharsets.UTF_8));

			Launcher addressBook = new Launcher();
			while (true) {
				System.out.print("\n> ");
				try {
					int command = scan.nextInt();
					if (addressBook.doCommand(command, scan)) {
						break;
					}
				} catch (InputMismatchException e) {}
			}
		}
	}

	private boolean doCommand(int command, Scanner scanner) {
		switch (command) {
			case 1:
				adbService.listAllPersons().stream().map(p -> p.getId() + ": " + p.getFirstName() + " " + p.getLastName())
						.forEach(System.out::println);
			break;
			case 2:
			break;
			case 3:
			break;
			case 4:
				System.out.print("\nEnter ID of person to delete: ");
				int id = scanner.nextInt();
				if (adbService.deletePerson(id)) {
					System.out.println("Delete successful.");
				} else {
					System.out.println("Person not found.");
				}
			break;
			case 5:
				return true;
			default:
		}
		return false;
	}
}
