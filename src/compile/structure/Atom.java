package compile.structure;

import runtime.Functor;
import runtime.Inline;
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
	 * �ǥХå�����:��������������Ǥνи�����(��)
	 * ����̵���Ȥ���-1������
	 * @author Tomohito Makino
	 */
	public int line;
	
	/**
	 * �ǥХå�����:��������������Ǥνи�����(��)
	 * ����̵���Ȥ���-1������
	 * @author Tomohito Makino
	 */
	public int column;

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ���Υ��ȥ�ο���
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 */
	public Atom(Membrane mem, String name, int arity) {
		this(mem,name,arity,-1,-1);
	}
	
	/**
	 * �ǥХå�������ݻ����륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param mem ���Υ��ȥ�ο���
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */	
	public Atom(Membrane mem, String name, int arity, int line, int column){
		this.mem = mem;
		functor = new Functor(name, arity);
		args = new LinkOccurrence[arity];
		this.line = line;
		this.column = column;
		
		//Inline
		Inline.add(name);
	}
	
	public String toString() {
		return functor+(args.length==0 ? "" : "("+Arrays.asList(args)+")");
	}
}