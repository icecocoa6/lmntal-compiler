package runtime;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import util.MultiMapIterator;
import util.NestedIterator;
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
	/** ���������̺︺�Τ��ᡢ�ǡ������ȥ�ϤޤȤ�ƴ��� */
	private ArrayList dataAtoms = new ArrayList();
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
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			ArrayList l = (ArrayList)(f.isOUTSIDE_PROXY() ? outs.get(f) : atoms.get(f));
			if (l == null) {
				return 0;
			} else {
				return l.size();
			}
		} else {
			Iterator it = new DataAtomIterator(f);
			int size = 0;
			while (it.hasNext()) {
				size++;
			}
			return size;
		}
	}
	/** OUTSIDE_PROXY�ο��μ��� */
	public int getOutCount() {
		Iterator i = outs.values().iterator();
		int k=0;
		while(i.hasNext()){
			k += ((ArrayList)i.next()).size();
		}
		return k;
	}
	/** �����ɤ������֤� */
	public boolean isEmpty() {
		return size == 0;
	}
	/** ���ν�����ˤ��륢�ȥ��ȿ���Ҥ��֤� */
	public Iterator iterator() {
		return new NestedIterator(new Iterator[] {dataAtoms.iterator(),
													new MultiMapIterator(outs),
													new MultiMapIterator(atoms)});
	}
	
	/** Ϳ����줿̾������ĥ��ȥ��ȿ���Ҥ��֤� */
	public Iterator iteratorOfFunctor(Functor f) {
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			ArrayList l = (ArrayList)(f.isOUTSIDE_PROXY() ? outs.get(f) : atoms.get(f));
			if (l == null) {
				return Util.NULL_ITERATOR;
			} else {
				return l.iterator();
			}
		} else {
			return new DataAtomIterator(f);
		}
	}
	/** OUTSIDE_PROXY��ȿ������֤� */
	public Iterator iteratorOfOUTSIDE_PROXY() {
		return new MultiMapIterator(outs);
	}
	/** 
	 * �����ƥ��֥��ȥ��Functor��ȿ���Ҥ��֤���
	 * ���ν�����ˤ��륢�ȥ��Functor�����Ƥ���ȿ���Ҥ�ȤäƼ����Ǥ��뤬��
	 * ����ȿ���ҤǼ����Ǥ���Functor����ĥ��ȥबɬ�����ν�����ˤ���Ȥϸ¤�ʤ���
	 * 
	 * todo remove���줿���ˡ����ȥ����0�ˤʤä�Functor�����褦�ˤ��������ɤ�����
	 * (n-kato) ���gc�᥽�åɡʥ��ԡ�GC�ˤ�������id�ο���ľ����Ԥ�ͽ��ˤ�Ǥ�������֤��Ƥ����Ȼפ��ޤ���
	 * ��������gc�᥽�åɤϸ��߸ƤФ�ޤ��󤱤ɡ�
	 */
	public Iterator activeFunctorIterator() {
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
		ArrayList l;
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			Map map = f.isOUTSIDE_PROXY() ? outs : atoms;
			l = (ArrayList)map.get(f);
			if (l == null) {
				l = new ArrayList();
				map.put(f, l);
			}
		} else {
			l = dataAtoms;
		}
		l.add(o);
		((Atom)o).index = l.size() - 1;
		size++;
		return true;
	}
	/**
	 * ���ꤵ�줿���ȥब����н���롣
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean remove(Object o) {
		Atom a = (Atom)o;
		Functor f = a.getFunctor();
		ArrayList l;
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			l = (ArrayList)(f.isOUTSIDE_PROXY() ? outs.get(f) : atoms.get(f));
		} else {
			l = dataAtoms;
		}
		if (a.index == l.size() - 1) {
			l.remove(l.size() - 1);
		} else {
			Atom t = (Atom)l.remove(l.size() - 1);
			l.set(a.index, t);
			t.index = a.index;
		}
		a.index = -1;
		size--;
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
	/** ���Ƥ����Ǥ����� */
	protected void clear() {
		atoms.clear();
		dataAtoms.clear();
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
	
	/** dataAtoms �������Υե��󥯥��������֤�ȿ���� */
	private class DataAtomIterator implements Iterator {
		private Iterator it;
		private Atom next;
		private Functor functor;
		DataAtomIterator(Functor functor) {
			this.functor = functor;
			it = dataAtoms.iterator();
			setNext();
		}
		private void setNext() {
			while (true) {
				if (!it.hasNext()) {
					next = null;
					break;
				}
				next = (Atom)it.next();
				if (next.getFunctor().equals(functor)) {
					break;
				}
			}
		}
		public boolean hasNext() {
			return next != null;
		}
		public Object next() {
			Atom ret = next;
			setNext();
			return ret;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public void gc() {
		Iterator it = atoms.keySet().iterator();
		while (it.hasNext()) {
			Functor f = (Functor)it.next();
			if (((ArrayList)atoms.get(f)).size() == 0)
				it.remove();
		}
	}
	/////////////////////////////////
	// non deterministic LMNtal
	private final Comparator sizeComparator = new Comparator() {
		public int compare(Object f0, Object f1) {
			return ((ArrayList)atoms.get(f0)).size() - ((ArrayList)atoms.get(f1)).size();
		}
	};
	private ArrayList startAtoms; 
	/**
	 * ���Υ��ȥॻ�åȤ��⤦�ѹ�����ʤ�����������롣
	 * ���ȥॻ�åȴ֤���Ӥ�Ԥ��ݤ��������ȡ��ϥå��女���ɤη׻���Ԥ���
	 */
	public void freeze() {
		gc();
		//��ӤΤ���ν���
		ArrayList funcs = new ArrayList();
		funcs.addAll(atoms.keySet());
		Collections.sort(funcs, sizeComparator);
		HashSet checked = new HashSet();
		startAtoms = new ArrayList();
		for (int i = 0; i < funcs.size(); i++) {
			searchAtomGroup((ArrayList)atoms.get(funcs.get(i)), checked);
		}
		searchAtomGroup(dataAtoms, checked);

		calcHashCode();
	}
	private void searchAtomGroup(ArrayList l, HashSet checked) {
		for (int j = 0; j < l.size(); j++) {
			Atom a = (Atom)l.get(j);
			if (!checked.contains(a)) {
				searchAtomGroup(a, checked);
				startAtoms.add(a);
				}
		}
	}
	private void searchAtomGroup(Atom a, HashSet checked) {
		checked.add(a);
		for (int i = 0; i < a.getArity(); i++) {
			Atom a2 = a.nthAtom(i);
			if (!checked.contains(a2))
				searchAtomGroup(a2, checked);
		}
	}
	
	public boolean equals(Object o) {
		AtomSet s = (AtomSet)o;
		if (size() != s.size()) return false;
		if (atoms.size() != s.atoms.size()) return false;
		if (dataAtoms.size() != s.dataAtoms.size()) return false;

		HashSet checked2 = new HashSet();
		HashMap map = new HashMap();
		for (int i = 0; i < startAtoms.size(); i++) {
			Atom a1 = (Atom)startAtoms.get(i);
			Functor f = a1.getFunctor();
			ArrayList l2;
			if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
				l2 = (ArrayList)s.atoms.get(f);
				if (l2 == null || ((ArrayList)atoms.get(f)).size() != l2.size()) return false;
			} else {
				l2 = s.dataAtoms;
			}
			boolean flg = false;
			for (int j = 0; j < l2.size(); j++) {
				if (checked2.contains(l2.get(j))) continue;
				map.clear();
				if (compare(a1, (Atom)l2.get(j), map, checked2)) {
					flg = true;
					checked2.addAll(map.values());
					break;
				}
			}
			if (!flg) return false;
		}
		return true;
	}
	private boolean compare(Atom a1, Atom a2, HashMap map, HashSet checked2) {
		if (!a1.getFunctor().equals(a2.getFunctor()))
			return false;
		if (map.containsKey(a1)) {
			return a2 == map.get(a1);
		}
		if (checked2.contains(a2)) throw new RuntimeException();
		map.put(a1, a2);
		for (int i = 0; i < a1.getArity(); i++) {
			if (!compare(a1.nthAtom(i), a2.nthAtom(i), map, checked2))
				return false;
		}
		return true;
	}

	/*--------------------�ϥå��女����--------------*/
	private int hashCode;
	private void calcHashCode() {
		hashCode = 0;
		Iterator it = atoms.values().iterator();
		while (it.hasNext()) {
			ArrayList l = (ArrayList)it.next();
			for (int i = 0; i < l.size(); i++) {
				Atom a = (Atom)l.get(i);
				int t = a.getFunctor().hashCode();
				for (int j = 0; j < a.getArity(); j++) {
					t = t * 31 + a.nthAtom(j).getFunctor().hashCode();
				}
				hashCode += t;
			}
		}
	}
	public int hashCode() {
		return hashCode;
	}
}
