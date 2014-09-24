package com.nhn.pinpoint.rpc.control;

/**
 * @author koo.taejin
 */
public class ControlMessageProtocolConstant {

	public static final int TYPE_CHARACTER_NULL = 'N';

	public static final int TYPE_CHARACTER_BOOL_TRUE = 'T';

	public static final int TYPE_CHARACTER_BOOL_FALSE = 'F';

	public static final int TYPE_CHARACTER_INT = 'I';

	public static final int TYPE_CHARACTER_LONG = 'L';

	public static final int TYPE_CHARACTER_DOUBLE = 'D';

	public static final int TYPE_CHARACTER_STRING = 'S';

	public static final int CONTROL_CHARACTER_LIST_START = 'V';

	public static final int CONTROL_CHARACTER_LIST_END = 'z';

	public static final int CONTROL_CHARACTER_MAP_START = 'M';

	public static final int CONTROL_CHARACTER_MAP_END = 'z';

}
