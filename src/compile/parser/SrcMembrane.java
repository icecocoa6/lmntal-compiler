package compile.parser;
import java.util.LinkedList;

/** �������ե����������ɽ�� */

class SrcMembrane {
	/** ������ƥץ�����ɽ�� */
	LinkedList process = null;
	/** ��λ�ե饰��̵ͭ */
	boolean stable = false;
	/** ̾�� */
	public String name;
	
	/**
	 * �������������ޤ� 
	 */
	public SrcMembrane() {
		this(null, new LinkedList());
	}
	
	/**
	 * ����̾���Ĥ����������ޤ� 
	 */
	public SrcMembrane(String name) {
		this(name, new LinkedList());
	}
	
	/**
	 * ���ꤵ�줿�ҥץ�����������������ޤ�
	 * @param process ��˴ޤޤ��ҥץ���
	 */
	public SrcMembrane(LinkedList process) {
		this(null, process);
	}
	
	/**
	 * ���ꤵ�줿�ҥץ��������̾���Ĥ����������ޤ�
	 * @param process ��˴ޤޤ��ҥץ���
	 */
	public SrcMembrane(String name, LinkedList process) {
		this.name = name;
		this.process = process;
		//runtime.Env.p("Membran name = "+name);
	}
	
	/**
	 * �ҥץ�����������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
}