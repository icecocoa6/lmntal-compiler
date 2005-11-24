package util;
import java.util.ArrayList;

public final class Stack {
	/** �� */
	private QueuedEntity head;
	/** �ȥå� */
	private QueuedEntity tail;
	
	public Stack() {
		head = new QueuedEntity();
		tail = new QueuedEntity();
		head.prev = tail.prev = head;
		head.next = tail.next = tail;
	}
	public void push(QueuedEntity entity) {
		if (entity.isQueued()) {
			System.out.println("SYSTEM ERROR: enqueued entity is already in a queue");
			entity.dequeue();
		}
		entity.stack = this;
		entity.prev = tail.prev;
		entity.next = tail;
		entity.prev.next = entity;
		tail.prev = entity;
	}
	/** �����å�stack�����Ƥ򤳤Υ����å�����˰�ư���� */
	public void moveFrom(Stack stack) {
		if (stack.isEmpty()) return;
		QueuedEntity oldFirst = head.next;
		QueuedEntity addedLast = stack.tail.prev;

		//��Ƭ
		head.next = stack.head.next;
		stack.head.next.prev = head;
		
		//(�������Ϥ��줿stack��)�Ǹ�
		addedLast.next = oldFirst;
		oldFirst.prev = addedLast;
		stack.head.next = stack.tail;
		stack.tail.prev = stack.head;

		//����
		for (QueuedEntity t = head.next; t != oldFirst; t = t.next) {
			t.stack = this;
		}
	}
	public QueuedEntity pop() {
		if(isEmpty()) return null;
		
		QueuedEntity ret = tail.prev;
		tail.prev = ret.prev;
		ret.prev.next = tail;

		ret.next = ret.prev = null;
		ret.stack = null;
		return ret;
	}
	public QueuedEntity peek() {
		if(isEmpty()) return null;
		return tail.prev;
	}
	/** �����å������ʤ�true */
	public boolean isEmpty() {
		return tail.prev == head;
	}
//	/**@deprecated*/
//	public void remove(QueuedEntity entity) {
//		entity.remove();
//	}
	public String toString() {
		ArrayList list = new ArrayList();
		QueuedEntity entity = head.next;
		while (entity != tail) {
			list.add(entity);
			entity = entity.next;
		}
		return list.toString();
	}
}
