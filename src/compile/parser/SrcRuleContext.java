package compile.parser;

/** �������ե�������Υ롼�륳��ƥ����Ȥ�ɽ�� */

class SrcRuleContext extends SrcContext {
	/**
	 * ���ꤵ�줿̾������ĥ롼�륳��ƥ����Ȥ�������ޤ�
	 * @param name �롼�륳��ƥ�����̾
	 */
	public SrcRuleContext(String name) {
		super(name);
	}
	public String getQualifiedName() {
		return "@" + name;
	}
}
