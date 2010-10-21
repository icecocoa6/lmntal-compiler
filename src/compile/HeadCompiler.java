/*
 * ������: 2003/10/28
 *
 */
package compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import runtime.Env;
import runtime.Functor;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.SpecialFunctor;
import util.Util;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.ProcessContextEquation;

/**
 * ����Ф���ޥå���̿�������Ϥ���
 * @author n-kato, pa
 * 
 * ������ID���ѻߤ��줿��
 * Ruby�ǤǤϲ�����ID�����Τޤޥܥǥ�̿����β������ֹ��ɽ���Ƥ����������ϰ㤦�Τǡ�������ID���Ȥˤϰ�̣��̵���ʤä����ᡣ
 * �����ѻߤ�ȼ�äơ�������ID�ǥ롼�פ�����ʬ������atoms.iterator()��Ȥ��褦���ѹ����롣
 * 
 * todo path�Ȥ���̿̾��������������ɽ���Ƥ��ʤ����ºݤ�varnum������Ĺ���Τ�id�ˤ��褦���Ȥ�פäƤ���
 * 
 * <p><b>����</b>��
 * ���ߥޥå���̿����Ǥϡ�������ѿ��ֹ��0����Ƴ���륢�ȥ���ѿ��ֹ��1�ˤ��Ƥ��롣����Ϻ�����Ѥ��ʤ���
 * �ܥǥ�̿����β������Ǥϡ����mems����󤷤Ƥ��顢����³�����ѿ��ֹ��atoms����󤷤Ƥ��롣
 */
class HeadCompiler {
	private boolean debug = false; //���Ū
	private boolean debug2 = false;
	boolean firsttime = true;
//	/** ������ */
//	public Membrane lhsmem;//m;
	/** �ޥå���̿����ʤΥ�٥��*/
	InstructionList matchLabel, tempLabel;
	/** matchLabel.insts */
	List<Instruction> match, tempMatch;

	List<Membrane> mems	= new ArrayList<Membrane>();	// �и�������Υꥹ�ȡ�[0]��m
	List<Atomic> atoms = new ArrayList<Atomic>();	// �и����륢�ȥ�Υꥹ��	
	HashMap<Membrane, Integer> memPaths	= new HashMap<Membrane, Integer>();	// Membrane -> �ѿ��ֹ�
	HashMap<Atomic, Integer>  atomPaths	= new HashMap<Atomic, Integer>();	// Atomic -> �ѿ��ֹ�
	Map<Integer, int[]> linkPaths = new HashMap<Integer, int[]>();	// Atom���ѿ��ֹ� -> ��󥯤��ѿ��ֹ������

	private Map<Atom, Integer> atomIds	= new HashMap<Atom, Integer>();	// Atom -> atoms���index���ѻߤ������Ǹ�Ƥ�����
	private HashSet<Atom> visited = new HashSet<Atom>();	// Atom -> boolean, �ޥå���̿��������������ɤ���
	private HashSet<Membrane> memVisited = new HashSet<Membrane>();	// Membrane -> boolean, compileMembrane��Ƥ�����ɤ���

	boolean fFindDataAtoms;						// �ǡ������ȥ��findatom���Ƥ褤���ɤ���
	private final boolean UNTYPED_COMPILE	= false;			// fFindDataAtoms�ν����

	int varCount;	// �����쥢�ȥ�����ʬ����٤����Ȼפ�
	int maxVarCount;

	private static int findAtomCount = 0;
	private static int anyMemCount = 0;

	private HashMap<Membrane, ProcessContextEquation> proccxteqMap = new HashMap<Membrane, ProcessContextEquation>(); // Membrane -> ProcessContextEquation

	final boolean isAtomLoaded(Atomic atom) { return atomPaths.containsKey(atom); }
	final boolean isMemLoaded(Membrane mem) { return memPaths.containsKey(mem); }

	protected final int atomToPath(Atomic atom) { 
		if (!isAtomLoaded(atom)) return UNBOUND;
		return atomPaths.get(atom);
	}
	protected final int memToPath(Membrane mem) {
		if (!isMemLoaded(mem)) return UNBOUND;
		return memPaths.get(mem);
	}
	protected final int linkToPath(int atomid, int pos) { // todo HeadCompiler�λ��ͤ˹�碌�롩GuardCompiler�⡣
		if (!linkPaths.containsKey(new Integer(atomid))) return UNBOUND;
		return linkPaths.get(atomid)[pos];
	}

	protected static final int UNBOUND = -1;

	HeadCompiler() {
	}

