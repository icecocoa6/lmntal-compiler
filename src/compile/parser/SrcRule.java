/**
 * ��������Υ롼���ɽ���ޤ�
 */

package compile.parser;
import java.util.LinkedList;

class SrcRule {
	
	private LinkedList head = null;
	private LinkedList body = null;
	
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(LinkedList head, LinkedList body) {
		this.head = head;
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
	 * �롼��Υܥǥ���������ޤ�
	 * @return �ܥǥ��Υꥹ��
	 */
	public LinkedList getBody() {
		return this.head;
	}
}