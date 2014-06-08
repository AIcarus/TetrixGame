package com.accela.tetrixgame.util;

import java.util.*;

public class KeyList implements Iterable<KeyList.KeyEntry> {
	private List<KeyEntry> keys = new LinkedList<KeyEntry>();

	public synchronized void keyPressed(int keyCode) {
		KeyEntry entry = new KeyEntry(keyCode, true);
		assert (entry != null);
		keys.add(entry);
	}

	public synchronized void keyReleased(int keyCode) {
		KeyEntry entry = new KeyEntry(keyCode, false);
		assert (entry != null);
		keys.add(entry);
	}

	public boolean isEmpty() {
		return keys.isEmpty();
	}

	public synchronized void addAll(KeyList keyList) {
		if (null == keyList) {
			throw new NullPointerException("keyList should not be null");
		}

		assert (keyList.keys != null);

		this.keys.addAll(keyList.keys);
	}

	@Override
	public Iterator<KeyEntry> iterator() {
		return keys.iterator();
	}

	public synchronized void clear() {
		keys.clear();
	}

	public int size() {
		return keys.size();
	}

	public static class KeyEntry {
		private int keyCode;
		private boolean pressed;

		public KeyEntry(int keyCode, boolean pressed) {
			this.keyCode = keyCode;
			this.pressed = pressed;
		}

		public int getKeyCode() {
			return keyCode;
		}

		public boolean isPressed() {
			return pressed;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + keyCode;
			result = prime * result + (pressed ? 1231 : 1237);
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
			KeyEntry other = (KeyEntry) obj;
			if (keyCode != other.keyCode)
				return false;
			if (pressed != other.pressed)
				return false;
			return true;
		}

	}
}
