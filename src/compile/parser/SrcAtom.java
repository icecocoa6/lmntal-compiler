package compile.parser;
import java.util.LinkedList;


/**���Խ����*/
/** �������ե�������Υ��ȥ�ɽ�� */
class SrcAtom {

	protected LinkedList process = null;
	protected String name = null;
	
	/** ̾���ȡ�����μ���
	 * <p>��̾���Τ���Υ��饹���ꡢ���ऴ�Ȥ˥��֥��饹������Τ��������������ܤä���*/
	int nametype;

	// type����
	static final int PLAIN   = 0;		// aaa
	static final int SYMBOL  = 1;		// 'aaa' 'AAA'
	static final int STRING  = 2;		// "aaa" "AAA"
	static final int QUOTED  = 3;		// [[aaa]] [[AAA]]
	static final int PATHED  = 4;		// aaa.bbb
	static final int INTEGER = 10;	// 345 -345
	static final int FLOAT   = 11;	// 3.14 -3.14e-33
	
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
	 * ���ꤵ�줿PLAIN��̾���λҥץ����ʤ��Υ��ȥ๽ʸ����������
	 * @param name ���ȥ�̾
	 */
	public SrcAtom(String name) {
		this(name, PLAIN);
	}

	/**
	 * ���ꤵ�줿̾���ǻҶ��ץ����ʤ��ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param nametype ̾���ȡ�����μ���
	 */
	public SrcAtom(String name, int nametype) {
		this(name, nametype, new LinkedList(), -1,-1);
	}
	
	/**
	 * ���ꤵ�줿̾���ȻҶ��ץ����ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 */
	public SrcAtom(String name, LinkedList process) {
		this(name, PLAIN, process, -1,-1);
	}

	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��(�Ҷ��ץ���̵��)
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(String name, int nametype, int line, int column) {
		this(name, nametype, new LinkedList(), line, column);
	}
	
	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(String name, int nametype, LinkedList process, int line, int column) {
		this.name = name;
		this.nametype = nametype;
		this.process = process;
		this.line = line;
		this.column = column;	
	}
	
	public void setSourceLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}

	
	/**
	 * ���Υ��ȥ��̾�������ޤ�
	 * @return ���ȥ�̾�򤢤�魯ʸ����
	 */
	public String getName() { return name; }

	public int getNameType() { return nametype; }
	
	/**
	 * ���Υ��ȥ�λҥץ��������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
}
