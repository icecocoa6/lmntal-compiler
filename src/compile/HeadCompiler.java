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
 * TODO path�Ȥ���̿̾��������������ɽ���Ƥ��ʤ����ºݤ�varnum������Ĺ���Τ�id�ˤ��褦���Ȥ�פäƤ���
 * 
 * <p><b>����</b>��
 * ���ߥޥå���̿����Ǥϡ�������ѿ��ֹ��0����Ƴ���륢�ȥ���ѿ��ֹ��1�ˤ��Ƥ��롣����Ϻ�����Ѥ��ʤ���
 * �ܥǥ�̿����β������Ǥϡ����mems����󤷤Ƥ��顢����³�����ѿ��ֹ��atoms����󤷤Ƥ��롣
 */
public class HeadCompiler {
	public Membrane m;
	
	/**
	 * OBSOLETE: ��������ݻ����롣�°����ꥹ�Ȥ��̾異�ȥफ�����뤬������˥ޥå���������϶��켫�Ȥ��°����Ȥʤ롣
	 * OBSOLETE: �°����ꥹ����ǡ�Ǥ�դζ����Ǥ�դΥ��ȥ��������롣
	 * ���: ����ȸ��äƤ�{$p}�ʤɤ�ޤޤ��
	 */
	public InstructionList matchLabel;
	public List match;								// �ޥå���̿����
	
	public List mems			= new ArrayList();	// �и�������Υꥹ�ȡ�[0]��m
	public List atoms			= new ArrayList();	// �и����륢�ȥ�Υꥹ��	
	public Map  mempaths		= new HashMap();	// Membrane -> �ѿ��ֹ�
	public Map  atompaths		= new HashMap();	// Atom -> �ѿ��ֹ�
	
	private Map atomids		= new HashMap();	// Atom -> atoms���index���ѻߤ������Ǹ�Ƥ�����
	private HashSet visited	= new HashSet();	// Atoms -> boolean, �ޥå���̿��������������ɤ���
	
	int varcount;	// �����쥢�ȥ�����ʬ����٤����Ȼפ�
	
	final boolean isAtomLoaded(Atom atom) { return atompaths.containsKey(atom); }
	final boolean isMemLoaded(Membrane mem) { return mempaths.containsKey(mem); }

	final int atomToPath(Atom atom) { 
		if (!isAtomLoaded(atom)) return UNBOUND;
		return ((Integer)atompaths.get(atom)).intValue();
	}
	final int memToPath(Membrane mem) {
		 if (!isMemLoaded(mem)) return UNBOUND;
		 return ((Integer)mempaths.get(mem)).intValue();
	}
	static final int UNBOUND = -1;
	
	HeadCompiler(Membrane m) {
		//Env.n("HeadCompiler");
		this.m = m;
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
		varcount = 1;	// [0]������
		mempaths.clear();
		atompaths.clear();
		visited.clear();
		matchLabel = new InstructionList();
		match = matchLabel.insts;
	}
	
