package compile.structure;

/**
 * ��������������Υ�������Ĺ�¤��ɽ����ݥ��饹��
 * ���ȥࡢ���ȥླྀ�ġ�����ƥ����Ȥʤɤοƥ��饹��
 * @author Takahiko Nagata, n-kato
 * @date 2003/10/28
 */
abstract public class Atomic {
	/** ��°�� */
	public Membrane mem = null;
	/** ���ȥ�Υ����ʤޤ��ϥ��ȥླྀ�ĤΥ��«��� */
	public LinkOccurrence[] args;

	/**
	 * �ǥХå�����:��������������Ǥνи�����(��)
	 * ����̵���Ȥ���-1������
	 * @author Tomohito Makino
	 */
	public int line = -1;
	
	/**
	 * �ǥХå�����:��������������Ǥνи�����(��)
	 * ����̵���Ȥ���-1������
	 * @author Tomohito Makino
	 */
	public int column = -1;

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param arity ������Ĺ��
	 */
	public Atomic(Membrane mem, int arity) {
		this.mem = mem;
		args = new LinkOccurrence[arity];
	}
	
	public void setSourceLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}
	abstract public String toString();
	/** ����Ū�ʼ�ͳ��󥯰����θĿ���������롣*/
	public int getArity() {
		return args.length;
	}
	/** �ե��󥯥���̾����������롣�ե��󥯥���̵�����϶�ʸ������֤���*/
	public String getName() {
		return "";
	}
}