	/** �����������浪��ӥܥǥ��Υ���ѥ���ǻȤ�����ˡ�
	 * this����ꤵ�줿hc���Ф������������줿HeadCompiler�Ȥ��롣
	 * �������Ȥϡ����դ����ƤΥ��ȥप�������Ф��ơ�������/�ܥǥ��Ѥβ������ֹ��
	 * �ѿ��ֹ�Ȥ��ƺ��դΥޥå��󥰤��꽪��ä��������֤���Ĥ褦�ˤ��뤳�Ȥ��̣���롣*/
	protected final void initNormalizedCompiler(HeadCompiler hc) {
		matchLabel = new InstructionList();
		match = matchLabel.insts;
		mems.addAll(hc.mems);
		atoms.addAll(hc.atoms);
		varCount = 0;
		for(Membrane mem : mems){
			memPaths.put(mem, new Integer(varCount++));
		}
		Iterator it = atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			atomPaths.put(atom, new Integer(varCount));
			atomIds.put(atom, new Integer(varCount++));
			visited.add(atom);
		}
	}

	/** ������������Υ���ѥ���ǻȤ������this���Ф������������줿HeadCompiler����������֤���
	 * �������Ȥϡ����դ����ƤΥ��ȥप�������Ф��ơ�������/�ܥǥ��Ѥβ������ֹ��
	 * �ѿ��ֹ�Ȥ��ƺ��դΥޥå��󥰤��꽪��ä��������֤���Ĥ褦�ˤ��뤳�Ȥ��̣���롣*/
	final HeadCompiler getNormalizedHeadCompiler() {
		HeadCompiler hc = new HeadCompiler();
		hc.initNormalizedCompiler(this);
		return hc;
	}
	/** ��mem�λ�¹�����ƤΥ��ȥ����򡢤��줾��ꥹ��atoms��mems���ɲä��롣
	 * �ꥹ������ɲä��줿���֤����Υ��ȥप�����β�����ID�ˤʤ롣*/
	void enumFormals(Membrane mem) {
		Env.c("enumFormals");
		for (Atom atom : mem.atoms) {
			// ���դ˽и��������ȥ����Ͽ����
			atomIds.put(atom, new Integer(atoms.size()));
			atoms.add(atom);
		}
		mems.add(mem);	// �����mems[0]
		for (Membrane m : mem.mems) {
			enumFormals(m);
		}
	}

	void prepare() {
		Env.c("prepare");
		memPaths.clear();
		atomPaths.clear();
		visited.clear();
		memVisited.clear();
		matchLabel = new InstructionList();
		tempLabel = new InstructionList();
		match = matchLabel.insts;
		tempMatch = tempLabel.insts;
		varCount = 1;	// [0]������
//		mempaths.put(mems.get(0), new Integer(0));	// ������ѿ��ֹ�� 0
		fFindDataAtoms = UNTYPED_COMPILE;
	}

	/**
	 * ���ꤵ�줿���ȥ���Ф���getlink��Ԥ����ѿ��ֹ��linkpaths����Ͽ���롣
	 * RISC����ȼ���ɲ�(mizuno)
	 */
	final void getLinks(int atompath, int arity, List<Instruction> insts) {
		int[] paths = new int[arity];
		for (int i = 0; i < arity; i++) {
			paths[i] = varCount;
			insts.add(new Instruction(Instruction.GETLINK, varCount, atompath, i));
			varCount++;
		}
		linkPaths.put(new Integer(atompath), paths);
	}
	private void searchLinkedGroup(Atom startatom, HashSet<Atom> qatoms, Atom firstatom, Membrane firstmem) {
		LinkedList<Membrane> newmemlist = new LinkedList<Membrane>();
		LinkedList<Atom> atomqueue = new LinkedList<Atom>();
		atomqueue.add(startatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = atomqueue.removeFirst();			
			if(atom.functor.getArity()==0 && atom.mem==firstmem){
				qatoms.add(atom);
				firstmem.connect(firstatom, atom);
			}
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence buddylink = atom.args[pos].buddy;
				if (buddylink == null) {
					if(atom.mem==firstmem){
						qatoms.add(atom);
						firstmem.connect(firstatom, atom);
					}
					continue;
				} // ������ƿ̾��󥯤�̵��
				if (!atomIds.containsKey(buddylink.atom)) {
					if(atom.mem==firstmem){
						firstmem.connect(firstatom, atom);
						qatoms.add(atom);
					}
					continue;
				} // ���դ�$p�ʤ����lhs->neg�ˤؤΥ�󥯤�̵��
				Atom buddyatom = (Atom)buddylink.atom;

				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ����硣
					// - �����Υ��ȥ�buddyatom�Ȱ����˼����������ȥ��Ʊ�����򸡺����롣
					//   �������������ѿ��ֹ椪��Ӱ����ֹ���Ȥ˴�Ť����������ΤߤǤ褤��
					// neg(�������եȥåץ�٥�)->lhs(���դ���ȥåץ�٥�)�ΤȤ�
					if (proccxteqMap.containsKey(atom.mem)
							&& !proccxteqMap.containsKey(buddyatom.mem)
							&& buddyatom.mem.parent != null) {
						// just skip
					}
					else {
						// lhs(>)->lhs(<) �ޤ��� neg(>)->neg(<) �ʤ�С�
						// ���Ǥ�Ʊ�������ǧ���륳���ɤ���Ϥ��Ƥ��뤿�ᡢ���⤷�ʤ�
						int b = atomToPath(buddyatom);
						int t = atomToPath(atom);
						if (b < t) continue;
						if (b == t && buddylink.pos < pos) continue;
					}
				}
				// �����Υ��ȥ�򿷤����ѿ��˼������� (*A)
				int buddyatompath = varCount++;

				// ����褬¾���������դΥ��ȥ�ξ��������֥�󥯤ξ���
				// ��֤μ�ͳ��󥯴������ȥ຿�θ����򤷡��쳬�ؤ��ޥå����뤫������Ԥ���
				// �ޤ�*A��DEREF����4���������buddyatompath���������롣
				if (proccxteqMap.containsKey(atom.mem)
						&& proccxteqMap.containsKey(buddyatom.mem) && buddyatom.mem != atom.mem) {				
					// ( 0: 1:{$p[|*X],2:{$q[|*Y]}} :- \+($p=(atom(L),$pp),$q=(buddy(L),$qq)) | ... )
					// ���Υ롼��Υ����ɤΰ�̣:
					// ( 0: 1:{atom(L),$pp[|*XX],2:{buddy(L),$qq[|*YY]}} :- ... ) �ˤϥޥå����ʤ�
					//
					LinkedList<Membrane> atomSupermems  = new LinkedList<Membrane>(); // atom�ι�����������ʿ���¦����Ƭ��
					LinkedList<Membrane> buddySupermems = new LinkedList<Membrane>(); // buddy�ι�����������ʿ���¦����Ƭ��
					// ������������η׻�
					// atomSupermems = {0,1}; buddySupermems = {0,1,2}
					Membrane mem = proccxteqMap.get(buddyatom.mem).def.lhsOcc.mem;
					while (mem != null) {
						buddySupermems.addFirst(mem);
						mem = mem.parent;
					}
					mem = proccxteqMap.get(atom.mem).def.lhsOcc.mem;
					while (mem != null) {
						atomSupermems.addFirst(mem);
						mem = mem.parent;
					}
					// ������������ζ�����ʬ���
					// atomSupermems = {}; buddySupermems = {2}
					Iterator<Membrane> ita = atomSupermems.iterator();
					Iterator<Membrane> itb = buddySupermems.iterator();
					while (ita.hasNext() && itb.hasNext() && ita.next() == itb.next()) {
						ita.remove();
						itb.remove();
					}

				}

				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ�����
					// lhs(<)->lhs(>), neg(<)->neg(>), neg->lhs �ʤΤǥ����Υ��ȥ��Ʊ�������ǧ
					continue;
				}

				// �����Υ��ȥ���ѿ��˼�������

				atomPaths.put(buddyatom, new Integer(buddyatompath));
				if(buddyatom.mem == firstmem){
					qatoms.add(buddyatom);
					firstmem.connect(firstatom, buddyatom);
				}
				atomqueue.addLast( buddyatom );

				// �������������
				if (atom.functor.isOutsideProxy() && pos == 0) {
					// ����ؤΥ�󥯤ξ�硢�����Ʊ�����򸡺����ʤ���Фʤ�ʤ�
					Membrane buddymem = buddyatom.mem;				
					int buddymempath = memToPath(buddyatom.mem);
					if (buddymempath == UNBOUND) {
						buddymempath = varCount++;
						memPaths.put(buddymem, new Integer(buddymempath));
						newmemlist.add(buddymem);
						connectAtomMem(firstatom, buddymem);
					}
				}
			}
		}
		// ���Ĥ��ä�����������ˤ��륢�ȥ��ͥ��Ū�˸������롣
		for(Membrane mem : newmemlist){
			connectAtomMem(firstatom, mem);
			for(Atom atom : mem.atoms){
				searchMembrane(mem, qatoms, firstatom, firstmem);
			}
		}
	}

	private void searchMembrane(Membrane mem, HashSet<Atom> qatoms, Atom firstatom, Membrane firstmem) {
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);

		int thismempath = memToPath(mem);
		for(Atom atom : mem.atoms){
			if (!atom.functor.isActive() && !fFindDataAtoms) continue;
			if (atomToPath(atom) != UNBOUND) continue;
			// ���Ĥ��ä����ȥ���ѿ��˼�������
			int atompath = varCount++;
			// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
			Membrane[] testmems = { mem };
			if (proccxteqMap.containsKey(mem)) {
				// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
				testmems = new Membrane[]{ mem, proccxteqMap.get(mem).def.lhsOcc.mem };
			}
			atomPaths.put(atom, new Integer(atompath));
			searchLinkedGroup(atom, qatoms, firstatom, firstmem);
		}
		for(Membrane submem : mem.mems){
			int submempath = memToPath(submem);
			if (submempath == UNBOUND) {
				// ������ѿ��˼�������
				submempath = varCount++;
				memPaths.put(submem, new Integer(submempath));
			}
			//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
			searchMembrane(submem, qatoms, firstatom, firstmem);
		}
	}

	/** ��󥯤ǤĤʤ��ä����ȥप��Ӥ��ν�°����Ф��ƥޥå��󥰤�Ԥ���
	 * �ޤ�������Ǹ��Ĥ��ä��ֿ�������פΤ��줾����Ф��ơ�compileMembrane��Ƥ֡�
	 */
	void compileLinkedGroup(Atom startatom, InstructionList list) {
		Env.c("compileLinkedGroup");
		//if(debug2)Util.println("compileLinkedGroup called ; startatom :" + startatom + " list :" + list.insts);
		List<Instruction> insts = list.insts;
		LinkedList<Membrane> newmemlist = new LinkedList<Membrane>();
		LinkedList<Atom> atomqueue = new LinkedList<Atom>();
		if(debug)Util.println("start "+startatom);
		atomqueue.add(startatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = atomqueue.removeFirst();			
			if(debug) Util.println("before " + atom);
			if (visited.contains(atom)) continue;
			if(debug) Util.println("after " + atom);
			visited.add(atom);

			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence buddylink = atom.args[pos].buddy;
				if (buddylink == null) continue; // ������ƿ̾��󥯤�̵��
				if (!atomIds.containsKey(buddylink.atom)) continue; // ���դ�$p�ʤ����lhs->neg�ˤؤΥ�󥯤�̵��
				Atom buddyatom = (Atom)buddylink.atom;

				if(debug)Util.println("proc1 " + atom);
				if(debug)Util.println("proc1 " + buddyatom);
				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ����硣
					// - �����Υ��ȥ�buddyatom�Ȱ����˼����������ȥ��Ʊ�����򸡺����롣
					//   �������������ѿ��ֹ椪��Ӱ����ֹ���Ȥ˴�Ť����������ΤߤǤ褤��
					// neg(�������եȥåץ�٥�)->lhs(���դ���ȥåץ�٥�)�ΤȤ�
					if (proccxteqMap.containsKey(atom.mem)
							&& !proccxteqMap.containsKey(buddyatom.mem)
							&& buddyatom.mem.parent != null) {
						// just skip
						if(debug)Util.println("proc2 " + atom);
					}
					else {
						if(debug)Util.println("proc3 " + atom);
						// lhs(>)->lhs(<) �ޤ��� neg(>)->neg(<) �ʤ�С�
						// ���Ǥ�Ʊ�������ǧ���륳���ɤ���Ϥ��Ƥ��뤿�ᡢ���⤷�ʤ�
						int b = atomToPath(buddyatom);
						int t = atomToPath(atom);
						if (b < t) continue;
						if (b == t && buddylink.pos < pos) continue;
					}
					if(debug)Util.println("proc4 " + atom);
				}
				if(debug)Util.println("proc5 " + atom);
				// �����Υ��ȥ�򿷤����ѿ��˼������� (*A)
				int buddyatompath = varCount++;
				insts.add( new Instruction(Instruction.DEREF,
						buddyatompath, atomToPath(atom), pos, buddylink.pos ));

				// ����褬¾���������դΥ��ȥ�ξ��������֥�󥯤ξ���
				// ��֤μ�ͳ��󥯴������ȥ຿�θ����򤷡��쳬�ؤ��ޥå����뤫������Ԥ���
				// �ޤ�*A��DEREF����4���������buddyatompath���������롣
				if (proccxteqMap.containsKey(atom.mem)
						&& proccxteqMap.containsKey(buddyatom.mem) && buddyatom.mem != atom.mem) {				
					// ( 0: 1:{$p[|*X],2:{$q[|*Y]}} :- \+($p=(atom(L),$pp),$q=(buddy(L),$qq)) | ... )
					// ���Υ롼��Υ����ɤΰ�̣:
					// ( 0: 1:{atom(L),$pp[|*XX],2:{buddy(L),$qq[|*YY]}} :- ... ) �ˤϥޥå����ʤ�
					int firstindex = insts.size() - 1; // atom�����DEREF̿���ؤ�
					//
					LinkedList<Membrane> atomSupermems  = new LinkedList<Membrane>(); // atom�ι�����������ʿ���¦����Ƭ��
					LinkedList<Membrane> buddySupermems = new LinkedList<Membrane>(); // buddy�ι�����������ʿ���¦����Ƭ��
					// ������������η׻�
					// atomSupermems = {0,1}; buddySupermems = {0,1,2}
					Membrane mem = proccxteqMap.get(buddyatom.mem).def.lhsOcc.mem;
					while (mem != null) {
						buddySupermems.addFirst(mem);
						mem = mem.parent;
					}
					mem = proccxteqMap.get(atom.mem).def.lhsOcc.mem;
					while (mem != null) {
						atomSupermems.addFirst(mem);
						mem = mem.parent;
					}
					// ������������ζ�����ʬ���
					// atomSupermems = {}; buddySupermems = {2}
					{
						Iterator<Membrane> ita = atomSupermems.iterator();
						Iterator<Membrane> itb = buddySupermems.iterator();
						while (ita.hasNext() && itb.hasNext() && ita.next() == itb.next()) {
							ita.remove();
							itb.remove();
						}
					}
					// �������������̿������Ѵ���buddyatompath����������
					while (!atomSupermems.isEmpty()) {
						mem = atomSupermems.removeLast();
						insts.add( new Instruction(Instruction.FUNC, buddyatompath, Functor.INSIDE_PROXY) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					while (!buddySupermems.isEmpty()) {
						mem = buddySupermems.removeFirst();
						insts.add( new Instruction(Instruction.FUNC, buddyatompath, new SpecialFunctor("$out",2, mem.kind) ) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						insts.add( new Instruction(Instruction.TESTMEM, memToPath(mem), buddyatompath + 1) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					varCount = buddyatompath + 1;
					int lastindex = insts.size() - 1; // buddyatom��������뤿���DEREF̿���ؤ�

					// deref̿�����4������������					
					// - deref [-tmp1atom,atom,atompos,buddypos] ==> deref [-tmp1atom,atom,atompos,1]
//					((Instruction)insts.get(firstindex)).setArg4(new Integer(1));
					Instruction oldfirst = insts.remove(firstindex);
					Instruction newfirst = new Instruction(Instruction.DEREF,
							oldfirst.getIntArg1(), oldfirst.getIntArg2(), oldfirst.getIntArg3(), 1);
					insts.add(firstindex,newfirst);
					// - deref [-buddyatom,tmpatom,tmppos,1] ==> deref [-buddyatom,buddypos,atompos,buddypos]
//					((Instruction)insts.get(lastindex)).setArg4(new Integer(buddylink.pos));
					Instruction oldlast = insts.remove(lastindex);
					Instruction newlast = new Instruction(Instruction.DEREF,
							oldlast.getIntArg1(), oldlast.getIntArg2(), oldlast.getIntArg3(), buddylink.pos);
					insts.add(lastindex,newlast);
				}

				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ�����
					// lhs(<)->lhs(>), neg(<)->neg(>), neg->lhs �ʤΤǥ����Υ��ȥ��Ʊ�������ǧ
					insts.add( new Instruction(Instruction.EQATOM,
							buddyatompath, atomToPath(buddyatom) ));
					continue;
				}

				// �����Υ��ȥ��ޤ��������Ƥ��ʤ����

				// �����Υ��ȥ�buddyatom�ȥե��󥯥�����ӽ�°�줬Ʊ�����ȥ�Τ�����
				// ���ޤǼ����������ȥ�Ǥ��ꡢ���ĺ����������ΰ������֤Υ�󥯤�ո����ˤ��ɤ��
				// ���դޤ���$p�ˤĤʤ���褦�ʤɤΥ��ȥ�Ȥ�buddyatom���ۤʤ뤳�Ȥ��ǧ���롣(2004.6.4)
				Membrane[] testmems = { buddyatom.mem };
				if (proccxteqMap.containsKey(buddyatom.mem)) {
					// $p�����ȥåץ�٥�ؤΥ�󥯤ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
					testmems = new Membrane[]{ buddyatom.mem, proccxteqMap.get(buddyatom.mem).def.lhsOcc.mem };
				}
				for (int i = 0; i < testmems.length; i++) {
					for(Atom otheratom : testmems[i].atoms){
						int other = atomToPath(otheratom);
						if (other == UNBOUND) continue;
						if (!otheratom.functor.equals(buddyatom.functor)) continue;
						if (atomIds.containsKey(otheratom.args[buddylink.pos].buddy.atom)) continue;
						insts.add(new Instruction(Instruction.NEQATOM, buddyatompath, other));
					}
				}	

				// �����Υ��ȥ���ѿ��˼�������

				atomPaths.put(buddyatom, new Integer(buddyatompath));
				//qatoms.add(buddyatom);
//				if(ratoms!=null)ratoms.add(buddyatom);
				atomqueue.addLast( buddyatom );
				insts.add(new Instruction(Instruction.FUNC, buddyatompath, buddyatom.functor));

				// �������������
				if (atom.functor.isOutsideProxy() && pos == 0) {
					// ����ؤΥ�󥯤ξ�硢�����Ʊ�����򸡺����ʤ���Фʤ�ʤ�
					Membrane buddymem = buddyatom.mem;								
					int buddymempath = memToPath(buddyatom.mem);
					if (buddymempath != UNBOUND) {
						insts.add(new Instruction( Instruction.TESTMEM, buddymempath, buddyatompath ));
					}
					else {
						buddymempath = varCount++;
						memPaths.put(buddymem, new Integer(buddymempath));
						insts.add(new Instruction( Instruction.LOCKMEM, buddymempath, buddyatompath, buddyatom.mem.name ));
						newmemlist.add(buddymem);
						if(Env.slimcode){
							// GETMEM����Υ�����
							for(Membrane othermem : buddymem.parent.mems){
								if (othermem != buddymem && memToPath(othermem) != UNBOUND) {
									insts.add(new Instruction( Instruction.NEQMEM,	buddymempath, memToPath(othermem) ));
									buddymem.parent.connect(buddymem, othermem);
								}
							}
						}
					}
				}
				//��󥯤ΰ�����(RISC��) by mizuno
				getLinks(buddyatompath, buddyatom.functor.getArity(), insts);
			}
		}
		// ���Ĥ��ä�����������ˤ��륢�ȥ��ͥ��Ū�˸������롣
		// �����������ƥ��֥��ȥब�������ͥ�褹�롣
		nextmem:
			for(Iterator<Membrane> it = newmemlist.iterator(); it.hasNext();){
				Membrane mem = it.next();
				for(Atom atom : mem.atoms){
					if (!isAtomLoaded(atom) && atom.functor.isActive()) {
						if(Env.findatom2) compileMembraneForSlimcode(mem, list, false);
						else compileMembrane(mem, list);
						it.remove();
						continue nextmem;
					}
				}
			}

		for(Membrane mem : newmemlist){
			if(Env.findatom2) compileMembraneForSlimcode(mem, list, false);
			else compileMembrane(mem, list);
		}
	}
	/** ����³�����Υإåɤ򷿤ʤ��ǥ���ѥ��뤹�뤿��ν����򤹤롣*/
	void switchToUntypedCompilation() {
		fFindDataAtoms = true;
		memVisited.clear();
	}

	/** �줪��ӻ�¹������Ф��ƥޥå��󥰤�Ԥ� */
	void compileMembrane(Membrane mem, InstructionList list) {
		Env.c("compileMembrane");
		List<Instruction> insts = list.insts;
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);
		if(debug2){
			Util.println("\ncompileMembrane called\n" + " mem :"+mem+" list :\n" + list.insts );
		}
		int thismempath = memToPath(mem);
		for(Atom atom : mem.atoms){

			if (!atom.functor.isActive() && !fFindDataAtoms) continue;
			if (atomToPath(atom) != UNBOUND) continue;
			// ���Ĥ��ä����ȥ���ѿ��˼�������
			int atompath = varCount++;
			insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
//			insts.add(Instruction.findatom2(atompath, thismempath, findatomcount, atom.functor));

//				
			// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
			emitNeqAtoms(mem, atom, atompath, insts);
			atomPaths.put(atom, new Integer(atompath));
			//��󥯤ΰ�����(RISC��) by mizuno
			getLinks(atompath, atom.functor.getArity(), insts);
			compileLinkedGroup(atom, list);
		}
		for(Membrane submem : mem.mems){
			int submempath = memToPath(submem);
			if (submempath == UNBOUND) {
				// !fFindDataAtoms�ΤȤ��������ƥ��֥��ȥ��ޤޤʤ�����μ������󤷤ˤ���
				if ( !fFindDataAtoms && !hasActiveAtom(submem)) continue;

				// ������ѿ��˼�������
				submempath = varCount++;
				insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
				if(Env.slimcode){
					// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
					for(Membrane othermem : mem.mems){
						int other = memToPath(othermem);
						if (other == UNBOUND) continue;
						//if (othermem == submem) continue;
						insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
					}
				}
				memPaths.put(submem, new Integer(submempath));
			}
			
			
			//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
			compileMembrane(submem, list);

		}
		if(varCount > maxVarCount)
			maxVarCount = varCount;
		if(debug2)Util.println("\ncompileMembrane return\n inst:");
		if(debug2)Util.println(insts);
	}
	
	void makeSameNameMap(Membrane mem, HashMap sameNameMap, HashMap linkNameToAtomMap) {
		for (ProcessContext pc : mem.typedProcessContexts) {
			if (pc.hasSameName()) {
				for (int i = 0; i < pc.getSameNameList().size(); i++) 
					 sameNameMap.put(pc.getSameNameList().get(i).toString(), pc.linkName);
			}	
		}
		for (Atom atom : mem.atoms) {
			for (int i = 0; i < atom.args.length; i++) 
				linkNameToAtomMap.put(atom.args[i].name, atom);
		}
		for (Membrane submem : mem.mems)
			makeSameNameMap(submem, sameNameMap, linkNameToAtomMap);
	}
	
	/* Ʊ̾���դ��ץ���ʸ̮��ʬΥ��ȼ�������̿��findproccxt���ɲä��� */
	void compileSameProcessContext(Membrane mem, InstructionList list) {//seiji
		List<Instruction> insts = list.insts;
		HashMap<String, String> sameNameMap = new HashMap<String, String>();
		HashMap<String, Atom> linkNameToAtomMap = new HashMap<String, Atom>();
		makeSameNameMap(mem, sameNameMap, linkNameToAtomMap);

		sameProcessContext(mem, list, sameNameMap, linkNameToAtomMap);

	}
	void sameProcessContext(Membrane mem, InstructionList list, HashMap sameNameMap, HashMap linkNameToAtomMap) {//seiji
		List<Instruction> insts = list.insts;
		String newname = null;
		
		for (Atom newatom : mem.atoms){
			for (int i = 0; i < newatom.args.length; i++) {
				newname = newatom.args[i].name;
				
				if (sameNameMap.containsKey(newname)){
					String oriname = (String)sameNameMap.get(newname);
					Atom oriatom = (Atom)linkNameToAtomMap.get(oriname);
					for (int j = 0; j < oriatom.args.length; j++) {
						if (oriatom.args[j].name.equals(oriname)) {
//							insts.add(0, new Instruction(Instruction.FINDPROCCXT, atomToPath(oriatom), j, atomToPath(newatom), i));
//							if (atomToPath(oriatom) < atomToPath(newatom))
							insts.add(new Instruction(Instruction.FINDPROCCXT, 
									atomToPath(oriatom), oriatom.args.length, j, atomToPath(newatom), newatom.args.length, i));
						}
					}
				}
			}
		}
		for (Membrane submem : mem.mems) {
			sameProcessContext(submem, list, sameNameMap, linkNameToAtomMap);
		}
	}
	
	/** ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺����� 
	 * (n-kato 2008.01.15) TODO ï�������Υ᥽�åɤ��ĥ�ޤ��ϻ��ͤˤ��ƥ�����unary���Υ���ѥ���Х���������
	 * �ƥ����ѥץ����-->   5($seven),7($five) :- $seven=$five+2 |. 5=7.
	 */
	private void emitNeqAtoms(Membrane mem, Atom atom, int atompath, List<Instruction> insts) {
		Membrane[] testmems = { mem };
		if (proccxteqMap.containsKey(mem)) {
			// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
			testmems = new Membrane[]{ mem, proccxteqMap.get(mem).def.lhsOcc.mem };
		}
		for (int i = 0; i < testmems.length; i++) {
			for(Atom otheratom : testmems[i].atoms){
				int other = atomToPath(otheratom);
				if (other == UNBOUND) continue;
				if (!otheratom.functor.equals(atom.functor)) continue;
				//if (otheratom == atom) continue;
				insts.add(new Instruction(Instruction.NEQATOM, atompath, other));
				/* NEQATOM�������硢Ʊ�ե��󥯥��Υ��ȥ�˥ޥå�����뤬��branch��ξ���Υ��ȥ�����Ȥ���̿���󤬽��Ϥ���뤿�ᡢconnect������
				  testmems[i].connect(otheratom, atom);
				 */
			}
		}
	}

	static InstructionList contLabel;
	void setContLabel(InstructionList contLabel){
		this.contLabel = contLabel;
	}


	void compileMembraneForSlimcode(Membrane mem, InstructionList list, boolean rireki) {
		Env.c("compileMembrane");
		if(debug2)Util.println("\ncompileMembraneForSlimcode called \n mem :" + mem + "list :\n" + list.insts + "rireki :" + rireki);
		List<Instruction> insts = list.insts;
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);
		mem.createRG();

		int thismempath;
		if(firsttime){
			for(Atom atom : mem.atoms){
				thismempath = memToPath(mem);
				InstructionList groupinst, nextgroupinst;
				nextgroupinst = new InstructionList(list);
				if (!atom.functor.isActive() && !fFindDataAtoms) continue;
				//Util.println(atom.getName());
				if(debug)Util.println("start from " + atom);
				if (atomToPath(atom) == UNBOUND) {
					//HashSet ratoms = new HashSet();
					HashSet<Atom> qatoms = new HashSet<Atom>();
					int restvarcount = varCount;
					int diffvarcount = 0;
					qatoms.clear();
					HashMap<Atomic, Integer> newatompaths = (HashMap)atomPaths.clone();
					HashMap<Membrane, Integer> newmempaths = (HashMap)memPaths.clone();
					HashSet<Atom> newvisited = (HashSet)visited.clone();
					HashSet<Membrane> newmemVisited = (HashSet)memVisited.clone();
					searchLinkedGroup(atom, qatoms, atom, atom.mem);
					varCount = restvarcount;
					if(debug)Util.println("qatoms = " + qatoms);
					groupinst = nextgroupinst;
					nextgroupinst = new InstructionList(list);
					for(Iterator<Atom> it3 = qatoms.iterator(); it3.hasNext();){
						atom = it3.next();
						visited = newvisited;
						memVisited = newmemVisited;
						atomPaths = newatompaths;
						memPaths = newmempaths;
						newvisited = (HashSet)visited.clone();
						newmemVisited = (HashSet)memVisited.clone();
						newatompaths = (HashMap)((HashMap)atomPaths).clone();
						newmempaths = (HashMap)((HashMap)memPaths).clone();
						visited.clear();
						//ratoms.clear();

						InstructionList subinst = new InstructionList(groupinst);
						groupinst.add(new Instruction(Instruction.BRANCH, subinst));

						//tmplabel.insts = ;
						// ���Ĥ��ä����ȥ���ѿ��˼�������
						int atompath = varCount++;
						if(!Env.findatom2){
							//insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom2(atompath, thismempath, findAtomCount, atom.functor));
							findAtomCount++;
						} else{
							//	insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
						}
						emitNeqAtoms(mem, atom, atompath, insts);
						atomPaths.put(atom, new Integer(atompath));
						if(debug)Util.println("put " + atom);

						//��󥯤ΰ�����(RISC��) by mizuno
						getLinks(atompath, atom.functor.getArity(), subinst.insts);
						compileLinkedGroup(atom, subinst);
						// if(ratoms!=null)ratoms.add(atom);
						List<Integer> memActuals  = getMemActuals();
						List<Integer> atomActuals = getAtomActuals();
						List varActuals  = getVarActuals();
						// - ������#1

						subinst.add(new Instruction(Instruction.RESETVARS,memActuals, atomActuals, varActuals) );
						subinst.add(new Instruction(Instruction.PROCEED));
						//varcount = 0;
						if(varCount!=restvarcount){
							diffvarcount = varCount - restvarcount;
							varCount = restvarcount;
						}
						memPaths.put(mems.get(0), new Integer(0));
					}
					varCount += diffvarcount;
					if(!groupinst.insts.isEmpty()){
						insts.add(new Instruction(Instruction.GROUP, groupinst));
						if(varCount > maxVarCount)
							maxVarCount = varCount;
						resetMemActuals();
						resetAtomActuals();
					}
				}
			}
			for(Membrane submem : mem.mems){
				thismempath = memToPath(mem);
				int submempath = memToPath(submem);
				if (submempath == UNBOUND) {
					// !fFindDataAtoms�ΤȤ��������ƥ��֥��ȥ��ޤޤʤ�����μ������󤷤ˤ���
					if ( !fFindDataAtoms && !hasActiveAtom(submem)) continue;

					// ������ѿ��˼�������
					submempath = varCount++;
					if(Env.findatom2){
						//	insts.add(Instruction.anymem2(submempath, thismempath, submem.kind, anymemcount, submem.name));
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
						anyMemCount++;
					} else
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
					if(Env.slimcode){
						// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
						for(Membrane othermem : mem.mems){
							int other = memToPath(othermem);
							if (other == UNBOUND) continue;
							//if (othermem == submem) continue;
							insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
							mem.connect(submem, othermem);
						}
					}
					memPaths.put(submem, new Integer(submempath));
				}
				//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
				compileMembraneForSlimcode(submem, list, false);
			}
		} else {
			if(debug2){
				Util.println("allKnownElments is");
				Util.println(mem.allKnownElements());
			}
			for(List atommems : mem.allKnownElements()){
				compileMembraneSecondTime(mem, list, atommems, rireki);
			}
		}
		if(varCount > maxVarCount)
			maxVarCount = varCount;
		if(debug)Util.println(insts);
		if(debug2)Util.println("\ncompileMembraneForSlimcode return \n insts\n");
		if(debug2)Util.println(insts);
	}

	private void compileMembraneSecondTimeAtomic(Membrane mem, InstructionList list, List atommems, boolean rireki){

	}
	private void compileMembraneSecondTime(Membrane mem, InstructionList list, List atommems, boolean rireki) {
		if(debug2)Util.println("\ncompileMembraneSecondTime called \n mem :"+mem+" list:\n"+list.insts+" atommems :"+atommems+"rireki :"+rireki);
		int thismempath;
		List<Instruction> insts = list.insts;
//		mem.printfRG();
		for(int listi=0; listi<atommems.size();listi++){
			Object atommem = atommems.get(listi);
			if(atommem instanceof Atomic){
				Atom atom = (Atom)atommem;
				thismempath = memToPath(mem);
				InstructionList groupinst, nextgroupinst;
				nextgroupinst = new InstructionList(list);
				if (!atom.functor.isActive() && !fFindDataAtoms) continue;
				if (atomToPath(atom) == UNBOUND) {
					HashSet<Atom> qatoms = new HashSet<Atom>();
					int restvarcount = varCount;
					int diffvarcount = 0;
					if(!rireki){
						int atompath = varCount++;
						if(Env.findatom2 && rireki){
//							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							insts.add(Instruction.findatom2(atompath, thismempath, findAtomCount, atom.functor));
							findAtomCount++;
						} else {
							insts.add(Instruction.findatom(atompath, thismempath, atom.functor));}
						emitNeqAtoms(mem, atom, atompath, insts);
						atomPaths.put(atom, new Integer(atompath));

						//��󥯤ΰ�����(RISC��) by mizuno
						getLinks(atompath, atom.functor.getArity(), insts);
						compileLinkedGroup(atom, list);
						compileMembraneSecondTime(mem, list, atommems, false);
						return;
					}
					qatoms.clear();
					HashMap<Atomic, Integer> newatompaths = (HashMap)atomPaths.clone();
					HashMap<Membrane, Integer> newmempaths = (HashMap)memPaths.clone();
					HashSet<Atom> newvisited = (HashSet)visited.clone();
					HashSet<Membrane> newmemVisited = (HashSet)memVisited.clone();
					for(int listi2 = listi; listi2<atommems.size();listi2++){
						if(atommems.get(listi2) instanceof Membrane) continue;
						if(visited.contains(atommems.get(listi2))) continue;
						if( ((Atom)atommems.get(listi2)).functor.isNumber()) continue;
						searchLinkedGroup((Atom)atommems.get(listi2), qatoms, (Atom)atommems.get(listi2), atom.mem);
					}
					varCount = restvarcount;
					groupinst = nextgroupinst;
					nextgroupinst = new InstructionList(list);
					for(Iterator<Atom> it = qatoms.iterator(); it.hasNext();){
						atom = it.next();
						if( atom.functor.isNumber()) continue;
						visited = newvisited;
						memVisited = newmemVisited;
						atomPaths = newatompaths;
						memPaths = newmempaths;
						newvisited = (HashSet)visited.clone();
						newmemVisited = (HashSet)memVisited.clone();
						newatompaths = (HashMap)atomPaths.clone();
						newmempaths = (HashMap)memPaths.clone();
//						visited.clear();

						InstructionList subinst = new InstructionList(groupinst);
						groupinst.add(new Instruction(Instruction.BRANCH, subinst));

						// ���Ĥ��ä����ȥ���ѿ��˼�������
						int atompath = varCount++;
						if(Env.findatom2 && rireki){
//							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom2(atompath, thismempath, findAtomCount, atom.functor));
							findAtomCount++;
						} else{
							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));}
						emitNeqAtoms(mem, atom, atompath, subinst.insts);
						atomPaths.put(atom, new Integer(atompath));

						//��󥯤ΰ�����(RISC��) by mizuno
						getLinks(atompath, atom.functor.getArity(), subinst.insts);
						compileLinkedGroup(atom, subinst);
						compileMembraneSecondTime(mem, subinst, atommems, false);
						List<Integer> memActuals  = getMemActuals();
						List<Integer> atomActuals = getAtomActuals();
						List varActuals  = getVarActuals();

						subinst.add(new Instruction(Instruction.RESETVARS,memActuals, atomActuals, varActuals) );
						subinst.add(new Instruction(Instruction.PROCEED));
						if(varCount!=restvarcount){
							diffvarcount = varCount - restvarcount;
							varCount = restvarcount;
						}
						memPaths.put(mems.get(0), new Integer(0));
					}
					varCount += diffvarcount;
					if(!groupinst.insts.isEmpty()){
						groupinst.add(new Instruction(Instruction.STOP));
						insts.add(new Instruction(Instruction.GROUP, groupinst));
						if(varCount > maxVarCount)
							maxVarCount = varCount;
						resetMemActuals();
						resetAtomActuals();
					}
					return ;
				}
			} else if(atommem instanceof Membrane){
				Membrane submem = (Membrane)atommem;
				thismempath = memToPath(mem);
				int submempath = memToPath(submem);
				if (submempath == UNBOUND) {
					// !fFindDataAtoms�ΤȤ��������ƥ��֥��ȥ��ޤޤʤ�����μ������󤷤ˤ���
					if ( !fFindDataAtoms && !hasActiveAtom(submem) ) continue;

					// ������ѿ��˼�������
					submempath = varCount++;
					if(Env.findatom2 && rireki){
						//insts.add(Instruction.anymem2(submempath, thismempath, submem.kind, anymemcount, submem.name));
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
						anyMemCount++;
					} else
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
					if(Env.slimcode){
						// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
						for(Membrane othermem : mem.mems){
							int other = memToPath(othermem);
							if (other == UNBOUND) continue;
							//if (othermem == submem) continue;
							insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
						}
					}
					memPaths.put(submem, new Integer(submempath));
				}
				//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
				compileMembraneForSlimcode(submem, list, false);
				compileMembraneSecondTime(mem, list, atommems.subList(listi+1, atommems.size()), false);
				return ;
			} else {
				System.err.println("Undef Class occured");
			}
		}
	}


	/** �줪��ӻ�¹������Ф��Ƽ�ͳ��󥯤θĿ���Ĵ�٤롣
	 * <p>���Ĥ�$p����������ʳ��ξ��ϡ���ͳ��󥯤˴ؤ��븡����Ԥ�ɬ�פ����ä���
	 * ���������� redex "T��" �� = ��ޤ�Ǥ�褤������ͤˤʤäƤ��뤿�ᡢ���θ����ϼ¤����ס�
	 * �������äƤ��Υ᥽�åɤϸƤФ�ʤ���(n-kato 2004.11.24--2004.11.26) 
	 * <p>���Υ᥽�åɤ��������Ȥ�������̲ᤷ�ƻ����ͳ�ǺƤӿ������äƤ�������ͳ��󥯤����ФǤ��ʤ���
	 * ���«��̵�����Υ����ɤ�ɬ�פʤΤ����褵������(2004.12.4)
	 * */
	void checkFreeLinkCount(Membrane mem, List<Instruction> insts) {
		if (!mem.processContexts.isEmpty()) {
			int thismempath = memToPath(mem);
			ProcessContext pc = mem.processContexts.get(0); // �������$p��ɬ����ȥå����
//			// ����Ū�ʥ�����ɬ����ȥå���Υ��ȥ�ʼ�ͳ��󥯴������ȥ��ޤ�ˡˤ�
//			// ��ͳ��󥯽��ϴ������ȥ�Ǥʤ����Ȥ��ǧ����
//			for (int i = 0; i < pc.args.length; i++) {
//			int freelinktestedatompath = varcount++;
//			match.add(new Instruction(Instruction.DEREFATOM, freelinktestedatompath,
//			atomToPath(pc.args[i].buddy.atom), pc.args[i].buddy.pos));
//			match.add(new Instruction(Instruction.NOTFUNC, freelinktestedatompath,
//			Functor.INSIDE_PROXY));
//			}
			// ���«��̵�����
			if (pc.bundle == null) {
				insts.add(new Instruction(Instruction.NFREELINKS, thismempath,
						mem.getFreeLinkAtomCount()));					
			}
		}
		for(Membrane submem : mem.mems){
			checkFreeLinkCount(submem, insts);
		}
	}
