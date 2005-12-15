package runtime;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import util.Util;

/**
 * ���ȥ�ν����������뤿��Υ��饹��
 * ���Ǥ�Atom���饹�Υ��󥹥��󥹤ΤߤȲ��ꤹ�롣
 * Functor�򥭡��Ȥ�������Functor����ĥ��ȥ�ν�����ͤȤ���Map��Ȥäơ�
 * Functor��˥��ȥ��������Ƥ��롣
 * @author Mizuno
 */
public final class AtomSet implements Serializable {
	/** atoms��Υ��ȥ�ο�������������� */
	private int size = 0;
	/** �ºݤ˥��ȥ�ν����������Ƥ����ѿ� */
	private Map atoms = new HashMap();
	/** OUTSIDE_PROXY�ν����������Ƥ����ѿ� */
	private Map outs = new HashMap();

	/** ���ȥ���μ��� */
	public int size() {
		return size;
	}
	/** ��ͳ��󥯴������ȥ�ʳ��Υ��ȥ�ο��μ��� */
	public int getNormalAtomCount() {
		return size - getAtomCountOfFunctor(Functor.INSIDE_PROXY)
					 - getOutCount();
	}
	/** ���ꤵ�줿Functor����ĥ��ȥ�ο��μ��� */
	public int getAtomCountOfFunctor(Functor f) {
		Collection c = f.isOUTSIDE_PROXY() ? (Collection)outs.get(f)
										   : (Collection)atoms.get(f);
		if (c == null) {
			return 0;
		} else {
			return c.size();
		}
	}
	/** OUTSIDE_PROXY�ο��μ��� */
	public int getOutCount() {
		Iterator i = outs.values().iterator();
		int k=0;
		while(i.hasNext()){
			k += ((Collection)i.next()).size();
		}
		return k;
	}
	/** �����ɤ������֤� */
	public boolean isEmpty() {
		return size == 0;
	}
	/** Ϳ����줿���ȥब���ν�����ˤ��뤫�ɤ������֤� */
	public boolean contains(Object o) {
		return ((Atom)o).mem.atoms == this;
	}

	/** ���ν�����ˤ��륢�ȥ��ȿ���Ҥ��֤� */
	public Iterator iterator() {
		return new AtomIterator(atoms, outs);
	}
	
	/** Ϳ����줿̾������ĥ��ȥ��ȿ���Ҥ��֤�����°������̵�뤹���� */
//	public Iterator ignorePathIterator(Functor f) {
//		return new AtomIgnorePathIterator(this, f);
//	}
	
	/** Ϳ����줿̾������ĥ��ȥ��ȿ���Ҥ��֤� */
	public Iterator iteratorOfFunctor(Functor f) {
		Collection c = f.isOUTSIDE_PROXY() ? (Collection)outs.get(f)
										   : (Collection)atoms.get(f);
		if (c == null) {
			return Util.NULL_ITERATOR;
		} else {
			return c.iterator();
		}
	}
	/** OUTSIDE_PROXY��ȿ������֤� */
	public Iterator iteratorOfOUTSIDE_PROXY() {
		return new OutIterator(outs);
	}
	/** 
	 * OUTSIDE_PROXY�����Functor��ȿ���Ҥ��֤���
	 * ���ν�����ˤ��륢�ȥ��Functor�����Ƥ���ȿ���Ҥ�ȤäƼ����Ǥ��뤬��
	 * ����ȿ���ҤǼ����Ǥ���Functor����ĥ��ȥबɬ�����ν�����ˤ���Ȥϸ¤�ʤ���
	 * 
	 * todo remove���줿���ˡ����ȥ����0�ˤʤä�Functor�����褦�ˤ��������ɤ�����
	 * (n-kato) ���gc�᥽�åɡʥ��ԡ�GC�ˤ�������id�ο���ľ����Ԥ�ͽ��ˤ�Ǥ�������֤��Ƥ����Ȼפ��ޤ���
	 * ��������gc�᥽�åɤϸ��߸ƤФ�ޤ��󤱤ɡ�
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
		Collection c = f.isOUTSIDE_PROXY() ? (Collection)outs.get(f)
										   : (Collection)atoms.get(f);
		if (c == null) {
			if (Env.shuffle >= Env.SHUFFLE_ATOMS)
				c = new RandomSet();
			else
				c = new HashSet();
			if(f.isOUTSIDE_PROXY())
				outs.put(f, c);
			else
				atoms.put(f, c);
		}
		if (c.add(o)) {
			size++;
			return true;
		} else {
			return false;
		}
	}
	/**
	 * ���ꤵ�줿���ȥब����н���롣
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean remove(Object o) {
		Functor f = ((Atom)o).getFunctor();
		Collection c = f.isOUTSIDE_PROXY() ? (Collection)outs.get(f)
										   : (Collection)atoms.get(f);
		if (c.remove(o)) {
			size--;
			return true;
		} else {
			return false;
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
				//add�᥽�åɤ����size���ѹ����Ƥ���
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
			ret |= remove(it.next());
			//remove�᥽�åɤ����size���ѹ����Ƥ���
		}
		return ret;
	}
	/** ���ݡ��Ȥ��ʤ� */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/** ���Ƥ����Ǥ����� */
	protected void clear() {
		atoms.clear();
		outs.clear();
		size = 0;
	}
	
