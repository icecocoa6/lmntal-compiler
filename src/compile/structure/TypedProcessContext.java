package compile.structure;

// TODO ���Υե�������ѻ�

/** ��������������Ǥη��դ��ץ���ʸ̮�и���ɽ�����饹 */
final public class TypedProcessContext extends Context {
	/** ���η��դ��ץ���ʸ̮�μ�ͳ��󥯡ʤ�����¦�� */
	public LinkOccurrence freeLink;
	/** ���η��դ��ץ���ʸ̮��̾�� */
	protected String typedName;
	/** �������и� */
	public TypedProcessContext src;
	/** ���󥹥ȥ饯�� */
	public TypedProcessContext(Membrane mem, String name, LinkOccurrence freeLink) {
		super(mem,"",1);	// 1�ϥХ��θ��˰㤤�ʤ�
		this.typedName = name;
		this.freeLink = freeLink;
	}
	public String getName() {
		return "$" + typedName;
	}
	public String toString() {
		return "$" + typedName + "[" + freeLink + "]";
	}
}
