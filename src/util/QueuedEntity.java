package util;

/**
 * Stack���Ѥޤ�����ǤΤ���οƥ��饹��
 * ���Υ��饹�Υ��󥹥��󥹤�head/tail���ü�����Ӥ����ǤΤ���ˤΤ߻��Ѥ���
 * �ºݤ����Ǥˤϻҥ��饹�Υ��󥹥��󥹤���Ѥ��롣
 */
public class QueuedEntity {
	/** �ҥ��饹�⤫��Ϥ������ѿ���ľ�ܥ����������ʤ� */
	QueuedEntity next, prev;
	/** ���Υ��饹�Υ��󥹥��󥹤�ľ����������Τ�Ʊ��ѥå�������Τ� */
	protected QueuedEntity() {
		next = prev = null;
	}
	/** �����å����Ѥޤ�Ƥ������true�� */
	public boolean isQueued() {
		return next == null && prev == null;
	}
	/** �����å�������� */
	public void dequeue() {
		next.prev = prev;
		prev.next = next;
	}
}
