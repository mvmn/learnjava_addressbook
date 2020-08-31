package x.mvmn.learn.java.addressbook.impl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import x.mvmn.learn.java.addressbook.api.model.PhoneNumber;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class MutablePhoneNumberImpl implements PhoneNumber, MutableEntity<MutablePhoneNumberImpl> {
	protected long id;
	protected int countryCode;
	protected int number;
}
