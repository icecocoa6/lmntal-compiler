package runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.QueuedEntity;
import util.Stack;

/**
 * �������쥯�饹���¹Ի��Ρ����׻��Ρ�����ˤ������ɽ����
 * todo �ʸ�Ψ������ ��Ŭ���Ѥˡ���¹�˥롼�������Ĥ��Ȥ��Ǥ��ʤ��ʼ¹Ի����顼��Ф��ˡ֥ǡ�����ץ��饹���롣
 * <p><b>��¾����</b></p>
 * <p>lockThread �ե�����ɤؤ������ʤ��ʤ����å��μ����������ˤȡ���å��������Ԥ���碌�Τ����
 * ���Υ��饹�Υ��󥹥��󥹤˴ؤ��� synchronized ������Ѥ��롣
 * �ե�����ɤؤ������򤹤���ϡ��ä˵��Ҥ��ʤ��¤ꤳ����Υ�å���������Ƥ���ɬ�פ����롣
 * @author Mizuno, n-kato
 */
public final class Membrane extends QueuedEntity
{
	/** ���졣��⡼�Ȥˤ���ʤ��RemoteMembrane���֥������Ȥ򻲾Ȥ��롣GlobalRoot�ʤ��null��
	 * ��������Ȥ��ϡ�����Υ�å���������Ƥ���ɬ�פ����롣
	 * null �����������=�����������˻��ϡ�������ȿ����ξ���Υ�å���������Ƥ���ɬ�פ����롣 */
	protected Membrane parent;
	/** ���ȥ�ν��� */
	protected AtomSet atoms = new AtomSet();
	/** ����ν��� */
	protected Set<Membrane> mems = null;
//	/** ���Υ���μ�ͳ��󥯤ο� */
//	protected int freeLinkCount = 0;
	/** �롼�륻�åȤν��硣 */
	protected List<Ruleset> rulesets = new ArrayList<Ruleset>();
	/** ��Υ����� */
	protected int kind = 0;
	public static final int KIND_ND = 2;
	/** true�ʤ�Ф�����ʲ���Ŭ�ѤǤ���롼�뤬̵�� */
	protected boolean stable = false;
	/** ��³�ե饰��true�ʤ�Х롼��Ŭ�ѤǤ��ʤ��Ƥ�stable�ˤʤ�ʤ���*/
	public boolean perpetual = false;
	/** ��������å����Ƥ��륹��åɡ���å�����Ƥ��ʤ��Ȥ���null�����äƤ��롣*/
	protected Thread lockThread = null;

	private static int nextId = 0;
	private int id;

	/** ����->(���ԡ����Υ��ȥ�in����->���ԡ���Υ��ȥ�in���ԡ����줿����)
	 * TODO ��Υ����ѿ��Ǥ����Τ��ɤ��� */
	HashMap<Membrane, Map<Atom, Atom>> memToCopyMap = null; 

	/** �������̾����intern���줿ʸ����ޤ���null�� */
	String name = null;
	public boolean equalName(String s){
		if(name == null && s == null)return true;
		else if(name != null && name != null)
			return name.equals(s);
		else return false;
	}
	public String getName() { return name; }
	public void setName(String name) { this.name = name; } // ���ͤ��Ǥޤä��饳�󥹥ȥ饯�����Ϥ��褦�ˤ��٤�����

	/** �¹ԥ��ȥॹ���å���
	 * ����ݤˤ�����Υ�å���������Ƥ���ɬ�פϤʤ���
	 * ��¾����ˤϡ�Stack ���󥹥��󥹤˴ؤ��� synchronized ������Ѥ��Ƥ��롣 */
	private Stack ready = null;

//	/** ��⡼�ȥۥ��ȤȤ��̿��Ǥ�����Υ��ȥ��Ʊ�ꤹ��Ȥ��˻��Ѥ����atomid��ɽ��
//	* <p>atomid (String) -> Atom
//	* <p>������Υ���å��������塢�������Ϣ³�����å�������Τ�ͭ����
//	* ����å����������˽�������졢����³����⡼�ȥۥ��Ȥ�����׵���᤹�뤿��˻��Ѥ���롣
//	* ��⡼�ȥۥ��Ȥ�����׵�ǿ��������ȥब���������ȡ���������NEW_�򥭡��Ȥ��륨��ȥ꤬�ɲä���롣
//	* $inside_proxy���ȥ�ξ�硢̿��֥�å��������Υ����ߥ󥰤ǥ�����ID�Ǿ�񤭤���롣
//	* $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
//	* @see Atom.remoteid */
//	protected HashMap atomTable = null;

