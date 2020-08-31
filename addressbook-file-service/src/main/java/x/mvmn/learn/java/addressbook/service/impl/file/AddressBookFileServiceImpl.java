package x.mvmn.learn.java.addressbook.service.impl.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import x.mvmn.learn.java.addressbook.api.model.Address;
import x.mvmn.learn.java.addressbook.api.model.Person;
import x.mvmn.learn.java.addressbook.api.model.PhoneNumber;
import x.mvmn.learn.java.addressbook.api.service.AddressBookService;
import x.mvmn.learn.java.addressbook.impl.model.MutableAddressImpl;
import x.mvmn.learn.java.addressbook.impl.model.MutableEntity;
import x.mvmn.learn.java.addressbook.impl.model.MutablePersonImpl;
import x.mvmn.learn.java.addressbook.impl.model.MutablePhoneNumberImpl;

public class AddressBookFileServiceImpl implements AddressBookService {

	private final File storageDir;
	private ObjectMapper objectMapper;
	private String fileExtension;
	private ConcurrentHashMap<Long, Object> personLock = new ConcurrentHashMap<>();

	private enum PersonSubEntityType {
		PHONE, ADDRESS;
	}

	public AddressBookFileServiceImpl(ObjectMapper objectMapper, File storageDir, String fileExtension) {
		this.objectMapper = objectMapper;
		this.storageDir = storageDir;
		this.fileExtension = fileExtension != null && !fileExtension.isEmpty() ? fileExtension : "adbk";
	}

	public List<MutablePersonImpl> listAllPersons() {
		return Arrays.stream(storageDir.listFiles()).filter(File::isDirectory).filter(f -> f.getName().toLowerCase().startsWith("person_"))
				.map(file -> new File(file, "person." + fileExtension)).map(this::parse).collect(Collectors.toList());
	}

	public MutablePersonImpl getPerson(long id) {
		return doWithPersonLock(id, personId -> {
			File file = idToPersonFile(personId);
			if (file.exists() && file.isFile()) {
				return parse(file);
			}
			return null;
		});
	}

	public long savePerson(Person person) {
		long id = person.getId();
		if (id < 1) {
			id = generateNewPersonId();
			((MutableEntity<?>) person).setId(id);
		}
		final Person finalPerson = person;
		doWithPersonLock(id, personId -> {
			File personDir = idToPersonDirFile(personId);
			File personFile = idToPersonFile(personId);
			try {
				personDir.mkdir();
				byte[] data = objectMapper.writeValueAsBytes(finalPerson);
				FileUtils.writeByteArrayToFile(personFile, data, false);
			} catch (Exception e) {
				throw new RuntimeException("Failed to save person into file" + personFile.getAbsolutePath(), e);
			}
			return null;
		});
		return id;
	}

	private long generateNewPersonId() {
		synchronized (this) {
			return Arrays.stream(storageDir.listFiles()).filter(File::isDirectory).map(File::getName)
					.filter(fileName -> fileName.toLowerCase().startsWith("person_"))
					.map(fileName -> fileName.substring("person_".length())).mapToLong(Long::parseLong).max().orElse(0) + 1;
		}
	}

	private long generateNewPersonSubEntityId(long personId, PersonSubEntityType subEntityType) {
		synchronized (this) {
			File personDir = idToPersonDirFile(personId);
			if (!personDir.exists()) {
				return 1;
			} else {
				String fileNamePrefix = subEntityType.name().toLowerCase() + "_";
				return Arrays.stream(personDir.listFiles()).filter(File::isFile).map(File::getName)
						.filter(fileName -> fileName.toLowerCase().startsWith(fileNamePrefix))
						.map(fileName -> fileName.substring(fileNamePrefix.length())).mapToLong(Long::parseLong).max().orElse(0) + 1;
			}
		}
	}

	public boolean deletePerson(long id) {
		return doWithPersonLock(id, personId -> {
			File file = idToPersonDirFile(personId);
			if (file.exists() && file.isDirectory()) {
				Arrays.stream(file.listFiles()).forEach(File::delete);
				file.delete();
				return true;
			}
			return false;
		});
	}

	private File idToPersonFile(long id) {
		return new File(idToPersonDirFile(id), "person." + fileExtension);
	}

