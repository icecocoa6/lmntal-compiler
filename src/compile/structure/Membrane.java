package compile.structure;

import java.util.ArrayList;
import java.util.List;

import runtime.Env;

/** 
 * �����������������ι�¤��ɽ�����饹
 * memo:����1�Ĥ�������������ˡ�⤢�롣
 * �����Ǥ�List�Ȥ����ݻ�����롣
 */
public final class Membrane {
	/** 
	 * ���� 
	 */
	Membrane mem;
	
	/**
	 * ���ȥ�(compile.structure.Atom)�Υꥹ��
	 */
	public List atoms = new ArrayList();

	/** 
	 * ����(compile.structure.Membrane)�Υꥹ��
	 */
	public List mems = new ArrayList();
	
	/**
	 * �롼��(compile.structure.RuleStructure)�Υꥹ��
	 */
	public List rules = new ArrayList();
	
	/**
	 * �ץ����ѿ�(compile.structure.ProcessContext)�Υꥹ��
	 */
	public List processContexts = new ArrayList();
	
	/**
	 * �롼���ѿ�(compile.struct.RuleContext)�Υꥹ��
	 */
	public List ruleContexts = new ArrayList();
	
	/**
	 * ���եץ���ʸ̮(compile.struct.TypedProcessContext)�Υꥹ��
	 */
	public List typedProcessContexts = new ArrayList();
	
	/**
	 * ���󥹥ȥ饯��
	 * @param mem ����
	 */
	public Membrane(Membrane mem) {
		this.mem = mem;
	}
	
	/**
	 * {} �ʤ��ǽ��Ϥ��롣
	 * 
	 * �롼��ν��Ϥκݡ�{} �������
	 * (a:-b) �� ({a}:-{b}) �ˤʤä��㤦���顣
	 *  
	 * @return String 
	 */
	public String toStringWithoutBrace() {
		return 
		(atoms.isEmpty() ? "" : ""+Env.parray(atoms))+
		(mems.isEmpty() ? "" : " "+Env.parray(mems))+
		(rules.isEmpty() ? "" : " "+Env.parray(rules))+
		(processContexts.isEmpty() ? "" : " "+Env.parray(processContexts))+
		(ruleContexts.isEmpty() ? "" : " "+Env.parray(ruleContexts))+
		(typedProcessContexts.isEmpty() ? "" : " "+Env.parray(typedProcessContexts))+
		"";
		
	}
	public String toString() {
		return "{ " + toStringWithoutBrace() + " }";
	}
}
