package runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import util.QueuedEntity;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
public final class Atom extends QueuedEntity implements Serializable {
	
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹���ѹ����Ƥ褤��
	 * �������ͤ��ѹ�����Ȥ���index��Ʊ���˹������뤳�ȡ�(mem,index)==(null, -1)�Ͻ�°��ʤ���ɽ����
	 */
	Membrane mem;
	
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	public int index = -1;

	public int getid(){
		return id;
	}
	
	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	
	/** ���Υ��ȥ�Υ�����ID */
	int id;
	
	/** ��⡼�ȥۥ��ȤȤ��̿��ǻ��Ѥ���뤳�Υ��ȥ��ID����⡼����˽�°����Ȥ��Τ߻��Ѥ���롣
	 * <p>��°��Υ���å�������塢��°���Ϣ³�����å�������Τ�ͭ����
	 * ����å���������˽�������졢����³����⡼�ȥۥ��Ȥؤ��׵���ۤ��뤿��˻��Ѥ���롣
	 * ��⡼�ȥۥ��Ȥؤ��׵�ǿ��������ȥब���������ȡ��������NEW_����������롣
	 * $inside_proxy���ȥ�ξ�硢̿��֥�å�������������ƥ�⡼��¦�Υ�����ID�Ǿ�񤭤���롣
	 * $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
	 * TODO �ѻߤ���
	 * @see Membrane.atomTable */
//	protected String remoteid;

	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ��°��
	 */
	public Atom(Membrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		if(functor.getArity() > 0)
			args = new Link[functor.getArity()];
		else
			args = null;
		id = lastId++;
	}

	///////////////////////////////
	// ���
	public void setFunctor(Functor newFunctor) {
		if (args == null) {
			if (newFunctor.getArity() != 0) {
//				todo SystemError�Ѥ��㳰���饹���ꤲ��
				throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
			}
		} else if (newFunctor.getArity() > args.length) {
			// todo SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
		}
		functor = newFunctor;
	}
	/** �ե��󥯥�̾�����ꤹ�롣 */
	public void setName(String name) {
		setFunctor(name, getFunctor().getArity());
	}
	/** �ե��󥯥������ꤹ�롣
	 * AtomSet�򹹿����뤿�ᡢ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void setFunctor(String name, int arity) {
		mem.alterAtomFunctor(this, new SymbolFunctor(name, arity));
	}
	/** ���ȥ���°�줫��������ʥ����Υ��ȥ�Ͻ���ʤ���*/
	public void remove() {
		if(Env.fUNYO){
			unyo.Mediator.addRemovedAtom(this, mem.getMemID());
		}
		mem.removeAtom(this);
	}
	///////////////////////////////
	// ����μ���

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
	public Functor getFunctor(){
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
//	/** ��°������ꤹ�롣AbstractMembrane�Ȥ��Υ��֥��饹�Τ߸ƤӽФ��Ƥ褤��*/
//	void setMem(AbstractMembrane mem) {
//		this.mem = mem;
//	}
//	/**@deprecated*/	
//	void remove() {
//		mem.removeAtom(this);
//		mem = null;
//	}
//	/** �����å������äƤ���н���� */
//	public void dequeue() {
//		super.dequeue();
//	}
	
	///////////////////////////////////////////////////////////////
	
	/* *** *** *** *** *** BEGIN GUI *** *** *** *** *** */
//
//	DoublePoint pos;
//	Double3DPoint pos3d;
//	LMNTransformGroup objTrans;
//	double vx, vy, vz;
	
	public boolean isVisible() {
		return !(functor instanceof SpecialFunctor);
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
	
	/**
	 * ���Υ��ȥ��ľ�󲽤��ƥ��ȥ꡼��˽񤭽Ф��ޤ���
	 * ����å��幹���䡢�ץ���ʸ̮�ΰ����κݤ����Ѥ��ޤ���
	 * @param out ���ϥ��ȥ꡼��
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		//out.writeInt(id);
		out.writeObject(functor);
		if (functor.equals(Functor.INSIDE_PROXY)) {
			//����ؤΥ�󥯤��������ʤ�
			out.writeObject(args[1]);
		} else if (functor.isOutsideProxy()) {
			//����ؤΥ�󥯤ϡ���³��atomID/memID�Τ���������³���INSIDE_PROXY���裱�����ʤΤǡ����ȥ��ID�Τߤǽ�ʬ��
			Atom a = args[0].getAtom();
			Membrane mem = a.mem;
			out.writeObject(mem.getMemID());
			// n-kato ��� 2006-09-07
			// //�롼����ʳ��Ǥϥ����Х�ID����������Ƥ��ʤ��Τǡ��Ȥꤢ������ʬ�Ǻ�äƤ��롣
			// //todo ��äȤ褤��ˡ��ͤ���
			//out.writeObject(mem.getTask().getMachine().runtimeid);
			//out.writeObject(mem.getLocalID());
			//out.writeInt(a.id);//todo ��β����ܤμ�ͳ��󥯤���ž�����ʤ���Фʤ�ʤ�
			out.writeObject(args[1]);
		} else {
			out.writeObject(args);
		}
	}
	
	/**
	 * ���Υ��ȥ�����Ƥ򥹥ȥ꡼�फ���������ޤ���
	 * ����å��幹���䡢�ץ���ʸ̮�ΰ����κݤ����Ѥ��ޤ���
	 * TODO OUTSIDE_PROXY����������������
	 * @param in ���ϥ��ȥ꡼��
	 * @throws IOException �����ϥ��顼��ȯ��������硣
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		id = lastId++;
//		remoteid = Integer.toString(in.readInt());
		functor = (Functor)in.readObject();
		args = new Link[functor.getArity()];
		if (functor.equals(Functor.INSIDE_PROXY)) {
			//�Ȥꤢ�����������Ƥ��������RemoteMembrane��ǤĤʤ�ľ���졢���Υ��ȥ�ϻȤ��ʤ��ʤ롣
			args[1] = (Link)in.readObject();
		} else if (functor.isOutsideProxy()) {
			//�������INSIDE_PROXY����������Ƥ��ʤ��Τǡ��������������롣
			String globalid = (String)in.readObject();
//			String localid = (String)in.readObject();
//			String globalid = hostname + ":" + localid;
			Membrane mem = null; 		// todo �����줬ɬ��
//			mem.globalid = globalid;	// globalid��ʸ����ե�����ɤˤ��ʤ���Фʤ�ʤ��ʤ�Τ���
//			AbstractMembrane mem = IDConverter.lookupGlobalMembrane(globalid);
			//IDConverter�ˤϡ�RemoteMembrane.updateCache()����Ͽ�ѤߤΤϤ���
			Atom inside = new Atom(mem, Functor.INSIDE_PROXY);
			mem.atoms.add(inside);
			
//			inside.remoteid = Integer.toString(in.readInt());
			args[0] = new Link(inside, 0);
			inside.args[0] = new Link(this, 0);
			//inside����2��������³��ϡ��Ͷ��Υ��ȥ�ǽ�ü�����ۤ����褤���⡣
			args[1] = (Link)in.readObject();
		} else {
			args = (Link[])in.readObject();
		}
	}
}
