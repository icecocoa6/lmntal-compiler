package compile;

import java.util.*;
import runtime.Env;
import runtime.Rule;
//import runtime.InterpretedRuleset;
import runtime.InlineUnit;
import runtime.Instruction;
import runtime.InstructionList;
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
	public List guard;
	public List body;
	int varcount;			// �����ѿ��ֹ�
	
	List rhsatoms;
	Map  rhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	Map  rhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)	
	Map  rhslinkpath;		// ���դΥ�󥯽и�(LinkOccurence) -> �ѿ��ֹ�(Integer)
	//List rhslinks;		// ���դΥ�󥯽и�(LinkOccurence)�Υꥹ�ȡ������Τߡ� -> computeRHSLinks���֤��ˤ���
	List lhsatoms;
	List lhsmems;
	Map  lhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	Map  lhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)
	Map  lhslinkpath = new HashMap();		// ���դΥ��ȥ�Υ�󥯽и� (LinkOccurrence) -> �ѿ��ֹ�(Integer)
		// �㺸�դΥ��ȥ���ѿ��ֹ� (Integer) -> ��󥯤��ѿ��ֹ������ (int[])���䤫���ѹ�
	
	HeadCompiler hc;
	
	final int lhsmemToPath(Membrane mem) { return ((Integer)lhsmempath.get(mem)).intValue(); }
	final int rhsmemToPath(Membrane mem) { return ((Integer)rhsmempath.get(mem)).intValue(); }
	final int lhsatomToPath(Atomic atom) { return ((Integer)lhsatompath.get(atom)).intValue(); } 
	final int rhsatomToPath(Atomic atom) { return ((Integer)rhsatompath.get(atom)).intValue(); } 
	final int lhslinkToPath(Atomic atom, int pos) {
		return lhslinkToPath(atom.args[pos]);
	}
	final int lhslinkToPath(LinkOccurrence link) {
		return ((Integer)lhslinkpath.get(link)).intValue();
	}
	
	public String unitName;
	/** �إåɤΥޥå��󥰽�λ��η�³̿����Υ�٥� */
	private InstructionList contLabel;

	/**
	 * ���ꤵ�줿 RuleStructure �ѤΥ롼���Ĥ���
	 */
	RuleCompiler(RuleStructure rs) {
		this(rs, InlineUnit.DEFAULT_UNITNAME);
	}
	RuleCompiler(RuleStructure rs, String unitName) {
		//Env.n("RuleCompiler");
		//Env.d(rs);
		this.unitName = unitName;
		this.rs = rs;
	}
	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
	 */
	public Rule compile() throws CompileException {
		Env.c("compile");
		liftupActiveAtoms(rs.leftMem);
		simplify();
		theRule = new Rule(rs.toString());
		
		hc = new HeadCompiler();//rs.leftMem;
		hc.enumFormals(rs.leftMem);	// ���դ��Ф��벾�����ꥹ�Ȥ���
		
		//�Ȥꤢ������˥����ɥ���ѥ����Ƥֻ��ˤ��Ƥ��ޤ� by mizuno
//		if (!rs.typedProcessContexts.isEmpty() || !rs.guardNegatives.isEmpty()) {
		if (true) {
			theRule.guardLabel = new InstructionList();
			guard = theRule.guardLabel.insts;
		}
		else guard = null;
		theRule.bodyLabel = new InstructionList();
		body = theRule.bodyLabel.insts;
		contLabel = (guard != null ? theRule.guardLabel : theRule.bodyLabel);		
		
		compile_l();
		compile_g();
		compile_r();

		theRule.memMatch  = memMatch;
		theRule.atomMatch = atomMatch;
		theRule.guard     = guard;
		theRule.body      = body;
		optimize();
		return theRule;
	}
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_l() {
		Env.c("compile_l");
		
		theRule.atomMatchLabel = new InstructionList();
		atomMatch = theRule.atomMatchLabel.insts;
		
		int maxvarcount = 2;	// ���ȥ��Ƴ�ѡʲ���
		for (int firstid = 0; firstid <= hc.atoms.size(); firstid++) {
			hc.prepare(); // �ѿ��ֹ������			
			if (firstid < hc.atoms.size()) {			
				if (Env.shuffle >= Env.SHUFFLE_DONTUSEATOMSTACKS) continue;
				// Env.SHUFFLE_DEFAULT �ʤ�С��롼���ȿ����Ψ��ͥ�褹�뤿�ᥢ�ȥ��Ƴ�ƥ��ȤϹԤ�ʤ�
				
				Atom atom = (Atom)hc.atoms.get(firstid);
				if (!atom.functor.isActive()) continue;
				
				// ���ȥ��Ƴ
				InstructionList tmplabel = new InstructionList();
				tmplabel.insts = hc.match;
				atomMatch.add(new Instruction(Instruction.BRANCH, tmplabel));
				
				hc.mempaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
				hc.atompaths.put(atom, new Integer(1));		// ��Ƴ���륢�ȥ���ѿ��ֹ�� 1
				hc.varcount = 2;
				hc.match.add(new Instruction(Instruction.FUNC, 1, atom.functor));
				Membrane mem = atom.mem;
				if (mem == rs.leftMem) {
					hc.match.add(new Instruction(Instruction.TESTMEM, 0, 1));
				}
				else {
					hc.match.add(new Instruction(Instruction.GETMEM, hc.varcount, 1));
					hc.match.add(new Instruction(Instruction.LOCK,   hc.varcount));
					hc.mempaths.put(mem, new Integer(hc.varcount++));
					mem = mem.parent;
					while (mem != rs.leftMem) {
						hc.match.add(new Instruction(Instruction.GETPARENT,hc.varcount,hc.varcount-1));
						hc.match.add(new Instruction(Instruction.LOCK,     hc.varcount));
						hc.mempaths.put(mem, new Integer(hc.varcount++));
						mem = mem.parent;
					}
					hc.match.add(new Instruction(Instruction.GETPARENT,hc.varcount,hc.varcount-1));
					hc.match.add(new Instruction(Instruction.EQMEM, 0, hc.varcount++));
				}
				hc.getLinks(1, atom.functor.getArity()); //��󥯤ΰ�����(RISC��) by mizuno
				Atom firstatom = (Atom)hc.atoms.get(firstid);
				hc.compileLinkedGroup(firstatom);
				hc.compileMembrane(firstatom.mem);
			} else {
				// ���Ƴ
				theRule.memMatchLabel = hc.matchLabel;
				memMatch = hc.match;
				hc.mempaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
			}
			hc.compileMembrane(rs.leftMem);
			// ��ͳ�и������ǡ������ȥब�ʤ�����������
			if (!hc.fFindDataAtoms) {
				if (Env.debug >= 1) {
					Iterator it = hc.atoms.iterator();
					while (it.hasNext()) {
						Atom atom = (Atom)it.next();
						if (!hc.isAtomLoaded(atom)) {
							Env.warning("TYPE WARNING: Rule head contains free data atom: " + atom);
						}
					}
				}
				hc.switchToUntypedCompilation();
				hc.compileMembrane(rs.leftMem);
			}
			hc.checkFreeLinkCount(rs.leftMem); // ��������ѹ��ˤ��ƤФʤ��Ƥ褯�ʤä�����Ϥ�Ƥ�ɬ�פ���
			if (hc.match == memMatch) {
				hc.match.add(0, Instruction.spec(1, hc.varcount));
			}
			else {
				hc.match.add(0, Instruction.spec(2, hc.varcount));
			}
			// jump̿�ᷲ������
			List memActuals  = hc.getMemActuals();
			List atomActuals = hc.getAtomActuals();
			List varActuals  = hc.getVarActuals();
			// - ������#1
			hc.match.add( Instruction.jump(contLabel, memActuals, atomActuals, varActuals) );
			// - ������#2
//			hc.match.add( Instruction.inlinereact(theRule, memActuals, atomActuals, varActuals) );
//			int formals = memActuals.size() + atomActuals.size() + varActuals.size();
//			hc.match.add( Instruction.spec(formals, formals) );
//			hc.match.add( hc.getResetVarsInstruction() );
//			List brancharg = new ArrayList();
//			brancharg.add(body);
//			hc.match.add( new Instruction(Instruction.BRANCH, brancharg) );
			
			// jump̿�ᷲ�����������
			if (maxvarcount < hc.varcount) maxvarcount = hc.varcount;
		}
		atomMatch.add(0, Instruction.spec(2,maxvarcount));
	}
	
	// todo spec̿���¦�˻����夲���Ŭ������������
	
	/**
	 * ���դΥ��ȥ�Υ�󥯤��Ф���getlink��Ԥ����ѿ��ֹ����Ͽ���롣(RISC��)
	 * ����Ū�ˤϥ�󥯥��֥������Ȥ�ܥǥ�̿����ΰ������Ϥ��褦�ˤ��뤫�⤷��ʤ���
	 */
