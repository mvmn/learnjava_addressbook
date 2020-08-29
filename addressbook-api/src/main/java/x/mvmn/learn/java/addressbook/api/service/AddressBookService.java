package x.mvmn.learn.java.addressbook.api.service;

import java.util.Collection;

import x.mvmn.learn.java.addressbook.api.model.Person;

public interface AddressBookService {

	public Collection<Person> listAllPersons();

	public Person getPerson(long id);

	public Person savePeron(Person person);

	public boolean deletePerson(long id);
}
