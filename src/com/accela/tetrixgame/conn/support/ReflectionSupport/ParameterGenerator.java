package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.util.Stack;

//TODO 有待完善的测试
//TODO ObjectCreator可以用缓冲区优化，但这不是十分必要的，因为多数类还是可以改进构造方法的
class ParameterGenerator {
	private static final Integer[] intVals = new Integer[] { 0, 1, -1 };
	private static final Boolean[] booleanVals = new Boolean[] { true, false };
	private static final Double[] doubleVals = new Double[] { 0.0, 1.0, -1.0 };
	private static final Float[] floatVals = new Float[] { 0.0f, 1.0f, -1.0f };
	private static final Long[] longVals = new Long[] { 0L, -1L, 1L };
	private static final Character[] charVals = new Character[] { 'a', 'A',
			'\0', '0', ',', '.' };
	private static final Byte[] byteVals = new Byte[] { 0, -1, 1 };
	private static final Short[] shortsVal = new Short[] { 0, -1, 1 };

	private static enum ObjectVal {
		NULL, INSTANCE
	};

	private static final ObjectVal[] objectVals = new ObjectVal[] {
			ObjectVal.NULL, ObjectVal.INSTANCE };

	private int intValIdx = 0;
	private int booleanValIdx = 0;
	private int doubleValIdx = 0;
	private int floatValIdx = 0;
	private int longValIdx = 0;
	private int charValIdx = 0;
	private int byteValIdx = 0;
	private int shortValIdx = 0;
	private int objectValIdx = 0;

	private static final int[][] intArrayVals = new int[][] { null, new int[0],
			new int[] { 0 }, new int[] { 1 }, new int[] { -1 }, };
	private static final boolean[][] booleanArrayVals = new boolean[][] { null,
			new boolean[0], new boolean[] { true }, new boolean[] { false }, };
	private static final double[][] doubleArrayVals = new double[][] { null,
			new double[0], new double[] { 0 }, new double[] { 1 },
			new double[] { -1 }, };
	private static final float[][] floatArrayVals = new float[][] { null,
			new float[0], new float[] { 0 }, new float[] { 1 },
			new float[] { -1 }, };
	private static final long[][] longArrayVals = new long[][] { null,
			new long[0], new long[] { 0 }, new long[] { 1 }, new long[] { -1 }, };
	private static final char[][] charArrayVals = new char[][] { null,
			new char[0], new char[] { 'a' }, new char[] { 'A' },
			new char[] { '\0' }, new char[] { ',' }, new char[] { '.' },
			new char[] { '_', 't', 'e', 'm', 'p', '.', 't', 'x', 't' }, };
	private static final byte[][] byteArrayVals = new byte[][] { null,
			new byte[0], new byte[] { 0 }, new byte[] { 1 }, new byte[] { -1 }, };
	private static final short[][] shortArrayVals = new short[][] { null,
			new short[0], new short[] { 0 }, new short[] { 1 },
			new short[] { -1 }, };

	private static enum ObjectArrayVal {
		NULL, EMPTY, ONE_ELEMENT,
	}

	private static final ObjectArrayVal[] objectArrayVals = new ObjectArrayVal[] {
			ObjectArrayVal.NULL, ObjectArrayVal.EMPTY,
			ObjectArrayVal.ONE_ELEMENT, };

	private int intArrayIdx = 0;
	private int booleanArrayIdx = 0;
	private int doubleArrayIdx = 0;
	private int floatArrayIdx = 0;
	private int longArrayIdx = 0;
	private int charArrayIdx = 0;
	private int byteArrayIdx = 0;
	private int shortArrayIdx = 0;
	private int objectArrayIdx = 0;

	private static final int MAX_NUM_OF_ENUM_VALS = 2;
	private int enumValIdx = 0;
	// 为了重复生成枚举类型常量，使用buffer将它们保存
	private Enum<?>[] enumValBuffer = null;
	private Class<?> enumValBufferOwner = null;

	private boolean hasNext = true;

