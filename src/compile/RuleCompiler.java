package compile;

import java.util.*;
import runtime.Env;
import runtime.Rule;
//import runtime.InterpretedRuleset;
import runtime.Instruction;
import runtime.Functor;
import runtime.Inline;
import compile.structure.*;

/**
 * ����ѥ�����롼�빽¤��compile.structure.RuleStructure�ˤ�
 * �ʥ��󥿥ץ꥿ư���ǽ�ʡ˥롼�륪�֥������ȡ�runtime.Rule�ˤ��Ѵ����롣
 * <p>
 * �ҥ롼�빽¤��̵�뤵�������ˡ�Ʊ����λ��ĥ롼�륻�åȡ�runtime.Ruleset�ˤ����Ȥ���롣
 * �������äƤ��Υ��饹��ƤӽФ� RulesetCompiler �ϡ�
 * �ҥ롼�빽¤������˥롼�륻�åȤ˥���ѥ��뤷�Ƥ���ɬ�פ����롣
 * 
 * @author n-kato, hara
 */
public class RuleCompiler {
	/** ñ�첽���̣����ʤ�ΤȤ���RuleCompiler���ͤ���˥ե��󥯥���=/2 */
	public static final Functor FUNC_UNIFY = new Functor("=", 2);

	/** ����ѥ��뤵���롼�빽¤ */
	public RuleStructure rs;
	
	/** ����ѥ��뤵���롼����б�����롼�륪�֥������� */
	public Rule theRule;
	
	public List atomMatch;
	public List memMatch;
	public List body;
	
	public int varcount;
	
	public List rhsatoms;
	public Map  rhsatompath;
	public Map  rhsmempath;
	
	public List lhsatoms;
	public List lhsfreemems;
	public Map  lhsatompath;
	public Map  lhsmempath;
	
//	private List newatoms = new ArrayList();	// rhsatoms��Ʊ���ʤΤ�����
	
	HeadCompiler hc;
	
	final int lhsmemToPath(Membrane mem) { return ((Integer)lhsmempath.get(mem)).intValue(); }
	final int rhsmemToPath(Membrane mem) { return ((Integer)rhsmempath.get(mem)).intValue(); }
	//final int lhsatomToID(Atom atom) { return lhsatomToPath(atom) - 1; }
	final int lhsatomToPath(Atom atom) { return ((Integer)lhsatompath.get(atom)).intValue(); } 
	final int rhsatomToPath(Atom atom) { return ((Integer)rhsatompath.get(atom)).intValue(); } 
	