//	private void getLHSLinks() {
//		lhslinkpath = new HashMap();
//		for (int i = 0; i < lhsatoms.size(); i++) {
//			Atom atom = (Atom)lhsatoms.get(i);
//			int atompath = lhsatomToPath(atom);
//			int arity = atom.functor.getArity();
//			for (int j = 0; j < arity; j++) {
//				int linkpath;
//				// ����褬ground�ξ�硢����GETLINK��ȯ�Ԥ���Ƥ���(getGroundLinkPaths)
//				if(!(atom.args[j].buddy.atom instanceof Context &&
//					groundsrcs.containsKey(((Context)atom.args[j].buddy.atom).def))){
//					linkpath = varcount++;
//					body.add(new Instruction(Instruction.GETLINK, linkpath, atompath, j));
//				}
//				else{
//					linkpath = ((Integer)groundsrcs.get(((Context)atom.args[j].buddy.atom).def)).intValue();
//				}
//				lhslinkpath.put(atom.args[j],new Integer(linkpath));
//			}
//		}
//	}

	private List computeRHSLinks() {
		List rhslinks = new ArrayList();
		rhslinkpath = new HashMap();
		int rhslinkindex = 0;
		// ���ȥ�ΰ����Υ�󥯽и�
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()){
			Atom atom = (Atom)it.next();
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				body.add(new Instruction(Instruction.ALLOCLINK,varcount,rhsatomToPath(atom),pos));
				rhslinkpath.put(atom.args[pos],new Integer(varcount));
				if(!rhslinks.contains(atom.args[pos].buddy) &&
				!(atom.functor.equals(Functor.INSIDE_PROXY) && pos == 0))
					rhslinks.add(rhslinkindex++,atom.args[pos]);
				varcount++;
			}
		}

		// unary���եץ���ʸ̮�Υ�󥯽и�
		it = rhstypedcxtpaths.keySet().iterator();
		while(it.hasNext()){
			ProcessContext atom = (ProcessContext)it.next();
			body.add(new Instruction(Instruction.ALLOCLINK,varcount,rhstypedcxtToPath(atom),0));
			rhslinkpath.put(atom.args[0],new Integer(varcount));
			if(!rhslinks.contains(atom.args[0].buddy))rhslinks.add(rhslinkindex++,atom.args[0]);
			varcount++;
		}
		
		// ground���եץ���ʸ̮�Υ�󥯽и�
		it = rhsgroundpaths.keySet().iterator();
		while(it.hasNext()){
			ProcessContext atom = (ProcessContext)it.next();
			int linkpath = rhsgroundToPath(atom);
			rhslinkpath.put(atom.args[0],new Integer(linkpath));
			if(!rhslinks.contains(atom.args[0].buddy))rhslinks.add(rhslinkindex++,atom.args[0]);
		}

		// ���ʤ�
		it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext()) {
				ProcessContext atom = (ProcessContext)it2.next();
				for (int pos = 0; pos < atom.getArity(); pos++) {
//					LinkOccurrence srclink = atom.def.lhsOcc.args[pos].buddy;
//					int srclinkid;
//					if(!lhslinkpath.containsKey(srclink)){
//						srclinkid = varcount++;
//						body.add( new Instruction(Instruction.GETLINK,srclinkid, 
//													lhsatomToPath(srclink.atom), srclink.pos));
//						lhslinkpath.put(srclink,new Integer(srclinkid));
//					}
//					srclinkid = lhslinkToPath(srclink);
//					if (!(fUseMoveCells && atom.def.rhsOccs.size() == 1)) {							
//						int copiedlink = varcount++;
//						body.add( new Instruction(Instruction.LOOKUPLINK,
//										copiedlink, rhspcToMapPath(atom), srclinkid));
//						srclinkid = copiedlink;
//					}
//					rhslinkpath.put(atom.args[pos],new Integer(srclinkid));
					if(!rhslinks.contains(atom.args[pos].buddy))rhslinks.add(rhslinkindex++,atom.args[pos]);
				}
			}
		}
		return rhslinks;
	}
	
	private int getLinkPath(LinkOccurrence link){
		if(rhslinkpath.containsKey(link)){
			return ((Integer)rhslinkpath.get(link)).intValue();
		}
		else if (link.atom instanceof ProcessContext && !((ProcessContext)link.atom).def.typed){
			LinkOccurrence srclink = ((ProcessContext)link.atom).def.lhsOcc.args[link.pos].buddy;
			int linkpath = varcount++;
			body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(srclink.atom),srclink.pos));
			if(!(fUseMoveCells && ((ProcessContext)link.atom).def.rhsOccs.size() == 1)) {
				int copiedlink = varcount++;
				body.add( new Instruction(Instruction.LOOKUPLINK,
								copiedlink, rhspcToMapPath(((ProcessContext)link.atom)), linkpath));
				return copiedlink;
			}
			return linkpath;
		}
		else{
			if(!lhslinkpath.containsKey(link)){
				int linkpath = varcount++;
				body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(link.atom),link.pos));
				lhslinkpath.put(link,new Integer(linkpath));
			}
			return lhslinkToPath(link);
		}
	}

	/** ������򥳥�ѥ��뤹�� */
	private void compile_r() throws CompileException {
		Env.c("compile_r");
		int formals = varcount;
		body.add( Instruction.commit(theRule) );
		inc_guard();
			
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
		removeLHSTypedProcesses();
		if (removeLHSMem(rs.leftMem) >= 2) {
			body.add(new Instruction(Instruction.REMOVETOPLEVELPROXIES, toplevelmemid));
		}
		recursiveLockLHSNonlinearProcessContextMems();
		insertconnectors();
		buildRHSMem(rs.rightMem);
		if (!rs.rightMem.processContexts.isEmpty()) {
			body.add(new Instruction(Instruction.REMOVETEMPORARYPROXIES, toplevelmemid));
		}
		copyRules(rs.rightMem);
		loadRulesets(rs.rightMem);		
		buildRHSTypedProcesses();
		buildRHSAtoms(rs.rightMem);
		// ������varcount�κǽ��ͤ����ꤹ�뤳�ȤˤʤäƤ��롣�ѹ�����Ŭ�ڤ˲��˰�ư���뤳�ȡ�
		//getLHSLinks();
		updateLinks();
		deleteconnectors();
		enqueueRHSAtoms();
		addInline();
		addRegAndLoadModules();
		freeLHSSingletonProcessContexts();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms();
		freeLHSTypedProcesses();
//		freeLHSSingletonProcessContexts(); // freemem����Ԥ����뤿��3�Ծ�˰�ư���� n-kato 2005.1.13
		recursiveUnlockLHSNonlinearProcessContextMems();
		unlockReusedOrNewRootMem(rs.rightMem);
		//
		body.add(0, Instruction.spec(formals, varcount));
		
//		if (rs.rightMem.mems.isEmpty() && rs.rightMem.ruleContexts.isEmpty()
//		 && rs.rightMem.processContexts.isEmpty() && rs.rightMem.rulesets.isEmpty()) {
//			body.add(new Instruction(Instruction.CONTINUE));
//		} else 
		body.add(new Instruction(Instruction.PROCEED));
	}
		
	////////////////////////////////////////////////////////////////
	//
	// �����ɴط�
	//

	/** �إåɤ���ȥ��ȥ���Ф��ơ��������ֹ����Ͽ���� */
	private void genLHSPaths() {
		lhsatompath = new HashMap();
		lhsmempath  = new HashMap();
		varcount = 0;
		for (int i = 0; i < lhsmems.size(); i++) {
			lhsmempath.put(lhsmems.get(i), new Integer(varcount++));
		}
		for (int i = 0; i < lhsatoms.size(); i++) {
			lhsatompath.put(lhsatoms.get(i), new Integer(varcount++));
		}
	}
	
	private void inc_guard() {
		// �����ɤμ�����
		varcount = lhsatoms.size() + lhsmems.size();
		genTypedProcessContextPaths();
		// typedcxtdefs = gc.typedcxtdefs;
		// varcount = lhsatoms.size() + lhsmems.size() + rs.typedProcessContexts.size();
		//getLHSLinks();
		getGroundLinkPaths();
	}

