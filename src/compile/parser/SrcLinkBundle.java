package compile.parser;

/** �������ե�������Υ��«ɽ�� */

class SrcLinkBundle extends SrcContext {
	/**
	 * ���ꤵ�줿̾���Υ��«��������ޤ�
	 * @param name ���«��̾��
	 */
	public SrcLinkBundle(String name) {
		super(name);
	}
	public String getQualifiedName() {
		return "*" + name;
	}
}