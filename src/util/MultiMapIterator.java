package util;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * �ޥåפ��ͤ˳�Ǽ���줿���쥯�������Υǡ�������󤹤�ȿ���ҡ�
 * ʣ�����Ǥ��Ǽ����ޥåפ�¸����뤿��ˡ��ͤ˥��쥯��������������ޥåפ��Ф������Ѥ��롣
 * @author Mizuno
 */
public class MultiMapIterator<E> implements Iterator<E>{
	private Iterator<List<E>> setIterator;
	private Iterator<E> dataIterator;
	private E next;
	private void setNext() {
		while (!dataIterator.hasNext()) {
			if (!setIterator.hasNext()) {
				next = null;
				return;
			}
			dataIterator = setIterator.next().iterator();
		}
		next = dataIterator.next();
	}

	/** ���ꤵ�줿Map��ˤ���ǡ�������󤹤�ȿ���Ҥ��������� */
	public <K> MultiMapIterator(Map<K, List<E>> map) {
		setIterator = map.values().iterator();
		dataIterator = Util.NULL_ITERATOR;
		setNext();
	}
	public boolean hasNext() {
		return next != null;
	}
	public E next() {
		if (next == null)
			throw new NoSuchElementException();
		E ret = next;
		setNext();
		return ret;
	}
	/** ���ݡ��Ȥ��ʤ��Τǡ�UnsupportedOperationException���ꤲ�� */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
