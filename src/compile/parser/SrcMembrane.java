/**
 * ��������Ǥ���ɽ��
 */

package compile.parser;
import java.util.LinkedList;

class SrcMembrane {
	
	private LinkedList process = null;
	
	/**
	 * �������������ޤ� 
	 */
	public SrcMembrane() {
		this(null);	
	}
	
	/**
	 * ���ꤵ�줿�ҥץ�����������������ޤ�
	 * @param process ��˴ޤޤ��ҥץ���
	 */
	public SrcMembrane(LinkedList process) {
		this.process = process;	
	}
	
	/**
	 * �ҥץ�����������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
}