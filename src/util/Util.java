package util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author mizuno
 * ���ѥ桼�ƥ���ƥ��᥽�åɡ�����򽸤᤿���饹
 */
abstract public class Util {
	public static final Iterator NULL_ITERATOR = (new ArrayList()).iterator();
	public static void systemError(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}
