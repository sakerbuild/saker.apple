package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.api.SakerAppleUtils;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.IndeterminateSDKDescription;
import saker.sdk.support.api.ResolvedSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKDescriptionVisitor;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.UserSDKDescription;

public class XcodeExecutableSDKDescription implements IndeterminateSDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	private IndeterminateSDKDescription sdk;
	private String executableName;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeExecutableSDKDescription() {
	}

	protected XcodeExecutableSDKDescription(IndeterminateSDKDescription sdk, String executableName) {
		this.sdk = sdk;
		this.executableName = executableName;
	}

	@Override
	public SDKDescription getBaseSDKDescription() {
		return EnvironmentSDKDescription
				.create(new XcodeExecutableSDKReferenceEnvironmentProperty(sdk, executableName));
	}

	@Override
	public SDKDescription pinSDKDescription(SDKReference sdkreference) {
		if (sdkreference instanceof XcodeExecutableSDKReference) {
			XcodeExecutableSDKReference xcodeexecsdk = (XcodeExecutableSDKReference) sdkreference;
			SDKReference xcodesdk = xcodeexecsdk.getXcodeSDK();
			SDKDescription pinned = sdk.pinSDKDescription(xcodesdk);
			return EnvironmentSDKDescription
					.create(new XcodeExecutableSDKReferenceEnvironmentProperty(pinned, executableName));
		}
		//shouldn't happen, but handle just in case
		return getBaseSDKDescription();
	}

	public static SDKDescription create(SDKDescription sdk, String executablename) {
		SDKDescription[] result = { null };
		sdk.accept(new SDKDescriptionVisitor() {
			@Override
			public void visit(EnvironmentSDKDescription description) {
				result[0] = EnvironmentSDKDescription
						.create(new XcodeExecutableSDKReferenceEnvironmentProperty(description, executablename));
			}

			@Override
			public void visit(ResolvedSDKDescription description) {
				SDKReference sdkref = description.getSDKReference();
				try {
					SakerPath path = sdkref.getPath("exe." + executablename);
					if (path != null) {
						result[0] = ResolvedSDKDescription.create(new XcodeExecutableSDKReference(sdkref, path));
						return;
					}
				} catch (Exception e) {
					//TODO ignored exception
				}
				//unsupported if failed
				SDKDescriptionVisitor.super.visit(description);
			}

			@Override
			public void visit(UserSDKDescription description) {
				SakerPath path = ObjectUtils.getMapValue(description.getPaths(), "exe." + executablename);
				if (path != null) {
					result[0] = UserSDKDescription.create(description.getQualifier(), ImmutableUtils
							.singletonNavigableMap(SakerAppleUtils.SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE, path), null);
					return;
				}
				// unsupported otherwise
				SDKDescriptionVisitor.super.visit(description);
			}

			@Override
			public void visit(IndeterminateSDKDescription description) {
				result[0] = new XcodeExecutableSDKDescription(description, executablename);
			}

		});
		return result[0];
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(executableName);
		out.writeObject(sdk);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		executableName = SerialUtils.readExternalObject(in);
		sdk = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executableName == null) ? 0 : executableName.hashCode());
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
		XcodeExecutableSDKDescription other = (XcodeExecutableSDKDescription) obj;
		if (executableName == null) {
			if (other.executableName != null)
				return false;
		} else if (!executableName.equals(other.executableName))
			return false;
		if (sdk == null) {
			if (other.sdk != null)
				return false;
		} else if (!sdk.equals(other.sdk))
			return false;
		return true;
	}

}
