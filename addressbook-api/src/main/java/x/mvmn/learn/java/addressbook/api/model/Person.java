package x.mvmn.learn.java.addressbook.api.model;

import java.util.Collection;

public interface Person {

	public long getId();

	public String getFirstName();

	public String getLastName();

	public String getMiddleName();

	public String getPrefix();

	public Collection<? extends Address> getAddresses();

	public Collection<? extends PhoneNumber> getPhoneNumbers();
}
