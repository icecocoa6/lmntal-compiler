package compile.structure;

/** 
 * ��������������Υ롼��ι�¤��ɽ�����饹
 */
public final class RuleStructure {
	/**
	 * ��°�졣����ѥ�����ˤĤ����Τ�ͽ�᥻�åȤ��Ƥ�������
	 */
	public Membrane parent;
	/**
	 * Head�롼����Ǽ������
	 */
	public Membrane leftMem = new Membrane(null);
	
	/**
	 * Body�롼����Ǽ������
	 */
	public Membrane rightMem = new Membrane(null);
	
	/**
	 * �����ɥ롼����Ǽ������
	 */
	public Membrane guardMem = new Membrane(null);
	
	public String toString() {
		return "( "+leftMem.toStringWithoutBrace()+" :- "+rightMem.toStringWithoutBrace()+" )";
	}
}
