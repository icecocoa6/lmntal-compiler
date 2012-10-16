package runtime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import runtime.functor.Functor;
import runtime.functor.SpecialFunctor;

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
public final class AtomSet implements Iterable<Atom>
{
	/** atoms��Υ��ȥ�ο�������������� */
	private int size = 0;
	/** �ºݤ˥��ȥ�ν����������Ƥ����ѿ� */
	private Map<Functor, List<Atom>> atoms = null;
	/** ���������̺︺�Τ��ᡢ�ǡ������ȥ�ϤޤȤ�ƴ��� */
	private List<Atom> dataAtoms = null;
	/** OUTSIDE_PROXY�ν����������Ƥ����ѿ� */
	private Map<Functor, List<Atom>> outs = null;
	
	private List<Atom> startAtoms = null; 

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
			List<Atom> l = (f.isOutsideProxy() ? getOuts().get(f) : getAtoms().get(f));
			if (l == null) {
				return 0;
			} else {
				return l.size();
			}
		} else {
			Iterator<Atom> it = new DataAtomIterator(f);
			int size = 0;
			while (it.hasNext()) {
				size++;
			}
			return size;
		}
	}
	/** OUTSIDE_PROXY�ο��μ��� */
	public int getOutCount() {
		Iterator<List<Atom>> i = getOuts().values().iterator();
		int k=0;
		while(i.hasNext()){
			k += i.next().size();
		}
		return k;
	}
	/** �����ɤ������֤� */
	public boolean isEmpty() {
		return size == 0;
	}
	/** ���ν�����ˤ��륢	�ȥ��ȿ���Ҥ��֤� */

	@SuppressWarnings("unchecked")
	public Iterator<Atom> iterator() {
		return new NestedIterator<Atom>(new Iterator[] {getDataAtoms().iterator(),
				new MultiMapIterator<Atom>(getOuts()),
				new MultiMapIterator<Atom>(getAtoms())});
		 
	}
	
	
	/** Ϳ����줿̾������ĥ��ȥ��ȿ���Ҥ��֤� */
	public Iterator<Atom> iteratorOfFunctor(Functor f) {
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			List<Atom> l = (f.isOutsideProxy() ? getOuts().get(f) : getAtoms().get(f));
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
	public Iterator<Atom> iteratorOfOUTSIDE_PROXY() {
		return new MultiMapIterator<Atom>(getOuts());
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
	public Iterator<Functor> activeFunctorIterator() {
		return getAtoms().keySet().iterator();
	}
	/**
	 * ���ν���������ƤΥ��ȥफ��Ǽ����Ƥ���������֤���
	 * �֤��������μ¹Ի��η���Atom[]�Ǥ���
	 */
	public Object[] toArray() {
		Object[] ret = new Atom[size];
		int index = 0;
		Iterator<Atom> it = iterator();
		while (it.hasNext()) {
			ret[index++] = it.next();
		}
		if (index != size) {
			Util.errPrintln("SYSTEM ERROR!: AtomSet.size is incorrect");
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
		Iterator<Atom> it = iterator();
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
	public boolean add(Atom atom) {
		Functor f = atom.getFunctor();
		List<Atom> l;
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			Map<Functor, List<Atom>> map = f.isOutsideProxy() ? getOuts() : getAtoms();
			l = map.get(f);
			if (l == null) {
				l = new ArrayList<Atom>();
				map.put(f, l);
			}
		} else {
			l = getDataAtoms();
		}
		l.add(atom);
		atom.index = l.size() - 1;
		size++;
		return true;
	}
	/**
	 * ���ꤵ�줿���ȥब����н���롣
	 * @return ���ν��礬�ѹ����줿����true
	 */
	public boolean remove(Atom atom) {
		Atom a = atom;
		Functor f = a.getFunctor();
		List<Atom> l;
		
		if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
			l = f.isOutsideProxy() ? getOuts().get(f) : getAtoms().get(f);
		} else {
			l = getDataAtoms();
		}
		if (a.index == l.size() - 1) {
			l.remove(l.size() - 1);
			if(l.isEmpty()){ l = null; }
		} else {
			Atom t = l.remove(l.size() - 1);
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
	public boolean addAll(Collection<Atom> c) {
		boolean ret = false;
		Iterator<Atom> it = c.iterator();
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
	public boolean removeAll(Collection<Atom> c) {
		boolean ret = false;
		Iterator<Atom> it = c.iterator();
		while (it.hasNext()) {
			ret |= remove(it.next());
			//remove�᥽�åɤ����size���ѹ����Ƥ���
		}
		return ret;
	}
	/** ���Ƥ����Ǥ����� */
	protected void clear() {
		atoms = null;
		dataAtoms = null;
		outs = null;
		size = 0;
	}
	
	/**�ǥХå��ѽ���*/
	public void print() {
		Util.println("AtomSet: ");
		Iterator<Atom> it = iterator();
		while (it.hasNext()) {
			Util.println(it.next());
		}
	}
	
	/** dataAtoms �������Υե��󥯥��������֤�ȿ���� */
	private class DataAtomIterator implements Iterator<Atom> {
		private Iterator<Atom> it;
		private Atom next;
		private Functor functor;
		DataAtomIterator(Functor functor) {
			this.functor = functor;
			it = getDataAtoms().iterator();
			setNext();
		}
		private void setNext() {
			while (true) {
				if (!it.hasNext()) {
					next = null;
					break;
				}
				next = it.next();
				if (next.getFunctor().equals(functor)) {
					break;
				}
			}
		}
		public boolean hasNext() {
			return next != null;
		}
		public Atom next() {
			Atom ret = next;
			setNext();
			return ret;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public void gc() {
		Iterator<Functor> it = getAtoms().keySet().iterator();
		while (it.hasNext()) {
			Functor f = it.next();
			if (atoms.get(f).size() == 0)
				it.remove();
		}
	}
	
	/**
	 * ���Υ��ȥॻ�åȤ��⤦�ѹ�����ʤ�����������롣
	 * ���ȥॻ�åȴ֤���Ӥ�Ԥ��ݤ��������ȡ��ϥå��女���ɤη׻���Ԥ���
	 */
	public void freeze() {
		gc();
		/////////////////////////////////
		// non deterministic LMNtal
		Comparator<Functor> sizeComparator = new Comparator<Functor>() {
			public int compare(Functor f0, Functor f1) {
				return getAtoms().get(f0).size() - atoms.get(f1).size();
			}
		};
		//��ӤΤ���ν���
		List<Functor> funcs = new ArrayList<Functor>();
		funcs.addAll(getAtoms().keySet());
		Collections.sort(funcs, sizeComparator);
		HashSet<Atom> checked = new HashSet<Atom>();
		startAtoms = new ArrayList<Atom>();
		for (int i = 0; i < funcs.size(); i++) {
			searchAtomGroup(atoms.get(funcs.get(i)), checked);
		}
		searchAtomGroup(getDataAtoms(), checked);

		calcHashCode();
	}
	private void searchAtomGroup(List<Atom> name, HashSet<Atom> checked) {
		for (int j = 0; j < name.size(); j++) {
			Atom a = name.get(j);
			if (!checked.contains(a)) {
				searchAtomGroup(a, checked);
				startAtoms.add(a);
				}
		}
	}
	private void searchAtomGroup(Atom a, HashSet<Atom> checked) {
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
		if (getAtoms().size() != s.atoms.size()) return false;
		if (getDataAtoms().size() != s.dataAtoms.size()) return false;

		HashSet<Atom> checked2 = new HashSet<Atom>();
		HashMap<Atom, Atom> map = new HashMap<Atom, Atom>();
		for (int i = 0; i < startAtoms.size(); i++) {
			Atom a1 = (Atom)startAtoms.get(i);
			Functor f = a1.getFunctor();
			List<Atom> l2;
			if (!Env.fMemory || f.isSymbol() || f instanceof SpecialFunctor) {
				l2 = s.atoms.get(f);
				if (l2 == null || atoms.get(f).size() != l2.size()) return false;
			} else {
				l2 = s.dataAtoms;
			}
			boolean flg = false;
			for (int j = 0; j < l2.size(); j++) {
				if (checked2.contains(l2.get(j))) continue;
				map.clear();
				if (compare(a1, l2.get(j), map, checked2)) {
					flg = true;
					checked2.addAll(map.values());
					break;
				}
			}
			if (!flg) return false;
		}
		return true;
	}
	private boolean compare(Atom a1, Atom a2, HashMap<Atom, Atom> map, HashSet<Atom> checked2) {
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
		Iterator<List<Atom>> it = getAtoms().values().iterator();
		while (it.hasNext()) {
			List<Atom> l = it.next();
			for (int i = 0; i < l.size(); i++) {
				Atom a = l.get(i);
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
	private Map<Functor, List<Atom>> getOuts() {
		if(null == outs){ outs = new HashMap<Functor, List<Atom>>(); }
		return outs;
	}
	private Map<Functor, List<Atom>> getAtoms() {
		if(null == atoms){ atoms = new HashMap<Functor, List<Atom>>(); }
		return atoms;
	}
	private List<Atom> getDataAtoms() {
		if(null == dataAtoms){ dataAtoms = new ArrayList<Atom>(); }
		return dataAtoms;
	}
}
