package compile.parser;

/** �������ե�������Υ��«ɽ�� */

class SrcLinkBundle extends SrcLink {
	static final String PREFIX_TAG = "*";
	/**
	 * ���ꤵ�줿̾���Υ��«��������ޤ�
	 * @param name ���«��̾��
	 */
	public SrcLinkBundle(String name) {
		super(name);
	}
	public String getQualifiedName() {
		return PREFIX_TAG + name;
	}
}