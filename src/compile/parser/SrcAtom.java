/**
 * �������ե�������ǤΥ��ȥ�ɽ��
 */
package compile.parser;

import java.util.LinkedList;

class SrcAtom {

	protected LinkedList process = null;
	protected String name = null;
	
	/**
	 * ���ꤵ�줿̾���ǻҶ��ץ����ʤ��ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 */
	public SrcAtom(String name) {
		this(name, null);
	}
	/**
	 * ���ꤵ�줿̾���ȻҶ��ץ����ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 */
	public SrcAtom(String name, LinkedList process) {
		this.name = name;
		this.process = process;	
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
