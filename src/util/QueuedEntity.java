package util;

/**
 * Stack���Ѥޤ�����ǤΤ���οƥ��饹��
 * ���Υ��饹�Υ��󥹥��󥹤�head/tail���ü�����Ӥ����ǤΤ���ˤΤ߻��Ѥ���
 * �ºݤ����Ǥˤϻҥ��饹�Υ��󥹥��󥹤���Ѥ��롣
 */
public class QueuedEntity {
	QueuedEntity next, prev;
	protected QueuedEntity() {
		next = prev = null;
	}
	/** �����å����Ѥޤ�Ƥ������true���֤� */
	public boolean isQueued() {
		return next != null;
	}
	/** �����å����Ѥޤ�Ƥ���н���� */
	public void dequeue() {
		if (!isQueued()) {
			//System.out.println("SYSTEM ERROR: dequeued entity is not in a queue");
			return;
		}
		next.prev = prev;
		prev.next = next;
		prev = null;
		next = null;
	}
}
