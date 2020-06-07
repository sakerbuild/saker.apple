package saker.apple.impl.plist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;

public class PlistValueOption implements Externalizable {
	private static final long serialVersionUID = 1L;

	private Object value;

	/**
	 * For {@link Externalizable}.
	 */
	public PlistValueOption() {
	}

	private PlistValueOption(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public static PlistValueOption create(boolean value) {
		return new PlistValueOption(value);
	}

	public static PlistValueOption create(String value) {
		return new PlistValueOption(value);
	}

	public static PlistValueOption create(long value) {
		return new PlistValueOption(value);
	}

	public static PlistValueOption create(double value) {
		return new PlistValueOption(value);
	}

	public static PlistValueOption create(SDKPathReference value) {
		return new PlistValueOption(value);
	}

//	public static PlistValueOption create(SDKPathCollectionReference value) {
//		return new PlistValueOption(value);
//	}

	public static PlistValueOption create(SDKPropertyReference value) {
		return new PlistValueOption(value);
	}

//	public static PlistValueOption create(SDKPropertyCollectionReference value) {
//		return new PlistValueOption(value);
//	}

	public static PlistValueOption create(Collection<? extends PlistValueOption> array) {
		return new PlistValueOption(ImmutableUtils.makeImmutableList(array));
	}

	public static PlistValueOption create(Map<String, ? extends PlistValueOption> dict) {
		return new PlistValueOption(ImmutableUtils.makeImmutableNavigableMap(dict));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlistValueOption other = (PlistValueOption) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + value + "]";
	}

}
