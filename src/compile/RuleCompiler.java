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
import runtime.Inline;
import runtime.InlineUnit;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.Rule;
import runtime.SymbolFunctor;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.Context;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.ProcessContextEquation;
import compile.structure.RuleContext;
import compile.structure.RuleStructure;

import util.*;

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
	/** ����ѥ��뤵���롼�빽¤ */
	RuleStructure rs;

	/** ����ѥ��뤵���롼����б�����롼�륪�֥������� */
	Rule theRule;

	private List<Instruction> atomMatch;
	private List<Instruction> memMatch;
	private List<Instruction> tempMatch;// Slimcode���ˤΤ߻��Ѥ����(��Ȥ�memMatch)
	List<Instruction> guard;
	private List<Instruction> body;
	private int varcount;			// �����ѿ��ֹ�

	boolean hasISGROUND = true;

	List<Atom> rhsatoms;
	Map<Atom, Integer>  rhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	Map<Membrane, Integer>  rhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)	
	Map<LinkOccurrence, Integer>  rhslinkpath;		// ���դΥ�󥯽и�(LinkOccurence) -> �ѿ��ֹ�(Integer)
	//List rhslinks;		// ���դΥ�󥯽и�(LinkOccurence)�Υꥹ�ȡ������Τߡ� -> computeRHSLinks���֤��ˤ���
	List<Atomic> lhsatoms;
	List<Membrane> lhsmems;
	Map<Atomic, Integer>  lhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	Map<Membrane, Integer>  lhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)
	Map<LinkOccurrence, Integer>  lhslinkpath = new HashMap<LinkOccurrence, Integer>();		// ���դΥ��ȥ�Υ�󥯽и� (LinkOccurrence) -> �ѿ��ֹ�(Integer)
	// �㺸�դΥ��ȥ���ѿ��ֹ� (Integer) -> ��󥯤��ѿ��ֹ������ (int[])���䤫���ѹ�

	HeadCompiler hc, hc2;

	private boolean debug2 = false;

	private final int lhsmemToPath(Membrane mem) { return lhsmempath.get(mem); }
	private final int rhsmemToPath(Membrane mem) { return rhsmempath.get(mem); }
	private final int lhsatomToPath(Atomic atom) { return lhsatompath.get(atom); } 
	private final int rhsatomToPath(Atomic atom) { return rhsatompath.get(atom); } 
	private final int lhslinkToPath(Atomic atom, int pos) {	return lhslinkToPath(atom.args[pos]); }
	private final int lhslinkToPath(LinkOccurrence link) { return lhslinkpath.get(link); }

	private String unitName;
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
	Rule compile() throws CompileException {
		Env.c("compile");
		if(debug2){
			Util.println("\n*************************************************");
			Util.println("compile called : this.rs = " + rs);
			Util.println(rs.leftMem + "\n :- " + rs.guardMem + "\n | " + rs.rightMem + "\n");

		}
		liftupActiveAtoms(rs.leftMem);
		simplify();
//		theRule = new Rule(rs.toString());
		theRule = new Rule(rs.leftMem.getFirstAtomName(),rs.toString());
		theRule.name = rs.name;

		hc = new HeadCompiler();//rs.leftMem;
		hc.enumFormals(rs.leftMem);	// ���դ��Ф��벾�����ꥹ�Ȥ���
		hc2 = new HeadCompiler();
		hc2.enumFormals(rs.leftMem);
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


		if(debug2){
			Util.println("first compile_l() and compile_g() exit");
			Rule tmpRule = new Rule();
			tmpRule.memMatch = memMatch;
			tmpRule.tempMatch = tempMatch;
			tmpRule.atomMatch = atomMatch;
			tmpRule.guard = guard;
			tmpRule.body = body;
			tmpRule.showDetail();
		}
		hc = new HeadCompiler();//rs.leftMem;
		hc.enumFormals(rs.leftMem);	// ���դ��Ф��벾�����ꥹ�Ȥ���
		hc.firsttime = false;
		theRule.guardLabel = new InstructionList();
		guard = theRule.guardLabel.insts;
		contLabel = (guard != null ? theRule.guardLabel : theRule.bodyLabel);		
		compile_l();
		compile_g();
		compile_r();

		theRule.memMatch  = memMatch;
		theRule.tempMatch = tempMatch;
		theRule.atomMatch = atomMatch;
		theRule.guard     = guard;
		theRule.body      = body;
		theRule.body.add(1, Instruction.commit(theRule.name, theRule.lineno));
		if(debug2){
			Util.println("compile return theRule is\n");
			theRule.showDetail();
			Util.println("***************************************************\n");
		}
		optimize();
		return theRule;
	}

	/** ������򥳥�ѥ��뤹�� */
	private void compile_l() {
		Env.c("compile_l");
		if(debug2){
			Util.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++");
			Util.println("\ncompile_l called\n");
		}

		theRule.atomMatchLabel = new InstructionList();
		atomMatch = theRule.atomMatchLabel.insts;

		int maxvarcount = 2;	// ���ȥ��Ƴ�ѡʲ���
		for (int firstid = 0; firstid <= hc.atoms.size(); firstid++) {
			hc.prepare(); // �ѿ��ֹ������			
			hc2.prepare();
			if (firstid < hc.atoms.size()) {	
				if (Env.shuffle >= Env.SHUFFLE_DONTUSEATOMSTACKS || Env.slimcode || Env.memtestonly) continue;
				// Env.SHUFFLE_DEFAULT �ʤ�С��롼���ȿ����Ψ��ͥ�褹�뤿�ᥢ�ȥ��Ƴ�ƥ��ȤϹԤ�ʤ�

				Atom atom = (Atom)hc.atoms.get(firstid);
				if (!atom.functor.isActive()) continue;

				// ���ȥ��Ƴ
				InstructionList tmplabel = new InstructionList();
				tmplabel.insts = hc.match;
				atomMatch.add(new Instruction(Instruction.BRANCH, tmplabel));

				hc.memPaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
				hc.atomPaths.put(atom, new Integer(1));		// ��Ƴ���륢�ȥ���ѿ��ֹ�� 1
				hc.varCount = 2;
				hc.match.add(new Instruction(Instruction.FUNC, 1, atom.functor));
				Membrane mem = atom.mem;
				if (mem == rs.leftMem) {
					hc.match.add(new Instruction(Instruction.TESTMEM, 0, 1));
				}
				else {
					hc.match.add(new Instruction(Instruction.GETMEM, hc.varCount, 1, mem.kind, mem.name));
					hc.match.add(new Instruction(Instruction.LOCK,   hc.varCount));
					hc.memPaths.put(mem, new Integer(hc.varCount++));
					mem = mem.parent;
					while (mem != rs.leftMem) {
						hc.match.add(new Instruction(Instruction.GETPARENT,hc.varCount,hc.varCount-1));
						hc.match.add(new Instruction(Instruction.LOCK,     hc.varCount));
						hc.memPaths.put(mem, new Integer(hc.varCount++));
						mem = mem.parent;
					}
					hc.match.add(new Instruction(Instruction.GETPARENT,hc.varCount,hc.varCount-1));
					hc.match.add(new Instruction(Instruction.EQMEM, 0, hc.varCount++));
				}
				hc.getLinks(1, atom.functor.getArity(), hc.match); //��󥯤ΰ�����(RISC��) by mizuno
				Atom firstatom = (Atom)hc.atoms.get(firstid);
				hc.compileLinkedGroup(firstatom, hc.matchLabel);
				hc.compileMembrane(firstatom.mem, hc.matchLabel);
			} else {
				// ���Ƴ
				theRule.memMatchLabel = hc.matchLabel;
				if(Env.findatom2){
					tempMatch = hc.tempMatch;
					memMatch = hc.match;
				} else {
					memMatch = hc.match;
				}
				hc.memPaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
				hc2.memPaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
			}
			if(Env.findatom2){
				hc.compileMembraneForSlimcode(rs.leftMem, hc.matchLabel, hasISGROUND);
				hc2.compileMembrane(rs.leftMem, hc.tempLabel);
			} else {
				hc.compileMembrane(rs.leftMem, hc.matchLabel);
			}
			if(debug2){
				Util.println("first compile_l() and compile_g() exit");
				Rule tmpRule = new Rule();
				tmpRule.memMatch = memMatch;
				tmpRule.tempMatch = tempMatch;
				tmpRule.atomMatch = atomMatch;
				tmpRule.guard = guard;
				tmpRule.body = body;
				tmpRule.showDetail();
			}

			if(debug2) Util.println("\n-----------------------------------------------------");
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
				hc.setContLabel(contLabel);
				if(Env.findatom2){
					hc.compileMembraneForSlimcode(rs.leftMem, hc.matchLabel, hasISGROUND);
					hc2.compileMembrane(rs.leftMem, hc.tempLabel);
				} else {
					hc.compileMembrane(rs.leftMem, hc.matchLabel);
				}
			}
			hc.checkFreeLinkCount(rs.leftMem, hc.match); // ��������ѹ��ˤ��ƤФʤ��Ƥ褯�ʤä�����Ϥ�Ƥ�ɬ�פ���
			if (hc.match == memMatch) {
				hc.match.add(0, Instruction.spec(1, hc.maxVarCount));
				hc.tempMatch.add(0, Instruction.spec(1, hc.maxVarCount));
			}
			else {
				hc.match.add(0, Instruction.spec(2, hc.maxVarCount));
				hc.tempMatch.add(0, Instruction.spec(1, hc.maxVarCount));
			}
			// jump̿�ᷲ������
			List<Integer> memActuals  = hc.getMemActuals();
			List<Integer> atomActuals = hc.getAtomActuals();
			List varActuals  = hc.getVarActuals();
			// - ������#1
			hc.match.add( Instruction.jump(contLabel, memActuals, atomActuals, varActuals) );
			hc.tempMatch.add( Instruction.jump(contLabel, memActuals, atomActuals, varActuals) );
			// - ������#2
//			hc.match.add( Instruction.inlinereact(theRule, memActuals, atomActuals, varActuals) );
//			int formals = memActuals.size() + atomActuals.size() + varActuals.size();
//			hc.match.add( Instruction.spec(formals, formals) );
//			hc.match.add( hc.getResetVarsInstruction() );
//			List brancharg = new ArrayList();
//			brancharg.add(body);
//			hc.match.add( new Instruction(Instruction.BRANCH, brancharg) );

			// jump̿�ᷲ�����������
			if (maxvarcount < hc.varCount) maxvarcount = hc.maxVarCount;
		}
		atomMatch.add(0, Instruction.spec(2,maxvarcount));
		if(debug2){
			Util.println("\n compile_l return ");
			if(debug2){
				Util.println("first compile_l() and compile_g() exit");
				Rule tmpRule = new Rule();
				tmpRule.memMatch = memMatch;
				tmpRule.tempMatch = tempMatch;
				tmpRule.atomMatch = atomMatch;
				tmpRule.guard = guard;
				tmpRule.body = body;
				tmpRule.showDetail();
			}
			Util.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}

	}

	// todo spec̿���¦�˻����夲���Ŭ������������

	/**
	 * ���դΥ��ȥ�Υ�󥯤��Ф���getlink��Ԥ����ѿ��ֹ����Ͽ���롣(RISC��)
	 * ����Ū�ˤϥ�󥯥��֥������Ȥ�ܥǥ�̿����ΰ������Ϥ��褦�ˤ��뤫�⤷��ʤ���
	 */
