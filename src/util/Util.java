package util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author mizuno
 * �ėp���[�e�B���e�B���\�b�h�E�萔���W�߂��N���X
 */
abstract public class Util {
	public static final Iterator NULL_ITERATOR = (new ArrayList()).iterator();
	public static void systemError(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}
}
