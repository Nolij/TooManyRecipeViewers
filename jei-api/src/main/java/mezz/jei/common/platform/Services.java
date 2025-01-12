package mezz.jei.common.platform;

import mezz.jei.neoforge.platform.PlatformHelper;

public final class Services {
	
	public static final IPlatformHelper PLATFORM = new PlatformHelper();
	
	public static <T> T load(Class<T> serviceClass) {
		if (serviceClass == IPlatformHelper.class)
			//noinspection unchecked
			return (T) PLATFORM;
		
		throw new UnsupportedOperationException();
	}
	
}