//	private void inc_head(HeadCompiler hc) {
//		// �إåɤμ�����
//		lhsatoms = hc.atoms;
//		lhsmems  = hc.mems;
//		genLHSPaths();
//		varcount = lhsatoms.size() + lhsmems.size();
//	}

	/** �����ɤ򥳥�ѥ��뤹�� */
	private void compile_g() throws CompileException {
		lhsmems  = hc.mems;
		lhsatoms = hc.atoms;
		genLHSPaths();
		gc = new GuardCompiler(this, hc);
		if (guard == null) return;
		int formals = gc.varcount;
		gc.getLHSLinks();
		gc.fixTypedProcesses();
		gc.checkMembraneStatus();
		varcount = gc.varcount;
		compileNegatives();
		guard.add( 0, Instruction.spec(formals,varcount) );
		guard.add( Instruction.jump(theRule.bodyLabel, gc.getMemActuals(),
			gc.getAtomActuals(), gc.getVarActuals()) );
		//RISC���ǡ�������֤Ȥ��ƥ����ɤ�getlink����ʪ��ܥǥ����Ϥ��ʤ����ˤ����Τǡ�
		//������̿����ζɽ��ѿ��ο��ȥܥǥ�̿����ΰ����ο������פ��ʤ��ʤä���by mizuno
		varcount = gc.getMemActuals().size() + gc.getAtomActuals().size() 
					+ gc.getVarActuals().size();
	}
	void compileNegatives() throws CompileException{
		Iterator it = rs.guardNegatives.iterator();
		while (it.hasNext()) {
			LinkedList eqs = (LinkedList)it.next();
			HeadCompiler negcmp = hc.getNormalizedHeadCompiler();
			negcmp.varcount = varcount;
			negcmp.compileNegativeCondition(eqs);
			guard.add(new Instruction(Instruction.NOT, negcmp.matchLabel));
			if (varcount < negcmp.varcount)  varcount = negcmp.varcount;
		}
	}
	
	// ���դ��ץ���ʸ̮�ط�
	
	GuardCompiler gc;
	/** ���դ��ץ���ʸ̮�α��դǤνи� (Context) -> �ѿ��ֹ� */
	HashMap rhstypedcxtpaths = new HashMap();
	/** ground���դ��ץ���ʸ̮�α��դǤνи�(Context) -> (Link��ؤ�)�ѿ��ֹ� */
	HashMap rhsgroundpaths = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �������и��ʥ��ԡ����Ȥ���и��ˤ��ѿ��ֹ��Body�¹Ի��� */
	HashMap typedcxtsrcs  = new HashMap();
	/** ground���դ��ץ���ʸ̮���(ContextDef) -> �������и��ʥ��ԡ����Ȥ���и��ˤ��ѿ��ֹ��Body�¹Ի��� */
	HashMap groundsrcs = new HashMap();
	/** Body�¹Ի��ʤΤǡ�UNBOUND�ˤϤʤ�ʤ� */
	int typedcxtToSrcPath(ContextDef def) {
		return ((Integer)typedcxtsrcs.get(def)).intValue();
	}
	/** Body�¹Ի��ʤΤǡ�UNBOUND�ˤϤʤ�ʤ� */
	int groundToSrcPath(ContextDef def) {
		return ((Integer)groundsrcs.get(def)).intValue();
	}
	/***/
	int rhstypedcxtToPath(Context cxt) {
		return ((Integer)rhstypedcxtpaths.get(cxt)).intValue();
	}
	/***/
	int rhsgroundToPath(Context cxt) {
		return ((Integer)rhsgroundpaths.get(cxt)).intValue();
	}

	private void genTypedProcessContextPaths() {
		Iterator it = gc.typedcxtdefs.iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (gc.typedcxttypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
				typedcxtsrcs.put( def, new Integer(varcount++) );
			}
		}
	}
	
	// ground���դ��ץ���ʸ̮����ˤĤ��ơ��������Ȥʤ��󥯤��������
	private void getGroundLinkPaths() {
		Iterator it = gc.groundsrcs.keySet().iterator();
		while(it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if(gc.typedcxttypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
				int linkpath = varcount++;
				body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(def.lhsOcc.args[0].buddy.atom),def.lhsOcc.args[0].buddy.pos));
				groundsrcs.put(def,new Integer(linkpath));
			}
		}
	}
