package compile.structure;

/** 
 * ��������������Υ롼��ι�¤��ɽ�����饹
 */
public final class RuleStructure {
	/** ��°�졣����ѥ�����ˤĤ���
	 * todo parent�Ϥ�����mem��̾���ѹ����� */
	public Membrane parent;

	/** Head���Ǽ������ */
	public Membrane leftMem = new Membrane(null);
	
	/** Body���Ǽ������ */
	public Membrane rightMem = new Membrane(null);
	
	/** �����ɤ��Ǽ������ */
	public Membrane guardMem = new Membrane(null);

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 */
	public RuleStructure(Membrane mem) {
		this.parent = mem;
	}

	public String toString() {
		return "( "+leftMem.toStringWithoutBrace()+" :- "+rightMem.toStringWithoutBrace()+" )";
	}
}
