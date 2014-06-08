package com.accela.tetrixgame.gamelogic.control;

import java.util.*;

/**
 * 
 * 用来记录什么控制键对应于什么控制状态。
 * 
 */
public class KeyMap {
	private Map<Integer, HerizontalStatus> hKeyMap = new HashMap<Integer, HerizontalStatus>();
	private Map<Integer, VerticalStatus> vKeyMap = new HashMap<Integer, VerticalStatus>();
	private Map<Integer, ScrollStatus> sKeyMap = new HashMap<Integer, ScrollStatus>();

	// ///////////////////////////////////////////////////////////////////////////

	public HerizontalStatus setHerizontalKey(int keyCode,
			HerizontalStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		return hKeyMap.put(keyCode, status);
	}

	public HerizontalStatus removeHerziontalKey(int keyCode) {
		return hKeyMap.remove(keyCode);
	}

	public HerizontalStatus getHerizontalKey(int keyCode) {
		return hKeyMap.get(keyCode);
	}

	public List<Integer> getHerizontalKey(HerizontalStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		List<Integer> list = new LinkedList<Integer>();
		for (Integer keyCode : hKeyMap.keySet()) {
			assert (keyCode != null);

			if (hKeyMap.get(keyCode) == status) {
				list.add(keyCode);
			}
		}

		return list;
	}

	public boolean containsHerizontalKey(int keyCode) {
		return hKeyMap.containsKey(keyCode);
	}

	// ///////////////////////////////////////////////////////////////////////////

	public VerticalStatus setVerticalKey(int keyCode, VerticalStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		return vKeyMap.put(keyCode, status);
	}

	public VerticalStatus removeVerticalKey(int keyCode) {
		return vKeyMap.remove(keyCode);
	}

	public VerticalStatus getVerticalKey(int keyCode) {
		return vKeyMap.get(keyCode);
	}

	public List<Integer> getVerticalKey(VerticalStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		List<Integer> list = new LinkedList<Integer>();
		for (Integer keyCode : vKeyMap.keySet()) {
			assert (keyCode != null);

			if (vKeyMap.get(keyCode) == status) {
				list.add(keyCode);
			}
		}

		return list;
	}

	public boolean containsVerticalKey(int keyCode) {
		return vKeyMap.containsKey(keyCode);
	}

	// ///////////////////////////////////////////////////////////////////////////

	public ScrollStatus setScrollKey(int keyCode, ScrollStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		return sKeyMap.put(keyCode, status);
	}

	public ScrollStatus removeScrollKey(int keyCode) {
		return sKeyMap.remove(keyCode);
	}

	public ScrollStatus getScrollKey(int keyCode) {
		return sKeyMap.get(keyCode);
	}

	public List<Integer> getScrollKey(ScrollStatus status) {
		if (null == status) {
			throw new NullPointerException("status should not be null");
		}

		List<Integer> list = new LinkedList<Integer>();
		for (Integer keyCode : sKeyMap.keySet()) {
			assert (keyCode != null);

			if (sKeyMap.get(keyCode) == status) {
				list.add(keyCode);
			}
		}

		return list;
	}

	public boolean containsScrollKey(int keyCode) {
		return sKeyMap.containsKey(keyCode);
	}

}
