/**
 * ��������Υ롼���ɽ���ޤ�
 */

package compile.parser;
import java.util.LinkedList;

class SrcRule {
	
	public LinkedList head = null; // �إåɥץ���
	public LinkedList body = null; // �ܥǥ��ץ���
	public LinkedList guard = null; // �����ɥץ���
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȶ��Υ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(LinkedList head, LinkedList body) {
		this(head, new LinkedList(), body);
	}
	
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȥ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param gurad �����ɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(LinkedList head, LinkedList guard, LinkedList body) {
		this.head = head;
		this.guard = guard;
		this.body = body;
	}

	
	/**
	 * �롼��Υإåɤ�������ޤ�
	 * @return �إåɤΥꥹ��
	 */
	public LinkedList getHead() {
		return this.head;
	}
	
	/**
	 * �롼��Υ����ɤ����ޤ�
	 * @return �����ɤΥꥹ��
	 */
	public LinkedList getGuard() {
		return this.guard;
	}
	
	/**
	 * �롼��Υܥǥ���������ޤ�
	 * @return �ܥǥ��Υꥹ��
	 */
	public LinkedList getBody() {
		return this.body;
	}
}