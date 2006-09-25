package compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import runtime.Env;
import runtime.Functor;
import runtime.Instruction;
import runtime.SymbolFunctor;

import compile.structure.Atom;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;

public class GuardCompiler extends HeadCompiler {
	static final Object UNARY_ATOM_TYPE  = "U"; // 1�������ȥ�
	static final Object GROUND_LINK_TYPE = "G"; // �����ץ���
//	static final Object LINEAR_ATOM_TYPE = "L"; // Ǥ�դΥץ��� $p[X|*V]

	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �ǡ������μ����ɽ����åפ��줿������̿���ֹ�(Integer) */
	HashMap typedcxtdatatypes = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �ǡ������Υѥ������ɽ��������֥������� */
	HashMap typedcxttypes = new HashMap();
	/** ���դ��ץ���ʸ̮��� (ContextDef) -> �������и��ʥ��ԡ����Ȥ���и��ˤ��ѿ��ֹ� */
	HashMap typedcxtsrcs  = new HashMap();
	/** ground���դ��ץ���ʸ̮���(ContextDef) -> ��󥯤Υ������и��ʥ��ԡ����Ȥ���и��ˤΥꥹ�Ȥ��ѿ��ֹ� */
	HashMap groundsrcs = new HashMap();
	/** ��(Membrane) -> (�������¸�ߤ���ground���դ��ץ���ʸ̮���(ContextDef) -> �������ȥ��)�Ȥ����ޥå� */
	HashMap memToGroundSizes = new HashMap();
	/** �������и������ꤵ�줿���դ��ץ���ʸ̮����Υ��å�
	 * <p>identifiedCxtdefs.contains(x) �ϡ����դ˽и����뤫�ޤ���loaded�Ǥ��뤳�Ȥ�ɽ����*/
	HashSet identifiedCxtdefs = new HashSet(); 
	/** ���դ��ץ���ʸ̮����Υꥹ�ȡʲ�����ID�δ����˻��Ѥ����
	 * <p>�ºݤˤ�typedcxtsrcs�Υ������ɲä��줿���֤��¤٤���Ρ�*/
	List typedcxtdefs = new ArrayList();
	
	int typedcxtToSrcPath(ContextDef def) {
		if (!typedcxtsrcs.containsKey(def)) return UNBOUND;
		return ((Integer)typedcxtsrcs.get(def)).intValue();
	}
	
	int groundToSrcPath(ContextDef def) {
		if (!groundsrcs.containsKey(def)) return UNBOUND;
		return ((Integer)groundsrcs.get(def)).intValue();
	}
	
