package compile.structure;

import runtime.Functor;

/**
 * ��������������Υ��ȥ�ʤޤ��ϥ��ȥླྀ�Ĥޤ��Ϸ�����ˤι�¤��ɽ�����饹��
 * @author Takahiko Nagata
 * @date 2003/10/28
 */
public class Atom extends Atomic{
//	/** ����Ū�˻��ꤵ�줿�⥸�塼��̾������Ū�˻��ꤵ��Ƥ��ʤ�����null��*/
//	public String path = null;
	/** ���ȥ�Υե��󥯥� */
	public Functor functor;

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param functor �ե��󥯥�
	 */
	public Atom(Membrane mem, Functor functor) {
		super(mem, functor.getArity());
		this.functor = functor;
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
	public String getName() {
		return functor.getName();
	}

}