package compile.structure;

/** 
 * ��������������Υ롼��ι�¤��ɽ�����饹
 */
public final class RuleStructure {
	/**
	 * Head�롼����Ǽ������
	 */
	public Membrane leftMem = new Membrane(null);
	
	/**
	 * Body�롼����Ǽ������
	 */
	public Membrane rightMem = new Membrane(null);
	
	public String toString() {
		return "( "+leftMem.toStringWithoutBrace()+" :- "+rightMem.toStringWithoutBrace()+" )";
	}
}
