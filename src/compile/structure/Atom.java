package compile.structure;

import runtime.Functor;
import java.util.Arrays;

/**
 * ��������������Υ��ȥ�ι�¤��ɽ�����饹
 * �ǡ�����¤�Τߤ����
 * @author Takahiko Nagata
 * @date 2003/10/28
 */
public class Atom {
	/**
	 * ����
	 */
	public Membrane mem = null;
	
	/**
	 * ���ȥ��̾��
	 */
	public Functor functor;
	
	/**
	 * ���ȥ�Υ�󥯹�¤
	 */
	public LinkOccurrence[] args;
	
	/**
	 * ���󥹥ȥ饯��
	 * @param mem ���Υ��ȥ�ο���
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 */
	public Atom(Membrane mem, String name, int arity) {
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new LinkOccurrence[arity];
	}
	
	public String toString() {
		return functor+(args.length==0 ? "" : "("+Arrays.asList(args)+")");
	}
}