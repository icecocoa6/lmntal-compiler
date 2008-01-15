/*
 * ������: 2003/10/28
 *
 */
package compile;

import java.util.ArrayList;
import java.util.Collection;
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
public class HeadCompiler {
	boolean debug = false; //���Ū
	boolean firsttime = true;
//	/** ������ */
//	public Membrane lhsmem;//m;
	/** �ޥå���̿����ʤΥ�٥��*/
	public InstructionList matchLabel, tempLabel;
	/** matchLabel.insts */
	public List<Instruction> match, tempmatch;

	public List<Membrane> mems			= new ArrayList<Membrane>();	// �и�������Υꥹ�ȡ�[0]��m
	public List<Atomic> atoms			= new ArrayList<Atomic>();	// �и����륢�ȥ�Υꥹ��	
	public Map  mempaths		= new HashMap();	// Membrane -> �ѿ��ֹ�
	public Map  atompaths		= new HashMap();	// Atomic -> �ѿ��ֹ�
	public Map  linkpaths		= new HashMap();	// Atom���ѿ��ֹ� -> ��󥯤��ѿ��ֹ������
	
	private Map atomids		= new HashMap();	// Atom -> atoms���index���ѻߤ������Ǹ�Ƥ�����
	private HashSet visited	= new HashSet();	// Atom -> boolean, �ޥå���̿��������������ɤ���
	private HashSet memVisited	= new HashSet();	// Membrane -> boolean, compileMembrane��Ƥ�����ɤ���

	boolean fFindDataAtoms;						// �ǡ������ȥ��findatom���Ƥ褤���ɤ���
	boolean UNTYPED_COMPILE	= false;			// fFindDataAtoms�ν����
	
	int varcount;	// �����쥢�ȥ�����ʬ����٤����Ȼפ�
	int maxvarcount;

	static int findatomcount = 0;
	static int anymemcount = 0;
	
	private HashMap proccxteqMap = new HashMap(); // Membrane -> ProcessContextEquation
	
	final boolean isAtomLoaded(Atomic atom) { return atompaths.containsKey(atom); }
	final boolean isMemLoaded(Membrane mem) { return mempaths.containsKey(mem); }

	final int atomToPath(Atomic atom) { 
		if (!isAtomLoaded(atom)) return UNBOUND;
		return ((Integer)atompaths.get(atom)).intValue();
	}
	final int memToPath(Membrane mem) {
		 if (!isMemLoaded(mem)) return UNBOUND;
		 return ((Integer)mempaths.get(mem)).intValue();
	}
	final int linkToPath(int atomid, int pos) { // todo HeadCompiler�λ��ͤ˹�碌�롩GuardCompiler�⡣
		if (!linkpaths.containsKey(new Integer(atomid))) return UNBOUND;
		return ((int[])linkpaths.get(new Integer(atomid)))[pos];
	}
	
	static final int UNBOUND = -1;
	
	HeadCompiler() {
	}
	
