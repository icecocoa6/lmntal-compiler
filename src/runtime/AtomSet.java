package runtime;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import util.Util;
import util.RandomIterator;

/**
 * ���ȥ�ν����������뤿��Υ��饹��
 * ���Ǥ�Atom���饹�Υ��󥹥��󥹤ΤߤȲ��ꤹ�롣
 * Functor�򥭡��Ȥ�������Functor����ĥ��ȥ�ν�����ͤȤ���Map��Ȥäơ�
 * Functor��˥��ȥ��������Ƥ��롣
 * @author Mizuno
 */
public final class AtomSet implements Set{
	/** atoms��Υ��ȥ�ο�������������� */
	private int size = 0;
	/** �ºݤ˥��ȥ�ν����������Ƥ����ѿ� */
	private Map atoms = new HashMap();

	/** ���ȥ���μ��� */
	public int size() {
		return size;
	}
	/** ��ͳ��󥯴������ȥ�ʳ��Υ��ȥ�ο��μ��� */
	public int getNormalAtomCount() {
		return size - getAtomCountOfFunctor(Functor.INSIDE_PROXY)
					 - getAtomCountOfFunctor(Functor.OUTSIDE_PROXY);
	}
	/** ���ꤵ�줿Functor����ĥ��ȥ�ο��μ��� */
	public int getAtomCountOfFunctor(Functor f) {
		List s = (List)atoms.get(f);
		if (s == null) {
			return 0;
		} else {
			return s.size();
		}
	}
	/** �����ɤ������֤� */
	public boolean isEmpty() {
		return size == 0;
	}
	/** Ϳ����줿���ȥब���ν�����ˤ��뤫�ɤ������֤� */
	public boolean contains(Object o) {
		return ((Atom)o).mem.atoms == this;
//		Functor f = ((Atom)o).getFunctor();
//		Set s = (Set)atoms.get(f);
//		if (s == null) {
//			return false;
//		} else {
//			return s.contains(o);
//		}
	}