	static final int ISINT    = Instruction.ISINT;	// ������ΰ������������Ǥ��뤳�Ȥ�ɽ��
	static final int ISFLOAT  = Instruction.ISFLOAT;	// �� ��ư����������
	static final int ISSTRING = Instruction.ISSTRING;	// �� ʸ����
	static final int ISMEM    = Instruction.ANYMEM;	// �� ���getRuntime���ѡ�
	static HashMap guardLibrary1 = new HashMap(); // 1���ϥ����ɷ�����̾
	static HashMap guardLibrary2 = new HashMap(); // 2���ϥ����ɷ�����̾
	static {
		guardLibrary2.put(new SymbolFunctor("<.",   2), new int[]{ISFLOAT,ISFLOAT, Instruction.FLT});
		guardLibrary2.put(new SymbolFunctor("=<.",  2), new int[]{ISFLOAT,ISFLOAT, Instruction.FLE});
		guardLibrary2.put(new SymbolFunctor(">.",   2), new int[]{ISFLOAT,ISFLOAT, Instruction.FGT});
		guardLibrary2.put(new SymbolFunctor(">=.",  2), new int[]{ISFLOAT,ISFLOAT, Instruction.FGE});
		guardLibrary2.put(new SymbolFunctor("<",    2), new int[]{ISINT,  ISINT,   Instruction.ILT});
		guardLibrary2.put(new SymbolFunctor("=<",   2), new int[]{ISINT,  ISINT,   Instruction.ILE});
		guardLibrary2.put(new SymbolFunctor(">",    2), new int[]{ISINT,  ISINT,   Instruction.IGT});
		guardLibrary2.put(new SymbolFunctor(">=",   2), new int[]{ISINT,  ISINT,   Instruction.IGE});
		guardLibrary2.put(new SymbolFunctor("=:=",  2), new int[]{ISINT,  ISINT,   Instruction.IEQ});
		guardLibrary2.put(new SymbolFunctor("=\\=", 2), new int[]{ISINT,  ISINT,   Instruction.INE});
		guardLibrary2.put(new SymbolFunctor("=:=.", 2), new int[]{ISFLOAT,ISFLOAT, Instruction.FEQ});
		guardLibrary2.put(new SymbolFunctor("=\\=.",2), new int[]{ISFLOAT,ISFLOAT, Instruction.FNE});
		guardLibrary2.put(new SymbolFunctor("+.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FADD, ISFLOAT});
		guardLibrary2.put(new SymbolFunctor("-.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FSUB, ISFLOAT});
		guardLibrary2.put(new SymbolFunctor("*.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FMUL, ISFLOAT});
		guardLibrary2.put(new SymbolFunctor("/.",   3), new int[]{ISFLOAT,ISFLOAT, Instruction.FDIV, ISFLOAT});
		guardLibrary2.put(new SymbolFunctor("+",    3), new int[]{ISINT,  ISINT,   Instruction.IADD, ISINT});
		guardLibrary2.put(new SymbolFunctor("-",    3), new int[]{ISINT,  ISINT,   Instruction.ISUB, ISINT});
		guardLibrary2.put(new SymbolFunctor("*",    3), new int[]{ISINT,  ISINT,   Instruction.IMUL, ISINT});
		guardLibrary2.put(new SymbolFunctor("/",    3), new int[]{ISINT,  ISINT,   Instruction.IDIV, ISINT});
		guardLibrary2.put(new SymbolFunctor("mod",  3), new int[]{ISINT,  ISINT,   Instruction.IMOD, ISINT});	
		guardLibrary1.put(new SymbolFunctor("int",   1), new int[]{ISINT});
		guardLibrary1.put(new SymbolFunctor("float", 1), new int[]{ISFLOAT});
		guardLibrary1.put(new SymbolFunctor("+",     2), new int[]{ISINT,          -1,                    ISINT});
		guardLibrary1.put(new SymbolFunctor("-",     2), new int[]{ISINT,          Instruction.INEG,      ISINT});
		guardLibrary1.put(new SymbolFunctor("+.",    2), new int[]{ISFLOAT,        -1,                    ISFLOAT});
		guardLibrary1.put(new SymbolFunctor("-.",    2), new int[]{ISFLOAT,        Instruction.FNEG,      ISFLOAT});
		guardLibrary1.put(new SymbolFunctor("float", 2), new int[]{ISINT,          Instruction.INT2FLOAT, ISFLOAT});
		guardLibrary1.put(new SymbolFunctor("int",   2), new int[]{ISFLOAT,        Instruction.FLOAT2INT, ISINT});
	}
	
	//
	RuleCompiler rc;			// rc.rs��
	List typeConstraints;		// ������Υꥹ��
	Map  typedProcessContexts;	// ���դ��ץ���ʸ̮̾��������ؤΥޥå�
	
	GuardCompiler(RuleCompiler rc, HeadCompiler hc) {
		super();
		this.rc = rc;
		this.initNormalizedCompiler(hc);
		match = rc.guard;
		typeConstraints      = rc.rs.guardMem.atoms;
		typedProcessContexts = rc.rs.typedProcessContexts;
		
		guardLibrary1.put(new SymbolFunctor("string",1), new int[]{ISSTRING});
//		guardLibrary2.put(new SymbolFunctor("class",2), new int[]{0,      ISSTRING,Instruction.INSTANCEOF});
//		guardLibrary1.put(new SymbolFunctor("class", 2), new int[]{0,              Instruction.GETCLASS,  ISSTRING});
		guardLibrary1.put(new SymbolFunctor("connectRuntime",1), new int[]{ISSTRING, Instruction.CONNECTRUNTIME});
	}

	/** initNormalizedCompiler�ƤӽФ���˸ƤФ�롣
	 * ���մط����դ�$p���Ф��ơ��������Ѥβ������ֹ��
	 * �ѿ��ֹ�Ȥ��ƺ��մط����դ�$p�Υޥå��󥰤��꽪��ä��������֤���Ĥ褦�ˤ��롣*/
	final void initNormalizedGuardCompiler(GuardCompiler gc) {
		identifiedCxtdefs = (HashSet)gc.identifiedCxtdefs.clone();
		typedcxtdatatypes = (HashMap)gc.typedcxtdatatypes.clone();
		typedcxtdefs = (ArrayList)((ArrayList)gc.typedcxtdefs).clone();
		typedcxtsrcs = (HashMap)gc.typedcxtsrcs.clone();
		typedcxttypes = (HashMap)gc.typedcxttypes.clone();
		varcount = gc.varcount;	// ��ʣ
	}
	
	/** ������������Υ���ѥ���ǻȤ������this���Ф������������줿GuardCompiler����������֤���
	 * �������Ȥϡ����դ����ƤΥ��ȥ�/�줪��Ӻ��մط����դ�$p���Ф��ơ�������/�ܥǥ��Ѥβ������ֹ��
	 * �ѿ��ֹ�Ȥ��ƺ��դȺ��մط�������Υޥå��󥰤��꽪��ä��������֤���Ĥ褦�ˤ��뤳�Ȥ��̣���롣*/
	final GuardCompiler getNormalizedGuardCompiler() {
		GuardCompiler gc = new GuardCompiler(rc,this);
		gc.initNormalizedGuardCompiler(this);
		return gc;
	}
	//

	/**
	 * �ץ���ʸ̮�Τʤ����stable����θ�����Ԥ���
	 * RISC����ȼ�����إåɥ���ѥ��餫���ư���Ƥ����� by mizuno
	 */
	void checkMembraneStatus() {
		// �ץ���ʸ̮���ʤ��Ȥ��ϡ����ȥ�Ȼ���θĿ����ޥå����뤳�Ȥ��ǧ����
		for (int i = 0; i < mems.size(); i++) {
			Membrane mem = (Membrane)mems.get(i);
			int mempath = memToPath(mem);
			if (mempath == 0) continue; //������Ф��Ƥϲ��⤷�ʤ�
			if (mem.processContexts.isEmpty()) {
				countAtomsOfMembrane(mem);
				match.add(new Instruction(Instruction.NMEMS,  mempath, mem.mems.size()));
			}
			//
			if (mem.ruleContexts.isEmpty()) {
				match.add(new Instruction(Instruction.NORULES, mempath));
			}
			if (mem.stable) {
				match.add(new Instruction(Instruction.STABLE, mempath));
			}
		}
	}
	/** �������Ϥ��줿���ȥ�Υ�󥯤��Ф���getlink��Ԥ����ѿ��ֹ����Ͽ���롣(RISC��)
	 * <strike>����Ū�ˤϥ�󥯥��֥������Ȥ򥬡���̿����ΰ������Ϥ��褦�ˤ��뤫���Τ�ʤ���</strike>
	 * ����Ū�ˤϥ�����̿����ϥإå�̿����˥���饤��Ÿ�������ͽ��ʤΤǡ�
	 * ���Υ᥽�åɤ����������getlink�Ͼ�Ĺ̿��ν���ˤ��ä��븫���ߡ�*/
	void getLHSLinks() {
		for (int i = 0; i < atoms.size(); i++) {
			Atom atom = (Atom)atoms.get(i);
			int atompath = atomToPath(atom);
			int arity = atom.getArity();
			int[] paths = new int[arity];
			for (int j = 0; j < arity; j++) {
				paths[j] = varcount;
				match.add(new Instruction(Instruction.GETLINK, varcount, atompath, j));
				varcount++;
			}
			linkpaths.put(new Integer(atompath), paths);
		}
	}
	
	/** ���դ��ץ���ʸ̮��ɽ���ץ������դ˷��ꤹ�롣*/
	void fixTypedProcesses() throws CompileException {
		// STEP 1 - ���դ˽и����뷿�դ��ץ���ʸ̮�����ꤵ�줿��ΤȤ��ƥޡ������롣
		identifiedCxtdefs = new HashSet();
		Iterator it = typedProcessContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.lhsOcc != null) {
				identifiedCxtdefs.add(def);
				// ���դη��դ��ץ���ʸ̮������Ū�ʼ�ͳ��󥯤��褬�����դΥ��ȥ�˽и����뤳�Ȥ��ǧ���롣
				// �и����ʤ����ϥ���ѥ��륨�顼�Ȥ��롣�������¤�֥ѥå��ַ����¡פȸƤ֤��Ȥˤ��롣
				// ����աۥѥå��ַ����¤ϡ������󥢥��ƥ��֤ʥǡ�����ɽ�����Ȥ����ꤹ�뤳�Ȥˤ������������롣
				// �Ĥޤꡢ( 2(X) :- found(X) ) �� ( 2(3) :- ok ) ��2��3��$p��ɽ�����ȤϤǤ��ʤ���
				// �������ºݤˤϽ�����¦���Թ�ˤ�����¤Ǥ��롣
				// �ʤ����ץ���ߥ󥰤δ������顢���դη��դ��ץ���ʸ̮������Ū�ʼ�ͳ��󥯤����Ǥ�դȤ��Ƥ��롣
				//
				// ( 2006/09/13 kudo ) 2�����ʾ�η��դ��ץ���ʸ̮��Ƴ����ȼ�������Ƥΰ���������å�����褦�ˤ���
				for(int i=0;i<def.lhsOcc.args.length;i++){
					if (!atompaths.containsKey(def.lhsOcc.args[i].buddy.atom)) {
						error("COMPILE ERROR: a partner atom is required for the head occurrence of typed process context: " + def.getName());
					}
				}
			}
			else if (def.lhsMem != null) {
				if (def.lhsMem.pragmaAtHost.def == def) {
					// ���դΡ����������������
					identifiedCxtdefs.add(def);
					int atomid = varcount++;
					match.add(new Instruction(Instruction.GETRUNTIME, atomid, memToPath(def.lhsMem)));
					typedcxtsrcs.put(def, new Integer(atomid));
					typedcxtdefs.add(def);
					typedcxttypes.put(def, UNARY_ATOM_TYPE);
					typedcxtdatatypes.put(def, new Integer(ISSTRING));
				}
			}
		}
		// STEP 2 - ���Ƥη��դ��ץ���ʸ̮�����ꤵ�졢�������ꤹ��ޤǷ����֤�		
		LinkedList cstrs = new LinkedList();
		it = typeConstraints.iterator();
		while (it.hasNext()) cstrs.add(it.next());
		
		{
			// uniq, not_uniq ��ǽ�ˡʾ��ʤ��Ȥ�int, unary �ʤɤ����ˡ˽�������
			Iterator it0 = cstrs.iterator();
			LinkedList tmpFirst = new LinkedList();
			LinkedList tmpLast = new LinkedList();
			while(it0.hasNext()) {
				Atom a = (Atom)it0.next();
				if(a.functor.getName().endsWith("uniq") || a.functor.getName().equals("custom")) {
					tmpFirst.add(a);
					it0.remove();
				}
				if(a.functor.getName().startsWith("custom")) {
					tmpLast.add(a);
					it0.remove();
				}
			}
			tmpFirst.addAll(cstrs);
			tmpFirst.addAll(tmpLast);
			cstrs = tmpFirst;
		}
		
		boolean changed;
		do {
			changed = false;
			ListIterator lit = cstrs.listIterator();
			FixType:
			while (lit.hasNext()) {
				Atom cstr = (Atom)lit.next();
				Functor func = cstr.functor;
				ContextDef def1 = null;
				ContextDef def2 = null;
				ContextDef def3 = null;
				if (func.getArity() > 0)  def1 = ((ProcessContext)cstr.args[0].buddy.atom).def;
				if (func.getArity() > 1)  def2 = ((ProcessContext)cstr.args[1].buddy.atom).def;
				if (func.getArity() > 2)  def3 = ((ProcessContext)cstr.args[2].buddy.atom).def;

				if (func.equals(new SymbolFunctor("unary", 1))) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					match.add(new Instruction(Instruction.ISUNARY, atomid1));
				}
				else if (func.equals(new SymbolFunctor("ground", 1))){
					if (!identifiedCxtdefs.contains(def1)) continue;
					checkGroundLink(def1);
				}
				// �����ɥ���饤��
				else if (func.getName().startsWith("custom_")) {
					boolean hasError=false;
					if(func.getName().length()<7+func.getArity()+1) hasError=true;
					boolean[] isIn = new boolean[func.getArity()];
					if(func.getName().charAt(7+isIn.length)!='_') hasError=true;
					for(int i=0;i<isIn.length;i++) {
						char ch = func.getName().charAt(7+i);
						if(ch!='i' && ch!='o') hasError=true;
						isIn[i] = ch=='i';
					}
					if(hasError) {
						String mo = "";
						for(int i=0;i<isIn.length;i++) mo += "?";
						error("Guard "+func.getName()+" should be custom_"+mo+"_xxxx. (? : 'i' when input, 'o' when output)");
					}
					
					String guardID = func.getName().substring(7+func.getArity()+1);
					ArrayList vars = new ArrayList();
					ArrayList out = new ArrayList(); // ���ϰ���
					for(int k=0;k<cstr.args.length;k++) {
						ContextDef defK = ((ProcessContext)cstr.args[k].buddy.atom).def;
						// ���ϰ�����̤«���ʤ���
						if (isIn[k] && !identifiedCxtdefs.contains(defK)) {
							continue FixType;
						}
						int aid;
						if(identifiedCxtdefs.contains(defK)) {
							aid = typedcxtToSrcPath(defK);
							if(aid==UNBOUND) {
								checkGroundLink(defK);
								aid = groundToSrcPath(defK);
							}
//							aid = loadUnaryAtom(defK);
						} else {
							int atomid = varcount++;
							bindToUnaryAtom(defK, atomid);
							typedcxtdatatypes.put(def3, new Integer(ISINT));
							aid = typedcxtToSrcPath(defK);
							out.add(new Integer(aid));
						}
						vars.add(new Integer(aid));
//						vars.add(new Integer(loadUnaryAtom(def1)));
//						System.out.println("varcount "+varcount);
//						System.out.println("1 "+typedcxtdatatypes);
//						System.out.println("1 "+typedcxtdefs);
//						System.out.println("1 "+typedcxtsrcs);
//						System.out.println("1 "+typedcxttypes);
					}
//					System.out.println("vars "+vars);
					match.add(new Instruction(Instruction.GUARD_INLINE, guardID, vars, out));
				}
				else if (func.getName().equals("uniq") || func.getName().equals("not_uniq")){
					ArrayList uniqVars = new ArrayList();
					for(int k=0;k<cstr.args.length;k++) {
						ContextDef defK = ((ProcessContext)cstr.args[k].buddy.atom).def;
						if (!identifiedCxtdefs.contains(defK)) continue FixType; // ̤������ƤΥץ�����ɽ����-���դ���Ρˤ�ǧ��ʤ�
						int srcPath;
//						srcPath = typedcxtToSrcPath(defK);
//						Env.p("VAR# "+srcPath);
//						if(srcPath==UNBOUND) {
							checkGroundLink(defK);
							srcPath = groundToSrcPath(defK);
//						}
//						Env.p("VAR## "+srcPath);
						if(srcPath==UNBOUND) continue FixType;
						uniqVars.add(new Integer(srcPath));
					}
					if(func.getName().equals("uniq")) {
						match.add(new Instruction(Instruction.UNIQ, uniqVars));
					} else {
						match.add(new Instruction(Instruction.NOT_UNIQ, uniqVars));
					}
				}
				else if (func.equals(new SymbolFunctor("\\=",2))) {
					// NSAMEFUNC ���뤫��
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					
					//ground������ˤ�����(2006-02-18 by kudo)
					// .. :- unary(A),A\=B | ..
					//�ξ�硢B��ground�ǹ���ʤ���A��unary�ǡ�����B���ۤʤ빽¤�λ���ȿ�����롣
					//�������ϡ�==�Ȥϰ㤦��==�ξ�硢���⤽��Ʊ�����Ǥʤ���Хޥå����ʤ����ᡣ
					//B��unary�λ��˸��ꤷ������С�unary(B)��񤭲ä���Ф褤��
					//(����ͤ����˼��������餽���ʤä��Τ��������Ū�˰��ֽ����ľ��Ū�ʷ����Ȼפ���)
					if(!GROUND_ALLOWED ||
							typedcxttypes.get(def1) == UNARY_ATOM_TYPE ||
							typedcxttypes.get(def2) == UNARY_ATOM_TYPE){
						int atomid1 = loadUnaryAtom(def1);
						int atomid2 = loadUnaryAtom(def2);
						match.add(new Instruction(Instruction.ISUNARY, atomid1));
						match.add(new Instruction(Instruction.ISUNARY, atomid2));
						int funcid1 = varcount++;
						int funcid2 = varcount++;
						match.add(new Instruction(Instruction.GETFUNC, funcid1, atomid1));
						match.add(new Instruction(Instruction.GETFUNC, funcid2, atomid2));
						match.add(new Instruction(Instruction.NEQFUNC, funcid1, funcid2));
					}
					else{
						checkGroundLink(def1);
						checkGroundLink(def2);
						int linkid1 = loadGroundLink(def1);
						int linkid2 = loadGroundLink(def2);
//						match.add(new Instruction(Instruction.ISGROUND,linkid1));
//						match.add(new Instruction(Instruction.ISGROUND,linkid2));
						match.add(new Instruction(Instruction.NEQGROUND,linkid1,linkid2));
					}
				}
				// (2006.07.06 n-kato)
				else if (func.equals(new SymbolFunctor("class", 2))) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = loadUnaryAtom(def2);
					if (!new Integer(ISSTRING).equals(typedcxtdatatypes.get(def2))) {
						match.add(new Instruction(ISSTRING, atomid2));
						typedcxtdatatypes.put(def2, new Integer(ISSTRING));
					}
					// todo: Instruction.INSTANCEOF
					int classnameAtomid = varcount++;
					match.add(new Instruction(Instruction.GETCLASS, classnameAtomid, atomid1));
					match.add(new Instruction(Instruction.SUBCLASS, classnameAtomid, atomid2));
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
				else if (cstr.isSelfEvaluated && func.getArity() == 1) {
					bindToFunctor(def1, func);
					// typedcxtdatatypes.put(def1, new Integer(Instruction.ISUNARY));
				}
				else if (func.equals(Functor.UNIFY)) { // (-X = +Y)
					if (!identifiedCxtdefs.contains(def2)) { // (+X = -Y) �� (-Y = +X) �Ȥ��ƽ�������
						ContextDef swaptmp=def1; def1=def2; def2=swaptmp;
						if (!identifiedCxtdefs.contains(def2)) continue;
					}
					// ̤�����def1 = ground��def2 �ϵ�����ʤ�
					if(GROUND_ALLOWED && typedcxttypes.get(def2) != UNARY_ATOM_TYPE){
						if (!identifiedCxtdefs.contains(def1)) continue;
					}
					processEquivalenceConstraint(def1,def2);
				}
				else if (func.equals(new SymbolFunctor("==",2))) { // (+X == +Y)
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					processEquivalenceConstraint(def1,def2);
				}
				else if (guardLibrary1.containsKey(func)) { // 1��������
					int[] desc = (int[])guardLibrary1.get(func);
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					if (desc[0] != 0 && !new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						match.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (func.getArity() == 1) { // {t1,inst} --> p(+X1)
						// 060831okabe
						// �ʲ��򥳥��ȥ����ȡ�
						// �Ĥޤ�connectruntime ��put ����get ����������
						// TODO ��ä�connectruntime �Ϥ���ʤ��Τǲ��Ȥ����롥�ʥ饤�֥���Ȥä�ʬ������Ȥ��ޤ����֤Ǥ褤��
//						if (desc.length > 1) match.add(new Instruction(desc[1], atomid1));
					}
					else { // {t1,inst,t2} --> p(+X1,-X2)
						int atomid2;
						if (desc[1] == -1) { // ñ�� + �� +. �������̰��� 
							atomid2 = atomid1;
							//bindToUnaryAtom ��ǡ��ºݤ˻Ȥ����ȥ���������Ƥ��롣
						} else {
							atomid2 = varcount++;
							match.add(new Instruction(desc[1], atomid2, atomid1));
						}
						// 2006.07.06 n-kato //2006.07.01 by inui
						// if (func.equals(classFunctor)) bindToUnaryAtom(def2, atomid2, Instruction.SUBCLASS);
						// else
						bindToUnaryAtom(def2, atomid2);
						typedcxtdatatypes.put(def2, new Integer(desc[2]));
					}
				}
				else if (guardLibrary2.containsKey(func)) { // 2��������
					int[] desc = (int[])guardLibrary2.get(func);
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = loadUnaryAtom(def2);
					if (desc[0] != 0 && !new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						match.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (desc[1] != 0 && !new Integer(desc[1]).equals(typedcxtdatatypes.get(def2))) {
						match.add(new Instruction(desc[1], atomid2));
						typedcxtdatatypes.put(def2, new Integer(desc[1]));
					}
					if (func.getArity() == 2) { // {t1,t2,inst} --> p(+X1,+X2)
						match.add(new Instruction(desc[2], atomid1, atomid2));
					}
					else { // desc={t1,t2,inst,t3} --> p(+X1,+X2,-X3)
						int atomid3 = varcount++;
						match.add(new Instruction(desc[2], atomid3, atomid1, atomid2));
						bindToUnaryAtom(def3, atomid3);
						typedcxtdatatypes.put(def3, new Integer(desc[3]));
					}
				}
				else {
					error("COMPILE ERROR: unrecognized type constraint: " + cstr);
					discardTypeConstraint(cstr); // �����ˤ���ʤ�
				}
				lit.remove();
				changed = true;
			}
			if (cstrs.isEmpty()) return;
		}
		while (changed);
		// STEP 3 - ���դ�����
		ListIterator lit = cstrs.listIterator();
		String text = "";
		while (lit.hasNext()) {
			Atom cstr = (Atom)lit.next();
			discardTypeConstraint(cstr);
			if (text.length() > 0)  text += ", ";
			text += cstr.toStringAsTypeConstraint();
		}
		error("COMPILE ERROR: never proceeding type constraint: " + text);
	}
	boolean GROUND_ALLOWED = true;
	/** ���� X=Y �ޤ��� X==Y ��������롣������def2�����ꤵ��Ƥ��ʤ���Фʤ�ʤ���*/
	private void processEquivalenceConstraint(ContextDef def1, ContextDef def2) throws CompileException{
		boolean checkNeeded = (typedcxttypes.get(def1) == null
							 && typedcxttypes.get(def2) == null); // ���դ��Ǥ��뤳�Ȥθ�����ɬ�פ��ɤ���
		//boolean GROUND_ALLOWED = true;
		// GROUND_ALLOWED �ΤȤ� (unary = ?) �� (? = unary) �Ȥ��ƽ�������ʤ�����?��ground�ޤ���null��
		if (GROUND_ALLOWED && typedcxttypes.get(def2) != UNARY_ATOM_TYPE) {
			if (typedcxttypes.get(def1) == UNARY_ATOM_TYPE) {
				ContextDef swaptmp=def1; def1=def2; def2=swaptmp;
			}
		}
		if (GROUND_ALLOWED && typedcxttypes.get(def2) != UNARY_ATOM_TYPE) { // (? = ground)
			//if(checkNeeded){
				checkGroundLink(def1);
				checkGroundLink(def2);
			//}
			int linkid1 = loadGroundLink(def1);
			int linkid2 = loadGroundLink(def2);
			
			/** ground�ˤĤ��Ƥϡ�(̤�����$p1)=(����Ѥ�$p2)�Ȥ������ϵ�����ʤ���ΤȤ��롣fix..�ǤϤ��� */
			match.add(new Instruction(Instruction.EQGROUND,linkid1,linkid2));
		}
		else {
			int atomid2 = loadUnaryAtom(def2);
			if (checkNeeded) match.add(new Instruction(Instruction.ISUNARY, atomid2));
			if (!identifiedCxtdefs.contains(def1)) { // (̤�����$p1)=(����Ѥ�$p2) 
				// todo Ʊ���ѿ���ͭ���������褤���Ǥ��뤫��
				int funcid2 = varcount++;
				match.add(new Instruction(Instruction.GETFUNC, funcid2, atomid2));
				int atomid1 = varcount++;
				match.add(new Instruction(Instruction.ALLOCATOMINDIRECT, atomid1, funcid2));
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
	}
	
	/** ��������Ѵ����롣���顼�����ѥ᥽�å� */
	private void discardTypeConstraint(Atom cstr) throws CompileException{
		match.add(Instruction.fail());
		for (int i = 0; i < cstr.functor.getArity(); i++) {
			ContextDef def = ((ProcessContext)cstr.args[i].buddy.atom).def;
			bindToFunctor(def,new SymbolFunctor("*",1));
		}
	}
	/** ���դ��ץ���ʸ̮def��1�����ե��󥯥�func��«������ */
	private void bindToFunctor(ContextDef def, Functor func) throws CompileException{
		if (!identifiedCxtdefs.contains(def)) {
			identifiedCxtdefs.add(def);
			int atomid = varcount++;
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
			match.add(new Instruction(Instruction.ALLOCATOM, atomid, func));			
		}
		else {
			checkUnaryProcessContext(def);
			int atomid = typedcxtToSrcPath(def);
			if (atomid == UNBOUND) {
				LinkOccurrence srclink = def.lhsOcc.args[0].buddy; // def�Υ������и���ؤ����ȥ�¦�ΰ���
				atomid = varcount++;
				match.add(new Instruction(Instruction.DEREFATOM,
					atomid, atomToPath(srclink.atom), srclink.pos));
				typedcxtsrcs.put(def, new Integer(atomid));
				typedcxtdefs.add(def);
				match.add(new Instruction(Instruction.FUNC, atomid, func));
				getLinks(atomid, 1);
			} else {
				match.add(new Instruction(Instruction.FUNC, atomid, func));
			}
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
	}
	/** ���դ��ץ���ʸ̮def��1�������ȥ�$atomid�Υե��󥯥���«������ */
	//2006.07.01 «������̿��(?) bindid �������ɲ� by inui
	private void bindToUnaryAtom(ContextDef def, int atomid) {
		if (!identifiedCxtdefs.contains(def)) {
			identifiedCxtdefs.add(def);
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
		}
		else {
			int loadedatomid = typedcxtToSrcPath(def);
			if (loadedatomid == UNBOUND) {
				LinkOccurrence srclink = def.lhsOcc.args[0].buddy;
				loadedatomid = varcount++;
				match.add(new Instruction(Instruction.DEREFATOM,
					loadedatomid, atomToPath(srclink.atom), srclink.pos));
				typedcxtsrcs.put(def, new Integer(loadedatomid));
				typedcxtdefs.add(def);
				match.add(new Instruction(Instruction.SAMEFUNC, atomid, loadedatomid));
				getLinks(loadedatomid, 1);
			} else {
				match.add(new Instruction(Instruction.SAMEFUNC, atomid, loadedatomid));
			}
//			int funcid1 = varcount++;
//			int funcid2 = varcount++;
//			match.add(new Instruction(Instruction.GETFUNC, funcid1, atomid));
//			match.add(new Instruction(Instruction.GETFUNC, funcid2, loadedatomid));
//			match.add(new Instruction(Instruction.EQFUNC,  funcid1, funcid2));
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
	}
	/** ���դ��ץ���ʸ̮def�Ρ����ꤵ��Ƥ���˥������и���
	 * ������Ū�ʼ�ͳ��󥯤��и�����˥��ȥ��������롣
	 * �ޤ������Υ��ȥब1�����Ǥ���Ȳ��ꤷ�ơ�������򹹿����롣
	 * @return �����������ȥ���ѿ��ֹ� */
	private int loadUnaryAtom(ContextDef def) throws CompileException{
		int atomid = typedcxtToSrcPath(def);
		if (atomid == UNBOUND) {
			checkUnaryProcessContext(def);
			LinkOccurrence srclink = def.lhsOcc.args[0].buddy;
			atomid = varcount++;
			match.add(new Instruction(Instruction.DEREFATOM,
				atomid, atomToPath(srclink.atom), srclink.pos));
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
			getLinks(atomid, 1);
		}
		typedcxttypes.put(def, UNARY_ATOM_TYPE);
		return atomid;
	}	
	
	/** ���եץ���ʸ̮def�Ρ����ꤵ��Ƥ���˥������и��Υ�󥯤�������롣
	 *  ������ꥹ���ѿ��˳�Ǽ���롥
	 *  �����ѿ��ֹ��groundsrcs���ɲä��롣
	 *  ������򹹿����롣
	 *  @param def �ץ���ʸ̮���
	 *  @return ��󥯥ꥹ�Ȥ��ѿ��ֹ� */
	private int loadGroundLink(ContextDef def) {
//		ArrayList linkids = groundToSrcPath(def);
		int linkids = groundToSrcPath(def);
		if( linkids == UNBOUND ){
			linkids = varcount++;
			match.add(new Instruction(Instruction.NEWLIST,linkids));
			for(int i=0;i<def.lhsOcc.args.length;i++){
				int[] paths = (int[])linkpaths.get(new Integer(atomToPath(def.lhsOcc.args[i].buddy.atom)));
				//linkids[i] = paths[def.lhsOcc.args[i].buddy.pos];
//				linkids.set(i,new Integer(paths[def.lhsOcc.args[i].buddy.pos]));
//				groundsrcs.put(def,new Integer(linkids));
				match.add(new Instruction(Instruction.ADDTOLIST,linkids, paths[def.lhsOcc.args[i].buddy.pos]));
			}
			groundsrcs.put(def,new Integer(linkids));
		}
		return linkids;
	}
	
	//���դ���(Membrane) -> ������Υ��ȥब���ä�set��ؤ��ѿ��ֹ�(Integer)
//	HashMap memToAtomSetPath = new HashMap();

	//���դ���(Membrane) -> ������Υ��ȥ������Ū�ʼ�ͳ��󥯤����ä�list��ؤ��ѿ��ֹ�(Integer)
//	HashMap memToLinkListPath = new HashMap();
	
	/** ���եץ���ʸ̮def���������ץ������ɤ����������롣
	 *  @param def �ץ���ʸ̮��� */
	private void checkGroundLink(ContextDef def) {
		if(typedcxttypes.get(def) != UNARY_ATOM_TYPE && typedcxttypes.get(def) != GROUND_LINK_TYPE){
			typedcxttypes.put(def,GROUND_LINK_TYPE);
//			int linkid = loadGroundLink(def);
//			ArrayList linkids = loadGroundLink(def);
			int linkids = loadGroundLink(def);
			int srclinklistpath;
//			if(!memToLinkListPath.containsKey(def.lhsOcc.mem)){
				srclinklistpath = varcount++;
				// �򤱤��󥯤Υꥹ��
				match.add(new Instruction(Instruction.NEWLIST,srclinklistpath));
				
				// ���սи����ȥ�Ρ����Ƥΰ���(��ؤ����)�Τ���,
				// ���դμ�ͳ��󥯤⤷����Ʊ����Υץ���ʸ̮����³���Ƥ���
				// ���Υץ���ʸ̮�κ��Ǥʤ���Τ�ꥹ�Ȥ��ɲä���
				Iterator it = def.lhsOcc.mem.atoms.iterator();
				while(it.hasNext()){
					Atom atom = (Atom)it.next();
					int[] paths = (int[])linkpaths.get(new Integer(atomToPath(atom)));
					for(int i=0;i<atom.args.length;i++){
//						match.add(new Instruction(Instruction.ADDATOMTOSET,srcsetpath,atomToPath((Atom)it.next())));
						if(def.lhsOcc.mem.parent == null){ // ���սи����롼��ǳ���
							if( atom.args[i].buddy.atom.mem!=rc.rs.rightMem)
								// ȿ��¦�����սи��λ��Τ��ɲ�
								if(!def.lhsOcc.mem.typedProcessContexts.contains(atom.args[i].buddy.atom))
									continue;
						}else{ // ���սи�������
							if(!def.lhsOcc.mem.processContexts.contains(atom.args[i].buddy.atom))  // ȿ��¦���ץ���ʸ̮�ΰ����λ��Τ��ɲ�
								if(!def.lhsOcc.mem.typedProcessContexts.contains(atom.args[i].buddy.atom))
									continue;
						}
						boolean flgNotAdd = false; // ���ΰ������򤱤�٤��ꥹ�Ȥˡֲä��ʤ��׾��true
						for(int j=0;j<def.lhsOcc.args.length;j++){
							LinkOccurrence ro = def.lhsOcc.args[j].buddy;
							if(ro == atom.args[i])
								flgNotAdd = true;
						}
						if(!flgNotAdd)
							match.add(new Instruction(Instruction.ADDTOLIST,srclinklistpath,paths[i]));
					}
				}
//				memToLinkListPath.put(def.lhsOcc.mem,new Integer(srclinklistpath));
//			}
//			else srclinklistpath = ((Integer)memToLinkListPath.get(def.lhsOcc.mem)).intValue();
			int natom = varcount++;
			match.add(new Instruction(Instruction.ISGROUND, natom, linkids, srclinklistpath));//,memToPath(def.lhsOcc.mem)));
			if(!memToGroundSizes.containsKey(def.lhsOcc.mem))memToGroundSizes.put(def.lhsOcc.mem,new HashMap());
			((Map)memToGroundSizes.get(def.lhsOcc.mem)).put(def,new Integer(natom));
		}
		return;
	}

	/**
	 * unary�������󤵤줿�ץ���ʸ̮��1�����Ǥ��뤳�Ȥ��ǧ���롥
	 * @param def
	 * @throws CompileException
	 */
	private void checkUnaryProcessContext(ContextDef def) throws CompileException{
		if(def.lhsOcc.args.length!=1)	
			error("COMPILE ERROR: unary type process context must has exactly one argument : " + def.lhsOcc);
	}
	
	
	/**
	 * ����Υ��ȥ��������롣$p��̵�����Ȥ���
	 * ground,unary�ˤĤ��Ƥ⤭����ȹͤ��롣
	 * 
	 * @param mem ��������å�������
	 */
	private void countAtomsOfMembrane(Membrane mem){
		if(!memToGroundSizes.containsKey(mem)){ // ������RISC������ʤ顢natoms��ʬ����٤���
			match.add(new Instruction(Instruction.NATOMS, memToPath(mem),
				mem.getNormalAtomCount() + mem.typedProcessContexts.size() ));
		}else{
			Map gmap = (Map)memToGroundSizes.get(mem);
			//���̤Υ��ȥ�θĿ��ȡ�unary�θĿ�
			int ausize = mem.getNormalAtomCount() + mem.typedProcessContexts.size() - gmap.size();
			int ausfunc = varcount++;
			match.add(new Instruction(Instruction.LOADFUNC,ausfunc,new runtime.IntegerFunctor(ausize)));
			//��ground�ˤĤ��ơ�isground̿�����äƤ���ground�������ȥ����­���Ƥ���
			int allfunc = ausfunc;	
			Iterator it2 = gmap.keySet().iterator();
			while(it2.hasNext()){
				ContextDef def = (ContextDef)it2.next();
				int natomfp = ((Integer)gmap.get(def)).intValue();
				int newfunc = varcount++;
				match.add(new Instruction(Instruction.IADDFUNC,newfunc,allfunc,natomfp));
				allfunc = newfunc;
			}
			match.add(new Instruction(Instruction.NATOMSINDIRECT,memToPath(mem),allfunc));
		}
	}
	
	
	////////////////////////////////////////////////////////////////

	/** HeadCompiler.getAtomActuals�Υ����С��饤�ɡ�
	 * GuardCompiler�ϸ����Ǥϡ�atoms���б������ѿ��ֹ�Υꥹ�Ȥθ�ˡ�
	 * typedcxtdefs�Τ���UNARY_ATOM_TYPE�Ǥ���褦�ʤ�Τ��ѿ��ֹ�Υꥹ�Ȥ�Ĥʤ���ArrayList���֤���*/
	public List getAtomActuals() {
		List args = new ArrayList();		
		for (int i = 0; i < atoms.size(); i++) {
			args.add( atompaths.get(atoms.get(i)) );
		}
		Iterator it = typedcxtdefs.iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (typedcxttypes.get(def) == UNARY_ATOM_TYPE)
				args.add( new Integer(typedcxtToSrcPath(def)) );
		}
		return args;
	}
	
	//
	
	void error(String text) throws CompileException {
		Env.error(text);
		throw new CompileException("COMPILE ERROR");
	}

}