	/**
	 * ���ꤵ�줿 RuleStructure �ѤΥ롼���Ĥ���
	 */
	RuleCompiler(RuleStructure rs) {
		//Env.n("RuleCompiler");
		//Env.d(rs);
		this.rs = rs;
	}
	
	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
	 */
	public Rule compile() {
		Env.c("compile");
		simplify();
		theRule = new Rule(rs.toString());
		//@ruleid = rule.ruleid		
		
		hc = new HeadCompiler(rs.leftMem);
		hc.enumFormals(rs.leftMem);	// �إåɤ��Ф��벾�����ꥹ�Ȥ���
		
		compile_l();
		compile_r();
		
		optimize();	// optimize if $optlevel > 0
		
		//showInstructions();
		
		//rule.register(@atomMatches,@memMatch,@body)
		theRule.memMatch  = memMatch;
		theRule.atomMatch = atomMatch;
		theRule.body      = body;
		
		theRule.showDetail();
		return theRule;
	}
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_l() {
		Env.c("compile_l");
		
		atomMatch = new ArrayList();
		int maxvarcount = 2;	// ���ȥ��Ƴ�ѡʲ���
		for (int firstid = 0; firstid <= hc.atoms.size(); firstid++) {
			hc.prepare(); // �ѿ��ֹ������			
			if (firstid < hc.atoms.size()) {			
				if (true) continue; // �׻�����ա������Ƥ�褤���������ȥ��Ƴ�ϸ���̤�ƥ��ȡ�
				if (Env.fRandom) continue; // �롼���ȿ����Ψ��ͥ�褹�뤿�ᥢ�ȥ��Ƴ�ƥ��ȤϹԤ�ʤ�
				// ���ȥ��Ƴ
				List singletonListArgToBranch = new ArrayList();
				singletonListArgToBranch.add(hc.match);
				atomMatch.add(new Instruction(Instruction.BRANCH, singletonListArgToBranch));
				hc.mempath.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
				hc.atomidpath.set(firstid, new Integer(1));	// ��Ƴ���륢�ȥ���ѿ��ֹ�� 1
				hc.varcount = 2;
				Atom atom = (Atom)hc.atoms.get(firstid);
				hc.match.add(new Instruction(Instruction.FUNC, 1, atom.functor));
				Membrane mem = atom.mem;
				if (mem == rs.leftMem) {
					hc.match.add(new Instruction(Instruction.TESTMEM, 0, 1));
				}
				else {
					hc.match.add(new Instruction(Instruction.GETMEM, varcount, 1));
					hc.mempath.put(mem, new Integer(varcount++));
					do {
						hc.match.add(new Instruction(Instruction.GETPARENT,varcount,varcount-1));
						hc.mempath.put(mem, new Integer(varcount++));
						mem = mem.mem;
					}	
					while (mem != rs.leftMem);
					hc.match.add(new Instruction(Instruction.EQMEM, 0, varcount-1));
					for (int i = varcount-1; --i >= 2; ) {
						hc.match.add(new Instruction(Instruction.LOCK,i));
					}
				}
				hc.compileLinkedGroup(firstid);
			} else {
				// ���Ƴ
				memMatch = hc.match;
				hc.mempath.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
			}
			hc.compileMembrane(rs.leftMem);
			hc.match.add(0, Instruction.spec(hc.varcount,0));	// �Ȥꤢ�����������ɲáʲ���
			hc.match.add( Instruction.react(theRule, hc.getMemActuals(), hc.getAtomActuals()) );
			if (maxvarcount < hc.varcount) maxvarcount = hc.varcount;
		}
		atomMatch.add(0, Instruction.spec(maxvarcount,0));	// �Ȥꤢ�����������ɲáʲ���
	}
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_r() {
		Env.c("compile_r");
		
		// �إåɤμ�����
		lhsatoms = hc.atoms;
		lhsfreemems = hc.freemems;
		genLHSPaths();
		varcount = lhsatoms.size() + lhsfreemems.size();
		int formals = varcount;
		
		//
		body = new ArrayList();
		rhsatoms    = new ArrayList();
		rhsatompath = new HashMap();
		rhsmempath  = new HashMap();
		int toplevelmemid = lhsmemToPath(rs.leftMem);
		rhsmempath.put(rs.rightMem, new Integer(toplevelmemid));
		
		//Env.d("rs.leftMem -> "+rs.leftMem);
		//Env.d("lhsmempaths.get(rs.leftMem) -> "+lhsmempaths.get(rs.leftMem));
		//Env.d("rhsmempaths -> "+rhsmempaths);
		
		dequeueLHSAtoms();
		removeLHSAtoms();
		if (removeLHSMem(rs.leftMem) >= 2) {
			body.add(new Instruction(Instruction.REMOVETOPLEVELPROXIES, toplevelmemid));
		}
		buildRHSMem(rs.rightMem);
		if (!rs.rightMem.processContexts.isEmpty()) {
			body.add(new Instruction(Instruction.REMOVETEMPORARYPROXIES, toplevelmemid));
		}
		copyRules(rs.rightMem);
		loadRulesets(rs.rightMem);		
		buildRHSAtoms(rs.rightMem);
		body.add(0, Instruction.spec(formals, varcount));
		updateLinks();
		enqueueRHSAtoms();
		addInline();
		addLoadModules();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms();
		body.add(new Instruction(Instruction.PROCEED));
	}
	
	/** �롼��κ��դȱ��դ��Ф���staticUnify��Ƥ� */
	public void simplify() {
		staticUnify(rs.leftMem);
		staticUnify(rs.rightMem);
	}
	
