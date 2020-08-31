package x.mvmn.learn.java.addressbook.api.service;

import java.util.Collection;

import x.mvmn.learn.java.addressbook.api.model.Address;
import x.mvmn.learn.java.addressbook.api.model.Person;
import x.mvmn.learn.java.addressbook.api.model.PhoneNumber;

public interface AddressBookService {

	public Collection<? extends Person> listAllPersons();

	public Person getPerson(long personId);

	public long savePerson(Person person);

	public boolean deletePerson(long personId);

	public Collection<? extends Address> getAddresses(long personId);

	public Collection<? extends PhoneNumber> getPhoneNumbers(long personId);

	public long addAddress(long personId, Address address);

	public boolean deleteAddress(long personId, long addressId);

	public long addPhoneNumber(long personId, PhoneNumber phoneNumber);

	public boolean deletePhoneNumber(long personId, long phoneNumberId);
}
