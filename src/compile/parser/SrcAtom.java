/**
 * �������ե�������ǤΥ��ȥ�ɽ��
 */
package compile.parser;

import java.util.LinkedList;

class SrcAtom {

	protected LinkedList process = null;
	protected String name = null;

	/**
	 * ��������������Ǥνи�����(��)
	 * @author Tomohito Makino
	 */
	int line;
	/**
	 * ��������������Ǥνи�����(��)
	 * @author Tomohito Makino
	 */
	int column;

	/**
	 * ���ꤵ�줿̾���ǻҶ��ץ����ʤ��ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 */
	public SrcAtom(String name) {
		this(name, new LinkedList());
	}
	
	/**
	 * ���ꤵ�줿̾���ȻҶ��ץ����ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 */
	public SrcAtom(String name, LinkedList process) {
		this(name, process, -1, -1);
	}

	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��(�Ҷ��ץ���̵��)
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(String name, int line, int column) {
		this(name, new LinkedList(), line, column);
	}
	
	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(String name, LinkedList process, int line, int column) {
		this.name = name;
		this.process = process;
		this.line = line;
		this.column = column;	
	}
	
	
	/**
	 * ���Υ��ȥ��̾�������ޤ�
	 * @return ���ȥ�̾�򤢤�魯ʸ����
	 */
	public String getName() { return name; }
	
	/**
	 * ���Υ��ȥ�λҥץ��������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
}
