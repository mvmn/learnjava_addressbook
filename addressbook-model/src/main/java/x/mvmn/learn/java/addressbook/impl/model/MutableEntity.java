package x.mvmn.learn.java.addressbook.impl.model;

public interface MutableEntity<T> {

	public long getId();

	public T setId(long id);
}
