package com.accela.tetrixgame.gamelogic.control;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

/**
 * 
 * 这个类用来为KeyStatusRecorder存放键盘按键。 它根据键盘输入的特殊性工作，能够保证不会存储 重复元素。
 * 
 * 但是这个类不同用来装其他东西，否则就会导致其 工作不正常。
 * 
 */
public class KeyStack<T> implements Iterable<T> {
	private Stack<T> stack = new Stack<T>();

	public boolean push(T element) {
		if (null == element) {
			throw new NullPointerException("element should not be null");
		}

		boolean result = false;
		if (stack.isEmpty()) {
			stack.push(element);
			result = true;
		} else if (!element.equals(stack.peek())) {
			stack.push(element);
			result = true;
		} else {
			result = false;
		}

		assert (checkNoRepeatElement());
		return result;
	}

	public T pop() {
		if (stack.isEmpty()) {
			throw new IllegalStateException("stack is empty");
		}

		return stack.pop();
	}

	public T peek() {
		if (stack.isEmpty()) {
			throw new IllegalStateException("stack is empty");
		}

		return stack.peek();
	}

	public T remove(T element) {
		if (null == element) {
			throw new NullPointerException("element should not be null");
		}

		assert (checkNoRepeatElement());

		if (stack.isEmpty()) {
			return null;
		} else if (stack.peek().equals(element)) {
			return stack.pop();
		} else if (stack.size() == 1) {
			return null;
		} else {
			ListIterator<T> itr = stack.listIterator();

			while (itr.hasNext()) {
				T e = itr.next();
				assert (e != null);

				if (e.equals(element)) {
					itr.remove();

					return e;
				}
			}

			return null;
		}

	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}

	@Override
	public Iterator<T> iterator() {
		return stack.iterator();
	}

	private boolean checkNoRepeatElement() {
		for (T element : stack) {
			if (null == element) {
				return false;
			}

			for (T element_inner : stack) {
				if (null == element_inner) {
					return false;
				}

				if (element != element_inner && element.equals(element_inner)) {
					return false;
				}
			}
		}

		return true;
	}

}
