package util;

/**
 * Stack���Ѥޤ�����ǤΤ���οƥ��饹��
 * ���Υ��饹�Υ��󥹥��󥹤�head/tail���ü�����Ӥ����ǤΤ���ˤΤ߻��Ѥ���
 * �ºݤ����Ǥˤϻҥ��饹�Υ��󥹥��󥹤���Ѥ��롣
 */
public class QueuedEntity {
	QueuedEntity next, prev;
	/** ���� entity ���Ĥޤ�Ƥ��륹���å� */
	protected Stack stack;
	
	protected QueuedEntity() {
		next = prev = null;
	}
	/** �����å����Ѥޤ�Ƥ������true���֤� */
	public boolean isQueued() {
		return stack != null;
	}
	/**
	 * �����å����Ѥޤ�Ƥ���н���롣
	 */
	public void dequeue() {
		if (!isQueued()) {
			//System.out.println("SYSTEM ERROR: dequeued entity is not in a queue");
			return;
		}
		try {
			synchronized (stack) {
				next.prev = prev;
				prev.next = next;
				prev = null;
				next = null;
				stack = null;
			}
		} catch (NullPointerException e) {
			//��Ʊ���˽����Ƥ����Τǡ����⤷�ʤ���
		}
	}
}
