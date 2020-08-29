package x.mvmn.learn.java.addressbook.impl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import x.mvmn.learn.java.addressbook.api.model.PhoneNumber;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MutablePhoneNumberImpl implements PhoneNumber {
	protected int countryCode;
	protected int number;
}