	/** ���ν�����ˤ��륢�ȥ��ȿ���Ҥ��֤� */
	public Iterator iterator() {
		return new AtomIterator(atoms);
	}
	/** Ϳ����줿̾������ĥ��ȥ��ȿ���Ҥ��֤� */
	public Iterator iteratorOfFunctor(Functor functor) {
		List s = (List)atoms.get(functor);
		if (s == null) {
			return Util.NULL_ITERATOR;
		} else {
			if (Env.fRandom) {
				return new RandomIterator(s);
			} else {
				return s.iterator();
			}
		}
	}
	/** 
	 * Functor��ȿ���Ҥ��֤���
	 * ���ν�����ˤ��륢�ȥ��Functor�����Ƥ���ȿ���Ҥ�ȤäƼ����Ǥ��뤬��
	 * ����ȿ���ҤǼ����Ǥ���Functor����ĥ��ȥबɬ�����ν�����ˤ���Ȥϸ¤�ʤ���
	 * TODO remove���줿���ˡ����ȥ����0�ˤʤä�Functor�����褦�ˤ��������ɤ�����
	 */
	public Iterator functorIterator() {
		return atoms.keySet().iterator();
	}
	/**
	 * ���ν���������ƤΥ��ȥफ��Ǽ����Ƥ���������֤���
	 * �֤��������μ¹Ի��η���Atom[]�Ǥ���
	 */
	public Object[] toArray() {
		Object[] ret = new Atom[size];
		int index = 0;
		Iterator it = iterator();
		while (it.hasNext()) {
			ret[index++] = it.next();
		}
		if (index != size) {
			System.err.println("SYSTEM ERROR!: AtomSet.size is incorrect");
		}
		return ret;
	}
	/**
	 * ���ν���������ƤΥ��ȥफ��Ǽ����Ƥ���������֤���
	 * �֤��������μ¹Ի��η��ϰ������Ϥ��줿����μ¹Ի��η���Ʊ���Ǥ���
	 */
	public Object[] toArray(Object[] a) {
		if (a.length < size) {
			a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
		}
		int index = 0;
		Iterator it = iterator();
		while (it.hasNext()) {
			a[index++] = it.next();
		}
		while (index < size) {
			a[index++] = null;
		}
		return a;
	}
	/**
	 * ���ꤵ�줿���ȥ���ɲä��롣
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean add(Object o) {
		Functor f = ((Atom)o).getFunctor();
		List s = (List)atoms.get(f);
		if (s == null) {
			s = new ArrayList();
			((Atom)o).index = 0;
			s.add(o);
			atoms.put(f, s);
			size++;
			return true;
		} else {
//			if (contains(o) && ((Atom)o).index != -1) {
//				return false;
//			}
			((Atom)o).index = s.size();
			s.add(o);
			size++;
			return true;
		}
	}
	/**
	 * ���ꤵ�줿���ȥब����н���롣
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean remove(Object o) {
		Functor f = ((Atom)o).getFunctor();
		List s = (List)atoms.get(f);
		if (!contains(o)) {
			return false;
		} else {
			Atom a = (Atom)s.get(s.size() - 1);
			s.set(((Atom)o).index, a);
			s.remove(s.size() - 1);
			a.index = ((Atom)o).index;
			((Atom)o).index = -1;
			size--;
			return true;
		}
	}
	/**
	 * ���ꤵ�줿���쥯�����������Ƥ����Ǥ��ޤޤ����ˤ�true���֤���
	 * <p>���ߤϸ�Ψ�ΰ��������򤷤Ƥ���Τǡ����ˤ˻Ȥ��ʤ��ѹ�����ɬ�פ����롣 
	 */
	public boolean containsAll(Collection c) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			if (contains(it.next()) == false) {
				return false;
			}
		}
		return true;
	}
	/**
	 * ���ꤵ�줿���쥯�����������Ƥ����Ǥ򤳤ν�����ɲä��롣
	 * <p>���ߤϸ�Ψ�ΰ��������򤷤Ƥ���Τǡ����ˤ˻Ȥ��ʤ��ѹ�����ɬ�פ����롣 
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean addAll(Collection c) {
		boolean ret = false;
		Iterator it = c.iterator();
		while (it.hasNext()) {
			if (add(it.next())) {
				ret = true;
			}
		}
		return ret;
	}
	/**
	 * ���ꤵ�줿���쥯�����������Ƥ����Ǥ򤳤ν��礫�����롣
	 * <p>���ߤϸ�Ψ�ΰ��������򤷤Ƥ���Τǡ����ˤ˻Ȥ��ʤ��ѹ�����ɬ�פ����롣 
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean removeAll(Collection c) {
		boolean ret = false;
		Iterator it = c.iterator();
		while (it.hasNext()) {
			if (remove(it.next())) {
				size--;
				ret = true;
			}
		}
		return ret;
	}
	/** ���ݡ��Ȥ��ʤ� */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/** ���Ƥ����Ǥ����� */
	public void clear() {
		Iterator it = iterator();
		while (it.hasNext()) {
			((Atom)it.next()).index = -1;
		}
		atoms.clear();
		size = 0;
	}
	
	/** size���������򸡺����롣�ǥХå��ѡ�*/
	public boolean verify() {
		int n = 0;
		Iterator it = atoms.values().iterator();
		while (it.hasNext()) {
			n += ((List)it.next()).size();
		}
		return size == n;
	}
	/**�ǥХå��ѽ���*/
	public void print() {
		System.out.println("AtomSet: ");
		Iterator it = iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		System.out.println("result of verify() is " + verify());
	}
}

/** AtomSet�����Ǥ��Ф��ƻ��Ѥ���ȿ���� */
final class AtomIterator implements Iterator {
	/** Functor�򥭡��Ȥ������ȥ�ν����Set�ˤ��ͤȤ���Map */
	Map atoms;
	/** ����Functor����ĥ��ȥ�ν����ȿ���� */
	Iterator atomSetIterator;
	/** ����Functor����ĥ��ȥ����󤹤�ȿ���ҡ� */
	Iterator atomIterator;

	/** ���ꤵ�줿Map��ˤ��륢�ȥ����󤹤�ȿ���Ҥ��������� */
	AtomIterator(Map atoms) {
		this.atoms = atoms;
		atomSetIterator = atoms.values().iterator();
		if (atomSetIterator.hasNext()) {
			atomIterator = ((List)atomSetIterator.next()).iterator();
		} else {
			atomIterator = Util.NULL_ITERATOR;
		}
	}
	public boolean hasNext() {
		while (atomIterator.hasNext() == false) {
			if (atomSetIterator.hasNext() == false) {
				return false;
			}
			atomIterator = ((List)atomSetIterator.next()).iterator();
		}
		return true;
	}
	public Object next() {
		while (atomIterator.hasNext() == false) {
			// �Ǹ�ޤ���Ƥ�����硢������NoSuchElementException��ȯ������
			atomIterator = ((List)atomSetIterator.next()).iterator();
		}
		return atomIterator.next();
	}
	/** ���ݡ��Ȥ��ʤ��Τǡ�UnsupportedOperationException���ꤲ�� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