	/** �����������浪��ӥܥǥ��Υ���ѥ���ǻȤ�����ˡ�
	 * this����ꤵ�줿hc���Ф������������줿HeadCompiler�Ȥ��롣
	 * �������Ȥϡ����դ����ƤΥ��ȥप�������Ф��ơ�������/�ܥǥ��Ѥβ������ֹ��
	 * �ѿ��ֹ�Ȥ��ƺ��դΥޥå��󥰤��꽪��ä��������֤���Ĥ褦�ˤ��뤳�Ȥ��̣���롣*/
	final void initNormalizedCompiler(HeadCompiler hc) {
		matchLabel = new InstructionList();
		match = matchLabel.insts;
		mems.addAll(hc.mems);
		atoms.addAll(hc.atoms);
		varcount = 0;
		Iterator it = mems.iterator();
		while (it.hasNext()) {
			mempaths.put(it.next(), new Integer(varcount++));
		}
		it = atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			atompaths.put(atom, new Integer(varcount));
			atomids.put(atom, new Integer(varcount++));
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
	public void enumFormals(Membrane mem) {
		Env.c("enumFormals");
		for (Atom atom : mem.atoms) {
			// ���դ˽и��������ȥ����Ͽ����
			atomids.put(atom, new Integer(atoms.size()));
			atoms.add(atom);
		}
		mems.add(mem);	// �����mems[0]
		for (Membrane m : mem.mems) {
			enumFormals(m);
		}
	}
	
	public void prepare() {
		Env.c("prepare");
		mempaths.clear();
		atompaths.clear();
		visited.clear();
		memVisited.clear();
		matchLabel = new InstructionList();
		tempLabel = new InstructionList();
		match = matchLabel.insts;
		tempmatch = tempLabel.insts;
		varcount = 1;	// [0]������
//		mempaths.put(mems.get(0), new Integer(0));	// ������ѿ��ֹ�� 0
		fFindDataAtoms = UNTYPED_COMPILE;
	}

	/**
	 * ���ꤵ�줿���ȥ���Ф���getlink��Ԥ����ѿ��ֹ��linkpaths����Ͽ���롣
	 * RISC����ȼ���ɲ�(mizuno)
	 */
	public final void getLinks(int atompath, int arity, List<Instruction> insts) {
		int[] paths = new int[arity];
		for (int i = 0; i < arity; i++) {
			paths[i] = varcount;
			insts.add(new Instruction(Instruction.GETLINK, varcount, atompath, i));
			varcount++;
		}
		linkpaths.put(new Integer(atompath), paths);
	}
	public void searchLinkedGroup(Atom firstatom, HashSet qatoms, Membrane firstmem) {
		LinkedList newmemlist = new LinkedList();
		LinkedList atomqueue = new LinkedList();
		atomqueue.add(firstatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = (Atom)atomqueue.removeFirst();			
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
				if (!atomids.containsKey(buddylink.atom)) {
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
				int buddyatompath = varcount++;
				
				// ����褬¾���������դΥ��ȥ�ξ��������֥�󥯤ξ���
				// ��֤μ�ͳ��󥯴������ȥ຿�θ����򤷡��쳬�ؤ��ޥå����뤫������Ԥ���
				// �ޤ�*A��DEREF����4���������buddyatompath���������롣
				if (proccxteqMap.containsKey(atom.mem)
				 && proccxteqMap.containsKey(buddyatom.mem) && buddyatom.mem != atom.mem) {				
					// ( 0: 1:{$p[|*X],2:{$q[|*Y]}} :- \+($p=(atom(L),$pp),$q=(buddy(L),$qq)) | ... )
					// ���Υ롼��Υ����ɤΰ�̣:
					// ( 0: 1:{atom(L),$pp[|*XX],2:{buddy(L),$qq[|*YY]}} :- ... ) �ˤϥޥå����ʤ�
					//
					LinkedList atomSupermems  = new LinkedList(); // atom�ι�����������ʿ���¦����Ƭ��
					LinkedList buddySupermems = new LinkedList(); // buddy�ι�����������ʿ���¦����Ƭ��
					// ������������η׻�
					// atomSupermems = {0,1}; buddySupermems = {0,1,2}
					Membrane mem = ((ProcessContextEquation)proccxteqMap.get(buddyatom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						buddySupermems.addFirst(mem);
						mem = mem.parent;
					}
					mem = ((ProcessContextEquation)proccxteqMap.get(atom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						atomSupermems.addFirst(mem);
						mem = mem.parent;
					}
					// ������������ζ�����ʬ���
					// atomSupermems = {}; buddySupermems = {2}
					Iterator ita = atomSupermems.iterator();
					Iterator itb = buddySupermems.iterator();
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
				
				atompaths.put(buddyatom, new Integer(buddyatompath));
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
						buddymempath = varcount++;
						mempaths.put(buddymem, new Integer(buddymempath));
						newmemlist.add(buddymem);
						connectAtomMem(firstatom, buddymem);
					}
				}
			}
		}
		// ���Ĥ��ä�����������ˤ��륢�ȥ��ͥ��Ū�˸������롣
		Iterator it = newmemlist.iterator();
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			connectAtomMem(firstatom, mem);
			Iterator it2 = mem.atoms.iterator();
			while (it2.hasNext()) {
				Atom atom = (Atom)it2.next();
				searchMembrane(mem, qatoms, firstmem);
			}
		}
	}
		
	public void searchMembrane(Membrane mem, HashSet qatoms, Membrane firstmem) {
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);

		int thismempath = memToPath(mem);
		
		Iterator it = mem.atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (!atom.functor.isActive() && !fFindDataAtoms) continue;
			if (atomToPath(atom) == UNBOUND) {
				// ���Ĥ��ä����ȥ���ѿ��˼�������
				int atompath = varcount++;
				// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
				Membrane[] testmems = { mem };
				if (proccxteqMap.containsKey(mem)) {
					// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
					testmems = new Membrane[]{ mem,
						((ProcessContextEquation)proccxteqMap.get(mem)).def.lhsOcc.mem };
				}
				atompaths.put(atom, new Integer(atompath));
				searchLinkedGroup(atom, qatoms, firstmem);
			}
//			compileLinkedGroup(atom);	// 2�Ծ�˰�ư���Ƥߤ� n-kato (2004.7.16)
		}
		it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int submempath = memToPath(submem);
			if (submempath == UNBOUND) {
				// ������ѿ��˼�������
				submempath = varcount++;
				mempaths.put(submem, new Integer(submempath));
			}
			//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
			searchMembrane(submem, qatoms, firstmem);
		}
	}
	
	/** ��󥯤ǤĤʤ��ä����ȥप��Ӥ��ν�°����Ф��ƥޥå��󥰤�Ԥ���
	 * �ޤ�������Ǹ��Ĥ��ä��ֿ�������פΤ��줾����Ф��ơ�compileMembrane��Ƥ֡�
	 */
	public void compileLinkedGroup(Atom firstatom, InstructionList list) {
		Env.c("compileLinkedGroup");
		List<Instruction> insts = list.insts;
		LinkedList newmemlist = new LinkedList();
		LinkedList atomqueue = new LinkedList();
		if(debug)Util.println("start "+firstatom);
		atomqueue.add(firstatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = (Atom)atomqueue.removeFirst();			
			if(debug)Util.println("before " + atom);
			if (visited.contains(atom)) continue;
			if(debug)Util.println("after " + atom);
			visited.add(atom);
			
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence buddylink = atom.args[pos].buddy;
				if (buddylink == null) continue; // ������ƿ̾��󥯤�̵��
				if (!atomids.containsKey(buddylink.atom)) continue; // ���դ�$p�ʤ����lhs->neg�ˤؤΥ�󥯤�̵��
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
				int buddyatompath = varcount++;
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
					LinkedList atomSupermems  = new LinkedList(); // atom�ι�����������ʿ���¦����Ƭ��
					LinkedList buddySupermems = new LinkedList(); // buddy�ι�����������ʿ���¦����Ƭ��
					// ������������η׻�
					// atomSupermems = {0,1}; buddySupermems = {0,1,2}
					Membrane mem = ((ProcessContextEquation)proccxteqMap.get(buddyatom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						buddySupermems.addFirst(mem);
						mem = mem.parent;
					}
					mem = ((ProcessContextEquation)proccxteqMap.get(atom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						atomSupermems.addFirst(mem);
						mem = mem.parent;
					}
					// ������������ζ�����ʬ���
					// atomSupermems = {}; buddySupermems = {2}
					Iterator ita = atomSupermems.iterator();
					Iterator itb = buddySupermems.iterator();
					while (ita.hasNext() && itb.hasNext() && ita.next() == itb.next()) {
						ita.remove();
						itb.remove();
					}
					// �������������̿������Ѵ���buddyatompath����������
					while (!atomSupermems.isEmpty()) {
						mem = (Membrane)atomSupermems.removeLast();
						insts.add( new Instruction(Instruction.FUNC, buddyatompath, Functor.INSIDE_PROXY) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					while (!buddySupermems.isEmpty()) {
						mem = (Membrane)buddySupermems.removeFirst();
						insts.add( new Instruction(Instruction.FUNC, buddyatompath, new SpecialFunctor("$out",2, mem.kind) ) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						insts.add( new Instruction(Instruction.TESTMEM, memToPath(mem), buddyatompath + 1) );
						insts.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					varcount = buddyatompath + 1;
					int lastindex = insts.size() - 1; // buddyatom��������뤿���DEREF̿���ؤ�
					
					// deref̿�����4������������					
					// - deref [-tmp1atom,atom,atompos,buddypos] ==> deref [-tmp1atom,atom,atompos,1]
//					((Instruction)insts.get(firstindex)).setArg4(new Integer(1));
					Instruction oldfirst = (Instruction)insts.remove(firstindex);
					Instruction newfirst = new Instruction(Instruction.DEREF,
						oldfirst.getIntArg1(), oldfirst.getIntArg2(), oldfirst.getIntArg3(), 1);
					insts.add(firstindex,newfirst);
					// - deref [-buddyatom,tmpatom,tmppos,1] ==> deref [-buddyatom,buddypos,atompos,buddypos]
//					((Instruction)insts.get(lastindex)).setArg4(new Integer(buddylink.pos));
					Instruction oldlast = (Instruction)insts.remove(lastindex);
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
					testmems = new Membrane[]{ buddyatom.mem,
						((ProcessContextEquation)proccxteqMap.get(buddyatom.mem)).def.lhsOcc.mem };
				}
				for (int i = 0; i < testmems.length; i++) {
					Iterator it = testmems[i].atoms.iterator();
					while (it.hasNext()) {
						Atom otheratom = (Atom)it.next();					
						int other = atomToPath(otheratom);
						if (other == UNBOUND) continue;
						if (!otheratom.functor.equals(buddyatom.functor)) continue;
						if (atomids.containsKey(otheratom.args[buddylink.pos].buddy.atom)) continue;
						insts.add(new Instruction(Instruction.NEQATOM, buddyatompath, other));
						testmems[i].connect(otheratom, buddyatom);
					}
				}	
							
				// �����Υ��ȥ���ѿ��˼�������
				
				atompaths.put(buddyatom, new Integer(buddyatompath));
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
						buddymempath = varcount++;
						mempaths.put(buddymem, new Integer(buddymempath));
						insts.add(new Instruction( Instruction.LOCKMEM, buddymempath, buddyatompath, buddyatom.mem.name ));
						newmemlist.add(buddymem);
						if(Env.slimcode){
							// GETMEM����Υ�����
							Iterator it = buddymem.parent.mems.iterator();
							while (it.hasNext()) {
								Membrane othermem = (Membrane)it.next();
								if (othermem != buddymem && memToPath(othermem) != UNBOUND) {
									insts.add(new Instruction( Instruction.NEQMEM,
										buddymempath, memToPath(othermem) ));
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
		Iterator it = newmemlist.iterator();
		nextmem:
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			Iterator it2 = mem.atoms.iterator();
			while (it2.hasNext()) {
				Atom atom = (Atom)it2.next();
				if (!isAtomLoaded(atom) && atom.functor.isActive()) {
					if(Env.findatom2){
						compileMembraneForSlimcode(mem, list, false);
					} else {
						compileMembrane(mem, list);
					}
					it.remove();
					continue nextmem;
				}					
			}
		}
		it = newmemlist.iterator();
		while (it.hasNext()) {
			Membrane mem =(Membrane)it.next();
			if(Env.findatom2){
				compileMembraneForSlimcode(mem, list, false);
			} else {
				compileMembrane(mem, list);
			}
		}
	}
	/** ����³�����Υإåɤ򷿤ʤ��ǥ���ѥ��뤹�뤿��ν����򤹤롣*/
	public void switchToUntypedCompilation() {
		fFindDataAtoms = true;
		memVisited.clear();
	}

	/** �줪��ӻ�¹������Ф��ƥޥå��󥰤�Ԥ� */
	public void compileMembrane(Membrane mem, InstructionList list) {
		Env.c("compileMembrane");
		List<Instruction> insts = list.insts;
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);
		int thismempath = memToPath(mem);
		
		Iterator it = mem.atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (!atom.functor.isActive() && !fFindDataAtoms) continue;
			if (atomToPath(atom) == UNBOUND) {
				// ���Ĥ��ä����ȥ���ѿ��˼�������
				int atompath = varcount++;
				insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
//				insts.add(Instruction.findatom2(atompath, thismempath, findatomcount, atom.functor));
				// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
				emitNeqAtoms(mem, atom, atompath, insts);
				atompaths.put(atom, new Integer(atompath));
				//��󥯤ΰ�����(RISC��) by mizuno
				getLinks(atompath, atom.functor.getArity(), insts);
				compileLinkedGroup(atom, list);
			}
//			compileLinkedGroup(atom);	// 2�Ծ�˰�ư���Ƥߤ� n-kato (2004.7.16)
		}
		it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int submempath = memToPath(submem);
			if (submempath == UNBOUND) {
				// !fFindDataAtoms�ΤȤ��������ƥ��֥��ȥ��ޤޤʤ�����μ������󤷤ˤ���
				if (!fFindDataAtoms) {
					Iterator it2 = submem.atoms.iterator();
					boolean exists = false;
					while (it2.hasNext()) {
						Atom atom = (Atom)it2.next();
						if (atom.functor.isActive()) {
							exists = true;
							break;
						}
					}
					if (!exists) continue;
				}		
		
				// ������ѿ��˼�������
				submempath = varcount++;
				insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
				if(Env.slimcode){
					// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
					Iterator it2 = mem.mems.iterator();
					while (it2.hasNext()) {
						Membrane othermem = (Membrane)it2.next();
						int other = memToPath(othermem);
						if (other == UNBOUND) continue;
						//if (othermem == submem) continue;
						insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
					}
				}
				mempaths.put(submem, new Integer(submempath));
			}
			//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
			compileMembrane(submem, list);
		}
		if(varcount > maxvarcount)
			maxvarcount = varcount;
	}
	/** ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺����� 
	 * (n-kato 2008.01.15) TODO ï�������Υ᥽�åɤ��ĥ�ޤ��ϻ��ͤˤ��ƥ�����unary���Υ���ѥ���Х���������
	 * �ƥ����ѥץ����-->   5($seven),7($five) :- $seven=$five+2 |. 5=7.
	 */
	public void emitNeqAtoms(Membrane mem, Atom atom, int atompath, List<Instruction> insts) {
		Membrane[] testmems = { mem };
		if (proccxteqMap.containsKey(mem)) {
			// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
			testmems = new Membrane[]{ mem,
				((ProcessContextEquation)proccxteqMap.get(mem)).def.lhsOcc.mem };
		}
		for (int i = 0; i < testmems.length; i++) {
			Iterator it2 = testmems[i].atoms.iterator();
			while (it2.hasNext()) {
				Atom otheratom = (Atom)it2.next();					
				int other = atomToPath(otheratom);
				if (other == UNBOUND) continue;
				if (!otheratom.functor.equals(atom.functor)) continue;
				//if (otheratom == atom) continue;
				insts.add(new Instruction(Instruction.NEQATOM, atompath, other));
			}
		}
	}
	
	HashSet satoms = new HashSet();
	
	static InstructionList contLabel;
	public void setContLabel(InstructionList contLabel){
		this.contLabel = contLabel;
	}
	public void compileMembraneForSlimcode(Membrane mem, InstructionList list, boolean rireki) {
		Env.c("compileMembrane");
		List<Instruction> insts = list.insts;
		if (memVisited.contains(mem)) return;
		memVisited.add(mem);
		mem.createRG();

		int thismempath;
		if(firsttime){
			Iterator it = mem.atoms.iterator();
			while (it.hasNext()) {
				thismempath = memToPath(mem);
				InstructionList groupinst, nextgroupinst;
				nextgroupinst = new InstructionList(list);
				Atom atom = (Atom)it.next();
				if (!atom.functor.isActive() && !fFindDataAtoms) continue;
	//			Util.println(atom.getName());
				if(debug)Util.println("start from " + atom);
				if (atomToPath(atom) == UNBOUND) {
	//				HashSet ratoms = new HashSet();
					HashSet qatoms = new HashSet();
					int restvarcount = varcount;
					int diffvarcount = 0;
					qatoms.clear();
					HashMap newatompaths = (HashMap)((HashMap)atompaths).clone();
					HashMap newmempaths = (HashMap)((HashMap)mempaths).clone();
					HashSet newvisited = (HashSet)visited.clone();
					HashSet newmemVisited = (HashSet)memVisited.clone();
					searchLinkedGroup(atom, qatoms, atom.mem);
					varcount = restvarcount;
					Iterator it3 = qatoms.iterator();
					if(debug)Util.println("qatoms = " + qatoms);
					groupinst = nextgroupinst;
					nextgroupinst = new InstructionList(list);
					while(it3.hasNext()){
						atom = (Atom)it3.next();
	//					if(satoms.contains(atom))
	//						continue;
	//					satoms.add(atom);
	//					Iterator it4 = ratoms.iterator();
	//					if(debug)Util.println("ratom = " + ratoms);
	//					while(it4.hasNext()){
	//						Atom at = (Atom)it4.next();
	//						atompaths.remove(at);
	//						visited.remove(at);
	//					}
						visited = newvisited;
						memVisited = newmemVisited;
						atompaths = newatompaths;
						mempaths = newmempaths;
						newvisited = (HashSet)visited.clone();
						newmemVisited = (HashSet)memVisited.clone();
						newatompaths = (HashMap)((HashMap)atompaths).clone();
						newmempaths = (HashMap)((HashMap)mempaths).clone();
						visited.clear();
	//					ratoms.clear();
						
						InstructionList subinst = new InstructionList(groupinst);
						groupinst.add(new Instruction(Instruction.BRANCH, subinst));
	
						//				tmplabel.insts = ;
						// ���Ĥ��ä����ȥ���ѿ��˼�������
						int atompath = varcount++;
						if(!Env.findatom2){
		//					insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom2(atompath, thismempath, findatomcount, atom.functor));
							findatomcount++;
						} else
	//						insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
						// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
						Membrane[] testmems = { mem };
						if (proccxteqMap.containsKey(mem)) {
							// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
							testmems = new Membrane[]{ mem,
								((ProcessContextEquation)proccxteqMap.get(mem)).def.lhsOcc.mem };
						}
						for (int i = 0; i < testmems.length; i++) {
							Iterator it2 = testmems[i].atoms.iterator();
							while (it2.hasNext()) {
								Atom otheratom = (Atom)it2.next();					
								int other = atomToPath(otheratom);
								if (other == UNBOUND) continue;
								if (!otheratom.functor.equals(atom.functor)) continue;
								//if (otheratom == atom) continue;
								subinst.insts.add(new Instruction(Instruction.NEQATOM, atompath, other));
								testmems[i].connect(otheratom, atom);
							}
						}
						atompaths.put(atom, new Integer(atompath));
						if(debug)Util.println("put " + atom);
	
						//��󥯤ΰ�����(RISC��) by mizuno
						getLinks(atompath, atom.functor.getArity(), subinst.insts);
						compileLinkedGroup(atom, subinst);
	//					if(ratoms!=null)ratoms.add(atom);
						List memActuals  = getMemActuals();
						List atomActuals = getAtomActuals();
						List varActuals  = getVarActuals();
						// - ������#1
						
						subinst.add(new Instruction(Instruction.RESETVARS,memActuals, atomActuals, varActuals) );
						subinst.add(new Instruction(Instruction.PROCEED));
						//varcount = 0;
						if(varcount!=restvarcount){
							diffvarcount = varcount - restvarcount;
							varcount = restvarcount;
						}
						mempaths.put(mems.get(0), new Integer(0));
					}
					varcount += diffvarcount;
					if(!groupinst.insts.isEmpty()){
						insts.add(new Instruction(Instruction.GROUP, groupinst));
						if(varcount > maxvarcount)
							maxvarcount = varcount;
						resetMemActuals();
						resetAtomActuals();
					}
				}
	//			compileLinkedGroup(atom);	// 2�Ծ�˰�ư���Ƥߤ� n-kato (2004.7.16)
			}
			it = mem.mems.iterator();
			while (it.hasNext()) {
				thismempath = memToPath(mem);
				Membrane submem = (Membrane)it.next();
				int submempath = memToPath(submem);
				if (submempath == UNBOUND) {
					// !fFindDataAtoms�ΤȤ��������ƥ��֥��ȥ��ޤޤʤ�����μ������󤷤ˤ���
					if (!fFindDataAtoms) {
						Iterator it2 = submem.atoms.iterator();
						boolean exists = false;
						while (it2.hasNext()) {
							Atom atom = (Atom)it2.next();
							if (atom.functor.isActive()) {
								exists = true;
								break;
							}
						}
						if (!exists) continue;
					}		
			
					// ������ѿ��˼�������
					submempath = varcount++;
					if(Env.findatom2){
	//					insts.add(Instruction.anymem2(submempath, thismempath, submem.kind, anymemcount, submem.name));
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
						anymemcount++;
					} else
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
					if(Env.slimcode){
						// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
						Iterator it2 = mem.mems.iterator();
						while (it2.hasNext()) {
							Membrane othermem = (Membrane)it2.next();
							int other = memToPath(othermem);
							if (other == UNBOUND) continue;
							//if (othermem == submem) continue;
							insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
							mem.connect(submem, othermem);
						}
					}
					mempaths.put(submem, new Integer(submempath));
				}
				//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
				compileMembraneForSlimcode(submem, list, false);
			}
		} else {
			Iterator<LinkedList> ite = mem.allKnownElements().iterator();
			while (ite.hasNext()) {
				compileMembraneSecondTime(mem, list, ite.next(), rireki);
			}
		}
		if(varcount > maxvarcount)
			maxvarcount = varcount;
		if(debug)Util.println(insts);
	}
	public void compileMembraneSecondTime(Membrane mem, InstructionList list, List atommems, boolean rireki) {
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
					HashSet qatoms = new HashSet();
					int restvarcount = varcount;
					int diffvarcount = 0;
					qatoms.clear();
					HashMap newatompaths = (HashMap)((HashMap)atompaths).clone();
					HashMap newmempaths = (HashMap)((HashMap)mempaths).clone();
					HashSet newvisited = (HashSet)visited.clone();
					HashSet newmemVisited = (HashSet)memVisited.clone();
					for(int listi2 = listi; listi2<atommems.size();listi2++){
						if(atommems.get(listi2) instanceof Membrane)
							continue;
						if(visited.contains((Atom)atommems.get(listi2)))
							continue;
						searchLinkedGroup((Atom)atommems.get(listi2), qatoms, atom.mem);
					}
//					searchLinkedGroup(atom, qatoms, atom.mem);
					varcount = restvarcount;
					Iterator it3 = qatoms.iterator();
					groupinst = nextgroupinst;
					nextgroupinst = new InstructionList(list);
					while(it3.hasNext()){
						atom = (Atom)it3.next();
						visited = newvisited;
						memVisited = newmemVisited;
						atompaths = newatompaths;
						mempaths = newmempaths;
						newvisited = (HashSet)visited.clone();
						newmemVisited = (HashSet)memVisited.clone();
						newatompaths = (HashMap)((HashMap)atompaths).clone();
						newmempaths = (HashMap)((HashMap)mempaths).clone();
						visited.clear();
						
						InstructionList subinst = new InstructionList(groupinst);
						groupinst.add(new Instruction(Instruction.BRANCH, subinst));
	
						// ���Ĥ��ä����ȥ���ѿ��˼�������
						int atompath = varcount++;
						if(Env.findatom2 && rireki){
//							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
							subinst.insts.add(Instruction.findatom2(atompath, thismempath, findatomcount, atom.functor));
							findatomcount++;
						} else
							subinst.insts.add(Instruction.findatom(atompath, thismempath, atom.functor));
						// ���Ǥ˼������Ƥ���Ʊ����°�줫��Ʊ���ե��󥯥�����ĥ��ȥ�Ȥ���Ʊ�����򸡺�����
						Membrane[] testmems = { mem };
						if (proccxteqMap.containsKey(mem)) {
							// $p�����ȥåץ�٥�Υ��ȥ�ΤȤ��ϡ�$p���إåɽи�������Ȥ���Ӥ���
							testmems = new Membrane[]{ mem,
								((ProcessContextEquation)proccxteqMap.get(mem)).def.lhsOcc.mem };
						}
						for (int i = 0; i < testmems.length; i++) {
							Iterator it2 = testmems[i].atoms.iterator();
							while (it2.hasNext()) {
								Atom otheratom = (Atom)it2.next();					
								int other = atomToPath(otheratom);
								if (other == UNBOUND) continue;
								if (!otheratom.functor.equals(atom.functor)) continue;
								subinst.insts.add(new Instruction(Instruction.NEQATOM, atompath, other));
							}
						}
						atompaths.put(atom, new Integer(atompath));
	
						//��󥯤ΰ�����(RISC��) by mizuno
						getLinks(atompath, atom.functor.getArity(), subinst.insts);
						compileLinkedGroup(atom, subinst);
						compileMembraneSecondTime(mem, subinst, atommems, false);
						List memActuals  = getMemActuals();
						List atomActuals = getAtomActuals();
						List varActuals  = getVarActuals();
						
						subinst.add(new Instruction(Instruction.RESETVARS,memActuals, atomActuals, varActuals) );
						subinst.add(new Instruction(Instruction.PROCEED));
						if(varcount!=restvarcount){
							diffvarcount = varcount - restvarcount;
							varcount = restvarcount;
						}
						mempaths.put(mems.get(0), new Integer(0));
					}
					varcount += diffvarcount;
					if(!groupinst.insts.isEmpty()){
						insts.add(new Instruction(Instruction.GROUP, groupinst));
						if(varcount > maxvarcount)
							maxvarcount = varcount;
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
					if (!fFindDataAtoms) {
						Iterator it2 = submem.atoms.iterator();
						boolean exists = false;
						while (it2.hasNext()) {
							Atom atom = (Atom)it2.next();
							if (atom.functor.isActive()) {
								exists = true;
								break;
							}
						}
						if (!exists) continue;
					}		
			
					// ������ѿ��˼�������
					submempath = varcount++;
					if(Env.findatom2 && rireki){
	//					insts.add(Instruction.anymem2(submempath, thismempath, submem.kind, anymemcount, submem.name));
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
						anymemcount++;
					} else
						insts.add(Instruction.anymem(submempath, thismempath, submem.kind, submem.name));
					if(Env.slimcode){
						// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
						Iterator it2 = mem.mems.iterator();
						while (it2.hasNext()) {
							Membrane othermem = (Membrane)it2.next();
							int other = memToPath(othermem);
							if (other == UNBOUND) continue;
							//if (othermem == submem) continue;
							insts.add(new Instruction(Instruction.NEQMEM, submempath, other));
						}
					}
					mempaths.put(submem, new Integer(submempath));
				}
				//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
				compileMembraneForSlimcode(submem, list, false);
				compileMembraneSecondTime(mem, list, atommems, false);
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
	public void checkFreeLinkCount(Membrane mem, List<Instruction> insts) {
		if (!mem.processContexts.isEmpty()) {
			int thismempath = memToPath(mem);
			ProcessContext pc = (ProcessContext)mem.processContexts.get(0); // �������$p��ɬ����ȥå����
//			// ����Ū�ʥ�����ɬ����ȥå���Υ��ȥ�ʼ�ͳ��󥯴������ȥ��ޤ�ˡˤ�
//			// ��ͳ��󥯽��ϴ������ȥ�Ǥʤ����Ȥ��ǧ����
//			for (int i = 0; i < pc.args.length; i++) {
//				int freelinktestedatompath = varcount++;
//				match.add(new Instruction(Instruction.DEREFATOM, freelinktestedatompath,
//					atomToPath(pc.args[i].buddy.atom), pc.args[i].buddy.pos));
//				match.add(new Instruction(Instruction.NOTFUNC, freelinktestedatompath,
//					Functor.INSIDE_PROXY));
//			}
			// ���«��̵�����
			if (pc.bundle == null) {
				insts.add(new Instruction(Instruction.NFREELINKS, thismempath,
					mem.getFreeLinkAtomCount()));					
			}
		}
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			checkFreeLinkCount(submem, insts);
		}
	}
//	public Instruction getResetVarsInstruction() {
//		return Instruction.resetvars(getMemActuals(), getAtomActuals(), getVarActuals());
//	}
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤ����������֤���
	 * ����Ū�ˤ�mems���б������ѿ��ֹ�Υꥹ�Ȥ��Ǽ����ArrayList���֤���*/
	public List getMemActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < mems.size(); i++) {
			if(mempaths.get(mems.get(i)) != null)args.add( mempaths.get(mems.get(i)) );
		}
		return args;
	}
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤΥ��ȥ��������֤���
	 * ����Ū�ˤ�HeadCompiler��atoms���б������ѿ��ֹ�Υꥹ�Ȥ��Ǽ����ArrayList���֤���*/
	public List getAtomActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < atoms.size(); i++) {
			if(atompaths.get(atoms.get(i)) != null)args.add( atompaths.get(atoms.get(i)) );
		}
		return args;
	}		
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤ���䥢�ȥ�ʳ��ΰ�������֤���
	 * ����Ū�ˤ�HeadCompiler�϶���ArrayList���֤���*/
	public List getVarActuals() {
		return new ArrayList();
	}
	
	void resetAtomActuals(){
		Map newatompaths = new HashMap();
		for (int i = 0; i < atoms.size(); i++) {
			if(atompaths.get(atoms.get(i)) != null){
				newatompaths.put( atoms.get(i), new Integer(varcount));
				varcount++;
			}
		}
		atompaths = newatompaths;
	}
	void resetMemActuals(){
		Map newmempaths = new HashMap();
		varcount = 0;
		for (int i = 0; i < mems.size(); i++) {
			if(mempaths.get(mems.get(i)) != null){
				newmempaths.put( mems.get(i), new Integer(varcount));
				varcount++;
			}
		}
		mempaths = newmempaths;
	}
	////////////////////////////////////////////////////////////////
	
	/** ������������򥳥�ѥ��뤹�� */
	void compileNegativeCondition(LinkedList eqs, InstructionList list) throws CompileException{
		List<Instruction> insts = list.insts;
		//int formals = varcount;
		//matchLabel.setFormals(formals);
		Iterator it = eqs.iterator();
		while (it.hasNext()) {
			ProcessContextEquation eq = (ProcessContextEquation)it.next();
			enumFormals(eq.mem);
			mempaths.put(eq.mem, mempaths.get(eq.def.lhsOcc.mem));
			proccxteqMap.put(eq.mem, eq);
		}
		it = eqs.iterator();
		while (it.hasNext()) {
			ProcessContextEquation eq = (ProcessContextEquation)it.next();
			if(Env.findatom2){
				compileMembraneForSlimcode(eq.mem, list, false);
			} else {
				compileMembrane(eq.mem, list);
			}
			// �ץ���ʸ̮���ʤ��Ȥ��ϡ����ȥ�Ȼ���θĿ����ޥå����뤳�Ȥ��ǧ����
			if (eq.mem.processContexts.isEmpty()) {
				// TODO �ʵ�ǽ��ĥ��ñ��Υ��ȥ�ʳ��˥ޥå����뷿�դ��ץ���ʸ̮�Ǥ�������ư���褦�ˤ���(2)
				insts.add(new Instruction(Instruction.NATOMS, mempaths.get(eq.mem),
					  eq.def.lhsOcc.mem.getNormalAtomCount() + eq.def.lhsOcc.mem.typedProcessContexts.size()
					+ eq.mem.getNormalAtomCount() + eq.mem.typedProcessContexts.size() ));
				insts.add(new Instruction(Instruction.NMEMS, mempaths.get(eq.mem),
					eq.def.lhsOcc.mem.mems.size() + eq.mem.mems.size() ));
			}
			else {
				ProcessContext pc = (ProcessContext)eq.mem.processContexts.get(0);
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
}
// TODO �ʵ�ǽ��ĥ�˥��������������η��դ��ץ���ʸ̮�򥳥�ѥ��뤹��