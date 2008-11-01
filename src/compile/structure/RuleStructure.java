package compile.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/** 
 * ��������������Υ롼��ι�¤��ɽ�����饹
 */
public final class RuleStructure {
	/** ��°�졣����ѥ�����ˤĤ���
	 * <p>todo parent�Ϥ�����mem��̾���ѹ����� */
	public Membrane parent;
	
	/** �롼��̾
	 */
	public String name;
	/** �ƥ�����ɽ�� */
	private  String text;
	
	/** ���դ����ΤȤ��ηٹ���������뤫�ɤ��� */
	public boolean fSuppressEmptyHeadWarning = false;

	/** Head���Ǽ������ */
	public Membrane leftMem = new Membrane(null);
	
	/** Body���Ǽ������ */
	public Membrane rightMem = new Membrane(null);
	
	/** �����ɷ�������Ǽ������ */
	public Membrane guardMem = new Membrane(null);

//	/** �����ɤη����� (TypeConstraint) �Υꥹ�� */
//	public LinkedList typeConstraints = new LinkedList();
	
	/** �������������ProcessContextEquation��LinkedList�ˤΥꥹ�� */
	public LinkedList guardNegatives = new LinkedList();
	
	/** �ץ���ʸ̮�θ���̾ ("$p"�ʤɤ�String) -> ʸ̮����� (ContextDef) */
	public HashMap<String, ContextDef> processContexts = new HashMap<String, ContextDef>();

	/** �롼��ʸ̮�θ���̾ ("@p"�ʤɤ�String) -> ʸ̮����� (ContextDef) */
	public HashMap<String, ContextDef> ruleContexts = new HashMap<String, ContextDef>();

	/** ���դ��ץ���ʸ̮�θ���̾ ("$p"�ʤɤ�String) -> ʸ̮����� (ContextDef) */
	public HashMap<String, ContextDef> typedProcessContexts = new HashMap<String, ContextDef>();
	
	/** ���ֹ� 2006.1.22 by inui */
	public int lineno;

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 */
	public RuleStructure(Membrane mem, String text) {
		this.parent = mem;
		// io:{(print:-inline)} �� print �� io.print �ˤ�������
		// ����print �� io ��ľ°�ǤϤʤ��롼�뺸�դ���˽�°����Τǥ롼������Ʊ��̾����Ĥ��Ƥ�����
		leftMem.name = mem.name;
		rightMem.name = mem.name;
		this.text = text;
	}
	
	//2006.1.22 by inui
	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param lineno ���ֹ�
	 */
	public RuleStructure(Membrane mem, String text, int lineno) {
		this(mem, text);
		this.lineno = lineno;
	}

	public String toString() {
		return text;
	}
}
