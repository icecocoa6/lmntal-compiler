package util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ���ꤵ�줿�������ȿ���Ҥ��֤��ͤ����֤�ȿ����
 * @author Mizuno
 */
public class NestedIterator implements Iterator {
	private Iterator[] its;
	private int nextIndex;
	private Iterator it;
	private Object next;
	private void setNext() {
		while (!it.hasNext()) {
			if (nextIndex == its.length) {
				next = null;
				return;
			}
			it = its[nextIndex++];
		}
		next = it.next();
	}

	/** ���ꤵ�줿Map��ˤ���ǡ�������󤹤�ȿ���Ҥ��������� */
	public NestedIterator(Iterator[] its) {
		if (its.length == 0) {
			next = null;
		} else {
			this.its = its;
			it = its[0];
			nextIndex = 1;
			setNext();
		}
	}
	public boolean hasNext() {
		return next != null;
	}
	public Object next() {
		if (next == null)
			throw new NoSuchElementException();
		Object ret = next;
		setNext();
		return ret;
	}
	/** ���ݡ��Ȥ��ʤ��Τǡ�UnsupportedOperationException���ꤲ�� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
