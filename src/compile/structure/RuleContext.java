package compile.structure;

/** ��������������Υ롼��ʸ̮�и���ɽ�����饹 */

public final class RuleContext extends Context {
	/**
	 * @param mem ����
	 * @param qualifiedName ����̾ */
	public RuleContext(Membrane mem, String qualifiedName) {
		super(mem,qualifiedName,0);
	}
	public String toString() {
		return getQualifiedName();
	}
}