package compile.structure;

import runtime.Functor;

/**
 * ��������������Υ��ȥ�ʤޤ��ϥ��ȥླྀ�ġˤι�¤��ɽ�����饹��
 * �ǡ�����¤�Τߤ���ġ�
 * @author Takahiko Nagata
 * @date 2003/10/28
 */
public class Atom {
	/** ��°�� */
	public Membrane mem = null;
//	/** ����Ū�˻��ꤵ�줿�⥸�塼��̾������Ū�˻��ꤵ��Ƥ��ʤ�����null��*/
//	public String path = null;
	/** ���ȥ�Υե��󥯥� */
	public Functor functor;
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
	 * @param functor �ե��󥯥�
	 */
	public Atom(Membrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new LinkOccurrence[functor.getArity()];
		// �����Ǥ����Τ��� hara
		if(functor.getName().equals("system_ruleset")) mem.is_system_ruleset=true;
		// todo �֥⥸�塼�뵡ǽ�פ�Ȥä�ɽ���������������Ȼפ��ޤ���
	}
	
	/**
	 * ���󥹥ȥ饯��
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 */
	public Atom(Membrane mem, String name, int arity) {
		this(mem,new Functor(name,arity));
	}
	
	/**
	 * �ǥХå�������ݻ����륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 * @deprecated
	 */	
	public Atom(Membrane mem, String name, int arity, int line, int column){
		this(mem,name,arity);
		setSourceLocation(line,column);		
	}
	public void setSourceLocation(int line, int column) {
		this.line = line;
		this.column = column;
	}
	public String toString() {
		if (args.length == 0) return functor.getQuotedAtomName();
		String argstext = "";
		for (int i = 0; i < args.length; i++) {
			argstext += "," + args[i];
		}
		return functor.getQuotedFunctorName() + "(" + argstext.substring(1) + ")";
	}
	public String toStringAsTypeConstraint() {
		if (args.length == 0) return functor.getQuotedAtomName();
		String argstext = "";
		for (int i = 0; i < args.length; i++) {
			argstext += "," + ((ProcessContext)args[i].buddy.atom).getQualifiedName();
		}
		return functor.getQuotedFunctorName() + "(" + argstext.substring(1) + ")";
	}
	public String getPath() {
		return functor.getPath();
	}
	
}