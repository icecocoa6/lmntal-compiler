package compile.structure;

import java.util.HashMap;

/** 
 * ��������������Υ롼��ι�¤��ɽ�����饹
 */
public final class RuleStructure {
	/** ��°�졣����ѥ�����ˤĤ���
	 * <p>todo parent�Ϥ�����mem��̾���ѹ����� */
	public Membrane parent;

	/** Head���Ǽ������ */
	public Membrane leftMem = new Membrane(null);
	
	/** Body���Ǽ������ */
	public Membrane rightMem = new Membrane(null);
	
	/** �����ɤ��Ǽ������ */
	public Membrane guardMem = new Membrane(null);
	
	/** �ץ���ʸ̮�θ���̾ -> ContextDef */
	public HashMap processContexts = new HashMap();

	/** �롼��ʸ̮�θ���̾ -> ContextDef */
	public HashMap ruleContexts = new HashMap();

	/** ���դ��ץ���ʸ̮�θ���̾ -> ContextDef */
	public HashMap typedProcessContexts = new HashMap();

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 */
	public RuleStructure(Membrane mem) {
		this.parent = mem;
		// io:{(print:-inline)} �� print �� io.print �ˤ�������
		// ����print �� io ��ľ°�ǤϤʤ��롼�뺸�դ���˽�°����Τǥ롼������Ʊ��̾����Ĥ��Ƥ�����
		leftMem.name = mem.name;
		rightMem.name = mem.name;
	}

	public String toString() {
		return "( "+leftMem.toStringWithoutBrace()+" :- "+rightMem.toStringWithoutBrace()+" )";
	}
}
