package util;

import java.util.Iterator;
import java.util.Collections;

/**
 * @author mizuno
 * ���ѥ桼�ƥ���ƥ��᥽�åɡ�����򽸤᤿���饹
 */
abstract public class Util {
	public static final Iterator NULL_ITERATOR = Collections.EMPTY_SET.iterator();
	public static void systemError(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}
