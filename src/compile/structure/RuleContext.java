package compile.structure;

/** ��������������Υ롼��ʸ̮�ι�¤��ɽ�����饹 */
public final class RuleContext extends Context{
	public RuleContext(String name) {
		super(name);
	}
	
	public String toString() {
		return "@" + getName();
	}
}
