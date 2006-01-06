package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import runtime.AbstractMembrane;
import runtime.Atom;
import runtime.Env;
import runtime.Functor;
import runtime.Link;

/**
 * @author mizuno
 * ���ѥ桼�ƥ���ƥ��᥽�åɡ�����򽸤᤿���饹
 */
abstract public class Util {
	public static Functor DOT = new Functor(".", 3);
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
	/**
	 * LMNtal �ꥹ�Ȥ򤦤��ȤꡢObject����ˤ����֤����ꥹ�ȤϾä��ʤ���
	 * @param link
	 * @return
	 */
	public static String[] arrayOfList(Link link, String s) {
		Object v [] = arrayOfList(link);
		String vs[] = new String[v.length];
		for(int i=0;i<v.length;i++) {
			vs[i] = (String)v[i];
		}
		return vs;
	}
	public static Object[] arrayOfList(Link link) {
		List l = new ArrayList();
		while(true) {
			Atom a = link.getAtom();
			if(!a.getFunctor().equals(DOT)) break;
//			System.out.println(a);
//			System.out.println(a.getArg(0).getAtom().getFunctor().getValue().getClass());
			l.add(a.getArg(0).getAtom().getFunctor().getValue());
			link = a.getArg(1);
		}
//		System.out.println("list = "+l);
		return l.toArray();
	}
	
	/**
	 * ���ꤵ�줿ʸ�����ɽ��ʸ�����ƥ��Υƥ�����ɽ����������롣
	 * �ü�ʸ���� quoter �򡢥Хå�����å���ǥ��������פ��롣
	 * @param text �Ѵ�����ʸ����
	 * @param quoter ��������ʸ�������̤� " �� ' ��Ȥ���
	 * @return �Ѵ����ʸ����
	 */ 
	public static String quoteString(String text, char quoter) {
		text = text.replaceAll("\\\\", "\\\\\\\\");
		text = text.replaceAll("" + quoter, "\\\\" + quoter);
		text = text.replaceAll("\n", "\\\\n");
		text = text.replaceAll("\t", "\\\\t");
		text = text.replaceAll("\f", "\\\\f");
		text = text.replaceAll("\r", "\\\\r");
		return quoter + text + quoter;
	}
	
	public static long getTime(){
		if(Env.majorVersion==1 &&Env.minorVersion>4)
	        return System.nanoTime();
		else
	        return System.currentTimeMillis();
	}
}
