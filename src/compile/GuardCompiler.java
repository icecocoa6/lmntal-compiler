package compile;

import java.util.*;
//import runtime.Env;
import runtime.Instruction;
//import runtime.InstructionList;
import runtime.Functor;
import compile.structure.*;

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
	
	static final int ISINT    = Instruction.ISINT;	// ������ΰ������������Ǥ��뤳�Ȥ�ɽ��
	static final int ISFLOAT  = Instruction.ISFLOAT;	// �� ��ư����������
	static final int ISSTRING = Instruction.ISSTRING;	// �� ʸ����
	static final int ISMEM    = Instruction.ANYMEM;	// �� ���getRuntime���ѡ�
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
		guardLibrary1.put(new Functor("connectRuntime",1), new int[]{ISSTRING, Instruction.CONNECTRUNTIME});
	}
	
	//
	RuleCompiler rc;			// ���顼������
	List typeConstraints;		// ������Υꥹ��
	Map  typedProcessContexts;	// ���դ��ץ���ʸ̮̾��������ؤΥޥå�
	
	GuardCompiler(RuleCompiler rc, HeadCompiler hc) {
		super();
		this.rc = rc;
		this.initNormalizedCompiler(hc);
		match = rc.guard;
		typeConstraints      = rc.rs.guardMem.atoms;
		typedProcessContexts = rc.rs.typedProcessContexts;
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
	//				match.add(new Instruction(Instruction.NATOMS, submempath, submem.atoms.size()));
				// TODO �ʵ�ǽ��ĥ��ñ��Υ��ȥ�ʳ��˥ޥå����뷿�դ��ץ���ʸ̮�Ǥ�������ư���褦�ˤ���(1)
				match.add(new Instruction(Instruction.NATOMS, mempath,
					mem.getNormalAtomCount() + mem.typedProcessContexts.size() ));
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
	void fixTypedProcesses() {
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
				if (!atompaths.containsKey(def.lhsOcc.args[0].buddy.atom)) {
					rc.error("COMPILE ERROR: a partner atom is required for the head occurrence of typed process context: " + def.getName());
					rc.corrupted();
					match.add(Instruction.fail());
					return;
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
					match.add(new Instruction(Instruction.ISUNARY, atomid1));
				}
				else if (func.equals(new Functor("\\=",2))) {
					// NSAMEFUNC ���뤫��
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
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
				else if (func.getSymbolFunctorID().equals("class_2")) {
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					int atomid2 = varcount++;
					match.add(new Instruction(Instruction.GETCLASS, atomid2, atomid1));
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
				else if (func.equals(RuleCompiler.FUNC_UNIFY)) { // (-X = +Y)
					if (!identifiedCxtdefs.contains(def2)) { // (+X = -Y) �� (-Y = +X) �Ȥ��ƽ�������
						ContextDef swaptmp=def1; def1=def2; def2=swaptmp;
						if (!identifiedCxtdefs.contains(def2)) continue;
					}
					processEquivalenceConstraint(def1,def2);
				}
				else if (func.equals(new Functor("==",2))) { // (+X == +Y)
					if (!identifiedCxtdefs.contains(def1)) continue;
					if (!identifiedCxtdefs.contains(def2)) continue;
					processEquivalenceConstraint(def1,def2);
				}
				else if (guardLibrary1.containsKey(func)) { // 1��������
					int[] desc = (int[])guardLibrary1.get(func);
					if (!identifiedCxtdefs.contains(def1)) continue;
					int atomid1 = loadUnaryAtom(def1);
					if (!new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						match.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (func.getArity() == 1) { // {t1,inst} --> p(+X1)
						if (desc.length > 1) match.add(new Instruction(desc[1], atomid1));
					}
					else { // {t1,inst,t2} --> p(+X1,-X2)
						int atomid2 = varcount++;
						match.add(new Instruction(desc[1], atomid2, atomid1));
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
					if (!new Integer(desc[0]).equals(typedcxtdatatypes.get(def1))) {
						match.add(new Instruction(desc[0], atomid1));
						typedcxtdatatypes.put(def1, new Integer(desc[0]));
					}
					if (!new Integer(desc[1]).equals(typedcxtdatatypes.get(def2))) {
						match.add(new Instruction(desc[1], atomid2));
						typedcxtdatatypes.put(def1, new Integer(desc[1]));
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
					rc.error("COMPILE ERROR: unrecognized type constraint: " + cstr);
					discardTypeConstraint(cstr);
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
		rc.error("COMPILE ERROR: never proceeding type constraint: " + text);
	}
	/** ���� X=Y �ޤ��� X==Y ��������롣������def2�����ꤵ��Ƥ��ʤ���Фʤ�ʤ���*/
	private void processEquivalenceConstraint(ContextDef def1, ContextDef def2) {
		boolean checkNeeded = (typedcxttypes.get(def1) == null
							 && typedcxttypes.get(def2) == null); // ���դ��Ǥ��뤳�Ȥθ�����ɬ�פ��ɤ���
		boolean GROUND_ALLOWED = false;
		// GROUND_ALLOWED �ΤȤ� (unary = ?) �� (? = unary) �Ȥ��ƽ�������ʤ�����?��ground�ޤ���null��
		if (GROUND_ALLOWED && typedcxttypes.get(def2) != UNARY_ATOM_TYPE) {
			if (typedcxttypes.get(def1) == UNARY_ATOM_TYPE) {
				ContextDef swaptmp=def1; def1=def2; def2=swaptmp;
			}
		}
		if (GROUND_ALLOWED && typedcxttypes.get(def2) != UNARY_ATOM_TYPE) { // (? = ground)
			// todo ����
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
	private void discardTypeConstraint(Atom cstr) {
		match.add(Instruction.fail());
		for (int i = 0; i < cstr.functor.getArity(); i++) {
			ContextDef def = ((ProcessContext)cstr.args[i].buddy.atom).def;
			bindToFunctor(def,new Functor("*",1));
		}
	}
	/** ���դ��ץ���ʸ̮def��1�����ե��󥯥�func��«������ */
	private void bindToFunctor(ContextDef def, Functor func) {
		if (!identifiedCxtdefs.contains(def)) {
			identifiedCxtdefs.add(def);
			int atomid = varcount++;
			typedcxtsrcs.put(def, new Integer(atomid));
			typedcxtdefs.add(def);
			match.add(new Instruction(Instruction.ALLOCATOM, atomid, func));			
		}
		else {
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
	private int loadUnaryAtom(ContextDef def) {
		int atomid = typedcxtToSrcPath(def);
		if (atomid == UNBOUND) {
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
}