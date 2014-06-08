package com.accela.tetrixgame.gamelogic.control;

/**
 * 
 * 游戏的控制键的记录和管理。 这个类提供了对控制键的更高级的管理方法，当玩家按下一个键，然后再不松开的情况下
 * 又按下了另一个键的时候，这个了仍然能够以适当的方式处理案件结果
 * 
 * 游戏中实现控制键的基本思路是，游戏内部定义了HerizontalStatus，VerticalStatius和
 * ScrollStatus这些控制状态，状态处理器根据他们的值来得知到底现在的bar被控制着做什么。
 * 而玩家的控制键如何转化成上述的控制状态，就由Control来完成。
 * 
 */
public class Control {
	private KeyMap keyMap = new KeyMap();

	public boolean containsHerizontalKey(int keyCode) {
		return keyMap.containsHerizontalKey(keyCode);
	}

	public boolean containsScrollKey(int keyCode) {
		return keyMap.containsScrollKey(keyCode);
	}

	public boolean containsVerticalKey(int keyCode) {
		return keyMap.containsVerticalKey(keyCode);
	}

	public HerizontalStatus getHerizontalKey(int keyCode) {
		return keyMap.getHerizontalKey(keyCode);
	}

	public ScrollStatus getScrollKey(int keyCode) {
		return keyMap.getScrollKey(keyCode);
	}

	public VerticalStatus getVerticalKey(int keyCode) {
		return keyMap.getVerticalKey(keyCode);
	}

	public HerizontalStatus removeHerziontalKey(int keyCode) {
		return keyMap.removeHerziontalKey(keyCode);
	}

	public ScrollStatus removeScrollKey(int keyCode) {
		return keyMap.removeScrollKey(keyCode);
	}

	public VerticalStatus removeVerticalKey(int keyCode) {
		return keyMap.removeVerticalKey(keyCode);
	}

	public HerizontalStatus setHerizontalKey(int keyCode,
			HerizontalStatus status) {
		return keyMap.setHerizontalKey(keyCode, status);
	}

	public ScrollStatus setScrollKey(int keyCode, ScrollStatus status) {
		return keyMap.setScrollKey(keyCode, status);
	}

	public VerticalStatus setVerticalKey(int keyCode, VerticalStatus status) {
		return keyMap.setVerticalKey(keyCode, status);
	}

	// //////////////////////////////////////////////////////////////////////

	private KeyStack<Integer> hStack = new KeyStack<Integer>();
	private KeyStack<Integer> vStack = new KeyStack<Integer>();
	private KeyStack<Integer> sStack = new KeyStack<Integer>();

	public void keyPressed(int keyCode) {
		if (keyMap.containsHerizontalKey(keyCode)) {
			hStack.push(keyCode);
		}

		if (keyMap.containsVerticalKey(keyCode)) {
			vStack.push(keyCode);
		}

		if (keyMap.containsScrollKey(keyCode)) {
			sStack.push(keyCode);
		}
	}

	public void keyReleased(int keyCode) {
		if (keyMap.containsHerizontalKey(keyCode)) {
			hStack.remove(keyCode);
		}

		if (keyMap.containsVerticalKey(keyCode)) {
			vStack.remove(keyCode);
		}

		if (keyMap.containsScrollKey(keyCode)) {
			sStack.remove(keyCode);
		}
	}

	public HerizontalStatus getHerizontalStatus() {
		if (hStack.isEmpty()) {
			return HerizontalStatus.DRIFT;
		} else {
			Integer keyCode = hStack.peek();
			assert (keyCode != null);

			HerizontalStatus status = keyMap.getHerizontalKey(keyCode);
			if (null == status) {
				throw new IllegalStateException(
						"found a key that has not been bound to a control status: "
								+ keyCode);
			}

			return status;

		}
	}

	public VerticalStatus getVerticalStatus() {
		if (vStack.isEmpty()) {
			return VerticalStatus.DRIFT;
		} else {
			Integer keyCode = vStack.peek();
			assert (keyCode != null);

			VerticalStatus status = keyMap.getVerticalKey(keyCode);
			if (null == status) {
				throw new IllegalStateException(
						"found a key that has not been bound to a control status: "
								+ keyCode);
			}

			return status;

		}
	}

	public ScrollStatus getScrollStatus() {
		if (sStack.isEmpty()) {
			return ScrollStatus.NONE;
		} else {
			Integer keyCode = sStack.peek();
			assert (keyCode != null);

			ScrollStatus status = keyMap.getScrollKey(keyCode);
			if (null == status) {
				throw new IllegalStateException(
						"found a key that has not been bound to a control status: "
								+ keyCode);
			}

			return status;

		}
	}

}
