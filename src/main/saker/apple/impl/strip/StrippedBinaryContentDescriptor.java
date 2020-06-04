package saker.apple.impl.strip;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class StrippedBinaryContentDescriptor implements ContentDescriptor, Externalizable {
	private static final long serialVersionUID = 1L;

	private ContentDescriptor inputContents;

	/**
	 * For {@link Externalizable}.
	 */
	public StrippedBinaryContentDescriptor() {
	}

	public StrippedBinaryContentDescriptor(ContentDescriptor inputContents) {
		this.inputContents = inputContents;
	}

	@Override
	public boolean isChanged(ContentDescriptor previouscontent) {
		if (!(previouscontent instanceof StrippedBinaryContentDescriptor)) {
			return true;
		}
		StrippedBinaryContentDescriptor ocd = (StrippedBinaryContentDescriptor) previouscontent;
		return this.inputContents.isChanged(ocd.inputContents);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(inputContents);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputContents = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputContents == null) ? 0 : inputContents.hashCode());
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
		StrippedBinaryContentDescriptor other = (StrippedBinaryContentDescriptor) obj;
		if (inputContents == null) {
			if (other.inputContents != null)
				return false;
		} else if (!inputContents.equals(other.inputContents))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + inputContents + "]";
	}

}
