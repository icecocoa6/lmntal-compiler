package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.QueuedEntity;
import util.Stack;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
class Atom extends QueuedEntity {
	/** ���졣Membrane���饹��addAtom�᥽�å���ǹ������롣 */
	private AbstractMembrane mem;
	/** ̾�� */
	private Functor functor;
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	private int id;
	
	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ����
	 */
	Atom(AbstractMembrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new Link[functor.getArity()];
		id = lastId++;
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
	/** ̾���μ��� */
	Functor getFunctor(){
		return functor;
	}
	String getName() {
		return functor.getName();
	}
	/** ��󥯿������ */
	int getArity() {
		return functor.getArity();
	}
	Link getLastArg() {
		return args[getArity() - 1];
	}
	AbstractMembrane getMem() {
		return mem;
	}
	
	void remove() {
		mem.removeAtom(this);
		mem = null;
	}
}



/**
 * ��󥯤���³��򡢥��ȥ�Ȱ����ֹ���ȤȤ���ɽ����LMNtal�Υ�󥯤ˤ�������̵���Τǡ�
 * ���ĤΥ�󥯤��Ф��Ƥ��Υ��饹�Υ��󥹥��󥹤򣲤Ļ��Ѥ��롣
 */
final class Link {
	/** �����Υ��ȥ� */
	private Atom atom;
	/** ����褬�貿������ */
	private int pos;

	private static int lastId = 0;
	private int id;
	///////////////////////////////
	// ���󥹥ȥ饯��
	
	Link(Atom atom, int pos) {
		set(atom, pos);
		id = lastId++;
	}

	///////////////////////////////
	// ����μ���

	/** �Фˤʤ룲�ĤΥ�󥯤�id�Τ������㤤�����󥯤��ֹ�Ȥ��ƻ��Ѥ��롣 */
	public String toString() {
		int i;
		if (this.id < atom.args[pos].id) {
			i = this.id;
		} else {
			i = atom.args[pos].id;
		}
		return "_" + i;
	}
				
	/** �����Υ��ȥ��������� */
	Atom getAtom() {
		return atom;
	}
	/** �����ΰ����ֹ��������� */
	int getPos() {
		return pos;
	}
	/** ���Υ�󥯤��Ф�ʤ��ո����Υ�󥯤�������� */
	Link getBuddy() {
		return atom.args[pos];
	}
	/** ����褬�ǽ���󥯤ξ���true���֤� */
	boolean isFuncRef() {
		return atom.getArity() - 1 == pos;
	}

	///////////////////////////////
	// ���
	/**
	 * ��³������ꤹ�롣
	 * �쥯�饹�Υ������ѥ᥽�å���ǤΤ߸ƤӽФ���롣
	 */
	void set(Atom atom, int pos) {
		this.atom = atom;
		this.pos = pos;
	}
	/**
	 * ���Υ�󥯤���³���Ϳ����줿��󥯤���³���Ʊ���ˤ��롣
	 * �쥯�饹�Υ������ѥ᥽�å���ǤΤ߸ƤӽФ���롣
	 */
	void set(Link link) {
		this.atom = link.atom;
		this.pos = link.pos;
	}
}

final class Machine extends AbstractMachine {
	/** �¹��쥹���å� */
	Stack memStack = new Stack();
	boolean idle;
	static final int maxLoop = 10;
	
	Machine() {
		root = new Membrane(this);
		memStack.push(root);
		idle = false;
	}
	
	boolean isIdle(){
		return idle;
	}
	void exec() {
		if(memStack.isEmpty()){ // ���ʤ�idle�ˤ��롣
			idle = true;
			return;
		}
		// �¹��쥹���å������Ǥʤ�
		Membrane mem = (Membrane)memStack.peek();
		if(!mem.lock(mem)) return; // ��å�����
		
		Atom a;
		for(int i=0; i < maxLoop && mem == memStack.peek(); i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
			
			a = mem.popReadyAtom();
			Iterator it = mem.rulesetIterator();
			boolean flag;
			if(a != null){ // �¹��쥹���å������Ǥʤ��Ȥ�
				flag = false;
				while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
					if(((Ruleset)it.next()).react(mem, a)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					if(!mem.isRoot()) mem.getMem().enqueueAtom(a);
				}
				else {}// �����ƥॳ���륢�ȥ�ʤ����ˤĤߡ�����������
			}else{ // �¹��쥹���å������λ�
				flag = false;
				while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
					if(((Ruleset)it.next()).react(mem)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					memStack.pop(); // �����pop
					// ���줬root�줫�Ŀ������Ĥʤ顢����������
					if(mem.isRoot() && mem.getMem() != null)
						((Membrane)mem.getMem()).activate();
					it = mem.memIterator();
					// ���줬����stable�ʤ顢�������stable�ˤ��롣
					flag = false;
					while(it.hasNext()){
						if(((Membrane)it.next()).isStable() == false)
								flag = true;
					}
					if(flag == false) mem.toStable();
				}
			}
		}
		// ���줬�Ѥ�ä�or�����������֤����顢��å���������ƽ�λ
		mem.unlock();
	}
}
abstract class AbstractMachine {
	/** �롼���� */
	protected AbstractMembrane root;
	
	/** �롼����μ��� */
	AbstractMembrane getRoot() {
		return root;
	}
}

/** �׻��Ρ��� */
final class LMNtalRuntime {
	List machines = new ArrayList();
	
	/** �׻��Ρ��ɤ����ĥޥ������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  machines���Ѥޤ줿��˼¹Ԥ��롣�ƥޥ���ͥ��ˤ��뤿��ˤ�
	 *  �ޥ����ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */
	void exec() {
		boolean allIdle;
		Iterator it;
		Machine m;
		do{
			allIdle = true; // idle�Ǥʤ��ޥ��󤬸��Ĥ��ä���false�ˤʤ롣
			it = machines.iterator();
			while(it.hasNext()){
				m = (Machine)it.next();
				if(!m.isIdle()){ // idle�Ǥʤ��ޥ��󤬤��ä���
					m.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ��ޥ��󤬤���
					break;
				}
			}
		}while(!allIdle);
	}
	
	Machine newMachine() {
		Machine m = new Machine();
		machines.add(m);
		return m;
	}
}

/** �ʤ������줬̵����javadoc������Ǥ��ʤ� */
class DataStructure {}
