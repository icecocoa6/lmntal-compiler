/*
 * ������: 2003/10/28
 *
 */
package compile;

import java.util.*;
import runtime.Env;
import runtime.Functor;
import runtime.Instruction;
import runtime.InstructionList;
import compile.structure.*;

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
//	/** ������ */
//	public Membrane lhsmem;//m;
	/** �ޥå���̿����ʤΥ�٥��*/
	public InstructionList matchLabel;
	/** matchLabel.insts */
	public List match;

	public List mems			= new ArrayList();	// �и�������Υꥹ�ȡ�[0]��m
	public List atoms			= new ArrayList();	// �и����륢�ȥ�Υꥹ��	
	public Map  mempaths		= new HashMap();	// Membrane -> �ѿ��ֹ�
	public Map  atompaths		= new HashMap();	// Atomic -> �ѿ��ֹ�
	public Map  linkpaths		= new HashMap();	// Atom���ѿ��ֹ� -> ��󥯤��ѿ��ֹ������
	
	private Map atomids		= new HashMap();	// Atom -> atoms���index���ѻߤ������Ǹ�Ƥ�����
	private HashSet visited	= new HashSet();	// Atom -> boolean, �ޥå���̿��������������ɤ���
	private HashSet memVisited	= new HashSet();	// Membrane -> boolean, compileMembrane��Ƥ�����ɤ���

	boolean fFindDataAtoms;						// �ǡ������ȥ��findatom���Ƥ褤���ɤ���
	boolean UNTYPED_COMPILE	= false;			// fFindDataAtoms�ν����
	
	int varcount;	// �����쥢�ȥ�����ʬ����٤����Ȼפ�
	
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
	final int linkToPath(int atomid, int pos) {
		if (!linkpaths.containsKey(new Integer(atomid))) return UNBOUND;
		return ((int[])linkpaths.get(new Integer(atomid)))[pos];
	}
	
	static final int UNBOUND = -1;
	
	HeadCompiler() {}
	
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
		Iterator it = mem.atoms.iterator();
		while (it.hasNext()) {
			// ���դ˽и��������ȥ����Ͽ����
			Atom atom = (Atom)it.next();
			atomids.put(atom, new Integer(atoms.size()));
			atoms.add(atom);
		}
		mems.add(mem);	// �����mems[0]
		it = mem.mems.iterator();
		while (it.hasNext()) {
			enumFormals((Membrane)it.next());
		}
	}
	
	public void prepare() {
		Env.c("prepare");
		mempaths.clear();
		atompaths.clear();
		visited.clear();
		memVisited.clear();
		matchLabel = new InstructionList();
		match = matchLabel.insts;
		varcount = 1;	// [0]������
//		mempaths.put(mems.get(0), new Integer(0));	// ������ѿ��ֹ�� 0
		fFindDataAtoms = UNTYPED_COMPILE;
	}

	/**
	 * ���ꤵ�줿���ȥ���Ф���getlink��Ԥ����ѿ��ֹ��linkpaths����Ͽ���롣
	 * RISC����ȼ���ɲ�(mizuno)
	 */
	public final void getLinks(int atompath, int arity) {
		int[] paths = new int[arity];
		for (int i = 0; i < arity; i++) {
			paths[i] = varcount;
			match.add(new Instruction(Instruction.GETLINK, varcount, atompath, i));
			varcount++;
		}
		linkpaths.put(new Integer(atompath), paths);
	}
	
	/** ��󥯤ǤĤʤ��ä����ȥप��Ӥ��ν�°����Ф��ƥޥå��󥰤�Ԥ���
	 * �ޤ�������Ǹ��Ĥ��ä��ֿ�������פΤ��줾����Ф��ơ�compileMembrane��Ƥ֡�
	 */
	public void compileLinkedGroup(Atom firstatom) {
		Env.c("compileLinkedGroup");
		LinkedList newmemlist = new LinkedList();
		LinkedList atomqueue = new LinkedList();
		atomqueue.add(firstatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = (Atom)atomqueue.removeFirst();			
			if (visited.contains(atom)) continue;
			visited.add(atom);
			
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence buddylink = atom.args[pos].buddy;
				if (buddylink == null) continue; // ������ƿ̾��󥯤�̵��
				if (!atomids.containsKey(buddylink.atom)) continue; // ���դ�$p�ʤ����lhs->neg�ˤؤΥ�󥯤�̵��
				Atom buddyatom = (Atom)buddylink.atom;
				
				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ����硣
					// - �����Υ��ȥ�buddyatom�Ȱ����˼����������ȥ��Ʊ�����򸡺����롣
					//   �������������ѿ��ֹ椪��Ӱ����ֹ���Ȥ˴�Ť����������ΤߤǤ褤��
					// neg(�������եȥåץ�٥�)->lhs(���դ���ȥåץ�٥�)�ΤȤ�
					if (proccxteqMap.containsKey(atom.mem)
					 && !proccxteqMap.containsKey(buddyatom.mem)
					 && buddyatom.mem.mem != null) {
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
				match.add( new Instruction(Instruction.DEREF,
					buddyatompath, atomToPath(atom), pos, buddylink.pos ));
				
				// ����褬¾���������դΥ��ȥ�ξ��������֥�󥯤ξ���
				// ��֤μ�ͳ��󥯴������ȥ຿�θ����򤷡��쳬�ؤ��ޥå����뤫������Ԥ���
				// �ޤ�*A��DEREF����4���������buddyatompath���������롣
				if (proccxteqMap.containsKey(atom.mem)
				 && proccxteqMap.containsKey(buddyatom.mem) && buddyatom.mem != atom.mem) {				
					// ( 0: 1:{$p[|*X],2:{$q[|*Y]}} :- \+($p=(atom(L),$pp),$q=(buddy(L),$qq)) | ... )
					// ���Υ롼��Υ����ɤΰ�̣:
					// ( 0: 1:{atom(L),$pp[|*XX],2:{buddy(L),$qq[|*YY]}} :- ... ) �ˤϥޥå����ʤ�
					int firstindex = match.size() - 1; // atom�����DEREF̿���ؤ�
					//
					LinkedList atomSupermems  = new LinkedList(); // atom�ι�����������ʿ���¦����Ƭ��
					LinkedList buddySupermems = new LinkedList(); // buddy�ι�����������ʿ���¦����Ƭ��
					// ������������η׻�
					// atomSupermems = {0,1}; buddySupermems = {0,1,2}
					Membrane mem = ((ProcessContextEquation)proccxteqMap.get(buddyatom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						buddySupermems.addFirst(mem);
						mem = mem.mem;
					}
					mem = ((ProcessContextEquation)proccxteqMap.get(atom.mem)).def.lhsOcc.mem;
					while (mem != null) {
						atomSupermems.addFirst(mem);
						mem = mem.mem;
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
						match.add( new Instruction(Instruction.FUNC, buddyatompath, Functor.INSIDE_PROXY) );
						match.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						match.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					while (!buddySupermems.isEmpty()) {
						mem = (Membrane)buddySupermems.removeFirst();
						match.add( new Instruction(Instruction.FUNC, buddyatompath, Functor.OUTSIDE_PROXY) );
						match.add( new Instruction(Instruction.DEREF, buddyatompath + 1, buddyatompath,     0, 0) );
						match.add( new Instruction(Instruction.TESTMEM, memToPath(mem), buddyatompath + 1) );
						match.add( new Instruction(Instruction.DEREF, buddyatompath + 2, buddyatompath + 1, 1, 1) );
						buddyatompath += 2;
					}
					varcount = buddyatompath + 1;
					int lastindex = match.size() - 1; // buddyatom��������뤿���DEREF̿���ؤ�
					
					// deref̿�����4������������					
					// - deref [-tmp1atom,atom,atompos,buddypos] ==> deref [-tmp1atom,atom,atompos,1]
//					((Instruction)match.get(firstindex)).setArg4(new Integer(1));
					Instruction oldfirst = (Instruction)match.remove(firstindex);
					Instruction newfirst = new Instruction(Instruction.DEREF,
						oldfirst.getIntArg1(), oldfirst.getIntArg2(), oldfirst.getIntArg3(), 1);
					match.add(firstindex,newfirst);
					// - deref [-buddyatom,tmpatom,tmppos,1] ==> deref [-buddyatom,buddypos,atompos,buddypos]
//					((Instruction)match.get(lastindex)).setArg4(new Integer(buddylink.pos));
					Instruction oldlast = (Instruction)match.remove(lastindex);
					Instruction newlast = new Instruction(Instruction.DEREF,
						oldlast.getIntArg1(), oldlast.getIntArg2(), oldlast.getIntArg3(), buddylink.pos);
					match.add(lastindex,newlast);
				}
				
				if (atomToPath(buddyatom) != UNBOUND) {
					// �����Υ��ȥ�򤹤Ǥ˼������Ƥ�����
					// lhs(<)->lhs(>), neg(<)->neg(>), neg->lhs �ʤΤǥ����Υ��ȥ��Ʊ�������ǧ
					match.add( new Instruction(Instruction.EQATOM,
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
						match.add(new Instruction(Instruction.NEQATOM, buddyatompath, other));
					}
				}	
							
				// �����Υ��ȥ���ѿ��˼�������
				
				atompaths.put(buddyatom, new Integer(buddyatompath));
				atomqueue.addLast( buddyatom );
				match.add(new Instruction(Instruction.FUNC, buddyatompath, buddyatom.functor));
				
				// �������������
				
				if (atom.functor.equals(runtime.Functor.OUTSIDE_PROXY) && pos == 0) {
					// ����ؤΥ�󥯤ξ�硢�����Ʊ�����򸡺����ʤ���Фʤ�ʤ�
					Membrane buddymem = buddyatom.mem;								
					int buddymempath = memToPath(buddyatom.mem);
					if (buddymempath != UNBOUND) {
						match.add(new Instruction( Instruction.TESTMEM, buddymempath, buddyatompath ));
					}
					else {
						buddymempath = varcount++;
						mempaths.put(buddymem, new Integer(buddymempath));
						match.add(new Instruction( Instruction.LOCKMEM, buddymempath, buddyatompath ));
						newmemlist.add(buddymem);
					// // GETMEM����Υ�����
					//	Iterator it = buddymem.mem.mems.iterator();
					//	while (it.hasNext()) {
					//		Membrane othermem = it.getNext();
					//		if (othermem != buddymem && memToPath(othermem) != UNBOUND) {
					//			match.add(new Instruction( Instruction.NEQMEM,
					//				buddymempath, memIDPath(othermem) ));
					//		}
					//	}
					}
				}
				//��󥯤ΰ�����(RISC��) by mizuno
				getLinks(buddyatompath, buddyatom.functor.getArity());
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
					compileMembrane(mem);
					it.remove();
					continue nextmem;
				}					
			}
		}
		it = newmemlist.iterator();
		while (it.hasNext()) {
			compileMembrane((Membrane)it.next());
		}
	}
	/** ����³�����Υإåɤ򷿤ʤ��ǥ���ѥ��뤹�뤿��ν����򤹤롣*/
	public void switchToUntypedCompilation() {
		fFindDataAtoms = true;
		memVisited.clear();
	}
	/** �줪��ӻ�¹������Ф��ƥޥå��󥰤�Ԥ� */
	public void compileMembrane(Membrane mem) {
		Env.c("compileMembrane");
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
				match.add(Instruction.findatom(atompath, thismempath, atom.functor));
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
						match.add(new Instruction(Instruction.NEQATOM, atompath, other));
					}
				}
				atompaths.put(atom, new Integer(atompath));
				//��󥯤ΰ�����(RISC��) by mizuno
				getLinks(atompath, atom.functor.getArity());
				compileLinkedGroup(atom);
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
				match.add(Instruction.anymem(submempath, thismempath));
// NEQMEM �����פˤʤäƤ��뤬�����ͤΤ���˥����ɤϻĤ��Ƥ�����
//				Iterator it2 = mem.mems.iterator();
//				while (it2.hasNext()) {
//					Membrane othermem = (Membrane)it2.next();
//					int other = memToPath(othermem);
//					if (other == UNBOUND) continue;
//					//if (othermem == submem) continue;
//					match.add(new Instruction(Instruction.NEQMEM, submempath, other));
//				}
				mempaths.put(submem, new Integer(submempath));
			}
			//�ץ���ʸ̮���ʤ�����stable�θ����ϡ������ɥ���ѥ���˰�ư������by mizuno
			compileMembrane(submem);
		}
	}
	/** �줪��ӻ�¹������Ф��Ƽ�ͳ��󥯤θĿ���Ĵ�٤롣
	 * <p>���Ĥ�$p����������ʳ��ξ��ϡ���ͳ��󥯤˴ؤ��븡����Ԥ�ɬ�פ����ä���
	 * ���������� redex "T��" �� = ��ޤ�Ǥ�褤������ͤˤʤäƤ��뤿�ᡢ���θ����ϼ¤����ס�
	 * �������äƤ��Υ᥽�åɤϸƤФ�ʤ���(n-kato 2004.11.24--2004.11.26) */
	public void checkFreeLinkCount(Membrane mem) {
		if (!mem.processContexts.isEmpty()) {
			int thismempath = memToPath(mem);
			ProcessContext pc = (ProcessContext)mem.processContexts.get(0); // �������$p��ɬ����ȥå����
			// ����Ū�ʥ�����ɬ����ȥå���Υ��ȥ�ʼ�ͳ��󥯴������ȥ��ޤ�ˡˤ�
			// ��ͳ��󥯽��ϴ������ȥ�Ǥʤ����Ȥ��ǧ����
			for (int i = 0; i < pc.args.length; i++) {
				int freelinktestedatompath = varcount++;
				match.add(new Instruction(Instruction.DEREFATOM, freelinktestedatompath,
					atomToPath(pc.args[i].buddy.atom), pc.args[i].buddy.pos));
				match.add(new Instruction(Instruction.NOTFUNC, freelinktestedatompath,
					Functor.INSIDE_PROXY));
			}
			// ���«��̵�����
			if (pc.bundle == null) {
				match.add(new Instruction(Instruction.NFREELINKS, thismempath,
					mem.getFreeLinkAtomCount()));					
			}
		}
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			checkFreeLinkCount(submem);
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
			args.add( mempaths.get(mems.get(i)) );
		}
		return args;
	}
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤΥ��ȥ��������֤���
	 * ����Ū�ˤ�HeadCompiler��atoms���б������ѿ��ֹ�Υꥹ�Ȥ��Ǽ����ArrayList���֤���*/
	public List getAtomActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < atoms.size(); i++) {
			args.add( atompaths.get(atoms.get(i)) );
		}
		return args;
	}		
	/** ����̿����ʥإå�̿���󢪥�����̿���󢪥ܥǥ�̿����ˤؤ���䥢�ȥ�ʳ��ΰ�������֤���
	 * ����Ū�ˤ�HeadCompiler�϶���ArrayList���֤���*/
	public List getVarActuals() {
		return new ArrayList();
	}
	
	////////////////////////////////////////////////////////////////
	
	/** ������������򥳥�ѥ��뤹�� */
	void compileNegativeCondition(LinkedList eqs) {
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
			compileMembrane(eq.mem);
			// �ץ���ʸ̮���ʤ��Ȥ��ϡ����ȥ�Ȼ���θĿ����ޥå����뤳�Ȥ��ǧ����
			if (eq.mem.processContexts.isEmpty()) {
				// TODO �ʵ�ǽ��ĥ��ñ��Υ��ȥ�ʳ��˥ޥå����뷿�դ��ץ���ʸ̮�Ǥ�������ư���褦�ˤ���(2)
				match.add(new Instruction(Instruction.NATOMS, mempaths.get(eq.mem),
					  eq.def.lhsOcc.mem.getNormalAtomCount() + eq.def.lhsOcc.mem.typedProcessContexts.size()
					+ eq.mem.getNormalAtomCount() + eq.mem.typedProcessContexts.size() ));
				match.add(new Instruction(Instruction.NMEMS, mempaths.get(eq.mem),
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
		// todo ��ͳ���
		match.add(new Instruction(Instruction.PROCEED));	// ��STOP
		//matchLabel.updateLocals(varcount);
	}
}
// TODO �ʵ�ǽ��ĥ�˥��������������η��դ��ץ���ʸ̮�򥳥�ѥ��뤹��
