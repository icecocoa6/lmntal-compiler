package compile.parser;

/** �������ե�������Υ��ɽ�� */

class SrcLink extends SrcContext {
	int lineno;//2006.6.20 by inui
	
	/**
	 * ���ꤵ�줿̾���Υ�󥯤�������ޤ�
	 * @param name ���̾
	 */
	public SrcLink(String name) {
		this(name, -1);
	}
	
	/**
	 * ���ꤵ�줿̾���ȹ��ֹ�Υ�󥯤�������ޤ�
	 * @param name ���̾
	 * @param lineno ���ֹ�
	 */
	public SrcLink(String name, int lineno) {
		super(name);
		this.lineno = lineno;
	}
	public String getQualifiedName() {
		return " " + name;
	}
}