	///////////////////////////////
	// ���󥹥ȥ饯��

	/** ���ꤵ�줿�������˽�°�������������롣newMem/newRoot ����ƤФ�롣*/
	private Membrane(Membrane parent) {
		mems = new HashSet<Membrane>();

		this.parent = parent;
		id = nextId++;
	}
	/** ���������ʤ����������롣Task.createFreeMembrane ����ƤФ�롣*/
	protected Membrane() {
		this(null);
	}

	/** ������Υ����Х�ID��������� */
	public String getMemID() { return Integer.toString(id); }
	
	
//	/** �����줬��°����׻��Ρ��ɤˤ����롢������λ��ꤵ�줿���ȥ��ID��������� */
//	public String getAtomID(Atom atom) { return atom.getLocalID(); }

	///////////////////////////////
	// ����μ���

	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	// 061028 okabe ��󥿥����ͣ��ĤʤΤ�Global/Local �ζ��̤�ɬ�פʤ�
//	/** ������Υ�����ID��������� */
//	public String getLocalID() {  //public�ʤΤ�LMNtalDaemon����Ƥ�Ǥ��뤫�颪�ƤФʤ��ʤä��Τ�protected�Ǥ褤
//	return Integer.toString(id);
//	}

	/** ����μ��� */
	public Membrane getParent() {
		return parent;
	}

	/** AtomSet����������Ǽ��� */
	// �ȤäƤ��꤬�ʤ��Τǥ����ȥ�����
//	public Atom[] getAtomSet() {
//	return (Atom[])atoms.toArray();
//	}
	/** 060727 */
	/** �롼�륻�åȿ������ */
	public int getRulesetCount() {
		return rulesets.size();
	}
	public int getMemCount() {
		return mems.size();
	}
	/** proxy�ʳ��Υ��ȥ�ο������
	 * todo ����̾���Ǥ����Τ��ɤ��� */
	public int getAtomCount() {
		return atoms.getNormalAtomCount();
	}
	/** ���ꤵ�줿�ե��󥯥����ĥ��ȥ�ο������*/
	public int getAtomCountOfFunctor(Functor functor) {
		return atoms.getAtomCountOfFunctor(functor);
	}
	/** ���Υ���μ�ͳ��󥯤ο������ */
	public int getFreeLinkCount() {
		return atoms.getAtomCountOfFunctor(Functor.INSIDE_PROXY);
	}
	/** ��Υ����פ���� */
	public int getKind() {
		return kind;
	}
	/** ��Υ����פ��ѹ� */
	public void changeKind(int k) {
		kind = k;
		if (kind == KIND_ND) {
			//�����Ū�¹���ϡ����̤���ˡ�Ǥϼ¹Ԥ��ʤ�
			dequeue();
			toStable();
		}
	}
	/** ������Ȥ��λ�¹��Ŭ�ѤǤ���롼�뤬�ʤ�����true */
	public boolean isStable() {
		return stable;
	}
	/** stable�ե饰��ON�ˤ��� 10/26���� Task#exec()��ǻȤ�*/
	void toStable(){
		stable = true;
	}
	/** ��³�ե饰��ON�ˤ��� */
	public void makePerpetual(boolean f) {
		perpetual = f;
	}
//	/** ��³�ե饰��OFF�ˤ��� */
//	public void makeNotPerpetual() {
//	AbstractLMNtalRuntime machine = getTask().getMachine();
//	synchronized(machine) {
//	perpetual = false;
//	machine.notify();
//	}
//	}
	/** ������˥롼�뤬�����true */
	public boolean hasRules() {
		return !rulesets.isEmpty();
	}
	public boolean isNondeterministic() {
		return kind == KIND_ND;
	}

	// ȿ����

	public Object[] getAtomArray() {
		return atoms.toArray();
	}
	public Object[] getMemArray() {
		if(mems.isEmpty()){
			return null;
		}
		return mems.toArray();
	}
	/** 06/07/27 */
	/** �롼�륻�åȤΥ��ԡ������ */
	public ArrayList<Ruleset> getRuleset() {
		ArrayList<Ruleset> al = new ArrayList<Ruleset>(rulesets);
		return al;
	}
	/** ����Υ��ԡ������ */
	public HashSet<Membrane> getMemCopy() {
		return new HashSet<Membrane>(mems);
		//RandomSet s = new RandomSet();
		//s.addAll(mems);
		//return s;
	}
	/** ������ˤ��륢�ȥ��ȿ���Ҥ�������� */
	public Iterator<Atom> atomIterator() {
		return atoms.iterator();
	}
	/** ������ˤ�������ȿ���Ҥ�������� */
	public Iterator<Membrane> memIterator() {
		return mems.iterator();
	}
	/** ̾��func����ĥ��ȥ��ȿ���Ҥ�������� */
	public Iterator<Atom> atomIteratorOfFunctor(Functor functor) {
		return atoms.iteratorOfFunctor(functor);
	}
	/** ������ˤ���롼�륻�åȤ�ȿ���Ҥ��֤� */
	public Iterator<Ruleset> rulesetIterator() {
		return rulesets.iterator();
	}

