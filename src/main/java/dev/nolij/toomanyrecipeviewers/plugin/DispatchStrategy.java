package dev.nolij.toomanyrecipeviewers.plugin;

public enum DispatchStrategy {
	SKIP, SYNC_EMI, SYNC_MAIN, ASYNC;
	
	public DispatchStrategy downgrade(boolean allowAsync, DispatchStrategy fallback) {
		if (allowAsync || this != ASYNC)
			return this;
		
		return fallback;
	}
}
