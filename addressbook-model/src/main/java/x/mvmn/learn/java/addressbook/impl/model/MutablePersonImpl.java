package x.mvmn.learn.java.addressbook.impl.model;

import java.util.List;

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
public class MutablePersonImpl implements Person {
	protected long id;
	protected String firstName;
	protected String lastName;
	protected String middleName;
	protected String prefix;
	protected List<MutableAddressImpl> addresses;
	protected List<MutablePhoneNumberImpl> phoneNumbers;
}
