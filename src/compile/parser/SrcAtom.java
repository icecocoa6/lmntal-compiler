package compile.parser;
import java.util.LinkedList;

/** �������ե�������Υ��ȥ�ɽ�� */
class SrcAtom {
	protected LinkedList process = null;
	/** ̾���ȡ����� */
	protected SrcName srcname;
	
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
	public SrcAtom(String name) {
		this(new SrcName(name));
	}
	/**
	 * ���ꤵ�줿̾���ȡ��������Ļҥץ����ʤ��Υ��ȥ๽ʸ����������
	 * @param srcname ̾���ȡ�����
	 */
	public SrcAtom(SrcName srcname) {
		this(srcname, new LinkedList(), -1,-1);
	}
	
	/**
	 * ���ꤵ�줿̾���ȻҶ��ץ����ǽ�������ޤ�
	 * @param name ���ȥ�̾
	 * @param process �Ҷ��ץ���
	 */
	public SrcAtom(String name, LinkedList process) {
		this(new SrcName(name), process, -1,-1);
	}

	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��(�Ҷ��ץ���̵��)
	 * @author Tomohito Makino
	 * @param srcname ̾���ȡ�����
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(SrcName srcname, int line, int column) {
		this(srcname, new LinkedList(), line, column);
	}	
	/**
	 * �ǥХå�����������륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param nametoken ̾���ȡ�����
	 * @param process �Ҷ��ץ���
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */
	public SrcAtom(SrcName nametoken, LinkedList process, int line, int column) {
		this.srcname = nametoken;
		this.process = process;
		this.line = line;
		this.column = column;	
	}
	
	public void setSourceLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}

	
	/** ̾���ȡ������������� 
	 * @deprecated*/
	public SrcName getSrcName() { return srcname; }

	/** ���ȥ�̾��������� */
	public String getName() { return srcname.getName(); }
	/** ���ȥ�̾�ȡ�����μ����������� */
	public int getNameType() { return srcname.getType(); }
	
	/**
	 * ���Υ��ȥ�λҥץ��������ޤ�
	 * @return �ҥץ����Υꥹ��
	 */
	public LinkedList getProcess() { return process; }
	
	public String toString() { return SrcDumper.dumpAtom(this,0); }
}
