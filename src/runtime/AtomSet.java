package runtime;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.Util;

/**
 * �A�g���̏W�����Ǘ����邽�߂̃N���X�B
 * �v�f��AbstractAtom�N���X�����̎q���̃N���X�̃C���X�^���X�݂̂Ɖ��肷��B
 * Functor���L�[�Ƃ��A����Functor�����A�g���̏W����l�Ƃ���Map���g���āA
 * Functor���ɃA�g�����Ǘ����Ă���B
 * @author Mizuno
 */
public final class AtomSet implements Set{
	/** atoms���̃A�g���̐��B�������v���� */
	private int size = 0;
	/** ���ۂɃA�g���̏W�����Ǘ����Ă���ϐ� */
	private Map atoms = new HashMap();

	/** �A�g�����̎擾 */	
	public int size() {
		return size;
	}
	/** ��̏ꍇ��true */
	public boolean isEmpty() {
		return size == 0;
	}
	/** �^����ꂽ�A�g�������̏W�����ɂ���ꍇ��true�B */
	public boolean contains(Object o) {
		Functor f = ((Atom)o).getFunctor();
		Set s = (Set)atoms.get(f);
		if (s == null) {
			return false;
		} else {
			return s.contains(o);
		}
	}

//2003/10/22 Mizuno ���̃��\�b�h�͂�߂āAiteratorOfFunctor���g���悤�ɂ���B
//	/**
//	 * �^����ꂽFunctor�����A�g���̏W����Ԃ��B
//	 * ���̂悤�ȃA�g�����Ȃ��ꍇ�͋�̏W����Ԃ��B
//	 * �Ԃ��ꂽ�W���ɒ���add/remove���̑��삵�Ȃ����ƁB
//	 */	
//	public Set getAtomsOfFunctor(Functor f) {
//		Set s = (Set)atoms.get(f);
//		if (s == null) {
//			return new HashSet();
//		} else {
//			return s;
//		}
//	}
	/** ���̏W�����ɂ���A�g���̔����q��Ԃ� */
	public Iterator iterator() {
		return new AtomIterator(atoms);
	}
	/** �^����ꂽ���O�����A�g���̔����q��Ԃ� */
	public Iterator iteratorOfFunctor(Functor functor) {
		Set s = (Set)atoms.get(functor);
		if (s == null) {
			return Util.NULL_ITERATOR;
		} else {
			return s.iterator();
		}
	}
	/** 
	 * Functor�̔����q��Ԃ��B
	 * ���̏W�����ɂ���A�g����Functor�͑S�Ă��̔����q���g���Ď擾�ł��邪�A
	 * ���̔����q�Ŏ擾�ł���Functor�����A�g�����K�����̏W�����ɂ���Ƃ͌���Ȃ��B
	 */
	public Iterator functorIterator() {
		return atoms.keySet().iterator();
	}
	/**
	 * ���̏W�����̑S�ẴA�g�����i�[����Ă���z���Ԃ��B
	 * �Ԃ����z��̎��s���̌^��AbstractAtom[]�ł��B
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
	 * ���̏W�����̑S�ẴA�g�����i�[����Ă���z���Ԃ��B
	 * �Ԃ����z��̎��s���̌^�͈����ɓn���ꂽ�z��̎��s���̌^�Ɠ����ł��B
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
	 * �w�肳�ꂽ�A�g����ǉ�����B
	 * @return ���̏W�����ύX���ꂽ�ꍇ��true
	 */
	public boolean add(Object o) {
		Functor f = ((Atom)o).getFunctor();
		Set s = (Set)atoms.get(f);
		if (s == null) {
			s = new HashSet();
			s.add(o);
			atoms.put(f, s);
			size++;
			return true;
		} else if (s.add(o)) {
			size++;
			return true;
		} else {
			return false;
		}
	}
	/**
	 * �w�肳�ꂽ�A�g��������Ώ�������B
	 * @return ���̏W�����ύX���ꂽ�ꍇ��true
	 */
	public boolean remove(Object o) {
		Functor f = ((Atom)o).getFunctor();
		Set s = (Set)atoms.get(f);
		if (s == null) {
			return false;
		} else if (s.remove(o)) {
			size--;
			return true;
		} else {
			return false;
		}
	}
	/**
	 * �w�肳�ꂽ�R���N�V�������̑S�Ă̗v�f���܂܂��ꍇ�ɂ�true��Ԃ��B
	 * ���݂͌����̈������������Ă���̂ŁA�����ύX����K�v������B 
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
	 * �w�肳�ꂽ�R���N�V�������̑S�Ă̗v�f�����̏W���ɒǉ�����B
	 * ���݂͌����̈������������Ă���̂ŁA�����ύX����K�v������B 
	 * @return ���̏W�����ύX���ꂽ�ꍇ��true
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
	 * �w�肳�ꂽ�R���N�V�������̑S�Ă̗v�f�����̏W�����珜������B
	 * ���݂͌����̈������������Ă���̂ŁA�����ύX����K�v������B 
	 * @return ���̏W�����ύX���ꂽ�ꍇ��true
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
	/** �T�|�[�g���Ȃ� */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/** �S�Ă̗v�f���������� */
	public void clear() {
		atoms.clear();
		size = 0;
	}
}
/** AtomSet�̗v�f�ɑ΂��Ďg�p���锽���q */
final class AtomIterator implements Iterator {
	/** Functor���L�[�Ƃ��A�A�g���̏W���iSet�j��l�Ƃ���Map */
	Map atoms;
	/** ����Functor�����A�g���̏W���̔����q */
	Iterator atomSetIterator;
	/** ����Functor�����A�g����񋓂��锽���q�B */
	Iterator atomIterator;

	/** �w�肳�ꂽMap���ɂ���A�g����񋓂��锽���q�𐶐����� */
	AtomIterator(Map atoms) {
		this.atoms = atoms;
		atomSetIterator = atoms.values().iterator();
		if (atomSetIterator.hasNext()) {
			atomIterator = ((Set)atomSetIterator.next()).iterator();
		} else {
			atomIterator = Util.NULL_ITERATOR;
		}
	}
	public boolean hasNext() {
		while (atomIterator.hasNext() == false) {
			if (atomSetIterator.hasNext() == false) {
				return false;
			}
			atomIterator = ((Set)atomSetIterator.next()).iterator();
		}
		return true;
	}
	public Object next() {
		while (atomIterator.hasNext() == false) {
			// �Ō�܂ŗ��Ă����ꍇ�A������NoSuchElementException����������
			atomIterator = ((Set)atomSetIterator.next()).iterator();
		}
		return atomIterator.next();
	}
	/** �T�|�[�g���Ȃ��̂ŁAUnsupportedOperationException�𓊂��� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
