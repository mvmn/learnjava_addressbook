package x.mvmn.learn.java.addressbook.service.impl.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import x.mvmn.learn.java.addressbook.api.model.Person;
import x.mvmn.learn.java.addressbook.api.service.AddressBookService;
import x.mvmn.learn.java.addressbook.impl.model.MutablePersonImpl;

public class AddressBookFileServiceImpl implements AddressBookService {

	private final File storageDir;
	private ObjectMapper objectMapper;
	private String fileExtension;

	public AddressBookFileServiceImpl(ObjectMapper objectMapper, File storageDir, String fileExtension) {
		this.objectMapper = objectMapper;
		this.storageDir = storageDir;
		this.fileExtension = fileExtension != null && !fileExtension.isEmpty() ? fileExtension : "adbk";
	}

	public Collection<Person> listAllPersons() {
		return Arrays.stream(storageDir.listFiles()).filter(f -> f.getName().toLowerCase().endsWith("." + fileExtension)).map(this::parse)
				.collect(Collectors.toList());
	}

	public Person getPerson(long id) {
		File file = idToFile(id);
		if (file.exists() && file.isFile()) {
			return parse(file);
		}
		return null;
	}

	public Person savePeron(Person person) {
		if (person.getId() < 1) {
			long id;
			synchronized (this) {
				id = Arrays.stream(storageDir.listFiles()).map(File::getName)
						.filter(fileName -> fileName.toLowerCase().endsWith("." + fileExtension))
						.map(fileName -> fileName.substring(0, fileName.indexOf("."))).mapToLong(Long::parseLong).max().orElse(0) + 1;
			}
			if (person instanceof MutablePersonImpl) {
				((MutablePersonImpl) person).setId(id);
			} else {
				throw new IllegalArgumentException("Unsupported");
			}
		}
		File file = idToFile(person.getId());
		try {
			byte[] data = objectMapper.writeValueAsBytes(person);
			FileUtils.writeByteArrayToFile(file, data, false);
		} catch (Exception e) {
			throw new RuntimeException("Failed to save person into file" + file.getAbsolutePath(), e);
		}
		return person;
	}

	public boolean deletePerson(long id) {
		File file = idToFile(id);
		if (file.exists() && file.isFile()) {
			file.delete();
			return true;
		}
		return false;
	}

	private File idToFile(long id) {
		return new File(storageDir, String.format("%s.%s", id, fileExtension));
	}

	private MutablePersonImpl parse(File file) {
		try {
			return objectMapper.readValue(file, MutablePersonImpl.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse file as address book entry: " + file.getAbsolutePath(), e);
		}
	}
}
