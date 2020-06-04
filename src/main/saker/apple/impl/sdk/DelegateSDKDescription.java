package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKDescriptionVisitor;

public class DelegateSDKDescription implements SDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	private SDKDescription sdk;

	/**
	 * For {@link Externalizable}.
	 */
	public DelegateSDKDescription() {
	}

	public DelegateSDKDescription(SDKDescription description) {
		this.sdk = description;
	}

	@Override
	public void accept(SDKDescriptionVisitor visitor) throws NullPointerException {
		sdk.accept(visitor);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sdk);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdk = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sdk == null) ? 0 : sdk.hashCode());
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
		DelegateSDKDescription other = (DelegateSDKDescription) obj;
		if (sdk == null) {
			if (other.sdk != null)
				return false;
		} else if (!sdk.equals(other.sdk))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (sdk != null ? "sdk=" + sdk : "") + "]";
	}
}