	private File idToPersonDirFile(long id) {
		return new File(storageDir, "person_" + id);
	}

	private MutablePersonImpl parse(File file) {
		try {
			return objectMapper.readValue(file, MutablePersonImpl.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse file as address book person entry: " + file.getAbsolutePath(), e);
		}
	}

	private MutableAddressImpl parseAddress(File file) {
		try {
			return objectMapper.readValue(file, MutableAddressImpl.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse file as address book address entry: " + file.getAbsolutePath(), e);
		}
	}

	private MutablePhoneNumberImpl parsePhone(File file) {
		try {
			return objectMapper.readValue(file, MutablePhoneNumberImpl.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse file as address book phone entry: " + file.getAbsolutePath(), e);
		}
	}

	private <T> T doWithPersonLock(long id, Function<Long, T> task) {
		boolean lockAcquired = false;
		int attempts = 0;
		Object lockIndicator = new Object();
		do {
			lockAcquired = personLock.putIfAbsent(id, lockIndicator) == null;
			if (!lockAcquired) {
				attempts++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Interrupted", e);
				}
			}
			if (attempts > 10) {
				throw new RuntimeException("Failed to acquire lock for person " + id);
			}
		} while (!lockAcquired);
		T result = null;
		try {
			result = task.apply(id);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		} finally {
			personLock.remove(id);
		}
		return result;
	}

	@Override
	public List<MutableAddressImpl> getAddresses(long personId) {
		return doWithPersonLock(personId,
				id -> Arrays.stream(idToPersonDirFile(id).listFiles()).filter(File::isFile)
						.filter(file -> file.getName().toUpperCase().startsWith(PersonSubEntityType.ADDRESS.name() + "_"))
						.map(this::parseAddress).collect(Collectors.toList()));
	}

	@Override
	public long addAddress(long personId, Address address) {
		return addSubEntity(personId, (MutableEntity<?>) address, PersonSubEntityType.ADDRESS);
	}

	protected long addSubEntity(long personId, MutableEntity<?> subEntity, PersonSubEntityType subEntityType) {
		return doWithPersonLock(personId, id -> {
			long subEntityId = generateNewPersonSubEntityId(personId, subEntityType);
			subEntity.setId(subEntityId);

			File personDir = idToPersonDirFile(id);
			if (!personDir.exists()) {
				throw new RuntimeException("Person doesn't exist with id " + id);
			}
			try {
				objectMapper.writeValue(subEntityFile(personId, subEntityId, subEntityType), subEntity);
			} catch (Exception e) {
				throw new RuntimeException("Failed to serialize address", e);
			}

			return subEntityId;
		});
	}

	@Override
	public boolean deleteAddress(long personId, long addressId) {
		return deleteSubEntity(personId, addressId, PersonSubEntityType.ADDRESS);
	}

	protected boolean deleteSubEntity(long personId, long subEntityId, PersonSubEntityType subEntityType) {
		return doWithPersonLock(personId, id -> {
			File addresFile = subEntityFile(personId, subEntityId, subEntityType);
			if (addresFile.exists()) {
				addresFile.delete();
				return true;
			}

			return false;
		});
	}

	protected File subEntityFile(long personId, long subEntityId, PersonSubEntityType subEntityType) {
		File personDir = idToPersonDirFile(personId);
		return new File(personDir, subEntityType.name().toLowerCase() + "_" + subEntityId);
	}

	@Override
	public List<MutablePhoneNumberImpl> getPhoneNumbers(long personId) {
		return doWithPersonLock(personId,
				id -> Arrays.stream(idToPersonDirFile(id).listFiles()).filter(File::isFile)
						.filter(file -> file.getName().toUpperCase().startsWith(PersonSubEntityType.PHONE.name() + "_"))
						.map(this::parsePhone).collect(Collectors.toList()));
	}

	@Override
	public long addPhoneNumber(long personId, PhoneNumber phoneNumber) {
		return addSubEntity(personId, (MutableEntity<?>) phoneNumber, PersonSubEntityType.PHONE);
	}

	@Override
	public boolean deletePhoneNumber(long personId, long phoneNumberId) {
		return deleteSubEntity(personId, phoneNumberId, PersonSubEntityType.PHONE);
	}
}
