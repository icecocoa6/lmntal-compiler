package runtime;

import runtime.functor.Functor;
import util.QueuedEntity;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
public final class Atom extends QueuedEntity
{
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹���ѹ����Ƥ褤��
	 * �������ͤ��ѹ�����Ȥ���index��Ʊ���˹������뤳�ȡ�(mem,index)==(null, -1)�Ͻ�°��ʤ���ɽ����
	 */
	Membrane mem;
	
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	public int index = -1;

	public int getid()
	{
		return id;
	}

	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	
	/** ���Υ��ȥ�Υ�����ID */
	int id;

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ��°��
	 */
	public Atom(Membrane mem, Functor functor)
	{
		this.mem = mem;
		this.functor = functor;
		if(functor.getArity() > 0)
			args = new Link[functor.getArity()];
		else
			args = null;
		id = lastId++;
	}

	public void setFunctor(Functor newFunctor)
	{
		if (args == null)
		{
			if (newFunctor.getArity() != 0)
			{
				throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
			}
		}
		else if (newFunctor.getArity() > args.length)
		{
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
		}
		functor = newFunctor;
	}
	
	/** �ե��󥯥�̾�����ꤹ�롣 */
	public void setName(String name)
	{
		setFunctor(name, getFunctor().getArity());
	}
	
	/** �ե��󥯥������ꤹ�롣
	 * AtomSet�򹹿����뤿�ᡢ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void setFunctor(String name, int arity)
	{
		//mem.alterAtomFunctor(this, new SymbolFunctor(name, arity));
	}

	public String toString() {
		return functor.getName();
	}
	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	/** ���Υ��ȥ�Υ�����ID��������� */
	String getLocalID() {
		return Integer.toString(id);
	}

	/** ��°��μ��� */
	public Membrane getMem() {
		return mem;
	}

	/** �ե��󥯥������ */
	public Functor getFunctor() {
		return functor;
	}

	/** ̾������� */
	public String getName() {
		return functor.getName();
	}

	/** ��󥯿������ */
	public int getArity() {
		return functor.getArity();
	}

	/** �ǽ���������� */
	public Link getLastArg() {
		return args[getArity() - 1];
	}

	/** ��pos�����˳�Ǽ���줿��󥯥��֥������Ȥ���� */
	public Link getArg(int pos) {
		return args[pos];
	}

	/** �� n �����ˤĤʤ��äƤ륢�ȥ��̾����������� */
	public String nth(int n) {
		return nthAtom(n).getFunctor().getName();
	}

	/** �� n �����ˤĤʤ��äƤ륢�ȥ��������� */
	public Atom nthAtom(int n) {
		
		return args[n].getAtom();
	}

	/**
	 * �ץ��������Ф����ºݤ��٤Υ��ȥ���������
	 * @param index
	 * @return
	 */
	public Atom getNthAtom(int index) {
		if(null == args[index]){
			return null;
		}
		Atom a = nthAtom(index);
		while(a.getFunctor().isInsideProxy() || a.getFunctor().isOutsideProxy()) {
			a = a.nthAtom(0).nthAtom(1);
		}
		return a;
	}

	public int getEdgeCount() {
		return functor.getArity();
	}
}