	public void reset() {
		intValIdx = 0;
		booleanValIdx = 0;
		doubleValIdx = 0;
		floatValIdx = 0;
		longValIdx = 0;
		charValIdx = 0;
		byteValIdx = 0;
		shortValIdx = 0;
		objectValIdx = 0;

		intArrayIdx = 0;
		booleanArrayIdx = 0;
		doubleArrayIdx = 0;
		floatArrayIdx = 0;
		longArrayIdx = 0;
		charArrayIdx = 0;
		byteArrayIdx = 0;
		shortArrayIdx = 0;
		objectArrayIdx = 0;

		enumValIdx = 0;
		enumValBuffer = null;
		enumValBufferOwner = null;

		hasNext = true;
	}

	public Object createParameter(final Class<?> parameterType,
			final ObjectCreator objectCreator) {
		if (null == parameterType) {
			throw new NullPointerException("parameterType should not be null");
		}
		if (null == objectCreator) {
			throw new NullPointerException("objectCreator should not be null");
		}
		if (!hasNext) {
			throw new ArrayIndexOutOfBoundsException("no more elements left");
		}

		Object result = createParameterImpl(parameterType, objectCreator);

		if (intArrayIdx >= intArrayVals.length) {
			hasNext = false;
		}
		if (booleanArrayIdx >= booleanArrayVals.length) {
			hasNext = false;
		}
		if (doubleArrayIdx >= doubleArrayVals.length) {
			hasNext = false;
		}
		if (floatArrayIdx >= floatArrayVals.length) {
			hasNext = false;
		}
		if (longArrayIdx >= longArrayVals.length) {
			hasNext = false;
		}
		if (charArrayIdx >= charArrayVals.length) {
			hasNext = false;
		}
		if (byteArrayIdx >= byteArrayVals.length) {
			hasNext = false;
		}
		if (shortArrayIdx >= shortArrayVals.length) {
			hasNext = false;
		}
		if (objectArrayIdx >= objectArrayVals.length) {
			hasNext = false;
		}

		if (intValIdx >= intVals.length) {
			hasNext = false;
		}
		if (booleanValIdx >= booleanVals.length) {
			hasNext = false;
		}
		if (doubleValIdx >= doubleVals.length) {
			hasNext = false;
		}
		if (floatValIdx >= floatVals.length) {
			hasNext = false;
		}
		if (longValIdx >= longVals.length) {
			hasNext = false;
		}
		if (charValIdx >= charVals.length) {
			hasNext = false;
		}
		if (byteValIdx >= byteVals.length) {
			hasNext = false;
		}
		if (shortValIdx >= shortsVal.length) {
			hasNext = false;
		}
		if (objectValIdx >= objectVals.length) {
			hasNext = false;
		}

		if (enumValBuffer != null
				&& (enumValIdx - 1 >= enumValBuffer.length || enumValIdx > MAX_NUM_OF_ENUM_VALS)) {
			hasNext = false;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Object createParameterImpl(final Class<?> parameterType,
			final ObjectCreator objectCreator) {
		if (parameterType.isArray()) {
			if (parameterType == int[].class) {
				if (intArrayIdx < intArrayVals.length) {
					return intArrayVals[intArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == boolean[].class) {
				if (booleanArrayIdx < booleanArrayVals.length) {
					return booleanArrayVals[booleanArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == double[].class) {
				if (doubleArrayIdx < doubleArrayVals.length) {
					return doubleArrayVals[doubleArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == float[].class) {
				if (floatArrayIdx < floatArrayVals.length) {
					return floatArrayVals[floatArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == long[].class) {
				if (longArrayIdx < longArrayVals.length) {
					return longArrayVals[longArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == char[].class) {
				if (charArrayIdx < charArrayVals.length) {
					return charArrayVals[charArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == byte[].class) {
				if (byteArrayIdx < byteArrayVals.length) {
					return byteArrayVals[byteArrayIdx++];
				} else {
					return null;
				}
			} else if (parameterType == short[].class) {
				if (shortArrayIdx < shortArrayVals.length) {
					return shortArrayVals[shortArrayIdx++];
				} else {
					return null;
				}
			} else {
				if (objectArrayIdx < objectArrayVals.length) {
					if (objectArrayVals[objectArrayIdx] == ObjectArrayVal.NULL) {
						objectArrayIdx++;
						return null;
					} else if (objectArrayVals[objectArrayIdx] == ObjectArrayVal.EMPTY) {
						assert (parameterType.getComponentType() != null);
						Object[] objectArray = (Object[]) java.lang.reflect.Array
								.newInstance(parameterType.getComponentType(),
										0);

						objectArrayIdx++;
						return objectArray;
					} else if (objectArrayVals[objectArrayIdx] == ObjectArrayVal.ONE_ELEMENT) {
						assert (parameterType.getComponentType() != null);
						Object[] objectArray = (Object[]) java.lang.reflect.Array
								.newInstance(parameterType.getComponentType(),
										1);
						objectArray[0] = objectCreator
								.createObject(parameterType.getComponentType());

						objectArrayIdx++;
						return objectArray;
					} else {
						objectArrayIdx++;
						assert (false);
						return null;
					}
				} else {
					return null;
				}
			}
		} else if (parameterType.isPrimitive()) {
			if (parameterType == int.class) {
				if (intValIdx < intVals.length) {
					return intVals[intValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == boolean.class) {
				if (booleanValIdx < booleanVals.length) {
					return booleanVals[booleanValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == double.class) {
				if (doubleValIdx < doubleVals.length) {
					return doubleVals[doubleValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == float.class) {
				if (floatValIdx < floatVals.length) {
					return floatVals[floatValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == long.class) {
				if (longValIdx < longVals.length) {
					return longVals[longValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == char.class) {
				if (charValIdx < charVals.length) {
					return charVals[charValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == byte.class) {
				if (byteValIdx < byteVals.length) {
					return byteVals[byteValIdx++];
				} else {
					return null;
				}

			} else if (parameterType == short.class) {
				if (shortValIdx < shortsVal.length) {
					return shortsVal[shortValIdx++];
				} else {
					return null;
				}

			} else {
				assert (false);
				throw new IllegalStateException();
			}
		} else if (parameterType.isEnum()) {
			if (enumValBufferOwner != parameterType) {
				Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) parameterType;
				enumValBuffer = enumClass.getEnumConstants();
			}
			assert (enumValBuffer != null);

			if (0 == enumValIdx) {
				enumValIdx++;
				return null;
			} else if (enumValIdx - 1 < enumValBuffer.length
					&& enumValIdx <= MAX_NUM_OF_ENUM_VALS) {
				;
				return enumValBuffer[enumValIdx++ - 1];
			} else {
				return null;
			}

		} else {
			if (objectValIdx < objectVals.length) {
				if (objectVals[objectValIdx] == ObjectVal.NULL) {
					objectValIdx++;
					return null;
				} else if (objectVals[objectValIdx] == ObjectVal.INSTANCE) {
					objectValIdx++;
					return objectCreator.createObject(parameterType);
				} else {
					objectValIdx++;
					assert (false);
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public boolean hasNext() {
		return hasNext;
	}

	// //////////////////////////////////////////////////////////

	protected ParameterGenerator() {

	}

	/**
	 * 试探构造函数的过程中会大量使用到ParameterGenerator，为了优化，
	 * 使用一个栈将用过的ParameterGenerator保存起来，以便以后再用。
	 */
	private static Stack<ParameterGenerator> ppgStack = new Stack<ParameterGenerator>();

	/**
	 * 与ppgStack配合，取得一个ParameterGenerator。使用这个
	 * 方法来取ParameterGenerator对象才能完成ParameterGenerator 的使用优化。
	 * 
	 * @return 一个ParameterStack对象
	 */
	public synchronized static ParameterGenerator getParameterGenerator() {
		if (ppgStack.isEmpty()) {
			return new ParameterGenerator();
		} else {
			ParameterGenerator ppg = ppgStack.pop();
			ppg.reset();
			return ppg;
		}
	}

	/**
	 * 当用完了一个ParameterGenerator对象后，应该通过这个方法
	 * 来释放，这样才能使ParameterGenererator对象被重复利用。
	 * 
	 * @param ppg
	 *            要释放的ParameterGenerator对象
	 */
	public synchronized static void disposeParameterGenerator(
			ParameterGenerator ppg) {
		if (null == ppg) {
			throw new NullPointerException("ppg should not be null");
		}

		ppgStack.push(ppg);
	}

}