	/** size���������򸡺����롣�ǥХå��ѡ�*/
	public boolean verify() {
		int n = 0;
		Iterator it = atoms.values().iterator();
		while (it.hasNext()) {
			n += ((RandomSet)it.next()).size();
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
	Map atoms, outs;
	/** ����Functor����ĥ��ȥ�ν����ȿ���� */
	Iterator atomSetIterator, outSetIterator;
	/** ����Functor����ĥ��ȥ����󤹤�ȿ���ҡ� */
	Iterator atomIterator;
	/** atomSetIterator��hasNext()��false�ˤʤä���true */
	boolean atomSet_null = false;
	
	/** ���ꤵ�줿Map��ˤ��륢�ȥ����󤹤�ȿ���Ҥ��������� */
	AtomIterator(Map atoms, Map outs) {
		this.atoms = atoms;
		this.outs = outs;
		atomSetIterator = atoms.values().iterator();
		outSetIterator = outs.values().iterator();
		if (atomSetIterator.hasNext()) {
			atomIterator = ((Collection)atomSetIterator.next()).iterator();
		} else if(outSetIterator.hasNext()) {
			atomSet_null = true;
			atomIterator = ((Collection)outSetIterator.next()).iterator();
		} else {
			atomIterator = Util.NULL_ITERATOR;
		}
	}
	public boolean hasNext() {
		if(atomSet_null)
			return hasNext2();
		while (atomIterator.hasNext() == false) {
			if (atomSetIterator.hasNext() == false) {
				atomSet_null = true;
				return hasNext2();
			}
			atomIterator = ((Collection)atomSetIterator.next()).iterator();
		}
		return true;
	}
	private boolean hasNext2() {
		while (atomIterator.hasNext() == false) {
			if (outSetIterator.hasNext() == false) {
				return false;
			}
			atomIterator = ((Collection)outSetIterator.next()).iterator();
		}
		return true;
	}
	public Object next() {
		while (atomIterator.hasNext() == false) {
			// �Ǹ�ޤ���Ƥ�����硢������NoSuchElementException��ȯ������
			if(atomSet_null)				
				atomIterator = ((Collection)outSetIterator.next()).iterator();
			else if(atomSetIterator.hasNext())
				atomIterator = ((Collection)atomSetIterator.next()).iterator();
			else{
				atomSet_null = true;
				atomIterator = ((Collection)outSetIterator.next()).iterator();
			}
				
		}
		return atomIterator.next();
	}
	/** ���ݡ��Ȥ��ʤ��Τǡ�UnsupportedOperationException���ꤲ�� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}

/** AtomSet��OUTSIDE_PROXY�����Ǥ��Ф��ƻ��Ѥ���ȿ���� */
final class OutIterator implements Iterator {
	/** Functor�򥭡��Ȥ������ȥ�ν����Set�ˤ��ͤȤ���Map */
	Map outs;
	/** ����Functor����ĥ��ȥ�ν����ȿ���� */
	Iterator outSetIterator;
	/** ����Functor����ĥ��ȥ����󤹤�ȿ���ҡ� */
	Iterator outIterator;

	/** ���ꤵ�줿Map��ˤ��륢�ȥ����󤹤�ȿ���Ҥ��������� */
	OutIterator(Map outs) {
		this.outs = outs;
		outSetIterator = outs.values().iterator();
		if (outSetIterator.hasNext()) {
			outIterator = ((Collection)outSetIterator.next()).iterator();
		} else {
			outIterator = Util.NULL_ITERATOR;
		}
	}
	public boolean hasNext() {
		while (outIterator.hasNext() == false) {
			if (outSetIterator.hasNext() == false) {
				return false;
			}
			outIterator = ((Collection)outSetIterator.next()).iterator();
		}
		return true;
	}
	public Object next() {
		while (outIterator.hasNext() == false) {
			// �Ǹ�ޤ���Ƥ�����硢������NoSuchElementException��ȯ������
			outIterator = ((Collection)outSetIterator.next()).iterator();
		}
		return outIterator.next();
	}
	/** ���ݡ��Ȥ��ʤ��Τǡ�UnsupportedOperationException���ꤲ�� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}

//final class AtomIgnorePathIterator implements Iterator {
//	Iterator it;
//	Atom next;
//	Functor f;
//	
//	AtomIgnorePathIterator(AtomSet as, Functor f) {
//		it = as.iterator();
//		this.f = f;
//	}
//	public Object next() {
//		
//	}
//	public boolean hasNext() {
//		while(it.hasNext()) {
//			next = (Atom)it.next();
//			if(next.getFunctor())
//		}
//	}
//	public void remove() {
//		throw new UnsupportedOperationException();
//	}
//}
