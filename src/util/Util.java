package util;

import runtime.*;
import java.util.*;

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
	
	/**
	 * Ϳ����줿�ꥹ�Ȥ��� LMNtal �ꥹ�Ȥ���������Ϳ����줿�����ˤĤʤ���
	 * 
	 * @param parent
	 * @param pos
	 * @param l
	 */
	public static void makeList(Link link, List l) {
		Iterator it = l.iterator();
		AbstractMembrane mem = link.getAtom().getMem();
		Atom parent=null;
		boolean first=true;
		while(it.hasNext()) {
			Atom c = mem.newAtom(new Functor(".", 3));  // .(Value Next Parent)
			Atom v = mem.newAtom(new Functor(it.next().toString(), 1)); // value(Value)
			mem.newLink(c, 0, v, 0);
			if(first) {
				mem.inheritLink(c, 2, link);
			} else {
				mem.newLink(c, 2, parent, 1);
			}
			parent = c;
			first=false;
		}
		Atom nil = mem.newAtom(new Functor("[]", 1));
		if(first) {
			mem.inheritLink(nil, 0, link);
		} else {
			mem.newLink(nil, 0, parent, 1);
		}
	}
}
