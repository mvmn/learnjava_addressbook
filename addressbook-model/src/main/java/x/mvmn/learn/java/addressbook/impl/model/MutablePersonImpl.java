package x.mvmn.learn.java.addressbook.impl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import x.mvmn.learn.java.addressbook.api.model.Person;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MutablePersonImpl implements Person, MutableEntity<MutablePersonImpl> {
	protected long id;
	protected String firstName;
	protected String lastName;
	protected String middleName;
	protected String prefix;

	public MutablePersonImpl ofPerson(Person person) {
		if (person == null) {
			return null;
		}
		if (person instanceof MutablePersonImpl) {
			return (MutablePersonImpl) person;
		}
		return MutablePersonImpl.builder().id(person.getId()).firstName(person.getFirstName()).middleName(person.getMiddleName())
				.lastName(person.getLastName()).prefix(person.getPrefix()).build();
	}
}