	public void compileLinkedGroup(Atom firstatom) {
		Env.c("compileLinkedGroup");
		LinkedList atomqueue = new LinkedList();
		atomqueue.add(firstatom);
		while( ! atomqueue.isEmpty() ) {
			Atom atom = (Atom)atomqueue.removeFirst();			
			if (visited.contains(atom)) continue;
			visited.add(atom);
			
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence buddylink = atom.args[pos].buddy;
				if (buddylink == null) continue; // ������ƿ̾��󥯤�̵��
				
				Atom buddyatom = buddylink.atom;
				if (!atomids.containsKey(buddyatom)) continue; // ���աʤ䷿�դ�$p�ˤؤΥ�󥯤�̵��
				
				if (atomToPath(buddyatom) != UNBOUND) { // �����Υ��ȥ�򤹤Ǥ˼������Ƥ�����
					// lhs(>)->lhs(<) �ޤ��� neg(>)->sameneg(<) �ʤ�С�
					// ���Ǥ�Ʊ�������ǧ���륳���ɤ���Ϥ��Ƥ��뤿�ᡢ���⤷�ʤ�
					if (true 
					 || (buddyatom.mem == m && atom.mem == m) // �����拾��ѥ������debugͽ��
					   ) { 
						int b = atomToPath(buddyatom);
						int t = atomToPath(atom);
						if (b < t) continue;
						if (b == t && buddylink.pos < pos) continue;
					}
				}
				int buddyatompath = varcount++;
				match.add( new Instruction(Instruction.DEREF,
					buddyatompath, atomToPath(atom), pos, buddylink.pos ));
					
				if( atomToPath(buddyatom) != UNBOUND ) { // �����Υ��ȥ�򤹤Ǥ˼������Ƥ�����
					// lhs(<)->lhs(>), neg(<)->neg(>), neg->lhs �ʤΤǥ����Υ��ȥ��Ʊ�������ǧ
					match.add(new Instruction(Instruction.EQATOM,
						buddyatompath, atomToPath(buddyatom) ));
					continue;
				}
				
				// �����Υ��ȥ���ѿ��˼�������
				
				atompaths.put(buddyatom, new Integer(buddyatompath));
				atomqueue.addLast( buddyatom );
				match.add(new Instruction(Instruction.FUNC, buddyatompath, buddyatom.functor));
				
				if (atom.functor.equals(runtime.Functor.OUTSIDE_PROXY) && pos == 0) {
					// ����ؤΥ�󥯤ξ��
					Membrane buddymem = buddyatom.mem;								
					int buddymempath = memToPath(buddyatom.mem);
					if (buddymempath != UNBOUND) {
						match.add(new Instruction( Instruction.TESTMEM, buddymempath, buddyatompath ));
					}
					else {
						buddymempath = varcount++;
						mempaths.put(buddymem, new Integer(buddymempath));
						match.add(new Instruction( Instruction.LOCKMEM, buddymempath, buddyatompath ));
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
			}
		}
	}
	public void compileMembrane(Membrane mem) {
		Env.c("compileMembrane");
		int thismempath = memToPath(mem);
		
		Iterator it = mem.atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (atomToPath(atom) == UNBOUND) {
				// ���Ĥ��ä����ȥ���ѿ��˼�������
				int atompath = varcount++;
				match.add(Instruction.findatom(atompath, thismempath, atom.functor));
				// ���Ǥ˼������Ƥ��륢�ȥ�Ȥ���Ʊ�����򸡺�����
				Iterator it2 = mem.atoms.iterator();
				while (it2.hasNext()) {
					Atom otheratom = (Atom)it2.next();					
					int other = atomToPath(otheratom);
					if (other == UNBOUND) continue;
					if (!otheratom.functor.equals(atom.functor)) continue;
					//if (otheratom == atom) continue;
					match.add(new Instruction(Instruction.NEQATOM, atompath, other));
				}
				atompaths.put(atom, new Integer(atompath));
			}
			compileLinkedGroup(atom);
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
			// �ץ���ʸ̮���ʤ��Ȥ��ϡ����ȥ�Ȼ���θĿ����ޥå����뤳�Ȥ��ǧ����
			// �ʥ����ɥ���ѥ���˰�ư����ͽ���
			if (submem.processContexts.isEmpty()) {
//				match.add(new Instruction(Instruction.NATOMS, submempath, submem.atoms.size()));
				// TODO ñ��Υ��ȥ�ʳ��˥ޥå����뷿�դ��ץ���ʸ̮�Ǥ�������ư���褦�ˤ���
				match.add(new Instruction(Instruction.NATOMS, submempath,
					submem.getNormalAtomCount() + submem.typedProcessContexts.size()));
				match.add(new Instruction(Instruction.NMEMS,  submempath, submem.mems.size()));
			}
			//
			if (submem.ruleContexts.isEmpty()) {
				match.add(new Instruction(Instruction.NORULES, submempath));
			}
			if (submem.stable) {
				match.add(new Instruction(Instruction.STABLE, submempath));
			}
			compileMembrane(submem);
		}
		if (!mem.processContexts.isEmpty()) {
			ProcessContext pc = (ProcessContext)mem.processContexts.get(0);
			for (int i = 0; i < pc.args.length; i++) {
				int freelinktestedatompath = varcount++;
				match.add(new Instruction(Instruction.DEREFATOM, freelinktestedatompath,
					atomToPath(pc.args[i].buddy.atom), pc.args[i].buddy.pos));
				match.add(new Instruction(Instruction.NOTFUNC, freelinktestedatompath,
					Functor.INSIDE_PROXY));
			}
			if (pc.bundle == null) {
				match.add(new Instruction(Instruction.NFREELINKS, thismempath,
					mem.getFreeLinkAtomCount()));					
			}
		}
	}
	public Instruction getResetVarsInstruction() {
		return Instruction.resetvars(getMemActuals(), getAtomActuals(), getVarActuals());
	}
	public List getAtomActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < atoms.size(); i++) {
			args.add( atompaths.get(atoms.get(i)) );
		}
		return args;
	}		
	public List getMemActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < mems.size(); i++) {
			args.add( mempaths.get(mems.get(i)) );
		}
		return args;
	}
	public List getVarActuals() {
		return new ArrayList();
	}
}