//	public Instruction getResetVarsInstruction() {
//	return Instruction.resetvars(getMemActuals(), getAtomActuals(), getVarActuals());
//	}
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤ����������֤���
	 * ����Ū�ˤ�mems���б������ѿ��ֹ�Υꥹ�Ȥ��Ǽ����ArrayList���֤���*/
	List<Integer> getMemActuals() {
		List<Integer> args = new ArrayList<Integer>();		
		for (int i = 0; i < mems.size(); i++) {
			if(memPaths.get(mems.get(i)) != null)args.add( memPaths.get(mems.get(i)) );
		}
		return args;
	}
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤΥ��ȥ��������֤���
	 * ����Ū�ˤ�HeadCompiler��atoms���б������ѿ��ֹ�Υꥹ�Ȥ��Ǽ����ArrayList���֤���*/
	List<Integer> getAtomActuals() {
		List<Integer> args = new ArrayList<Integer>();		
		for (int i = 0; i < atoms.size(); i++) {
			if(atomPaths.get(atoms.get(i)) != null)args.add( atomPaths.get(atoms.get(i)) );
		}
		return args;
	}		
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤ���䥢�ȥ�ʳ��ΰ�������֤���
	 * ����Ū�ˤ�HeadCompiler�϶���ArrayList���֤���*/
	List getVarActuals() {
		return new ArrayList();
	}

	private void resetAtomActuals(){
		HashMap<Atomic, Integer> newatompaths = new HashMap<Atomic, Integer>();
		for (int i = 0; i < atoms.size(); i++) {
			if(atomPaths.get(atoms.get(i)) != null){
				newatompaths.put( atoms.get(i), varCount);
				varCount++;
			}
		}
		atomPaths = newatompaths;
	}
	private void resetMemActuals(){
		HashMap<Membrane, Integer> newmempaths = new HashMap<Membrane, Integer>();
		varCount = 0;
		for (int i = 0; i < mems.size(); i++) {
			if(memPaths.get(mems.get(i)) != null){
				newmempaths.put( mems.get(i), varCount);
				varCount++;
			}
		}
		memPaths = newmempaths;
	}
	////////////////////////////////////////////////////////////////

	/** ������������򥳥�ѥ��뤹�� */
	void compileNegativeCondition(List<ProcessContextEquation> eqs, InstructionList list) throws CompileException{
		List<Instruction> insts = list.insts;
		//int formals = varcount;
		//matchLabel.setFormals(formals);
		for(ProcessContextEquation eq : eqs){
			enumFormals(eq.mem);
			memPaths.put(eq.mem, memPaths.get(eq.def.lhsOcc.mem));
			proccxteqMap.put(eq.mem, eq);
		}
		for(ProcessContextEquation eq : eqs){
			if(Env.findatom2){
				compileMembraneForSlimcode(eq.mem, list, false);
			} else {
				compileMembrane(eq.mem, list);
			}
			// �ץ���ʸ̮���ʤ��Ȥ��ϡ����ȥ�Ȼ���θĿ����ޥå����뤳�Ȥ��ǧ����
			if (eq.mem.processContexts.isEmpty()) {
				// TODO �ʵ�ǽ��ĥ��ñ��Υ��ȥ�ʳ��˥ޥå����뷿�դ��ץ���ʸ̮�Ǥ�������ư���褦�ˤ���(2)
				insts.add(new Instruction(Instruction.NATOMS, memPaths.get(eq.mem),
						eq.def.lhsOcc.mem.getNormalAtomCount() + eq.def.lhsOcc.mem.typedProcessContexts.size()
						+ eq.mem.getNormalAtomCount() + eq.mem.typedProcessContexts.size() ));
				insts.add(new Instruction(Instruction.NMEMS, memPaths.get(eq.mem),
						eq.def.lhsOcc.mem.mems.size() + eq.mem.mems.size() ));
			}
			else {
				ProcessContext pc = eq.mem.processContexts.get(0);
				if (pc.bundle == null) {
					// TODO �ʵ�ǽ��ĥ�˼�ͳ��󥯤θĿ��򸡺����롣������������������$pp������Ū�ʼ�ͳ��󥯤ν�������餫�ˤ��ʤ���Фʤ�ʤ���
				}
			}
			// eq.mem.ruleContexts ��̵�뤵���					
		}
		// todo ��å��������unlock���롣
		// todo �줬��å��Ǥ��ʤ��ä�����Ȥ��ä��줬¸�ߤ��ʤ��櫓�ǤϤʤ��Х��򲿤Ȥ�����
		// todo ��ͳ���
		insts.add(new Instruction(Instruction.PROCEED));	// ��STOP
		//matchLabel.updateLocals(varcount);
	}

	private void connectAtomMem(Object a1, Object a2){
		Membrane m1, m2;
		if(a1 instanceof Atomic)
			m1 = ((Atomic)a1).mem;
		else
			m1 = ((Membrane)a1).parent;
		if(a2 instanceof Atomic)
			m2 = ((Atomic)a2).mem;
		else
			m2 = ((Membrane)a2).parent;
		if(m1==m2)
			m1.connect(a1, a2);
		else {
			Membrane p1, p2, c1, c2;
			p2 = m2.parent;
			c2 = m2;
			while(p2 !=null){
				if(m1==p2){
					m1.connect(a1, c2);
					return ;
				}
				c2 = p2;
				p2 = c2.parent;
			}

			p1 = m1.parent;
			c1 = m1;
			while(p1 !=null){
				if(p1==m2){
					m2.connect(c1, a2);
					return ;
				}
				c1 = p1;
				p1 = c1.parent;
			}

			p1 = m1.parent;
			c1 = m1;
			while(p1 !=null){
				p2 = m2.parent;
				c2 = m2;
				while(p2 !=null){
					if(p1==p2){
						p1.connect(c1, c2);
						return ;
					}
					c2 = p2;
					p2 = c2.parent;
				}
				c1 = p1;
				p1 = c1.parent;
			}
		}
	}
	private boolean hasActiveAtom(Membrane mem){
		for(Atom atom : mem.atoms) 
			if (atom.functor.isActive()) return true;	
		return false;
	}
}
//TODO �ʵ�ǽ��ĥ�˥��������������η��դ��ץ���ʸ̮�򥳥�ѥ��뤹��