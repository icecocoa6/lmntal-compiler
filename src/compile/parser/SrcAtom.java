package compile.parser;
import java.util.LinkedList;


/**���Խ����*/
/** �������ե�������Υ��ȥ�ɽ�� */
class SrcAtom {

	protected LinkedList process = null;
	//protected String name = null;
	protected SrcName name;
	
	/**
	 * ��������������Ǥνи�����(��)
	 * @author Tomohito Makino
	 */
	int line = -1;
	/**
	 * ��������������Ǥνи�����(��)
	 * @author Tomohito Makino
	 */
	int column = -1;

	/**
	 * ���ꤵ�줿̾���λҥץ����ʤ��Υ��ȥ๽ʸ����������
	 * @param name ���ȥ�̾
	 */
	public SrcAtom(SrcName name) {
		this(name, new LinkedList(), -1,-1);
	}
	public SrcAtom(String name) {
		this(new SrcName(name));
	}
	
	/**
	 * ���ꤵ�줿̾���ȻҶ��ץ����ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 */
	public SrcAtom(SrcName name, LinkedList process) {
		this(name, process, -1,-1);
	}
	public SrcAtom(String name, LinkedList process) {
		this(new SrcName(name), process);
	}

	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��(�Ҷ��ץ���̵��)
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(SrcName name, int line, int column) {
		this(name, new LinkedList(), line, column);
	}
	public SrcAtom(String name, int line, int column) {
		this(new SrcName(name),line,column);
	}
	
	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(SrcName name, LinkedList process, int line, int column) {
		this.name = name;
		this.process = process;
		this.line = line;
		this.column = column;	
	}
	public SrcAtom(String name, LinkedList process, int line, int column) {
		this(new SrcName(name),process,line,column);
	}
	
	public void setSourceLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}

	
	/** ���ȥ�̾���֤� */
	public SrcName getName() { return name; }
	
	/**
	 * ���Υ��ȥ�λҥץ��������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
}
