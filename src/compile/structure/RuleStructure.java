package compile.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

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
	public HashMap processContexts = new HashMap();

	/** �롼��ʸ̮�θ���̾ ("@p"�ʤɤ�String) -> ʸ̮����� (ContextDef) */
	public HashMap ruleContexts = new HashMap();

	/** ���դ��ץ���ʸ̮�θ���̾ ("$p"�ʤɤ�String) -> ʸ̮����� (ContextDef) */
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
		String text="";
		if(name!=null) text+=name+" @@ ";
		text += "( " + leftMem.toStringWithoutBrace() + " :- ";
		String guard = "";
		if (!guardMem.atoms.isEmpty()) {
			guard += guardMem.toStringAsGuardTypeConstraints() + " ";
		}
		Iterator it = guardNegatives.iterator();
		while (it.hasNext()) {
			String eqstext = "";
			Iterator it2 = ((LinkedList)it.next()).iterator();
			while (it2.hasNext()) {
				eqstext += "," + ((ProcessContextEquation)it2.next()).toString();
			}
			if (eqstext.length() > 0)  eqstext = eqstext.substring(1);
			guard += "\\+(" + eqstext + ") ";
		}
		if (guard.length() > 0)  text += guard + "| ";
		return text + rightMem.toStringWithoutBrace() + " )";
	}
	// 2006.01.02 okabe
	/**
	 * @return String���롼�빽¤��ʸ����ɽ���ʥ��ȥ�̾��ե��󥯥�̾�Ͼ�ά���ʤ���
	 */
	public String encode() {
		String text="";
		if(name!=null) text+=name+" @@ ";
		text += "( " + leftMem.encode() + " :- ";
		String guard = "";
		if (!guardMem.atoms.isEmpty()) {
			guard += guardMem.toStringAsGuardTypeConstraints() + " ";
		}
		Iterator it = guardNegatives.iterator();
		while (it.hasNext()) {
			String eqstext = "";
			Iterator it2 = ((LinkedList)it.next()).iterator();
			while (it2.hasNext()) {
				eqstext += "," + ((ProcessContextEquation)it2.next()).toString();
			}
			if (eqstext.length() > 0)  eqstext = eqstext.substring(1);
			guard += "\\+(" + eqstext + ") ";
		}
		if (guard.length() > 0)  text += guard + "| ";
		return text + rightMem.encode() + " )";
	}
}