	/** ���ꤵ�줿��Ȥ��λ�¹��¸�ߤ����Ĺ�� =��todo ����Ӽ�ͳ��󥯴������ȥ�ˤ����� */
	public void staticUnify(Membrane mem) {
		Env.c("RuleCompiler::staticUnify");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			staticUnify((Membrane)it.next());
		}
		ArrayList removedAtoms = new ArrayList();
		it = mem.atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (atom.functor.equals(FUNC_UNIFY)) {
				LinkOccurrence link1 = atom.args[0].buddy;
				LinkOccurrence link2 = atom.args[1].buddy;
				if (link1.atom.mem != mem && link2.atom.mem != mem) {
					// ñ�첽���ȥ�Υ���褬ξ���Ȥ�¾����ˤĤʤ��äƤ�����
					if (mem == rs.leftMem) {
						// ( X=Y :- p(X,Y) ) �ϰ�̣���ϥ��顼
						//$nerrors += 1;
						System.out.println("Compile error: head contains body unification");
					}
					else {
						// ( p(X,Y) :- X=Y ) ��UNIFY�ܥǥ�̿�����Ϥ���ΤǤ����Ǥϲ��⤷�ʤ�
					}
				} else {
					//link1.atom.args[link1.pos] = link2;
					//link2.atom.args[link2.pos] = link1;
					link1.buddy = link2;
					link2.buddy = link1;
					removedAtoms.add(atom);
				}
			}
		}
		it = removedAtoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			atom.mem.atoms.remove(atom);
		}
	}
	
	/** �إåɤ���ȥ��ȥ���Ф��ơ��������ֹ����Ͽ���� */
	private void genLHSPaths() {
		Env.c("RuleCompiler::genLHSMemPaths");
		lhsatompath = new HashMap();
		lhsmempath  = new HashMap();
		for (int i = 0; i < lhsfreemems.size(); i++) {
			lhsmempath.put(lhsfreemems.get(i), new Integer(i));
		}
		for (int i = 0; i < lhsatoms.size(); i++) {
			lhsatompath.put(lhsatoms.get(i), new Integer( lhsfreemems.size() + i ));
		}
		//Env.d("lhsmempaths"+lhsmempaths);
	}
	
	private void optimize() {
		Env.c("optimize");
		Optimizer.optimize(memMatch, body);
	}	
	/** ���դΥ��ȥ���°�줫�����롣*/
	private void removeLHSAtoms() {
		//Env.c("RuleCompiler::removeLHSAtoms");
		for (int i = 0; i < lhsatoms.size(); i++) {
			Atom atom = (Atom)lhsatoms.get(i);
			body.add( Instruction.removeatom(
				lhsatomToPath(atom), // �� lhsfreemems.size() + i �˰��פ���
				lhsmemToPath(atom.mem), atom.functor ));
		}
	}
	/** ���դΥ��ȥ��¹ԥ����å��������롣*/
	private void dequeueLHSAtoms() {
		for (int i = 0; i < lhsatoms.size(); i++) {
			Atom atom = (Atom)lhsatoms.get(i);
			if (!atom.functor.equals(Functor.INSIDE_PROXY)
			 && !atom.functor.equals(Functor.OUTSIDE_PROXY)) {
				body.add( Instruction.dequeueatom(
					lhsatomToPath(atom) // �� lhsfreemems.size() + i �˰��פ���
					));
			}
		}
	}
	/** ���դ�������¦����Ƶ�Ū�˽���롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int removeLHSMem(Membrane mem) {
		//Env.c("RuleCompiler::removeLHSMem");
		int procvarcount = mem.processContexts.size();
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int subcount = removeLHSMem(submem);
			body.add(new Instruction(Instruction.REMOVEMEM, lhsmemToPath(submem), lhsmemToPath(mem))); //��2�����ɲ� by mizuno
			if (subcount > 0) {
				body.add(new Instruction(Instruction.REMOVEPROXIES, lhsmemToPath(submem)));
			}
			procvarcount += subcount;
		}
		return procvarcount;
	}	

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int buildRHSMem(Membrane mem) {
		Env.c("RuleCompiler::buildRHSMem" + mem);
		int procvarcount = mem.processContexts.size();
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int submempath = varcount++;
			rhsmempath.put(submem, new Integer(submempath));
			body.add( Instruction.newmem(submempath, rhsmemToPath(mem)) );
			int subcount = buildRHSMem(submem);
			if (subcount > 0) {
				body.add(new Instruction(Instruction.INSERTPROXIES,
					rhsmemToPath(mem), rhsmemToPath(submem)));
			}
			procvarcount += subcount;
		}
		it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			if (pc.src.mem == null) {
				System.out.println("SYSTEM ERROR: ProcessContext.src.mem is not set");
			}
			if (rhsmemToPath(mem) != lhsmemToPath(pc.src.mem)) {
				body.add(new Instruction(Instruction.MOVECELLS,
					rhsmemToPath(mem), lhsmemToPath(pc.src.mem) ));
			}
		}
		return procvarcount;
	}
	
	/** ���դ�����Υ롼��ʸ̮�����Ƥ��������� */
	private void copyRules(Membrane mem) {
		Env.c("RuleCompiler::copyRules");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			copyRules((Membrane)it.next());
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();			
			if (rhsmemToPath(mem) == lhsmemToPath(rc.src.mem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.src.mem) ));
		}
	}
	/** ���դ�����Υ롼������Ƥ��������� */	
	private void loadRulesets(Membrane mem) {
		Env.c("RuleCompiler::loadRulesets");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			loadRulesets((Membrane)it.next());
		}
		it = mem.rulesets.iterator();
		while (it.hasNext()) {
//			if (!mem.rules.isEmpty()) {
				body.add(Instruction.loadruleset(rhsmemToPath(mem), (runtime.Ruleset)it.next()));
//			} 
		}
	}
	/** ���դ�����Υ��ȥ���������롣
	 * ñ�첽���ȥ�ʤ��UNIFY̿�����������
	 * ����ʳ��ʤ�б��դΥ��ȥ�Υꥹ��rhsatoms���ɲä��롣 */
	private void buildRHSAtoms(Membrane mem) {
		Env.c("RuleCompiler::buildRHSAtoms");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			buildRHSAtoms((Membrane)it.next());
		}
		it = mem.atoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();			
			if (atom.functor.equals(FUNC_UNIFY)) {
				LinkOccurrence link1 = atom.args[0].buddy;
				LinkOccurrence link2 = atom.args[1].buddy;
				body.add(new Instruction( Instruction.UNIFY,
					lhsatomToPath(link1.atom), link1.pos,
					lhsatomToPath(link2.atom), link2.pos));
			} else {
				int atomid = varcount++;
				rhsatompath.put(atom, new Integer(atomid));
				rhsatoms.add(atom);
//				// NEWATOM ����ʬ��Ф��Ƥ���
//				newatoms.add(atom);
				body.add( Instruction.newatom(atomid, rhsmemToPath(mem), atom.functor));
			}
		}
	}
	/** ��󥯤�ĥ���ؤ���������Ԥ� */
	private void updateLinks() {
		Env.c("RuleCompiler::updateLinks");
		Iterator it = rhsatoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();			
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence link = atom.args[pos].buddy;
				//Env.d(atom+"("+pos+")"+" buddy -> "+link.buddy.atom+" link.atom="+link.atom);
				if (link == null) {
					System.out.println("SYSTEM ERROR: buddy not set");
				}
				if (link.atom.mem == rs.leftMem) {
					body.add( new Instruction(Instruction.RELINK,
						rhsatomToPath(atom), pos,
						lhsatomToPath(link.atom), link.pos,
						rhsmemToPath(atom.mem) ));
				} else {
					if (rhsatomToPath(atom) < rhsatomToPath(link.atom)
					|| (rhsatomToPath(atom) == rhsatomToPath(link.atom) && pos < link.pos)) {
						body.add( new Instruction(Instruction.NEWLINK,
							rhsatomToPath(atom), pos,
							rhsatomToPath(link.atom), link.pos,
							rhsmemToPath(atom.mem) ));
					}
				}
			}
		}
	}
	/** ���դΥ��ȥ��¹ԥ����å����Ѥ� */
	private void enqueueRHSAtoms() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (!atom.functor.equals(Functor.INSIDE_PROXY)
			 && !atom.functor.equals(Functor.OUTSIDE_PROXY) ) {
				body.add( new Instruction(Instruction.ENQUEUEATOM, rhsatomToPath(atom)));
			 }
		}
	}
	/** ����饤��̿���¹Ԥ��� */
	private void addInline() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			int atomID = rhsatomToPath(atom);
			int codeID = Inline.getCodeID(atom.functor.getInternalName());
			if(codeID == -1) continue;
			body.add( new Instruction(Instruction.INLINE, atomID, codeID));
		}
	}
	/** �⥸�塼����ɤ߹��� */
	private void addLoadModules() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			if(atom.functor.getInternalName().equals("use") && atom.functor.getArity()==1) {
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem),
				atom.args[0].buddy.atom.functor.getInternalName()) );
			}
			if(atom.functor.path!=null && !atom.functor.path.equals(atom.mem.name)) {
				// ���λ����Ǥϲ��Ǥ��ʤ��⥸�塼�뤬����Τ�̾���ˤ��Ƥ���
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem),
					 atom.functor.path));
			}
		}
	}
	/** ���դ�����Ѵ����� */
	private void freeLHSMem(Membrane mem) {
		Env.c("RuleCompiler::freeLHSMem");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			freeLHSMem(submem);
			// �����Ѥ��줿���free���ƤϤ����ʤ�
			body.add(new Instruction(Instruction.FREEMEM, lhsmemToPath(submem)));
		}
	}
	/** ���դΥ��ȥ���Ѵ����� */
	private void freeLHSAtoms() {
		for (int i = 0; i < lhsatoms.size(); i++) {
			body.add( new Instruction(Instruction.FREEATOM, lhsfreemems.size() + i ));
		}
	}

	/**
	 * �ǥХå���ɽ��
	 */
	private void showInstructions() {
		Iterator it;
		it = atomMatch.listIterator();
		Env.d("--atomMatches :");
		while(it.hasNext()) Env.d((Instruction)it.next());
		
		it = memMatch.listIterator();
		Env.d("--memMatch :");
		while(it.hasNext()) Env.d((Instruction)it.next());
		
		it = body.listIterator();
		Env.d("--body :");
		while(it.hasNext()) Env.d((Instruction)it.next());
	}
}

