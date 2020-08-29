package x.mvmn.learn.java.addressbook.impl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import x.mvmn.learn.java.addressbook.api.model.Address;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MutableAddressImpl implements Address {
	protected String country;
	protected String region;
	protected String city;
	protected String street;
	protected String building;
	protected String appartment;
}
