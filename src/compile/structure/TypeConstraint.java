package compile.structure;

import runtime.Functor;
import java.util.Arrays;

/**
 * ��������������Ρʥ����ɤˤ�����˷�����ι�¤��ɽ�����饹��
 * <p>Atom�����Ѥ��Ƥ��뤿�ᡢ����̤���ѡ������餯���Υե�������ѻߤ���롣
 * @author n-kato
 */
public class TypeConstraint {
	/** �����󥢥ȥ�Υե��󥯥� */
	public Functor functor;
	/** �����󥢥ȥ�η��դ��ץ���ʸ̮̾���� */
	public ContextDef[] args;

	/** �ǥХå�����:��������������Ǥνи�����(��) */
	public int line = -1;
	/** �ǥХå�����:��������������Ǥνи�����(��) */
	public int column = -1;

	/**
	 * ���󥹥ȥ饯��
	 * @param name ������̾��ɽ��ʸ����
	 * @param arity �����θĿ�
	 */
	public TypeConstraint(Functor functor) {
		this.functor = functor;
		args = new ContextDef[functor.getArity()];
	}

	public String toString() {
		if (args.length == 0) return functor.getAbbrName();
		String argstext = Arrays.asList(args).toString();
		argstext = argstext.substring(1, argstext.length() - 1);
		argstext = argstext.replaceAll(", ",",");
		return functor.getAbbrName() + "(" + argstext + ")";
	}
}