package compile.parser;

/** �������ե�������Υ��ɽ�� */

class SrcLink extends SrcContext {
	/**
	 * ���ꤵ�줿̾���Υ�󥯤�������ޤ�
	 * @param name ���̾
	 */
	public SrcLink(String name) {
		super(name);
	}
	public String getQualifiedName() {
		return " " + name;
	}
}