package compile.parser;
import java.util.LinkedList;

/** �������ե����������ɽ�� */

class SrcMembrane {
	/** ������ƥץ�����ɽ�� */
	LinkedList process = null;
	/** ��λ�ե饰��̵ͭ */
	public boolean stable = false;
	
	/**
	 * �������������ޤ� 
	 */
	public SrcMembrane() {
		this(new LinkedList());
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