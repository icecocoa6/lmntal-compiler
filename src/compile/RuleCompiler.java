package compile;

import java.util.*;
import runtime.Env;
import runtime.Rule;
//import runtime.InterpretedRuleset;
import runtime.Instruction;
import runtime.Functor;
import runtime.Inline;
import compile.structure.*;

// TODO �ڳ�ǧ�ۺ�����μ�ͳ��󥯤��б����뼫ͳ��󥯴������ȥ����������ʤ��褦�ˤʤäƤ��뤫�ɤ���

/**
 * <pre>
 * ����ѥ�����ǡ�����¤����ˤ˥롼�륻�åȤ��ղä��롣
 * 
 * ����Ū�ˤϡ�Membrane -> RuleSet ���ղä��줿 Membrane
 * 
 * ��������ϡ�{ ( :- WORLD ) } �η����ǸƤФ�뤳�Ȥˤʤ롣
 * WORLD �ˤϥ롼�뤬�ޤޤ����⤢��Τǡ�
 * ���Ĥ��ä��롼�뤫��롼�륪�֥������Ȥ��������Ƥ�����Υ롼�륻�åȤ��ɲä���
 * �Ȥ�����Ȥ�Ƶ�Ū�ˤ�뤳�Ȥˤʤ롣
 * 
 * </pre>
 * 
 * - �롼����롼��ν�����ˡ�β����ƤˤĤ��Ƥϲ���todo�򻲾ȤΤ���
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
		Env.n("RuleCompiler");
		Env.p(rs);
		this.rs = rs;
	}
	
	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤷��
	 * ��°��Υ롼�륻�åȤ��ɲä��롣
	 */
	public void compile() {
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
		
		// TODO �ʲ���3�Ԥϡ�RulesetCompiler���Ԥ��٤��Ǥ��롣�ޤ����������������Թ�塢�롼����롼�����������˥���ѥ��뤹�٤��Ǥ��롣
		// compiler.structure.Membrane::ruleset�λ��Ȥ�¦�롼��Υ���ѥ���ǻ��Ѥ���ΤϹ���ʤ���
		
		((runtime.InterpretedRuleset)rs.parent.ruleset).rules.add(theRule);

		// �롼��α�����ʲ��˥롼�뤬���뤫�⤷��ʤ��ΤǺƵ�Ū������
		RuleSetGenerator.processMembrane(rs.leftMem); // ������դ�
		RuleSetGenerator.processMembrane(rs.rightMem);
	}
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_l() {
		Env.c("compile_l");
		
		atomMatch = new ArrayList();
		for (int firstid = 0; firstid <= hc.atoms.size(); firstid++) {
			hc.prepare(); // �ѿ��ֹ������			
			if (firstid < hc.atoms.size()) {			
				if (true) continue; // �׻�
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
			hc.match.add( Instruction.react(theRule, hc.getMemActuals(), hc.getAtomActuals()) );
		}
	}
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_r() {
		Env.c("compile_r");
		
		// �إåɤμ�����
		lhsatoms = hc.atoms;
		lhsfreemems = hc.freemems;
		genLHSMemPaths();
		varcount = lhsatoms.size() + lhsfreemems.size();
		int formals = varcount;
		
		//
		body = new ArrayList();
		rhsatoms    = new ArrayList();
		rhsatompath = new HashMap();
		rhsmempath  = new HashMap();
		int toplevelmemid = lhsmemToPath(rs.leftMem);
		rhsmempath.put(rs.rightMem, new Integer(toplevelmemid));
		
		//Env.p("rs.leftMem -> "+rs.leftMem);
		//Env.p("lhsmempaths.get(rs.leftMem) -> "+lhsmempaths.get(rs.leftMem));
		//Env.p("rhsmempaths -> "+rhsmempaths);
		
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
		addInline();
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
					link1.atom.args[link1.pos] = link2;
					link2.atom.args[link2.pos] = link1;
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
	private void genLHSMemPaths() {
		Env.c("RuleCompiler::genLHSMemPaths");
		lhsatompath = new HashMap();
		lhsmempath  = new HashMap();
		for (int i = 0; i < lhsfreemems.size(); i++) {
			lhsmempath.put(lhsfreemems.get(i), new Integer(i));
		}
		for (int i = 0; i < lhsatoms.size(); i++) {
			lhsatompath.put(lhsatoms.get(i), new Integer( lhsfreemems.size() + i ));
		}
		//Env.p("lhsmempaths"+lhsmempaths);
	}
	
	private void optimize() {
		Env.c("optimize");
	}	
	private void removeLHSAtoms() {
		Env.c("RuleCompiler::removeLHSAtoms");
		for (int i = 0; i < lhsatoms.size(); i++) {
			body.add( new Instruction(Instruction.REMOVEATOM, i+1) );
		}
	}
	/** ���դ�������¦����Ƶ�Ū�˽���롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int removeLHSMem(Membrane mem) {
		Env.c("RuleCompiler::removeLHSMem");
		int procvarcount = mem.processContexts.size();
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int subcount = removeLHSMem(submem);
			body.add(new Instruction(Instruction.REMOVEMEM, lhsmemToPath(submem)));
			if (subcount > 0) {
				body.add(new Instruction(Instruction.REMOVEPROXIES, lhsmemToPath(submem)));
			}
			procvarcount += subcount;
		}
		return procvarcount;
	}	

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return */
	private int buildRHSMem(Membrane mem) {
		Env.p("RuleCompiler::buildRHSMem" + mem);
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
			if (rhsmemToPath(mem) != lhsmemToPath(pc.lhsMem)) {
				body.add(new Instruction(Instruction.MOVECELLS,
					rhsmemToPath(mem), lhsmemToPath(pc.lhsMem) ));
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
			if (rhsmemToPath(mem) == lhsmemToPath(rc.lhsMem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.lhsMem) ));
		}
	}
	/** ���դ�����Υ롼������Ƥ��������� */	
	private void loadRulesets(Membrane mem) {
		Env.c("RuleCompiler::loadRulesets");
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			loadRulesets((Membrane)it.next());
		}
		if (!mem.rules.isEmpty()) {
			body.add(Instruction.loadruleset(rhsmemToPath(mem), mem.ruleset));
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
					lhsatomToPath(link2.atom), link2.pos ));
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
				//Env.p(atom+"("+pos+")"+" buddy -> "+link.buddy.atom+" link.atom="+link.atom);
				if (link.atom.mem == rs.leftMem) {
					body.add( new Instruction(Instruction.RELINK,
						rhsatomToPath(atom), pos,
						lhsatomToPath(link.atom), link.pos));
				} else {
					if (rhsatomToPath(atom) < rhsatomToPath(link.atom)
					|| (rhsatomToPath(atom) == rhsatomToPath(link.atom) && pos < link.pos)) {
						body.add( new Instruction(Instruction.NEWLINK,
							rhsatomToPath(atom), pos,
							rhsatomToPath(link.atom), link.pos));
					}
				}
			}
		}
	}
	/**
	 * ����饤��̿����������롣
	 */
	private void addInline() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			int atomID = rhsatomToPath(atom);
			int codeID = Inline.getCodeID(atom.functor.getName());
			if(codeID==-1) continue;
			body.add( new Instruction(Instruction.INLINE, atomID, codeID));
		}
	}
	/** ���դ�������� */
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
	private void freeLHSAtoms() {
		for (int i = 0; i < lhsatoms.size(); i++) {
			body.add( new Instruction(Instruction.FREEATOM, i+1) );
		}
	}

	/**
	 * �ǥХå���ɽ��
	 */
	private void showInstructions() {
		Iterator it;
		it = atomMatch.listIterator();
		Env.p("--atomMatches :");
		while(it.hasNext()) Env.p((Instruction)it.next());
		
		it = memMatch.listIterator();
		Env.p("--memMatch :");
		while(it.hasNext()) Env.p((Instruction)it.next());
		
		it = body.listIterator();
		Env.p("--body :");
		while(it.hasNext()) Env.p((Instruction)it.next());
	}
}

