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
	
	int varcount;
	
	List rhsatoms;
	/** ���դΥ��ȥ� (Atom) -> �ѿ��ֹ� (Integer) */
	Map  rhsatompath;
	/** ���դ��� (Membrane) -> �ѿ��ֹ� (Integer) */
	Map  rhsmempath;
	
	List lhsatoms;
	List lhsmems;
	/** ���դΥ��ȥ� (Atom) -> �ѿ��ֹ� (Integer) */
	Map  lhsatompath;
	/** ���դ��� (Membrane) -> �ѿ��ֹ� (Integer) */
	Map  lhsmempath;
	
//	private List newatoms = new ArrayList();	// rhsatoms��Ʊ���ʤΤ�����
	
	HeadCompiler hc;
	
	final int lhsmemToPath(Membrane mem) { return ((Integer)lhsmempath.get(mem)).intValue(); }
	final int rhsmemToPath(Membrane mem) { return ((Integer)rhsmempath.get(mem)).intValue(); }
	//final int lhsatomToID(Atom atom) { return lhsatomToPath(atom) - 1; }
	final int lhsatomToPath(Atom atom) { return ((Integer)lhsatompath.get(atom)).intValue(); } 
	final int rhsatomToPath(Atom atom) { return ((Integer)rhsatompath.get(atom)).intValue(); } 
	
	public String unitName;
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
	/** �إåɤΥޥå��󥰽�λ��η�³̿����Υ�٥� */
	private InstructionList contLabel;
	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
	 */
	public Rule compile() {
		Env.c("compile");
		liftupActiveAtoms(rs.leftMem);
		simplify();
		theRule = new Rule(rs.toString());
		
		hc = new HeadCompiler(rs.leftMem);
		hc.enumFormals(rs.leftMem);	// �إåɤ��Ф��벾�����ꥹ�Ȥ���
		
		if (!rs.typedProcessContexts.isEmpty()) {
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
		
		optimize();	// optimize if $optlevel > 0
		
		return theRule;
	}
	
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
				
				// ���ȥ��Ƴ
				InstructionList tmplabel = new InstructionList();
				tmplabel.insts = hc.match;
				atomMatch.add(new Instruction(Instruction.BRANCH, tmplabel));
				
				hc.mempaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
				Atom atom = (Atom)hc.atoms.get(firstid);
				hc.atompaths.put(atom, new Integer(1));	// ��Ƴ���륢�ȥ���ѿ��ֹ�� 1
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
					while (mem != rs.leftMem) {
						hc.match.add(new Instruction(Instruction.GETPARENT,hc.varcount,hc.varcount-1));
						hc.match.add(new Instruction(Instruction.LOCK,     hc.varcount));
						hc.mempaths.put(mem, new Integer(hc.varcount++));
						mem = mem.mem;
					}
					hc.match.add(new Instruction(Instruction.GETPARENT,hc.varcount,hc.varcount-1));
					hc.match.add(new Instruction(Instruction.EQMEM, 0, hc.varcount++));
				}
				hc.compileLinkedGroup((Atom)hc.atoms.get(firstid));
			} else {
				// ���Ƴ
				theRule.memMatchLabel = hc.matchLabel;
				memMatch = hc.match;
				hc.mempaths.put(rs.leftMem, new Integer(0));	// ������ѿ��ֹ�� 0
			}
			hc.compileMembrane(rs.leftMem);
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
	
	// TODO spec̿���¦�˻����夲���Ŭ������������
	
	/** ������򥳥�ѥ��뤹�� */
	private void compile_r() {
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
		buildRHSMem(rs.rightMem);
		if (!rs.rightMem.processContexts.isEmpty()) {
			body.add(new Instruction(Instruction.REMOVETEMPORARYPROXIES, toplevelmemid));
		}
		copyRules(rs.rightMem);
		loadRulesets(rs.rightMem);		
		buildRHSTypedProcesses();
		buildRHSAtoms(rs.rightMem);
		// ������varcount�κǽ��ͤ����ꤹ�뤳�ȤˤʤäƤ��롣�ѹ�����Ŭ�ڤ˲��˰�ư���뤳�ȡ�
		updateLinks();
		enqueueRHSAtoms();
		addInline();
		addLoadModules();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms();
		freeLHSTypedProcesses();
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
	
	
	static final Object UNARY_ATOM_TYPE  = "U"; // 1�������ȥ�
	static final Object LINEAR_ATOM_TYPE = "L"; // Ǥ�դΥץ��� $p[X|*V]
	static final Object GROUND_LINK_TYPE = "G"; // �����ץ���

	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �ǡ������μ����ɽ����åפ��줿������̿���ֹ�(Integer) */
	HashMap typedcxtdatatypes = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �ǡ������Υѥ������ɽ��������֥������� */
	HashMap typedcxttypes = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �������и����ѿ��ֹ��def.src �ϸ���̤���ѡ� */
	HashMap typedcxtsrcs  = new HashMap();
	/** ���դ��ץ���ʸ̮�α��դǤνи� (Context) -> �ѿ��ֹ� */
	HashMap rhstypedcxtpaths = new HashMap();
	/** �������и������ꤵ�줿���դ��ץ���ʸ̮����Υ��å�
	 * <p>identifiedCxtdefs.contains(x) �ϡ����դ˽и����뤫�ޤ���loaded�Ǥ��뤳�ȡ�*/
	HashSet identifiedCxtdefs = new HashSet(); 
	/** ���դ��ץ���ʸ̮����Υꥹ�ȡʲ�����ID�δ����˻��Ѥ����
	 * <p>�ºݤˤ�typedcxtsrcs�Υ������ɲä��줿���֤��¤٤���Ρ�*/
	List typedcxtdefs = new ArrayList();
		
	static final int UNBOUND = -1;
		
	int typedcxtToSrcPath(ContextDef def) {
		if (!typedcxtsrcs.containsKey(def)) return UNBOUND;
		return ((Integer)typedcxtsrcs.get(def)).intValue();
	}
	int rhstypedcxtToPath(Context cxt) {
		return ((Integer)rhstypedcxtpaths.get(cxt)).intValue();
	}
	
	static final int ISINT    = Instruction.ISINT;
	static final int ISFLOAT  = Instruction.ISFLOAT;
	static final int ISSTRING = Instruction.ISSTRING;
	static HashMap guardLibrary1 = new HashMap(); // 1���ϥ����ɷ�����̾
	static HashMap guardLibrary2 = new HashMap(); // 2���ϥ����ɷ�����̾
	static {
		guardLibrary2.put(new Functor("<.",   2), new int[]{ISFLOAT,ISFLOAT, Instruction.FLT});
		guardLibrary2.put(new Functor("=<.",  2), new int[]{ISFLOAT,ISFLOAT, Instruction.FLE});
		guardLibrary2.put(new Functor(">.",   2), new int[]{ISFLOAT,ISFLOAT, Instruction.FGT});
		guardLibrary2.put(new Functor(">=.",  2), new int[]{ISFLOAT,ISFLOAT, Instruction.FGE});
		guardLibrary2.put(new Functor("<",    2), new int[]{ISINT,  ISINT,   Instruction.ILT});
		guardLibrary2.put(new Functor("=<",   2), new int[]{ISINT,  ISINT,   Instruction.ILE});
		guardLibrary2.put(new Functor(">",    2), new int[]{ISINT,  ISINT,   Instruction.IGT});
		guardLibrary2.put(new Functor(">=",   2), new int[]{ISINT,  ISINT,   Instruction.IGE});
		guardLibrary2.put(new Functor("=:=",  2), new int[]{ISINT,  ISINT,   Instruction.IEQ});
		guardLibrary2.put(new Functor("=\\=", 2), new int[]{ISINT,  ISINT,   Instruction.INE});
		guardLibrary2.put(new Functor("=:=.", 2), new int[]{ISFLOAT,ISFLOAT, Instruction.FEQ});
		guardLibrary2.put(new Functor("=\\=.",2), new int[]{ISFLOAT,ISFLOAT, Instruction.FNE});
		guardLibrary2.put(new Functor("+.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FADD, ISFLOAT});
		guardLibrary2.put(new Functor("-.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FSUB, ISFLOAT});
		guardLibrary2.put(new Functor("*.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FMUL, ISFLOAT});
		guardLibrary2.put(new Functor("/.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FDIV, ISFLOAT});
		guardLibrary2.put(new Functor("+",    3), new int[]{ISINT,  ISINT,   Instruction.IADD, ISINT});
		guardLibrary2.put(new Functor("-",    3), new int[]{ISINT,  ISINT,   Instruction.ISUB, ISINT});
		guardLibrary2.put(new Functor("*",    3), new int[]{ISINT,  ISINT,   Instruction.IMUL, ISINT});
		guardLibrary2.put(new Functor("/",    3), new int[]{ISINT,  ISINT,   Instruction.IDIV, ISINT});
		guardLibrary2.put(new Functor("mod",  3), new int[]{ISINT,  ISINT,   Instruction.IMOD, ISINT});
		guardLibrary1.put(new Functor("int",   1), new int[]{ISINT});
		guardLibrary1.put(new Functor("string",1), new int[]{ISSTRING});
		guardLibrary1.put(new Functor("float", 1), new int[]{ISFLOAT});
		guardLibrary1.put(new Functor("float", 2), new int[]{ISINT,          Instruction.INT2FLOAT, ISFLOAT});
		guardLibrary1.put(new Functor("int",   2), new int[]{ISFLOAT,        Instruction.FLOAT2INT, ISINT});
	}	

	private void inc_head() {
		// �إåɤμ�����
		lhsatoms = hc.atoms;
		lhsmems  = hc.mems;
		genLHSPaths();
		varcount = lhsatoms.size() + lhsmems.size();
	}
	private void inc_guard() {
		// �����ɤμ�����
		varcount = lhsatoms.size() + lhsmems.size();
		// typedcxtdefs = gc.typedcxtdefs;
		genTypedProcessContextPaths();
//		varcount = lhsatoms.size() + lhsmems.size() + rs.typedProcessContexts.size();
	}
	private void genTypedProcessContextPaths() {
		Iterator it = typedcxtdefs.iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			typedcxtsrcs.put( def, new Integer(varcount++) );
		}
	}
//	public void enumTypedContextDefs() {
//		Iterator it = rs.typedProcessContexts.values().iterator();
//		while (it.hasNext()) {
//			ContextDef def = (ContextDef)it.next();
//			typedcxtdefs.add(def);
//		}
//	}
	
	/** �����ɤ򥳥�ѥ��뤹��ʲ��� */
	private void compile_g() {
		inc_head();
		if (guard == null) return;
		int formals = varcount;
		fixTypedProcesses();
		guard.add( 0, Instruction.spec(formals,varcount) );
		guard.add( Instruction.jump(theRule.bodyLabel, gc_getMemActuals(),
			gc_getAtomActuals(), gc_getVarActuals()) );
	}
	public List gc_getMemActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < lhsmems.size(); i++) {
			args.add( lhsmempath.get(lhsmems.get(i)) );
		}
		return args;
	}
	public List gc_getAtomActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < lhsatoms.size(); i++) {
			args.add( lhsatompath.get(lhsatoms.get(i)) );
		}
		Iterator it = typedcxtdefs.iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (typedcxttypes.get(def) == UNARY_ATOM_TYPE)
				args.add( new Integer(typedcxtToSrcPath(def)) );
		}
		return args;
	}		
	public List gc_getVarActuals() {
		return new ArrayList();
	}
	private void fixTypedProcesses() {
		// ���դ˽и����뷿�դ��ץ���ʸ̮�����ꤵ�줿��ΤȤ��ƥޡ������롣
		identifiedCxtdefs = new HashSet();
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.src != null) {
				if (def.src.mem == rs.guardMem) { def.src = null; } // �ƸƤӽФ����б��ʲ���
				else {
					identifiedCxtdefs.add(def);
					// ���դη��դ��ץ���ʸ̮������Ū�ʼ�ͳ��󥯤��褬�����դΥ��ȥ�˽и����뤳�Ȥ��ǧ���롣
					// �и����ʤ����ϥ���ѥ��륨�顼�Ȥ��롣�������¤�֥ѥå��ַ����¡פȸƤ֤��Ȥˤ��롣
					// ����աۥѥå��ַ����¤ϡ������󥢥��ƥ��֤ʥǡ�����ɽ�����Ȥ����ꤹ�뤳�Ȥˤ������������롣
					// �Ĥޤꡢ( 2(X) :- found(X) ) �� ( 2(3) :- ok ) ��2��3��$p��ɽ�����ȤϤǤ��ʤ���
					// �������ºݤˤϽ�����¦���Թ�ˤ�����¤Ǥ��롣
					// �ʤ����ץ���ߥ󥰤δ������顢���դη��դ��ץ���ʸ̮������Ū�ʼ�ͳ��󥯤����Ǥ�դȤ��Ƥ��롣
					if (!lhsatompath.containsKey(def.src.args[0].buddy.atom)) {
						error("COMPILE ERROR: a partner atom is required for the head occurrence of typed process context: " + def.getName());
						corrupted();
						guard.add(new Instruction(Instruction.LOCK, 0));
						return;
					}
				}
			}
		}
		// ���Ƥη��դ��ץ���ʸ̮�����ꤵ�졢�������ꤹ��ޤǷ����֤�
		LinkedList cstrs = new LinkedList();
		it = rs.guardMem.atoms.iterator();
		while (it.hasNext()) cstrs.add(it.next());
		boolean changed;
		do {
			changed = false;
			ListIterator lit = cstrs.listIterator();
			while (lit.hasNext()) {
				Atom cstr = (Atom)lit.next();
				Functor func = cstr.functor;
				ContextDef def1 = null;
				ContextDef def2 = null;
				ContextDef def3 = null;
				if (func.getArity() > 0)  def1 = ((ProcessContext)cstr.args[0].buddy.atom).def;
				if (func.getArity() > 1)  def2 = ((ProcessContext)cstr.args[1].buddy.atom).def;
				if (func.getArity() > 2)  def3 = ((ProcessContext)cstr.args[2].buddy.atom).def;

				if (func.getSymbolFunctorID().equals("unary_1")) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					guard.add(new Instruction(Instruction.ISUNARY, atomid1));
				}
				else if (func.equals(new Functor("\\=",2))) {
					// NSAMEFUNC ���뤫��
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = loadUnaryAtom(def2);
					guard.add(new Instruction(Instruction.ISUNARY, atomid1));
					guard.add(new Instruction(Instruction.ISUNARY, atomid2));
					int funcid1 = varcount++;
					int funcid2 = varcount++;
					guard.add(new Instruction(Instruction.GETFUNC, funcid1, atomid1));
					guard.add(new Instruction(Instruction.GETFUNC, funcid2, atomid2));
					guard.add(new Instruction(Instruction.NEQFUNC, funcid1, funcid2));
				}
				else if (func.getSymbolFunctorID().equals("class_2")) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = varcount++;
					guard.add(new Instruction(Instruction.GETCLASS, atomid2, atomid1));
					bindToUnaryAtom(def2, atomid2);
					typedcxtdatatypes.put(def2, new Integer(ISSTRING));
				}
				else if (func instanceof runtime.IntegerFunctor) {
					bindToFunctor(def1, func);
					typedcxtdatatypes.put(def1, new Integer(ISINT));
				}
				else if (func instanceof runtime.FloatingFunctor) {
					bindToFunctor(def1, func);
					typedcxtdatatypes.put(def1, new Integer(ISFLOAT));
				}
				else if (func instanceof runtime.StringFunctor) {
					bindToFunctor(def1, func);
					typedcxtdatatypes.put(def1, new Integer(ISSTRING));
				}
//				else if (func instanceof runtime.ObjectFunctor
//				&& ((runtime.ObjectFunctor)func).getObject() instanceof String) {
//					bindToFunctor(def1, func);
//					typedcxtdatatypes.put(def1, new Integer(ISSTRING));
//				}
				else if (func.equals(FUNC_UNIFY)) {
					if (!identifiedCxtdefs.contains(def2)) {
						ContextDef swaptmp=def1; def1=def2; def2=swaptmp;
						if (!identifiedCxtdefs.contains(def2)) continue;
					}
					int atomid2 = loadUnaryAtom(def2);
					if (!identifiedCxtdefs.contains(def1)) {
						// todo ʣ�����줿���Ȥ��������
						int funcid2 = varcount++;
						guard.add(new Instruction(Instruction.GETFUNC, funcid2, atomid2));
						int atomid1 = varcount++;
						guard.add(new Instruction(Instruction.ALLOCATOMINDIRECT, atomid1, funcid2));
						typedcxtsrcs.put(def1, new Integer(atomid1));
						typedcxtdefs.add(def1);
						identifiedCxtdefs.add(def1);
						typedcxttypes.put(def1, UNARY_ATOM_TYPE);
					}
					else bindToUnaryAtom(def1, atomid2);
					//
					Object newdatatype = typedcxtdatatypes.get(def2);
					if (newdatatype == null) newdatatype = typedcxtdatatypes.get(def1);
					typedcxtdatatypes.put(def1,newdatatype);
					typedcxtdatatypes.put(def2,newdatatype);
				}
				else if (func.equals(new Functor("==",2))) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					int atomid2 = loadUnaryAtom(def2);
					bindToUnaryAtom(def1, atomid2);
					//
					Object newdatatype = typedcxtdatatypes.get(def1);
					if (newdatatype == null) newdatatype = typedcxtdatatypes.get(def2);
					typedcxtdatatypes.put(def1,newdatatype);
					typedcxtdatatypes.put(def2,newdatatype);
				}
				else if (guardLibrary1.containsKey(func)) {
					int[] desc = (int[])guardLibrary1.get(func);
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					if (!new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						guard.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (func.getArity() == 1) {
						if (desc.length > 1) guard.add(new Instruction(desc[1], atomid1));
					}
					else {
						int atomid2 = varcount++;
						guard.add(new Instruction(desc[1], atomid2, atomid1));
						bindToUnaryAtom(def2, atomid2);
						typedcxtdatatypes.put(def2, new Integer(desc[2]));
					}
				}
				else if (guardLibrary2.containsKey(func)) {
					int[] desc = (int[])guardLibrary2.get(func);
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = loadUnaryAtom(def2);
					if (!new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						guard.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (!new Integer(desc[1]).equals(typedcxtdatatypes.get(def2))) {
						guard.add(new Instruction(desc[1], atomid2));
						typedcxtdatatypes.put(def1, new Integer(desc[1]));
					}
					if (func.getArity() == 2) {
						guard.add(new Instruction(desc[2], atomid1, atomid2));
					}
					else {
						int atomid3 = varcount++;
						guard.add(new Instruction(desc[2], atomid3, atomid1, atomid2));
						bindToUnaryAtom(def3, atomid3);
						typedcxtdatatypes.put(def3, new Integer(desc[3]));
					}
				}
				else {
					error("COMPILE ERROR: unrecognized guard type constraint name: " + cstr);
					corrupted();
					guard.add(new Instruction(Instruction.LOCK, 0));
					return;
				}
				lit.remove();
				changed = true;
			}
			if (cstrs.isEmpty()) return;
		}
		while (changed);
		// ���դ�����
		guard.add(new Instruction(Instruction.LOCK, 0));
		error("COMPILE ERROR: never proceeding guard type constraints: " + cstrs);
		corrupted();
	}
	/** ���դ��ץ���ʸ̮def��1�����ե��󥯥�func��«������ */
	private void bindToFunctor(ContextDef def, Functor func) {
		if (!identifiedCxtdefs.contains(def)) {
			identifiedCxtdefs.add(def);
			int atomid = varcount++;
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
			guard.add(new Instruction(Instruction.ALLOCATOM, atomid, func));			
		}
		else {
			int atomid = typedcxtToSrcPath(def);
			if (atomid == UNBOUND) {
				LinkOccurrence srclink = def.src.args[0].buddy; // def�Υ������и���ؤ����ȥ�¦�ΰ���
				atomid = varcount++;
				guard.add(new Instruction(Instruction.DEREFATOM,
					atomid, lhsatomToPath(srclink.atom), srclink.pos));
				typedcxtsrcs.put(def, new Integer(atomid));
				typedcxtdefs.add(def);
			}
			guard.add(new Instruction(Instruction.FUNC, atomid, func));
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
	}
	/** ���դ��ץ���ʸ̮def��1�������ȥ�$atomid�Υե��󥯥���«������ */
	private void bindToUnaryAtom(ContextDef def, int atomid) {
		if (!identifiedCxtdefs.contains(def)) {
			identifiedCxtdefs.add(def);
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
		}
		else {
			int loadedatomid = typedcxtToSrcPath(def);
			if (loadedatomid == UNBOUND) {
				LinkOccurrence srclink = def.src.args[0].buddy;
				loadedatomid = varcount++;
				guard.add(new Instruction(Instruction.DEREFATOM,
					loadedatomid, lhsatomToPath(srclink.atom), srclink.pos));
				typedcxtsrcs.put(def, new Integer(loadedatomid));
				typedcxtdefs.add(def);
			}
			guard.add(new Instruction(Instruction.SAMEFUNC, atomid, loadedatomid));
//			int funcid1 = varcount++;
//			int funcid2 = varcount++;
//			guard.add(new Instruction(Instruction.GETFUNC, funcid1, atomid));
//			guard.add(new Instruction(Instruction.GETFUNC, funcid2, loadedatomid));
//			guard.add(new Instruction(Instruction.EQFUNC,  funcid1, funcid2));
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
	}
	/** ���դ��ץ���ʸ̮def�Ρ����ꤵ��Ƥ���˥������и���
	 * ������Ū�ʼ�ͳ��󥯤��и�����˥��ȥ��������롣
	 * �ޤ������Υ��ȥब1�����Ǥ���Ȳ��ꤷ�ơ�������򹹿����롣
	 * @return �����������ȥ���ѿ��ֹ� */
	private int loadUnaryAtom(ContextDef def) {
		int atomid = typedcxtToSrcPath(def);
		if (atomid == UNBOUND) {
			LinkOccurrence srclink = def.src.args[0].buddy;
			atomid = varcount++;
			guard.add(new Instruction(Instruction.DEREFATOM,
				atomid, lhsatomToPath(srclink.atom), srclink.pos));
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
		return atomid;
	}
	
	private void removeLHSTypedProcesses() {
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Context pc = def.src;
			if (pc != null) { // �إåɤΤȤ��Τ�
				if (typedcxttypes.get(def) == UNARY_ATOM_TYPE) {
					body.add(new Instruction( Instruction.REMOVEATOM,
						typedcxtToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
			}
		}
	}	
	private void freeLHSTypedProcesses() {
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (typedcxttypes.get(def) == UNARY_ATOM_TYPE) {
				body.add(new Instruction( Instruction.FREEATOM,
					typedcxtToSrcPath(def) ));
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
				if (typedcxttypes.get(def) == UNARY_ATOM_TYPE) {
					int atompath = varcount++;
					body.add(new Instruction( Instruction.COPYATOM, atompath,
						rhsmemToPath(pc.mem),
						typedcxtToSrcPath(pc.def) ));
					rhstypedcxtpaths.put(pc, new Integer(atompath));
				}
			}
		}
	}	

	////////////////////////////////////////////////////////////////

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
						// ( X=Y :- p(X,Y) ) �ϰ�̣���ϥ��顼��=���̾�Υإåɥ��ȥ�ȸ��ʤ������֤�����
						error("COMPILE ERROR: head contains body unification");
					}
					else {
						// ( p(X,Y) :- X=Y ) ��UNIFY�ܥǥ�̿�����Ϥ���ΤǤ����Ǥϲ��⤷�ʤ�
					}
				} else {
					link1.buddy = link2;
					link2.buddy = link1;
					link2.name = link1.name;
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
		for (int i = 0; i < lhsmems.size(); i++) {
			lhsmempath.put(lhsmems.get(i), new Integer(i));
		}
		for (int i = 0; i < lhsatoms.size(); i++) {
			lhsatompath.put(lhsatoms.get(i), new Integer( lhsmems.size() + i ));
		}
		//Env.d("lhsmempaths"+lhsmempaths);
	}
	
	private void optimize() {
		Env.c("optimize");
//		Optimizer.optimize(memMatch, body);
		Optimizer.optimizeRule(theRule);
	}	
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

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int buildRHSMem(Membrane mem) {
		Env.c("RuleCompiler::buildRHSMem" + mem);
		int procvarcount = mem.processContexts.size();
		Iterator it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			if (pc.def.src.mem == null) {
				error("SYSTEM ERROR: ProcessContext.def.src.mem is not set");
			}
			if (rhsmemToPath(mem) != lhsmemToPath(pc.def.src.mem)) {
				if (pc.def.rhsOccs.get(0) == pc) {
					body.add(new Instruction(Instruction.MOVECELLS,
						rhsmemToPath(mem), lhsmemToPath(pc.def.src.mem) ));
				} 
				//else {
				//	error("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + pc);
				//	corrupted();
				//}
			}
		}
		it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			//
			Module.regMemName(submem.name, submem);
			int submempath = varcount++;
			rhsmempath.put(submem, new Integer(submempath));
			body.add( Instruction.newmem(submempath, rhsmemToPath(mem) ) );
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
			if (rhsmemToPath(mem) == lhsmemToPath(rc.def.src.mem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.def.src.mem) ));
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
				body.add( Instruction.newatom(atomid, rhsmemToPath(mem), atom.functor));
			}
		}
	}
	/** ��󥯤�ĥ���ؤ���������Ԥ�
	 * TODO �����ɤ��������� */
	private void updateLinks() {
		Env.c("RuleCompiler::updateLinks");
		
		// PART 1 - ���դΥ��ȥ�˽и�������
		Iterator it = rhsatoms.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();			
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence link = atom.args[pos].buddy;
				if (link.atom instanceof ProcessContext) {
					// ���ȥ�Υ���褬�ץ���ʸ̮/���դ��ץ���ʸ̮�ΤȤ�
					ProcessContext pc = (ProcessContext)link.atom;
					if (pc.mem.typedProcessContexts.contains(pc)) {
						// �ѥå��ַ����¤�ꡢ���դΥ��ȥ�Υ����η��դ��ץ���ʸ̮�ϱ��դ˸¤��롣
						// ( :- type($pc) | atom(X), $pc[X|] )
						if (typedcxttypes.get(pc.def) == UNARY_ATOM_TYPE) {
							body.add( Instruction.newlink(
											rhsatomToPath(atom), pos,
											rhstypedcxtToPath(pc), 0,
											rhsmemToPath(atom.mem) ));
						}
					} else { // ���դ��Ǥʤ��Ȥ�
						// ���դη��ʤ��ץ���ʸ̮�ϥȥåץ�٥��̵�������ջ�����Ȥ�ľ�ܥ�󥯤Ǥ��ʤ����ᡢ
						// �����η��ʤ��ץ���ʸ̮�ϱ��դ˸¤��롣�����ơ����Υץ���ʸ̮��
						// ���դǤνи��ˤ������б����뼫ͳ��󥯤ϡ����դΥ��ȥ����³���Ƥ��롣
						// ( { org(Y,), $pc[Y,|] } :- atom(X), $pc[X,|] )
						LinkOccurrence orglink = pc.buddy.args[link.pos].buddy; // org������Y�νи�
							body.add( new Instruction(Instruction.RELINK,
											rhsatomToPath(atom), pos,
											lhsatomToPath(orglink.atom), orglink.pos,
											rhsmemToPath(atom.mem) ));
					}
					continue;
				}
				// �����ϥ��ȥ�
				if (link.atom.mem == rs.leftMem) { // ( buddy(X) :- atom(X) )
					body.add( new Instruction(Instruction.RELINK,//LOCALRELINK�˽���ͽ��
						rhsatomToPath(atom), pos,
						lhsatomToPath(link.atom), link.pos,
						rhsmemToPath(atom.mem) ));
				} else { // ( :- buddy(X), atom(X) )
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
		// PART 2 - ���դη��դ��ץ���ʸ̮�˽и�������
		it = rhstypedcxtpaths.keySet().iterator();
		while (it.hasNext()) {
			ProcessContext atom = (ProcessContext)it.next();
			for (int pos = 0; pos < atom.functor.getArity(); pos++) {
				LinkOccurrence link = atom.args[pos].buddy;
				if (link == null) {
					error("SYSTEM ERROR: buddy of process context explicit free link is not set");
				}
				if (!(link.atom instanceof ProcessContext)) {
					// ���դ��ץ���ʸ̮�Υ���褬���ȥ�ΤȤ�
					if (lhsatoms.contains(link.atom)) { // ( buddy(X) :- type($atom) | $atom[X|] )
						if (typedcxttypes.get(atom.def) == UNARY_ATOM_TYPE) {
							body.add( new Instruction(Instruction.RELINK,
								rhstypedcxtToPath(atom), 0,
								lhsatomToPath(link.atom), link.pos,
								rhsmemToPath(atom.mem) ));
						}
					}
					else if (rhsatoms.contains(link.atom)) { // ( :- type($atom) | buddy(X), $atom[X|] )
						// PART1��newlink�ѤߤʤΤǡ����⤷�ʤ�
					}
					else {
						error("SYSTEM ERROR: unknown buddy of body typed process context");
						corrupted();
					}
					continue;
				}
				ProcessContext buddypc = (ProcessContext)link.atom;
				if (buddypc.mem.typedProcessContexts.contains(buddypc)) {
					// ����褬���դ��ץ���ʸ̮�ΤȤ����ѥå��ַ����¤�ꡢ�����ⱦ�ա�
					// ( :- type($atom),type($buddypc) | $buddypc[X|], $atom[X|] )
					if (rhstypedcxtToPath(atom) < rhstypedcxtToPath(buddypc)
					 || (rhstypedcxtToPath(atom) == rhstypedcxtToPath(buddypc) && pos < link.pos)) {
						if (typedcxttypes.get(atom.def) == UNARY_ATOM_TYPE
						 && typedcxttypes.get(buddypc.def) == UNARY_ATOM_TYPE) {
						 	body.add( new Instruction(Instruction.NEWLINK,
								rhstypedcxtToPath(atom), 0,
								rhstypedcxtToPath(buddypc), 0,
								rhsmemToPath(atom.mem) ));
						}
					}
				}
				else {
					// ����褬���դ��Ǥʤ��ץ���ʸ̮�ΤȤ���PART1��Ʊ����ͳ��$buddypc�ϱ��ա�
					// ( {org(Y,), $buddypc[Y,|]} :- type($atom) | $buddypc[X,|], $atom[X|] )
					LinkOccurrence orglink = buddypc.buddy.args[pos].buddy; // org������Y�νи�
					if (typedcxttypes.get(atom.def) == UNARY_ATOM_TYPE) {
						body.add( new Instruction(Instruction.RELINK,
												rhstypedcxtToPath(atom), 0,
												lhsatomToPath(orglink.atom), orglink.pos,
												rhsmemToPath(atom.mem) ));
					}
				}
			}
		}
		// PART 3 - ���դη��դ��Ǥʤ��ץ���ʸ̮�˽и�������
		it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext()) {
				ProcessContext atom = (ProcessContext)it2.next();
				for (int pos = 0; pos < atom.functor.getArity(); pos++) {
					LinkOccurrence link = atom.args[pos].buddy;
					if (!(link.atom instanceof ProcessContext)) {
						// ���դ��Ǥʤ��ץ���ʸ̮�Υ���褬���ȥ�ΤȤ�
						if (lhsatoms.contains(link.atom)) { // �����Ϻ��դΥȥåץ�٥�
							// ( {src(Z,),$atom[Z,|]},buddy(X) :- $atom[X,|] )
							LinkOccurrence srclink = atom.buddy.args[pos].buddy; // src������Z�νи�
							body.add( new Instruction(Instruction.LOCALUNIFY,
								lhsatomToPath(link.atom), link.pos,
								lhsatomToPath(srclink.atom), srclink.pos,
								rhsmemToPath(atom.mem) )); // ����
						}
						else if (rhsatoms.contains(link.atom)) { // ( :- buddy(X), $atom[X,|] )
							// PART1��newlink�ѤߤʤΤǡ����⤷�ʤ�
						}
						else {
							error("SYSTEM ERROR: unknown buddy of body typed process context");
							corrupted();
						}
						continue;
					}
					else {
						ProcessContext buddypc = (ProcessContext)link.atom;
						if (!buddypc.mem.typedProcessContexts.contains(buddypc)) {
							// ����褬���դ��Ǥʤ��ץ���ʸ̮�ΤȤ���PART1��Ʊ����ͳ��$buddypc�ϱ��ա�
							// ( {org(Y,),$buddypc[Y,|]},{src(Z,),$atom[Z,|]} :- $buddypc[X,|],$atom[X,|] )
							LinkOccurrence orglink = buddypc.buddy.args[link.pos].buddy; // org������Y�νи�
							LinkOccurrence srclink = atom.   buddy.args[link.pos].buddy; // src������Z�νи�
							body.add( new Instruction(Instruction.UNIFY,
														lhsatomToPath(srclink.atom), srclink.pos,
														lhsatomToPath(orglink.atom), orglink.pos,
														rhsmemToPath(atom.mem) ));
						}
					}
				}
			}
		}
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
	private void addLoadModules() {
		Iterator it = rhsatoms.iterator();
		while(it.hasNext()) {
			Atom atom = (Atom)it.next();
			if (atom.functor.equals(FUNC_USE)) {
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem),
					atom.args[0].buddy.atom.functor.getName()) );
			}
			String path = atom.getPath(); // .functor.path;
			if(path!=null && !path.equals(atom.mem.name)) {
				// ���λ����Ǥϲ��Ǥ��ʤ��⥸�塼�뤬����Τ�̾���ˤ��Ƥ���
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem), path));
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
			body.add( new Instruction(Instruction.FREEATOM, lhsmems.size() + i ));
		}
	}

	/**
	 * �ǥХå���ɽ��
	 */
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
	// ����LMNParser�Τ�Τ����礷�������餯Env�˰�ư����ͽ��
	
	public void corrupted() {
		error("SYSTEM ERROR: error recovery for the previous error is not implemented");
	}
	public void error(String text) {
		System.out.println(text);
	}
	public void warning(String text) {
		System.out.println(text);
	}
}

