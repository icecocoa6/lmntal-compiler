package compile.structure;

import runtime.Functor;
import runtime.Inline;
import java.util.Arrays;

/**
 * ��������������Υ��ȥ�ʤޤ��ϥ��ȥླྀ�ġˤι�¤��ɽ�����饹��
 * �ǡ�����¤�Τߤ���ġ�
 * @author Takahiko Nagata
 * @date 2003/10/28
 */
public class Atom {
	/** ��°�� */
	public Membrane mem = null;
	/** ���ȥ�Υե��󥯥� */
	public Functor functor;
	/** ���ȥ�Υ����ʤޤ��ϥ��ȥླྀ�ĤΥ��«��� */
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
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 */
	public Atom(Membrane mem, String name, int arity) {
		this(mem,name,arity,-1,-1);
	}
	
	/**
	 * �ǥХå�������ݻ����륳�󥹥ȥ饯��
	 * @author Tomohito Makino
	 * @param mem ���Υ��ȥ�ν�°��
	 * @param name ���ȥ�̾��ɽ��ʸ����
	 * @param arity ��󥯤ο�
	 * @param line �����������ɾ�Ǥνи�����(��)
	 * @param column �����������ɾ�Ǥνи�����(��)
	 */	
	public Atom(Membrane mem, String name, int arity, int line, int column){
		this.mem = mem;
		functor = new Functor(name, arity, mem);
		args = new LinkOccurrence[arity];
		this.line = line;
		this.column = column;
		
		//Inline
		Inline.add(name);
	}
	
	public String toString() {
		if (args.length == 0) return functor.getAbbrName();
		String argstext = Arrays.asList(args).toString();
		argstext = argstext.substring(1, argstext.length() - 1);
		argstext = argstext.replaceAll(", ",",");
		return functor.getAbbrName() + "(" + argstext + ")";
	}
}