	AtomSet getAtoms() {
		return atoms;
	}

	/** �����������פ�k�λ��������������������� */
	public Membrane newMem(int k){
		//		if (remote != null) {
		//		remote.send("NEWMEM",this);
		//		return null; // todo
		//	}
		Membrane m = new Membrane(this);
		m.changeKind(k);
		mems.add(m);
		// �����Ʊ���¹��쥹���å����Ѥ�
		if (k != KIND_ND)
			stack.push(m);
		return m;
	}
	/** �������ǥե���ȥ����פλ��������������������� */
	public Membrane newMem() {
		return newMem(0);
	}

	/** �ǥХå��� */
	String getReadyStackStatus() { return ready.toString(); }

	/** �¹ԥ��ȥॹ���å�����Ƭ�Υ��ȥ����������¹ԥ��ȥॹ���å�������� */
	Atom popReadyAtom() {
		if(null == ready){ return null; }
		Atom atom = (Atom)ready.pop();
		if(ready.isEmpty()){ ready = null; }
		return atom;
	}

	// hash ��������

	/* ��Υϥå��女���ɤ��֤� */
	static int calculate(Membrane m) {
		return calculate(m, new HashMap<Membrane, Integer>());
	}

	/*
	 * ��Υϥå��女���ɤ��֤�
	 * @param m �ϥå��女���ɻ����оݤ���
	 * @param m2hc m�λ��줫��ϥå��女���ɤؤΥޥåס�����Υϥå��女���ɻ��Ф򷫤��֤��ʤ�����˻Ȥ���
	 */
	private static int calculate(Membrane m, Map<Membrane, Integer> m2hc) {
		//System.out.println("membrane:" + m);
		final long MAX_VALUE = Integer.MAX_VALUE;
		/*
		 * add: m���ʬ�ҤΥϥå��女���ɤ��û�����Ƥ����ѿ�
		 * mult: m���ʬ�ҤΥϥå��女���ɤ��軻����Ƥ����ѿ�
		 */
		long add = 3412;        // 3412��Ŭ���ʽ����
		long mult = 3412;

		/* ����ѿ� */
		Atom a = null;
		Membrane mm = null;
		QueuedEntity q = null;

		/*
		 * contents:��������Υ��ȥ�Ȼ������Τν���
		 * toCalculate:���߷׻����ʬ�����̤�������ȥ�ޤ��ϻ���ν���
		 * calculated:���߷׻����ʬ����ν����ѥ��ȥ�ޤ��ϻ���ν���
		 */
		Set<QueuedEntity> contents = new HashSet<QueuedEntity>(), 
		toCalculate = new HashSet<QueuedEntity>(), 
		calculated = new HashSet<QueuedEntity>();

		for (Iterator<Atom> i = m.atomIterator(); i.hasNext(); ) {
			a = i.next();
			if (a.getFunctor().isOutsideProxy() || a.getFunctor().isInsideProxy()) {
				continue;
			}
			contents.add(a);
		}

		for (Iterator<Membrane> i = m.memIterator(); i.hasNext(); ) {
			mm = i.next();
			contents.add(mm);
			m2hc.put(mm, calculate(mm, m2hc));
		}

		while (!contents.isEmpty()) {
			//System.out.println("uncalculated:" + contents);
			q = contents.iterator().next();
			contents.remove(q);

			/*
			 * mol: ����ʬ�ҤΥϥå��女���ɤ��ݻ�����
			 * mol_add: ���ܷ׻�ñ�̤Υϥå��女���ɤ��û�����Ƥ����ѿ�
			 * mol_mult: ���ܷ׻�ñ�̤Υϥå��女���ɤ��軻����Ƥ����ѿ�
			 * temp: ���ܷ׻�ñ�̤Υϥå��女���ɤ��ݻ�����
			 */

			long mol = -1, mol_add = 0, mol_mult = 41, temp = 0;

			toCalculate.clear();
			calculated.clear();
			toCalculate.add(q);

			// ʬ�ҤΥϥå��女���ɤη׻�
			while (!toCalculate.isEmpty()) {
				q = toCalculate.iterator().next();
				calculated.add(q);
				toCalculate.remove(q);

				if (q instanceof Atom) {
					a = (Atom) q;
					temp = a.getFunctor().hashCode();

					// ���Υ��ȥ�Υ�󥯤��������
					int arity = a.getFunctor().getArity();
					for (int k = 0; k < arity; k++) {
						temp *= 31;
						Link link = a.getArg(k);
						if (link.getAtom().getFunctor().isInsideProxy()) {
							Atom inside = link.getAtom();
							int pos = link.getPos() + 1;
							temp += (inside.getFunctor().hashCode() * pos);	
						} else if (link.getAtom().getFunctor().isOutsideProxy()) { // ����褬����ξ��
							/*
							 * ����襢�ȥ�˻��ޤǴӤ�������Υϥå��女���ɤ�
							 * �ǽ�Ū�ʥ���襢�ȥ�Υϥå��女���ɤ�
							 * ���Υ��ȥ�ΰ����ֹ���Ȥ�
							 * ���Υ��ȥफ�����ؤΥ�󥯤�ɽ�������Ȥ��롣
							 */
							int t = 0;
							mm = link.getAtom().nthAtom(0).getMem();
							if (!calculated.contains(mm)) {
								toCalculate.add(mm);
							}
							while (link.getAtom().getFunctor().isOutsideProxy()) {
								link = link.getAtom().nthAtom(0).getArg(1);
								mm = link.getAtom().getMem();
								t += m2hc.get(mm);
								t *= 13;
							}

							t *= link.getAtom().getFunctor().hashCode();
							t *= link.getPos() + 1;
							temp += t;
						} else { // ����褬�ץ����ʳ��Υ��ȥ�ξ��
							Atom linked = link.getAtom();
							if (!calculated.contains(linked)) {
								toCalculate.add(linked);
							}
							int pos = link.getPos() + 1;
							// ��³��ΰ����ֹ��ϥå��女���ɤ˴�Ϳ������
							temp += (linked.getFunctor().hashCode() * pos);
						}
					}
				} else {
					Membrane mt = (Membrane) q;
					final int thisMembsHC = m2hc.get(mt);
					temp = thisMembsHC;

					// �����줫����γ����ؤΥ�󥯤��������
					Link link = null;
					for (Iterator<Atom> i = mt.atomIteratorOfFunctor(Functor.INSIDE_PROXY); i.hasNext(); ) {
						Atom inside =  i.next();
						// �����쳰���Ρʥץ����Ǥʤ��˥���襢�ȥ�ޤǥȥ졼��
						int s = 0;
						link = inside.nthAtom(0).getArg(1);

						if (link.getAtom().getFunctor().isOutsideProxy()) { // ������Υ���褬��ΤȤ�
							mm = link.getAtom().nthAtom(0).getMem();
							if (!calculated.contains(mm)) {
								toCalculate.add(mm);
							}
						} else { // ������Υ���褬���ȥ�ξ��
							a = link.getAtom();
							if (!calculated.contains(a)) {
								toCalculate.add(a);
							}
						}

						while (link.getAtom().getFunctor().isOutsideProxy()) {
							link = link.getAtom().nthAtom(0).getArg(1);
							s += m2hc.get(link.getAtom().getMem());
							s *= 13;
						}
						s += link.getAtom().getFunctor().hashCode();
						s *= link.getPos() + 1;

						// �����������Ρʥץ����Ǥʤ��˥�󥯸����ȥ�ޤǥȥ졼��
						int t = 0;
						link = inside.getArg(1);
						while (link.getAtom().getFunctor().isOutsideProxy()) {
							link = link.getAtom().nthAtom(0).getArg(1);
							t += m2hc.get(link.getAtom().getMem());
							t *= 13;
						}
						t *= link.getAtom().getFunctor().hashCode();
						t *= link.getPos() + 1;
						temp += thisMembsHC^t * s;
					}
				}

				mol_add += temp;
				mol_add %= MAX_VALUE;
				mol_mult *= temp;
				mol_mult %= MAX_VALUE;
			}
			mol = mol_add^mol_mult;
			//System.out.println("molecule: " + calculated + " = " + mol);
			/* �ϥå��女���ɤ򻻽Ф���ʬ�Ҥ�׻��оݤ�������� */
			contents.removeAll(calculated);

			add += mol;
			add %= MAX_VALUE;
			mult *= mol;
			mult %= MAX_VALUE;
		}

		//System.out.println("membrane:" + m + " = " + (mult^add) + " (mult=" + mult + ", add=" + add + ")");
		return (int) (mult^add);
	}
}