//	private void getLHSLinks() {
//	lhslinkpath = new HashMap();
//	for (int i = 0; i < lhsatoms.size(); i++) {
//	Atom atom = (Atom)lhsatoms.get(i);
//	int atompath = lhsatomToPath(atom);
//	int arity = atom.functor.getArity();
//	for (int j = 0; j < arity; j++) {
//	int linkpath;
//	// ����褬ground�ξ�硢����GETLINK��ȯ�Ԥ���Ƥ���(getGroundLinkPaths)
//	if(!(atom.args[j].buddy.atom instanceof Context &&
//	groundsrcs.containsKey(((Context)atom.args[j].buddy.atom).def))){
//	linkpath = varcount++;
//	body.add(new Instruction(Instruction.GETLINK, linkpath, atompath, j));
//	}
//	else{
//	linkpath = ((Integer)groundsrcs.get(((Context)atom.args[j].buddy.atom).def)).intValue();
//	}
//	lhslinkpath.put(atom.args[j],new Integer(linkpath));
//	}
//	}
//	}

	/** ���դΥ�󥯤�����ޤ����������� */
	private List<LinkOccurrence> computeRHSLinks() {
		List<LinkOccurrence> rhslinks = new ArrayList<LinkOccurrence>();
		rhslinkpath = new HashMap<LinkOccurrence, Integer>();
		int rhslinkindex = 0;
		// ���ȥ�ΰ����Υ�󥯽и�
		for (Atom atom : rhsatoms){
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
		for(ProcessContext atom : rhstypedcxtpaths.keySet()){
			body.add(new Instruction(Instruction.ALLOCLINK,varcount,rhstypedcxtToPath(atom),0));
			rhslinkpath.put(atom.args[0],new Integer(varcount));
			if(!rhslinks.contains(atom.args[0].buddy))rhslinks.add(rhslinkindex++,atom.args[0]);
			varcount++;
		}

		// ground���եץ���ʸ̮�Υ�󥯽и�
		for(ProcessContext ground : rhsgroundpaths.keySet()){
			int linklistpath = rhsgroundToPath(ground);
			for(int i=0;i<ground.def.lhsOcc.args.length;i++){
				int linkpath = varcount++;
				body.add(new Instruction(Instruction.GETFROMLIST,linkpath, linklistpath, i));
//				int linkpath = rhsgroundToPath(atom);
				rhslinkpath.put(ground.args[i],new Integer(linkpath));
				if(!rhslinks.contains(ground.args[i].buddy))rhslinks.add(rhslinkindex++,ground.args[i]);
			}
		}

		// ���ʤ�

		for(ContextDef def : rs.processContexts.values()){
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext()) {
				ProcessContext atom = (ProcessContext)it2.next();
				for (int pos = 0; pos < atom.getArity(); pos++) {
//					LinkOccurrence srclink = atom.def.lhsOcc.args[pos].buddy;
//					int srclinkid;
//					if(!lhslinkpath.containsKey(srclink)){
//					srclinkid = varcount++;
//					body.add( new Instruction(Instruction.GETLINK,srclinkid, 
//					lhsatomToPath(srclink.atom), srclink.pos));
//					lhslinkpath.put(srclink,new Integer(srclinkid));
//					}
//					srclinkid = lhslinkToPath(srclink);
//					if (!(fUseMoveCells && atom.def.rhsOccs.size() == 1)) {							
//					int copiedlink = varcount++;
//					body.add( new Instruction(Instruction.LOOKUPLINK,
//					copiedlink, rhspcToMapPath(atom), srclinkid));
//					srclinkid = copiedlink;
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
			return rhslinkpath.get(link);
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

	/** ������򥳥�ѥ��뤹�� 
	 * 
	 * LMNParser�������դ˽и������󥯤��Ф���ɬ�פ˱����Ƽ�ͳ��󥯴������ȥ���������Ƥ���
	 * 
	 * ���դ���Υ�å��ϥޥå��󥰤λ����ǹԤ�
	 * 
	 * */
	private void compile_r() throws CompileException {
		Env.c("compile_r");
		int formals = varcount;
		//body.add( Instruction.commit(theRule) );
		inc_guard();

		rhsatoms    = new ArrayList<Atom>();
		rhsatompath = new HashMap<Atom, Integer>();
		rhsmempath  = new HashMap<Membrane, Integer>();
		int toplevelmemid = lhsmemToPath(rs.leftMem);
		rhsmempath.put(rs.rightMem, new Integer(toplevelmemid));

		//Env.d("rs.leftMem -> "+rs.leftMem);
		//Env.d("lhsmempaths.get(rs.leftMem) -> "+lhsmempaths.get(rs.leftMem));
		//Env.d("rhsmempaths -> "+rhsmempaths);

		/*
		 * ���դ�����Ū�ʥץ��������(��°��Ȥδط������)����
		 * ������$p�����Ƥλ����Ƶ�Ū��lock����
		 * ������$p�μ�ͳ��󥯤˥��ͥ�������������
		 * 
		 * �����ȡ�
		 * ���դΥ��ȥ�(����Ū�ʼ�ͳ��󥯴������ȥ��ޤ�)/unary���ѿ��ֹ�˥Х���ɤ��졤��°�줫��Ͻ���졤�¹ԥ��ȥॹ���å����������Ƥ���
		 * ���դ�����ѿ��ֹ�˥Х���ɤ��졤���줪��Ӽ¹��쥹���å��������졤��å�����Ƥ���
		 * ���դ�ground�Ϻ����ѿ��ֹ�˥Х���ɤ��졤��°�줫��Ͻ����Ƥ���
		 * ���ʤ�$p�ϥޥå������ץ��������Ƥ�����Ū�Ǥʤ���ͳ��󥯤�star����1�����˽и�����褦�ˤʤäƤ���
		 * ���������ʤ�$p�ξ�繹������Ū�ʼ�ͳ��󥯤�=/2���������졤����Ū�ʼ�ͳ��󥯤Υꥹ�ȤؤΥޥåפ���������Ƥ���
		 * ������$p�λ���ϺƵ�Ū�˥�å�����Ƥ���
		 */
		dequeueLHSAtoms();
		removeLHSAtoms();
		removeLHSTypedProcesses();
		if (removeLHSMem(rs.leftMem) >= 2) {
			body.add(new Instruction(Instruction.REMOVETOPLEVELPROXIES, toplevelmemid));
		}

		recursiveLockLHSNonlinearProcessContextMems();
		insertconnectors();

		// insertconnectors�θ�Ǥʤ���Ф��ޤ������ʤ��ΤǺ�ȯ�� ( 2006/09/15 kudo)
		getGroundLinkPaths();


		// ���դι�¤��$p�����ơ���Ƶ�Ū����������
		// $p������Ū�Ǥʤ���󥯤�Ϥ�

		buildRHSMem(rs.rightMem);
		/* ���դ�$p�����֤��줿ľ�塣���Υ����ߥ󥰤Ǥʤ���Фʤ�ʤ�Ȧ */
		if (!rs.rightMem.processContexts.isEmpty()) {
			body.add(new Instruction(Instruction.REMOVETEMPORARYPROXIES, toplevelmemid));
		}
		copyRules(rs.rightMem);
		loadRulesets(rs.rightMem);		
		buildRHSTypedProcesses();
		buildRHSAtoms(rs.rightMem);
		// ������varcount�κǽ��ͤ����ꤹ�뤳�ȤˤʤäƤ��롣�ѹ�����Ŭ�ڤ˲��˰�ư���뤳�ȡ�


		//���դ�����Ū�ʥ�󥯤�Ž��
		//getLHSLinks();
		updateLinks();
		deleteconnectors();

		//���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ�
		enqueueRHSAtoms();

		//����2�Ĥϱ��դι�¤�������ʹߤʤ餤�ĤǤ�褤
		addInline();
		if (Env.slimcode) {
      addCallback();
    }
		addRegAndLoadModules();

		// ���դλĤä��ץ������������
		freeLHSNonlinearProcessContexts();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms();
		freeLHSTypedProcesses();
//		freeLHSSingletonProcessContexts(); // freemem����Ԥ����뤿��3�Ծ�˰�ư���� n-kato 2005.1.13

		// ���unlock����

		recursiveUnlockLHSNonlinearProcessContextMems();
		unlockReusedOrNewRootMem(rs.rightMem);
		//
		body.add(0, Instruction.spec(formals, varcount));

//		if (rs.rightMem.mems.isEmpty() && rs.rightMem.ruleContexts.isEmpty()
//		&& rs.rightMem.processContexts.isEmpty() && rs.rightMem.rulesets.isEmpty()) {
//		body.add(new Instruction(Instruction.CONTINUE));
//		} else 
		body.add(new Instruction(Instruction.PROCEED));
	}

	////////////////////////////////////////////////////////////////
	//
	// �����ɴط�
	//

	/** �إåɤ���ȥ��ȥ���Ф��ơ��������ֹ����Ͽ���� */
	private void genLHSPaths() {
		lhsatompath = new HashMap<Atomic, Integer>();
		lhsmempath  = new HashMap<Membrane, Integer>();
		varcount = 0;
		for (int i = 0; i < lhsmems.size(); i++) {
			lhsmempath.put(lhsmems.get(i), new Integer(varcount++));
		}
		for (Atomic atomic : lhsatoms)
			lhsatompath.put(atomic, new Integer(varcount++));
	}

	/** �����ɤμ����� */
	private void inc_guard() {
		varcount = lhsatoms.size() + lhsmems.size();
		genTypedProcessContextPaths();
		// typedcxtdefs = gc.typedcxtdefs;
		// varcount = lhsatoms.size() + lhsmems.size() + rs.typedProcessContexts.size();
		//getLHSLinks();
		getGroundLinkPaths();
	}

//	private void inc_head(HeadCompiler hc) {
//	// �إåɤμ�����
//	lhsatoms = hc.atoms;
//	lhsmems  = hc.mems;
//	genLHSPaths();
//	varcount = lhsatoms.size() + lhsmems.size();
//	}

	/** �����ɤ򥳥�ѥ��뤹�� */
	private void compile_g() throws CompileException {
		lhsmems  = hc.mems;
		lhsatoms = hc.atoms;
		genLHSPaths();
		gc = new GuardCompiler(this, hc);		/* �ѿ��ֹ�������� */
		if (guard == null) return;
		int formals = gc.varCount;
		gc.getLHSLinks();								/* ���դ����ƤΥ��ȥ�Υ�󥯤ˤĤ���getlink̿���ȯ�Ԥ��� */
		gc.fixTypedProcesses();						/* ���դ��ץ���ʸ̮���դ˷��ꤹ�� */
		gc.checkMembraneStatus();					/* �ץ���ʸ̮�Τʤ����stable����θ����򤹤� */
		varcount = gc.varCount;
		compileNegatives();							/* ������Υ���ѥ��� */
		fixUniqOrder();									/* uniq̿���Ǹ�˰�ư */
		guard.add( 0, Instruction.spec(formals,varcount) );
		guard.add( Instruction.jump(theRule.bodyLabel, gc.getMemActuals(),
				gc.getAtomActuals(), gc.getVarActuals()) );
		//RISC���ǡ�������֤Ȥ��ƥ����ɤ�getlink����ʪ��ܥǥ����Ϥ��ʤ����ˤ����Τǡ�
		//������̿����ζɽ��ѿ��ο��ȥܥǥ�̿����ΰ����ο������פ��ʤ��ʤä���by mizuno
		varcount = gc.getMemActuals().size() + gc.getAtomActuals().size() 
		+ gc.getVarActuals().size();
	}
	/**
	 * uniq ̿����ĤˤޤȤ�ƥ�����̿����κǸ�˰�ư���롣
	 * uniq ̿��ϡ����Ƥμ��Ԥ����륬����̿��Τ����Ǹ����ˤʤ��Ȥ����ʤ���
	 * hara
	 */
	void fixUniqOrder() {
		boolean found = false;
		List vars = new ArrayList();
		Iterator<Instruction> it = guard.iterator();
		while(it.hasNext()) {
			Instruction inst = it.next();
			if(inst.getKind() == Instruction.UNIQ) {
				found = true;
				vars.addAll((ArrayList)inst.getArg(0));
				it.remove();
			}
		}
		if(found) guard.add(new Instruction(Instruction.UNIQ, vars));
	}

	/** ������򥳥�ѥ��뤹�� */
	void compileNegatives() throws CompileException{
		Iterator<List<ProcessContextEquation>> it = rs.guardNegatives.iterator();
		while (it.hasNext()) {
			List<ProcessContextEquation> eqs = it.next();
			HeadCompiler negcmp = hc.getNormalizedHeadCompiler();
			negcmp.varCount = varcount;
			negcmp.compileNegativeCondition(eqs, negcmp.matchLabel);
			guard.add(new Instruction(Instruction.NOT, negcmp.matchLabel));
			if (varcount < negcmp.varCount)  varcount = negcmp.varCount;
		}
	}

	// ���դ��ץ���ʸ̮�ط�

	private GuardCompiler gc;
	/** ���դ��ץ���ʸ̮�α��դǤνи� (Context) -> �ѿ��ֹ� */
	private HashMap<ProcessContext, Integer> rhstypedcxtpaths = new HashMap<ProcessContext, Integer>();
	/** ground���դ��ץ���ʸ̮�α��դǤνи�(Context) -> (Link�Υꥹ�Ȥ�ؤ�)�ѿ��ֹ� */
	private HashMap<ProcessContext, Integer> rhsgroundpaths = new HashMap<ProcessContext, Integer>();
	/** ground���դ��ץ���ʸ̮�α��դǤνи�(Context) -> (Link��ؤ�)�ѿ��ֹ�Υꥹ�� */
	private HashMap rhsgroundlinkpaths = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �������и��ʥ��ԡ����Ȥ���и��ˤ��ѿ��ֹ��Body�¹Ի��� */	
	private HashMap<ContextDef, Integer> typedcxtsrcs  = new HashMap<ContextDef, Integer>();
	/** ground���դ��ץ���ʸ̮���(ContextDef) -> �������и��ʥ��ԡ����Ȥ���и��ˤ��ѿ��ֹ��Body�¹Ի��ˤΥꥹ�Ȥ��ѿ��ֹ� */
	private HashMap<ContextDef, Integer> groundsrcs = new HashMap<ContextDef, Integer>();
	/** Body�¹Ի��ʤΤǡ�UNBOUND�ˤϤʤ�ʤ� */
	private int typedcxtToSrcPath(ContextDef def) {
		return typedcxtsrcs.get(def);
	}
	/** Body�¹Ի��ʤΤǡ�UNBOUND�ˤϤʤ�ʤ� */
	private int groundToSrcPath(ContextDef def) {
		return groundsrcs.get(def);
	}
	/**��*/
	private int rhstypedcxtToPath(Context cxt) {
		return rhstypedcxtpaths.get(cxt);
	}
	/**��*/
	private int rhsgroundToPath(Context cxt) {
		return rhsgroundpaths.get(cxt);
	}

	/** unary���ץ���ʸ̮�ˤĤ��ơ��ѿ��ֹ�򥬡��ɥ���ѥ��餫��������� */
	private void genTypedProcessContextPaths() {
		for(ContextDef def : gc.typedCxtDefs){
			if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
				typedcxtsrcs.put( def, new Integer(varcount++) );
			}
		}
	}

	/** ground���դ��ץ���ʸ̮����ˤĤ��ơ����Ȥʤ��󥯤Υꥹ�Ȥ�������� */
	private void getGroundLinkPaths() {
		groundsrcs = new HashMap<ContextDef, Integer>();
		for(ContextDef def : gc.groundSrcs.keySet()){
			if(gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
//				ProcessContext lhsOcc = def.lhsOcc
				int linklistpath = varcount++;
				body.add(new Instruction(Instruction.NEWLIST,linklistpath));
				// ���Ƥΰ������Ф���ȯ�Ԥ���
				for(int i=0;i<def.lhsOcc.args.length;i++){
					int linkpath = varcount++;
					body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(def.lhsOcc.args[i].buddy.atom),def.lhsOcc.args[i].buddy.pos));
					body.add(new Instruction(Instruction.ADDTOLIST,linklistpath,linkpath));
					groundsrcs.put(def,new Integer(linklistpath));
				}
			}
		}
	}
//	public void enumTypedContextDefs() {
//	Iterator it = rs.typedProcessContexts.values().iterator();
//	while (it.hasNext()) {
//	ContextDef def = (ContextDef)it.next();
//	typedcxtdefs.add(def);
//	}
//	}
	/** ���դη��դ��ץ���ʸ̮������ */
	private void removeLHSTypedProcesses() {
		for(ContextDef def : rs.typedProcessContexts.values()){
			Context pc = def.lhsOcc;
			if (pc != null) { // �إåɤΤȤ��Τ߽����
				if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
					//dequeue����Ƥ��ʤ��ä��Τ��ɲ�(2005/08/27) by mizuno
					body.add(new Instruction( Instruction.DEQUEUEATOM, typedcxtToSrcPath(def) ));
					body.add(new Instruction( Instruction.REMOVEATOM,
							typedcxtToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
				else if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
					body.add(new Instruction( Instruction.REMOVEGROUND,
							groundToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
			}
		}
	}
	/** ���դη��դ��ץ���ʸ̮��������� */
	private void freeLHSTypedProcesses() {
		for(ContextDef def : rs.typedProcessContexts.values()){
			if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
				body.add(new Instruction( Instruction.FREEATOM,
						typedcxtToSrcPath(def) ));
			}
			else if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
				body.add(new Instruction( Instruction.FREEGROUND,groundToSrcPath(def)));
			}
		}
	}	

	/** �������ץ���ʸ̮�κ��սи���������� */
	private void freeLHSNonlinearProcessContexts(){
		for(ContextDef def : rs.processContexts.values()){
			if (def.rhsOccs.size() != 1) { // �������ΤȤ�1�Ĥ��������Ѥ���褦�ˤ����� size == 0 ��ľ���� -> �����ѤϺ�Ŭ����Ǥ���뤳�Ȥˤ����Τ�����
				body.add(new Instruction( Instruction.DROPMEM,
						lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	/** �������ץ���ʸ̮�κ��սи����Ƶ�Ū�˥�å����� */
	private void recursiveLockLHSNonlinearProcessContextMems(){
		for(ContextDef def : rs.processContexts.values()){
			if (def.rhsOccs.size() != 1) {
				body.add(new Instruction( Instruction.RECURSIVELOCK,
						lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	/** �������ץ���ʸ̮�κ��սи����Ƶ�Ū�˥�å��������� */
	private void recursiveUnlockLHSNonlinearProcessContextMems(){
		for(ContextDef def : rs.processContexts.values()){
			if (def.rhsOccs.size() != 1) {
				if (false) { // �����Ѥ����Ȥ��Τ� recursiveunlock ����
					body.add(new Instruction( Instruction.RECURSIVEUNLOCK,
							lhsmemToPath(def.lhsOcc.mem) ));
				}
			}
		}
	}

	/** ���դη��դ��ץ���ʸ̮���ۤ��� */
	private void buildRHSTypedProcesses() {
		for(ContextDef def : rs.typedProcessContexts.values()){
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext()) {
				ProcessContext pc = (ProcessContext)it2.next();
				if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE) {
					int atompath = varcount++;
					body.add(new Instruction( Instruction.COPYATOM, atompath,
							rhsmemToPath(pc.mem),
							typedcxtToSrcPath(pc.def) ));
					rhstypedcxtpaths.put(pc, new Integer(atompath));
				}
				else if(gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE) {
					int retlistpath = varcount++;
//					System.out.println("cp");
//					int mappath = varcount++;
					body.add(new Instruction( Instruction.COPYGROUND, retlistpath,
							groundToSrcPath(pc.def), // ground�ξ��ϥ�󥯤��ѿ��ֹ�Υꥹ�Ȥ�ؤ��ѿ��ֹ�
							rhsmemToPath(pc.mem) ));
					int groundpath = varcount++;
					body.add(new Instruction( Instruction.GETFROMLIST,groundpath,retlistpath,0));
					int mappath = varcount++;
					body.add(new Instruction(Instruction.GETFROMLIST,mappath,retlistpath,1));
					rhsgroundpaths.put(pc, new Integer(groundpath));
					rhsmappaths.put(pc,new Integer(mappath));
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////

	/** �쳬�ز��ˤ��륢���ƥ��֥��ȥ����������Ƭ�����˥��饤�ɰ�ư���롣
	 *  slim�Τ���˥����ƥ��֥��ȥࡢ�ǡ������ȥ�(���ͥ��ȥ�ʳ�)��
	 *  ���ͥ��ȥ�ν���ѹ������ͥ��ȥ�Ȥ� IntegerFunctor��FloatingFunctor
	 *  ���ե��󥯥��Ǥ���褦�ʥ��ȥ�Τ��ȡ�*/
	private static void liftupActiveAtoms(Membrane mem) {
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			liftupActiveAtoms(it.next());
		}
		List<Atom> atomlist = new LinkedList<Atom>();
		for(Iterator<Atom> it = mem.atoms.iterator(); it.hasNext();){
			atomlist.add(it.next());
		}
		mem.atoms.clear();
		for(Iterator<Atom> it = atomlist.iterator(); it.hasNext();){
			Atom a = it.next();
			if (a.functor.isActive()) {
				mem.atoms.add(a);
				it.remove();
			}
		}
		for(Iterator<Atom> it = atomlist.iterator(); it.hasNext();){
			Atom a = it.next();
			if (!a.functor.isNumber()) {
				mem.atoms.add(a);
				it.remove();
			}
		}
		mem.atoms.addAll(atomlist);	
	}
	/** �롼��κ��դȱ��դ��Ф���staticUnify��Ƥ� */
	private void simplify() throws CompileException {
		staticUnify(rs.leftMem);
		checkExplicitFreeLinks(rs.leftMem);
		staticUnify(rs.rightMem);
		if (rs.leftMem.atoms.isEmpty() && rs.leftMem.mems.isEmpty() && !rs.fSuppressEmptyHeadWarning) {
			Env.warning("WARNING: rule with empty head: " + rs);
		}
		// ����¾ unary =/== ground �ν��֤��¤��ؤ���
		List<Atom> typeConstraints = rs.guardMem.atoms;
		LinkedList lists[] = {new LinkedList(),new LinkedList(),new LinkedList()};
		Iterator<Atom> it = typeConstraints.iterator();
		while (it.hasNext()) {
			Atom cstr = it.next();
			Functor func = cstr.functor;
			if (func.equals(new SymbolFunctor("unary",1)))  { lists[0].add(cstr); it.remove(); }
			if (func.equals(Functor.UNIFY))                 { lists[1].add(cstr); it.remove(); }
			if (func.equals(new SymbolFunctor("==",2)))     { lists[1].add(cstr); it.remove(); }
			if (func.equals(new SymbolFunctor("ground",1))) { lists[2].add(cstr); it.remove(); }
		}
		typeConstraints.addAll(lists[0]);
		typeConstraints.addAll(lists[1]);
		typeConstraints.addAll(lists[2]);

	}

	/** ���ꤵ�줿��Ȥ��λ�¹��¸�ߤ����Ĺ�� =��todo ����Ӽ�ͳ��󥯴������ȥ�ˤ����� */
	private void staticUnify(Membrane mem) throws CompileException {
		Env.c("RuleCompiler::staticUnify");
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			staticUnify(it.next());
		}
		List<Atom> removedAtoms = new ArrayList<Atom>();
		for(Atom atom : mem.atoms){
			if (atom.functor.equals(Functor.UNIFY)) {
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
		for(Atom atom : removedAtoms){
			atom.mem.atoms.remove(atom);
		}
	}

	/**
	 * ���ʤ��ץ���ʸ̮������Ū�ʰ�����Ƶ�Ū�˸������롥
	 * @param mem
	 * @throws CompileException
	 */
	private void checkExplicitFreeLinks(Membrane mem)throws CompileException {
		Env.c("RuleCompiler::checkExplicitFreeLinks");
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			checkExplicitFreeLinks(it.next());
		}
		for(ProcessContext pc : mem.processContexts){
			if(pc.def.isTyped())continue;
			HashSet<String> explicitfreelinks = new HashSet<String>();
			for (int i = 0; i < pc.args.length; i++) {
				LinkOccurrence lnk = pc.args[i];
				if (explicitfreelinks.contains(lnk.name)) {
					systemError("SYNTAX ERROR: explicit arguments of a process context in head must be pairwise disjoint: " + pc.def);
				}
				else {
					explicitfreelinks.add(lnk.name);
				}
			}
		}
	}

	/** ̿������Ŭ������ */
	private void optimize() {
		Env.c("optimize");
//		Optimizer.optimize(memMatch, body);
		if (!rs.fSuppressEmptyHeadWarning) {
			//���Υե饰��true <=> theRule�Ͻ���ǡ��������ѥ롼��
			Optimizer.optimizeRule(theRule);
		}
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
			if (atom.functor.isSymbol() ) {
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
		for(Membrane submem : mem.mems){
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

	HashMap<ProcessContext, Integer> rhsmappaths = new HashMap<ProcessContext, Integer>();	// ���դ�������$p�и�(ProcessContext) -> map���ѿ��ֹ�(Integer)
	static final int NOTCOPIED = -1;		// rhsmappaths̤��Ͽ������
	private int rhspcToMapPath(ProcessContext pc) {
		if (!rhsmappaths.containsKey(pc)) return NOTCOPIED;
		return rhsmappaths.get(pc).intValue();
	}

	//

	private boolean fUseMoveCells = true;	// ����$p���Ф���MOVECELLS��Ȥ���Ŭ�����뤫�ʳ�ȯ�������ѿ���

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int buildRHSMem(Membrane mem) throws CompileException {
		Env.c("RuleCompiler::buildRHSMem" + mem);
		int procvarcount = mem.processContexts.size();
		for(ProcessContext pc : mem.processContexts){
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
					body.add(new Instruction(Instruction.COPYCELLS,
							rethashmap, rhsmemToPath(mem), lhsmemToPath(pc.def.lhsOcc.mem) ));
					rhsmappaths.put(pc,new Integer(rethashmap));
					//else {
					//	systemError("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + pc);
					//}
				}
			}
		}
		for(Membrane submem : mem.mems){
			int submempath = varcount++;
			rhsmempath.put(submem, new Integer(submempath));
			if (submem.pragmaAtHost != null) { // ���դǡ����ꤵ��Ƥ�����
				if (submem.pragmaAtHost.def == null) {
					systemError("SYSTEM ERROR: pragmaAtHost.def is not set: " + submem.pragmaAtHost.getQualifiedName());
				}
				int nodedescatomid = typedcxtToSrcPath(submem.pragmaAtHost.def);
				body.add( new Instruction(Instruction.NEWROOT, submempath, rhsmemToPath(mem),
						nodedescatomid, submem.kind) );
			}
			else { // �̾�α�����ξ��
				body.add( Instruction.newmem(submempath, rhsmemToPath(mem), submem.kind ) );
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
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			copyRules(it.next());
		}
		for(RuleContext rc : mem.ruleContexts){
			if (rhsmemToPath(mem) == lhsmemToPath(rc.def.lhsOcc.mem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.def.lhsOcc.mem) ));
		}
	}
	/** ���դ�����Υ롼������Ƥ��������� */	
	private void loadRulesets(Membrane mem) {
		Env.c("RuleCompiler::loadRulesets");
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			loadRulesets(it.next());
		}
		for(Iterator<runtime.Ruleset> it = mem.rulesets.iterator(); it.hasNext();){
//			if (!mem.rules.isEmpty()) {
			body.add(Instruction.loadruleset(rhsmemToPath(mem), it.next()));
//			} 
		}
	}

	/** ���դ�����Υ��ȥ���������롣
	 * ñ�첽���ȥ�ʤ��UNIFY̿�����������
	 * ����ʳ��ʤ�б��դΥ��ȥ�Υꥹ��rhsatoms���ɲä��롣 */
	private void buildRHSAtoms(Membrane mem) {
		Env.c("RuleCompiler::buildRHSAtoms");
		for(Iterator<Membrane> it = mem.mems.iterator(); it.hasNext();){
			buildRHSAtoms(it.next());
		}
		for(Atom atom : mem.atoms){
			if (atom.functor.equals(Functor.UNIFY)) {
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

	/** �ץ���ʸ̮���->set���ѿ��ֹ� */
	private HashMap<ContextDef, Integer> cxtlinksetpaths = new HashMap<ContextDef, Integer>();

	/** ���ԡ�����$p�ˤĤ��ơ����Υ�󥯥��֥������Ȥؤλ��Ȥ��������
	 * ���Υꥹ�Ȥ������insertconnectors̿���ȯ�Ԥ��롣
	 * ����set���֥������Ȥؤλ��Ȥ��������줿�ѿ���Ф��Ƥ�����
	 * �ץ���ʸ̮���->set���ѿ��ֹ�
	 * �Ȥ����ޥåפ���Ͽ���롣
	 * 
	 * �ץ���ʸ̮�μ�ͳ��󥯤��¤϶ɽ��󥯤Ǥ������ɬ�פǤ���餷��
	 */
	private void insertconnectors(){
		for(ContextDef def : rs.processContexts.values()){
			if (def.rhsOccs.size() < 2) continue;
			List<Integer> linklist = new ArrayList<Integer>();
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
		for(ContextDef def : rs.typedProcessContexts.values()){
			if(gc.typedCxtTypes.get(def) != GuardCompiler.GROUND_LINK_TYPE) continue;
			List<Integer> linklist = new ArrayList<Integer>();
			int setpath = varcount++;
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
			body.add(new Instruction(Instruction.INSERTCONNECTORSINNULL,
					setpath,linklist));//,lhsmemToPath(def.lhsOcc.mem)));
			cxtlinksetpaths.put(def,new Integer(setpath));

		}
	}

	/** ��Ǻ��줿�ޥåפ�������Ƥ���set�ȡ����ȥ��ԡ����˺�ä��ޥåפ�
	 * �����ˤ��ơ�deleteconnectors̿���ȯ�Ԥ��롣
	 *
	 */
	private void deleteconnectors(){
		/*
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
		 */
		for(ContextDef def : rs.processContexts.values()){
			Iterator it2 = def.rhsOccs.iterator();
			if(def.rhsOccs.size() <2)continue;
			while (it2.hasNext()) {
				ProcessContext pc = (ProcessContext)it2.next();
				body.add(new Instruction(Instruction.DELETECONNECTORS,
						cxtlinksetpaths.get(def).intValue(),
						rhspcToMapPath(pc)));
//				rhsmemToPath(pc.mem)));
			}
		}
		/*
		it = rs.typedProcessContexts.values().iterator();
		while(it.hasNext()){
			ContextDef def = (ContextDef)it.next();
		 */
		for(ContextDef def : rs.typedProcessContexts.values()){
			if(gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE){
				Iterator it2 = def.rhsOccs.iterator();
				while(it2.hasNext()){
					ProcessContext pc = (ProcessContext)it2.next();
					body.add(new Instruction(Instruction.DELETECONNECTORS,
							cxtlinksetpaths.get(def).intValue(),
							rhspcToMapPath(pc)));
//					rhsmemToPath(pc.mem)));
				}
			}
		}
	}

	/** ��󥯤�ĥ���ؤ���������Ԥ� */
	private void updateLinks() throws CompileException {
		Env.c("RuleCompiler::updateLinks");
		if(true) {
			for(LinkOccurrence link : computeRHSLinks()){
				int linkpath = getLinkPath(link);
				int buddypath = getLinkPath(link.buddy);
				Membrane mem = link.atom.mem;
				int mempath = rhsmemToPath(mem);
				body.add(new Instruction(Instruction.UNIFYLINKS,linkpath,buddypath,mempath));
			}
		}
	}
	/** ���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ� */
	private void enqueueRHSAtoms() {
		int index = body.size(); // �����Ƶ���Ŭ���θ��̤���粽���뤿�ᡢ�ս���Ѥ�ʥ����ɤ���������
		for(Atom atom : rhsatoms){
			if (atom.functor.isSymbol() && atom.functor.isActive() ) {
				body.add(index, new Instruction(Instruction.ENQUEUEATOM, rhsatomToPath(atom)));
			}
		}
	}
  /** C������Хå���¹Ԥ���̿����������� */
	private void addCallback() {
		for(Atom atom : rhsatoms){
      if (atom.getName() == "$callback") {
        int atomID = rhsatomToPath(atom);
        body.add( new Instruction(Instruction.CALLBACK, rhsmemToPath(atom.mem), atomID));
      }
    }
	}
	/** ����饤�󥳡��ɤ�¹Ԥ���̿����������� */
	private void addInline() {
		for(Atom atom : rhsatoms){
			int atomID = rhsatomToPath(atom);
			Inline.register(unitName, atom.functor.getName());
			int codeID = Inline.getCodeID(unitName, atom.functor.getName());
			if(codeID == -1) continue;
			body.add( new Instruction(Instruction.INLINE, atomID, unitName, codeID));
		}
	}
	static final Functor FUNC_USE = new SymbolFunctor("use",1);
	/** �⥸�塼����ɤ߹��� */
	private void addRegAndLoadModules() {
		for(Atom atom : rhsatoms){
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
		Iterator<Membrane> it = mem.mems.iterator();
		while (it.hasNext()) unlockReusedOrNewRootMem(it.next());
		if (mem.pragmaAtHost != null) { // ���դǡ����ꤵ��Ƥ�����
			body.add(new Instruction(Instruction.UNLOCKMEM, rhsmemToPath(mem)));
		}
	}
	/** ���դ�����Ѵ����� */
	private void freeLHSMem(Membrane mem) {
		Env.c("RuleCompiler::freeLHSMem");
		for(Membrane submem : mem.mems){
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
		Iterator<Instruction> it;
		it = atomMatch.listIterator();
		Env.d("--atomMatches:");
		while(it.hasNext()) Env.d(it.next());

		it = memMatch.listIterator();
		Env.d("--memMatch:");
		while(it.hasNext()) Env.d(it.next());

		it = body.listIterator();
		Env.d("--body:");
		while(it.hasNext()) Env.d(it.next());
	}

	////////////////////////////////////////////////////////////////

	private void systemError(String text) throws CompileException {
		Env.error(text);
		throw new CompileException("SYSTEM ERROR");
	}
}
