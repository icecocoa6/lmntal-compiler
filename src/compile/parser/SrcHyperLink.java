package compile.parser;

/** �������ե�������Υ��ɽ�� */

class SrcHyperLink extends SrcLink {
	int lineno;//2012.7.10 by meguro
	
	/**
	 * ���ꤵ�줿̾���Υ�󥯤�������ޤ�
	 * @param name ���̾
	 */
	public SrcHyperLink(String name) {
		super(name, -1);
	}
	
	/**
	 * ���ꤵ�줿̾���ȹ��ֹ�Υ�󥯤�������ޤ�
	 * @param name ���̾
	 * @param lineno ���ֹ�
	 */
	public SrcHyperLink(String name, int lineno) {
	    super(name, lineno);
	}
	public String getQualifiedName() {
		return " " + name;
	}
}
