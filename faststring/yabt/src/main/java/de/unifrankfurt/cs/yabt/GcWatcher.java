package de.unifrankfurt.cs.yabt;

import java.lang.ref.WeakReference;

class GcWatcher {

	private WeakReference<Dummy> weakRef;
	
	GcWatcher() {
		initWeakRef();
	}

	private void initWeakRef() {
		weakRef = new WeakReference<>(new Dummy());
	}

	synchronized boolean wasGcActive() {
		return (weakRef.get() == null);
	}
	
	synchronized void reset() {		
		initWeakRef();
	}
	
	class Dummy { }
}