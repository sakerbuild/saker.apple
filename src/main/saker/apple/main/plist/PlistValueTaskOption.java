package saker.apple.main.plist;

import java.util.Collection;
import java.util.Map;

import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;

public abstract class PlistValueTaskOption {
	@Override
	public PlistValueTaskOption clone() {
		return this;
	}

	public abstract void accept(Visitor visitor);

	public static PlistValueTaskOption valueOf(String input) {
		if ("true".equalsIgnoreCase(input)) {
			return valueOf(true);
		}
		if ("false".equalsIgnoreCase(input)) {
			return valueOf(false);
		}
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(long input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(double input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(boolean input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(SDKPathReference input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(SDKPropertyReference input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(@SuppressWarnings("rawtypes") Collection input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public static PlistValueTaskOption valueOf(@SuppressWarnings("rawtypes") Map input) {
		return new PlistValueTaskOption() {
			@Override
			public void accept(Visitor visitor) {
				visitor.visit(input);
			}
		};
	}

	public interface Visitor {
		public void visit(String value);

		public void visit(boolean value);

		public void visit(long value);

		public void visit(double value);

		public void visit(SDKPathReference value);

		public void visit(SDKPropertyReference value);

		public void visit(Collection<?> value);

		public void visit(Map<?, ?> value);
	}
}