//	public void enumTypedContextDefs() {
//		Iterator it = rs.typedProcessContexts.values().iterator();
//		while (it.hasNext()) {
//			ContextDef def = (ContextDef)it.next();
//			typedcxtdefs.add(def);
//		}
//	}
	private void removeLHSTypedProcesses() {
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Context pc = def.lhsOcc;
			if (pc != null) { // �إåɤΤȤ��Τ߽����
				if (gc.typedcxttypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
					body.add(new Instruction( Instruction.REMOVEATOM,
						typedcxtToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
				else if (gc.typedcxttypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
					body.add(new Instruction( Instruction.REMOVEGROUND,
						groundToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
			}
		}
	}	
	private void freeLHSTypedProcesses() {
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (gc.typedcxttypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
				body.add(new Instruction( Instruction.FREEATOM,
					typedcxtToSrcPath(def) ));
			}
			else if (gc.typedcxttypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
				body.add(new Instruction( Instruction.FREEGROUND,groundToSrcPath(def)));
			}
		}
	}	

	//kudo
	private void freeLHSSingletonProcessContexts(){
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() != 1) { // �������ΤȤ�1�Ĥ��������Ѥ���褦�ˤ����� size == 0 ��ľ����
				body.add(new Instruction( Instruction.DROPMEM,
					lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	//kudo
	private void recursiveLockLHSNonlinearProcessContextMems(){
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() != 1) {
				body.add(new Instruction( Instruction.RECURSIVELOCK,
					lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	//kudo
	private void recursiveUnlockLHSNonlinearProcessContextMems(){
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() != 1) {
				if (false) { // �����Ѥ����Ȥ��Τ� recursiveunlock ����
					body.add(new Instruction( Instruction.RECURSIVEUNLOCK,
						lhsmemToPath(def.lhsOcc.mem) ));
				}
			}
		}
	}


	private void buildRHSTypedProcesses() {
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext()) {
				ProcessContext pc = (ProcessContext)it2.next();
				if (gc.typedcxttypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
					int atompath = varcount++;
					body.add(new Instruction( Instruction.COPYATOM, atompath,
						rhsmemToPath(pc.mem),
						typedcxtToSrcPath(pc.def) ));
					rhstypedcxtpaths.put(pc, new Integer(atompath));
				}
				else if(gc.typedcxttypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
					int groundpath = varcount++;
					body.add(new Instruction( Instruction.COPYGROUND, groundpath,
						groundToSrcPath(pc.def), // ground�ξ��ϥ�󥯤�ؤ��ѿ��ֹ�
						rhsmemToPath(pc.mem) ));
					rhsgroundpaths.put(pc, new Integer(groundpath));
				}
			}
		}
	}	

	////////////////////////////////////////////////////////////////

	/** �쳬�ز��ˤ��륢���ƥ��֥��ȥ����������Ƭ�����˥��饤�ɰ�ư���롣*/
	private static void liftupActiveAtoms(Membrane mem) {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			liftupActiveAtoms((Membrane)it.next());
		}
		LinkedList atomlist = new LinkedList();
		it = mem.atoms.iterator();
		while (it.hasNext()) {
			atomlist.add(it.next());
		}
		mem.atoms.clear();
		it = atomlist.iterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			if (a.functor.isActive()) {
				mem.atoms.add(a);
				it.remove();
			}
		}
		mem.atoms.addAll(atomlist);	
	}
	/** �롼��κ��դȱ��դ��Ф���staticUnify��Ƥ� */
	public void simplify() throws CompileException {
		staticUnify(rs.leftMem);
		staticUnify(rs.rightMem);
		if (rs.leftMem.atoms.isEmpty() && rs.leftMem.mems.isEmpty() && !rs.fSuppressEmptyHeadWarning) {
			Env.warning("WARNING: rule with empty head: " + rs);
		}
		// ����¾ unary =/== ground �ν��֤��¤��ؤ���
		List typeConstraints = rs.guardMem.atoms;
		LinkedList lists[] = {new LinkedList(),new LinkedList(),new LinkedList()};
		Iterator it = typeConstraints.iterator();
		while (it.hasNext()) {
			Atom cstr = (Atom)it.next();
			Functor func = cstr.functor;
			if (func.equals(new Functor("unary",1)))  { lists[0].add(cstr); it.remove(); }
			if (func.equals(new Functor("=",2)))      { lists[1].add(cstr); it.remove(); }
			if (func.equals(new Functor("==",2)))     { lists[1].add(cstr); it.remove(); }
			if (func.equals(new Functor("ground",1))) { lists[2].add(cstr); it.remove(); }
		}
		typeConstraints.addAll(lists[0]);
		typeConstraints.addAll(lists[1]);
		typeConstraints.addAll(lists[2]);

	}
	
	/** ���ꤵ�줿��Ȥ��λ�¹��¸�ߤ����Ĺ�� =��todo ����Ӽ�ͳ��󥯴������ȥ�ˤ����� */
	public void staticUnify(Membrane mem) throws CompileException {
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
							// // <strike> ( X=Y :- p(X,Y) ) �ϰ�̣���ϥ��顼
							// //��=���̾�Υإåɥ��ȥ�ȸ��ʤ������֤�����</strike>
							// error("COMPILE ERROR: head contains body unification");
						// ( X=Y :- p(X,Y) ) �� ( :- p(X,X) ) �ˤʤ�
					}
					else {
						// ( p(X,Y) :- X=Y ) ��UNIFY�ܥǥ�̿�����Ϥ���ΤǤ����Ǥϲ��⤷�ʤ�
						continue;
					}
				}
				link1.buddy = link2;
				link2.buddy = link1;
				link2.name = link1.name;
				removedAtoms.add(atom);
			}
		}
		it = removedAtoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			atom.mem.atoms.remove(atom);
		}
	}
	
	
	private void optimize() {
		Env.c("optimize");
//		Optimizer.optimize(memMatch, body);
		Optimizer.optimizeRule(theRule);
	}

	////////////////////////////////////////////////////////////////
	//
	// �ܥǥ��¹�
	//
	
	/** ���դΥ��ȥ���°�줫�����롣*/
	private void removeLHSAtoms() {
		//Env.c("RuleCompiler::removeLHSAtoms");
		for (int i = 0; i < lhsatoms.size(); i++) {
			Atom atom = (Atom)lhsatoms.get(i);
			body.add( Instruction.removeatom(
				lhsatomToPath(atom), // �� lhsmems.size() + i �˰��פ���
				lhsmemToPath(atom.mem), atom.functor ));
		}
	}
	/** ���դΥ��ȥ��¹ԥ��ȥॹ���å��������롣*/
	private void dequeueLHSAtoms() {
		for (int i = 0; i < lhsatoms.size(); i++) {
			Atom atom = (Atom)lhsatoms.get(i);
			if (!atom.functor.equals(Functor.INSIDE_PROXY)
			 && !atom.functor.equals(Functor.OUTSIDE_PROXY)
			 && atom.functor.isSymbol() ) {
				body.add( Instruction.dequeueatom(
					lhsatomToPath(atom) // �� lhsmems.size() + i �˰��פ���
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

	//
	
	HashMap rhsmappaths = new HashMap();	// ���դ�������$p�и�(ProcessContext) -> map���ѿ��ֹ�(Integer)
	static final int NOTCOPIED = -1;		// rhsmappaths̤��Ͽ������
	private int rhspcToMapPath(ProcessContext pc) {
		if (!rhsmappaths.containsKey(pc)) return NOTCOPIED;
		return ((Integer)rhsmappaths.get(pc)).intValue();
	}
	
	//
	
	private boolean fUseMoveCells = true;	// ����$p���Ф���MOVECELLS��Ȥ���Ŭ�����뤫�ʳ�ȯ�������ѿ���

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int buildRHSMem(Membrane mem) throws CompileException {
		Env.c("RuleCompiler::buildRHSMem" + mem);
		int procvarcount = mem.processContexts.size();
		Iterator it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			if (pc.def.lhsOcc.mem == null) {
				systemError("SYSTEM ERROR: ProcessContext.def.lhsOcc.mem is not set");
			}
			if (rhsmemToPath(mem) != lhsmemToPath(pc.def.lhsOcc.mem)) {
				if (fUseMoveCells && /*pc.def.rhsOccs.get(0) == pc*/ pc.def.rhsOccs.size() == 1) {
					body.add(new Instruction(Instruction.MOVECELLS,
						rhsmemToPath(mem), lhsmemToPath(pc.def.lhsOcc.mem) ));
				} 
				else {
					int rethashmap = varcount++;
					body.add(new Instruction(Instruction.COPYMEM,
						rethashmap, rhsmemToPath(mem), lhsmemToPath(pc.def.lhsOcc.mem) ));
					rhsmappaths.put(pc,new Integer(rethashmap));
					//else {
					//	systemError("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + pc);
					//}
				}
			}
		}
		it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			int submempath = varcount++;
			rhsmempath.put(submem, new Integer(submempath));
			if (submem.pragmaAtHost != null) { // ���դǡ����ꤵ��Ƥ�����
				if (submem.pragmaAtHost.def == null) {
					systemError("SYSTEM ERROR: pragmaAtHost.def is not set: " + submem.pragmaAtHost.getQualifiedName());
				}
				int nodedescatomid = typedcxtToSrcPath(submem.pragmaAtHost.def);
				body.add( new Instruction(Instruction.NEWROOT, submempath, rhsmemToPath(mem),
					nodedescatomid) );
			}
			else { // �̾�α�����ξ��
				body.add( Instruction.newmem(submempath, rhsmemToPath(mem) ) );
			}
			if (submem.name != null)
				body.add(new Instruction( Instruction.SETMEMNAME, submempath, submem.name.intern() ));
			int subcount = buildRHSMem(submem);
			if (subcount > 0) {
				body.add(new Instruction(Instruction.INSERTPROXIES,
					rhsmemToPath(mem), rhsmemToPath(submem)));
			}
			procvarcount += subcount;
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
			if (rhsmemToPath(mem) == lhsmemToPath(rc.def.lhsOcc.mem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.def.lhsOcc.mem) ));
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
					lhsatomToPath(link2.atom), link2.pos, rhsmemToPath(mem) ));
			} else {
				int atomid = varcount++;
				rhsatompath.put(atom, new Integer(atomid));
				rhsatoms.add(atom);
				body.add( Instruction.newatom(atomid, rhsmemToPath(mem), atom.functor));
			}
		}
	}
	
	HashMap cxtlinksetpaths = new HashMap();
	
	/** ���ԡ�����$p�ˤĤ��ơ����Υ�󥯥��֥������Ȥؤλ��Ȥ��������
	 * ���Υꥹ�Ȥ������insertconnectors̿���ȯ�Ԥ��롣
	 * ����set���֥������Ȥؤλ��Ȥ��������줿�ѿ���Ф��Ƥ�����
	 * �ץ���ʸ̮���->set���ѿ��ֹ�
	 * �Ȥ����ޥåפ���Ͽ���롣
	 */
	private void insertconnectors(){
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() > 1) {
				List linklist = new ArrayList();
				int setpath=varcount++;
				for(int i=0;i<def.lhsOcc.args.length;i++){
					if(!lhslinkpath.containsKey(def.lhsOcc.args[i])){
						int linkpath = varcount++;
						body.add(new Instruction(Instruction.GETLINK,linkpath,
							lhsatomToPath(def.lhsOcc.args[i].buddy.atom),def.lhsOcc.args[i].buddy.pos));
						lhslinkpath.put(def.lhsOcc.args[i],new Integer(linkpath));
					}
					int srclink = lhslinkToPath(def.lhsOcc.args[i]);
					linklist.add(new Integer(srclink));
				}
				body.add(new Instruction( Instruction.INSERTCONNECTORS,
					setpath,linklist,lhsmemToPath(def.lhsOcc.mem) ));
				cxtlinksetpaths.put(def,new Integer(setpath));
			}
		}
	}
	
	/** ��Ǻ��줿�ޥåפ�������Ƥ���set�ȡ����ȥ��ԡ����˺�ä��ޥåפ�
	 * �����ˤ��ơ�deleteconnectors̿���ȯ�Ԥ��롣
	 *
	 */
	private void deleteconnectors(){
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Iterator it2 = def.rhsOccs.iterator();
			if(def.rhsOccs.size() <2)continue;
			while (it2.hasNext()) {
				ProcessContext pc = (ProcessContext)it2.next();
				body.add(new Instruction(Instruction.DELETECONNECTORS,
				((Integer)cxtlinksetpaths.get(def)).intValue(),
				rhspcToMapPath(pc),
				rhsmemToPath(pc.mem)));
			}
		}
	}
	
	/** ��󥯤�ĥ���ؤ���������Ԥ�
	 * todo �����ɤ��������� */
	private void updateLinks() throws CompileException {
		Env.c("RuleCompiler::updateLinks");
		if(true) {
			Iterator it = computeRHSLinks().iterator();
			while(it.hasNext()){
				LinkOccurrence link = (LinkOccurrence)it.next();
				int linkpath = getLinkPath(link);
				int buddypath = getLinkPath(link.buddy);
				Membrane mem = link.atom.mem;
				int mempath = rhsmemToPath(mem);
				body.add(new Instruction(Instruction.UNIFYLINKS,linkpath,buddypath,mempath));
			}
		}
		
//		// PART 1 - ���դΥ��ȥ�˽и�������
//		Iterator it = rhsatoms.iterator();
//		while (it.hasNext()) {
//			Atom atom = (Atom)it.next();
//			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
//				LinkOccurrence link = atom.args[pos].buddy;
//				Membrane targetmem = atom.mem;
//				if (atom.functor.equals(Functor.INSIDE_PROXY) && pos == 0) {
//					targetmem = targetmem.parent;	// $in����1�����Ͽ��줬��������
//				}
//				if (link.atom instanceof ProcessContext) {
//					// ���ȥ�Υ���褬�ץ���ʸ̮/���դ��ץ���ʸ̮�ΤȤ�
//					ProcessContext pc = (ProcessContext)link.atom;
//					if (pc.mem.typedProcessContexts.contains(pc)) {
//						// �ѥå��ַ����¤�ꡢ���դΥ��ȥ�Υ����η��դ��ץ���ʸ̮�ϱ��դ˸¤��롣
//						// ( :- type($pc) | atom(X), $pc[X|] )
//						if (gc.typedcxttypes.get(pc.def) == GuardCompiler.UNARY_ATOM_TYPE) {
//							body.add( Instruction.newlink(
//										rhsatomToPath(atom), pos,
//										rhstypedcxtToPath(pc), 0,
//										rhsmemToPath(targetmem) ));
//						}
//					} else { // ���դ��Ǥʤ��Ȥ�
//						// ���դη��ʤ��ץ���ʸ̮�ϥȥåץ�٥��̵�������ջ�����Ȥ�ľ�ܥ�󥯤Ǥ��ʤ����ᡢ
//						// �����η��ʤ��ץ���ʸ̮�ϱ��դ˸¤��롣�����ơ����Υץ���ʸ̮��
//						// ���դǤνи��ˤ������б����뼫ͳ��󥯤ϡ����դΥ��ȥ����³���Ƥ��롣
//						// ( { org(Y,), $pc[Y,|] } :- atom(X), $pc[X,|] )
//						if (fUseMoveCells && pc.def.rhsOccs.size() == 1) {
//							LinkOccurrence orglink = pc.def.lhsOcc.args[link.pos].buddy; // org������Y�νи�
//							body.add( new Instruction(Instruction.RELINK,
//											rhsatomToPath(atom), pos,
//											lhsatomToPath(orglink.atom), orglink.pos,
//											rhsmemToPath(targetmem) ));
//						}
//						else {
//							LinkOccurrence orglink = pc.def.lhsOcc.args[link.pos].buddy; // org������Y�νи�
////							int srclink = varcount++;
//							int srclink = lhslinkToPath(orglink.atom, orglink.pos);
//							int copiedlink = varcount++;
//							int atomlink = varcount++;
////							body.add( new Instruction(Instruction.GETLINK,
////											srclink, lhsatomToPath(orglink.atom), orglink.pos ));
//							body.add( new Instruction(Instruction.LOOKUPLINK,
//											copiedlink, rhspcToMapPath(pc),
//											srclink));
//							body.add( new Instruction(Instruction.ALLOCLINK,
//											atomlink, rhsatomToPath(atom), pos ));
//							body.add( new Instruction(Instruction.UNIFYLINKS,
//											copiedlink, atomlink, rhsmemToPath(targetmem) ));
//						}
//					}
//					continue;
//				}
//				// �����ϥ��ȥ�
//				if (link.atom.mem == rs.leftMem) { // ( buddy(X) :- atom(X) )
//					body.add( new Instruction(Instruction.RELINK,//LOCALRELINK�˽���ͽ��
//						rhsatomToPath(atom), pos,
//						lhsatomToPath(link.atom), link.pos,
//						rhsmemToPath(targetmem) ));
//				} else { // ( :- buddy(X), atom(X) )
//					if (rhsatomToPath(atom) < rhsatomToPath(link.atom)
//					|| (rhsatomToPath(atom) == rhsatomToPath(link.atom) && pos < link.pos)) {
//						body.add( new Instruction(Instruction.NEWLINK,
//										rhsatomToPath(atom), pos,
//										rhsatomToPath(link.atom), link.pos,
//										rhsmemToPath(targetmem) ));
//					}
//				}
//			}
//		}
//		// PART 2 - ���դη��դ��ץ���ʸ̮�˽и�������
//		it = rhstypedcxtpaths.keySet().iterator();
//		while (it.hasNext()) {
//			ProcessContext atom = (ProcessContext)it.next();
//			for (int pos = 0; pos < atom.getArity(); pos++) {
//				LinkOccurrence link = atom.args[pos].buddy;
//				if (link == null) {
//					systemError("SYSTEM ERROR: buddy of process context explicit free link is not set");
//				}
//				if (!(link.atom instanceof ProcessContext)) {
//					// ���դ��ץ���ʸ̮�Υ���褬���ȥ�ΤȤ�
//					if (lhsatoms.contains(link.atom)) { // ( buddy(X) :- type($atom) | $atom[X|] )
//						if (gc.typedcxttypes.get(atom.def) == GuardCompiler.UNARY_ATOM_TYPE) {
//							body.add( new Instruction(Instruction.RELINK,
//								rhstypedcxtToPath(atom), 0,
//								lhsatomToPath(link.atom), link.pos,
//								rhsmemToPath(atom.mem) ));
//						}
//					}
//					else if (rhsatoms.contains(link.atom)) { // ( :- type($atom) | buddy(X), $atom[X|] )
//						// PART1��newlink�ѤߤʤΤǡ����⤷�ʤ�
//					}
//					else {
//						systemError("SYSTEM ERROR: unknown buddy of body typed process context");
//					}
//					continue;
//				}
//				ProcessContext buddypc = (ProcessContext)link.atom;
//				if (buddypc.mem.typedProcessContexts.contains(buddypc)) {
//					// ����褬���դ��ץ���ʸ̮�ΤȤ����ѥå��ַ����¤�ꡢ�����ⱦ�ա�
//					// ( :- type($atom),type($buddypc) | $buddypc[X|], $atom[X|] )
//					if (rhstypedcxtToPath(atom) < rhstypedcxtToPath(buddypc)
//					 || (rhstypedcxtToPath(atom) == rhstypedcxtToPath(buddypc) && pos < link.pos)) {
//						if (gc.typedcxttypes.get(atom.def) == GuardCompiler.UNARY_ATOM_TYPE
//						 && gc.typedcxttypes.get(buddypc.def) == GuardCompiler.UNARY_ATOM_TYPE) {
//							body.add( new Instruction(Instruction.NEWLINK,
//								rhstypedcxtToPath(atom), 0,
//								rhstypedcxtToPath(buddypc), 0,
//								rhsmemToPath(atom.mem) ));
//						}
//					}
//				}
//				else {
//					// ����褬���դ��Ǥʤ��ץ���ʸ̮�ΤȤ���PART1��Ʊ����ͳ��$buddypc�ϱ��ա�
//					// ( {org(Y,), $buddypc[Y,|]} :- type($atom) | $buddypc[X,|], $atom[X|] )
//					LinkOccurrence orglink = buddypc.def.lhsOcc.args[pos].buddy; // org������Y�νи�
//					if (gc.typedcxttypes.get(atom.def) == GuardCompiler.UNARY_ATOM_TYPE) {
//						body.add( new Instruction(Instruction.RELINK,
//												rhstypedcxtToPath(atom), 0,
//												lhsatomToPath(orglink.atom), orglink.pos,
//												rhsmemToPath(atom.mem) ));
//					}
//				}
//			}
//		}
//		// PART 3 - ���դη��դ��Ǥʤ��ץ���ʸ̮�˽и�������
//		it = rs.processContexts.values().iterator();
//		while (it.hasNext()) {
//			ContextDef def = (ContextDef)it.next();
//			Iterator it2 = def.rhsOccs.iterator();
//			while (it2.hasNext()) {
//				ProcessContext atom = (ProcessContext)it2.next();
//				for (int pos = 0; pos < atom.getArity(); pos++) {
//					LinkOccurrence link = atom.args[pos].buddy;	// ����Ū�ʼ�ͳ��󥯤Υ����Υ�󥯽и�
//					if (!(link.atom instanceof ProcessContext)) {
//						// ���դ��Ǥʤ��ץ���ʸ̮�Υ���褬���ȥ�ΤȤ�
//						if (lhsatoms.contains(link.atom)) { // �����Ϻ��դΥȥåץ�٥�
//							// ( {src(Z,),$atom[Z,|]},buddy(X) :- $atom[X,|] )
//							if (fUseMoveCells && atom.def.rhsOccs.size() == 1) {							
//								LinkOccurrence srclink = atom.def.lhsOcc.args[pos].buddy; // src������Z�νи�
//								body.add( new Instruction(Instruction.LOCALUNIFY,
//													lhsatomToPath(link.atom), link.pos,
//													lhsatomToPath(srclink.atom), srclink.pos,
//													rhsmemToPath(atom.mem) )); // ����
//							}
//							else {
//								LinkOccurrence srclink = atom.def.lhsOcc.args[pos].buddy; // src������Z�νи�
////								int srclinkid  = varcount++;
//								int srclinkid  = lhslinkToPath(srclink.atom, srclink.pos); 
//								int copiedlink = varcount++;
////								int buddylink  = varcount++;
//								int buddylink  = lhslinkToPath(link.atom, link.pos); 
////								body.add( new Instruction(Instruction.GETLINK,
////												srclinkid, lhsatomToPath(srclink.atom), srclink.pos ));
//								body.add( new Instruction(Instruction.LOOKUPLINK,
//												copiedlink, rhspcToMapPath(atom), srclinkid));
////								body.add( new Instruction(Instruction.GETLINK,
////												buddylink, lhsatomToPath(link.atom), link.pos ));
//								body.add( new Instruction(Instruction.UNIFYLINKS,
//												copiedlink, buddylink, rhsmemToPath(atom.mem) ));
//							}
//						}
//						else if (rhsatoms.contains(link.atom)) { // ( :- buddy(X), $atom[X,|] )
//							// PART1��newlink�ѤߤʤΤǡ����⤷�ʤ�
//						}
//						else {
//							systemError("SYSTEM ERROR: unknown buddy of body typed process context");
//						}
//						continue;
//					}
//					else {
//						ProcessContext buddypc = (ProcessContext)link.atom;
//						if (buddypc.mem.typedProcessContexts.contains(buddypc)) {
//							// PART2��relink�ѤߤʤΤǡ����⤷�ʤ�
//						}
//						else {
//							// ����褬���դ��Ǥʤ��ץ���ʸ̮�ΤȤ���PART1��Ʊ����ͳ��$buddypc�ϱ��ա�
//							// ( {org(Y,),$buddypc[Y,|]},{src(Z,),$atom[Z,|]} :- $buddypc[X,|],$atom[X,|] )
//							LinkOccurrence orglink = buddypc.def.lhsOcc.args[link.pos].buddy; // org������Y�νи�
//							LinkOccurrence srclink = atom.   def.lhsOcc.args[pos     ].buddy; // src������Z�νи�
//
//							// ����   �� ���դ�$p�и�����б������󥯽и���ȿ��¦�ʡ�orglink�ޤ���srclink��
//							// ������ �� (map���ѿ��ֹ�,����Ū�ʰ������pos)
//							int atommap  = rhspcToMapPath(atom);
//							int atompos  = pos;
//							int buddymap = rhspcToMapPath(buddypc);
//							int buddypos = link.pos; 
//							if (atommap == -1) {
//								atommap = lhsatomToPath(srclink.atom);
//								atompos = srclink.pos;
//							}
//							if (buddymap == -1) {
//								buddymap = lhsatomToPath(orglink.atom);
//								buddypos = orglink.pos;
//							}
//							
//							if (atommap < buddymap || (atommap == buddymap && atompos < buddypos)) {
//								if (fUseMoveCells && atom.def.rhsOccs.size() == 1 && buddypc.def.rhsOccs.size() == 1) {
//									body.add( new Instruction(Instruction.UNIFY,
//													lhsatomToPath(srclink.atom), srclink.pos,
//													lhsatomToPath(orglink.atom), orglink.pos,
//													rhsmemToPath(atom.mem) ));
//								}
//								else {
////									int buddylink       = varcount++;
//									int buddylink       = lhslinkToPath(srclink.atom, srclink.pos); 
//									int buddycopiedlink = buddylink;
////									body.add( new Instruction(Instruction.GETLINK,
////													buddylink, lhsatomToPath(srclink.atom), srclink.pos ));
//									if (buddypc.def.rhsOccs.size() != 1) {
//										buddycopiedlink = varcount++;
//										body.add( new Instruction(Instruction.LOOKUPLINK,
//														buddycopiedlink, rhspcToMapPath(buddypc), buddylink ));
//									}							
////									int atomlink       = varcount++;
//									int atomlink       = lhslinkToPath(orglink.atom, orglink.pos); 
//									int atomcopiedlink = atomlink;
////									body.add( new Instruction(Instruction.GETLINK,
////													atomlink, lhsatomToPath(orglink.atom), orglink.pos ));
//									if (atom.def.rhsOccs.size() != 1) {
//										atomcopiedlink = varcount++;
//										body.add( new Instruction(Instruction.LOOKUPLINK,
//														atomcopiedlink, rhspcToMapPath(atom), atomlink ));
//									}
//									body.add( new Instruction(Instruction.UNIFYLINKS,
//													buddycopiedlink, atomcopiedlink, rhsmemToPath(atom.mem) ));
//								}
//							}
//						}
//					}
//				}
//			}
//		}
	}
	/** ���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ� */
	private void enqueueRHSAtoms() {
		int index = body.size(); // �����Ƶ���Ŭ���θ��̤���粽���뤿�ᡢ�ս���Ѥ�ʥ����ɤ���������
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (!atom.functor.equals(Functor.INSIDE_PROXY)
			 && !atom.functor.equals(Functor.OUTSIDE_PROXY) 
			 && atom.functor.isSymbol()
			 && atom.functor.isActive() ) {
				body.add(index, new Instruction(Instruction.ENQUEUEATOM, rhsatomToPath(atom)));
			}
		}
	}
	/** ����饤�󥳡��ɤ�¹Ԥ���̿����������� */
	private void addInline() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			int atomID = rhsatomToPath(atom);
			Inline.register(unitName, atom.functor.getName());
			int codeID = Inline.getCodeID(unitName, atom.functor.getName());
			if(codeID == -1) continue;
			body.add( new Instruction(Instruction.INLINE, atomID, unitName, codeID));
		}
	}
	static final Functor FUNC_USE = new Functor("use",1);
	/** �⥸�塼����ɤ߹��� */
	private void addRegAndLoadModules() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			//REG
			if(atom.functor.getArity()==1 && atom.functor.getName().equals("module")) {
				Module.regMemName(atom.args[0].buddy.atom.getName(), atom.mem);
			}
			
			//LOAD
			if (atom.functor.equals(FUNC_USE)) {
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem),
					atom.args[0].buddy.atom.getName()) );
			}
			String path = atom.getPath(); // .functor.path;
			if(path!=null && !path.equals(atom.mem.name)) {
				// ���λ����Ǥϲ��Ǥ��ʤ��⥸�塼�뤬����Τ�̾���ˤ��Ƥ���
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem), path));
			}
		}
	}
	/**�ʺ����Ѥ��줿��ޤ��ϡ˿������롼������Ф��ơ���¹�줫����֤�UNLOCKMEM��ȯ�Ԥ��롣
	 * ���������ߤμ����Ǥϡ����λ����ǤϤޤ���Ϻ����Ѥ���Ƥ��ʤ���*/
	private void unlockReusedOrNewRootMem(Membrane mem) {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) unlockReusedOrNewRootMem((Membrane)it.next());
		if (mem.pragmaAtHost != null) { // ���դǡ����ꤵ��Ƥ�����
			body.add(new Instruction(Instruction.UNLOCKMEM, rhsmemToPath(mem)));
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
			body.add( new Instruction(Instruction.FREEATOM, lhsmems.size() + i ));
		}
	}
	
	/** �ǥХå���ɽ�� */
	private void showInstructions() {
		Iterator it;
		it = atomMatch.listIterator();
		Env.d("--atomMatches:");
		while(it.hasNext()) Env.d((Instruction)it.next());
		
		it = memMatch.listIterator();
		Env.d("--memMatch:");
		while(it.hasNext()) Env.d((Instruction)it.next());
		
		it = body.listIterator();
		Env.d("--body:");
		while(it.hasNext()) Env.d((Instruction)it.next());
	}

	////////////////////////////////////////////////////////////////

	public void systemError(String text) throws CompileException {
		Env.error(text);
		throw new CompileException("SYSTEM ERROR");
	}
}

