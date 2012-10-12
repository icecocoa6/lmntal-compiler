package compile.structure;

/** 
 * ��������������Υ��������������1�Ĥ���������: $p=(a(X),$pp) �ˤι�¤��ɽ�����饹<br>
 * 1�ĤΥ�����������ϡ�ProcessContextEquation��LinkedList�Ȥ���ɽ������롣
 * <p>
 * ��: [$p=(a(X),$pp),$q=(b(X),$qq)]
 */
public final class ProcessContextEquation
{
	/**
	 * �������դΥץ���ʸ̮�����
	 */
	public ContextDef def;

	/**
	 * �������դΥץ���
	 */
	public Membrane mem = new Membrane(null);

	/**
	 * ���󥹥ȥ饯��
	 * @param def �������դΥץ���ʸ̮�����
	 * @param mem �������դΥץ������Ǽ���벾��Ū����
	 */
	public ProcessContextEquation(ContextDef def, Membrane mem)
	{
		this.def = def;
		this.mem = mem;
	}

	public String toString()
	{
		return def.toString() + "=(" + mem.toStringWithoutBrace() + ")";
	}
}
