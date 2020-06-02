package saker.apple.impl;

import java.util.Set;
import java.util.function.Predicate;

import saker.build.thirdparty.saker.util.function.Functionals;
import saker.nest.version.VersionRange;

public class SakerAppleImplUtils {
	private SakerAppleImplUtils() {
		throw new UnsupportedOperationException();
	}

	public static Predicate<? super String> getSDKVersionPredicate(Set<String> versionranges) {
		if (versionranges == null) {
			return Functionals.alwaysPredicate();
		}
		Object[] array = versionranges.toArray();
		for (int i = 0; i < array.length; i++) {
			try {
				array[i] = VersionRange.valueOf((String) array[i]);
			} catch (IllegalArgumentException e) {
			}
		}
		return v -> {
			if (v == null) {
				return false;
			}
			for (Object e : array) {
				if (e instanceof VersionRange) {
					if (((VersionRange) e).includes(v)) {
						return true;
					}
				} else {
					if (v.equals(e)) {
						return true;
					}
				}
			}
			return false;
		};
	}
}
