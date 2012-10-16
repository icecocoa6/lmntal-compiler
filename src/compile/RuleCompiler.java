package compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.Inline;
import runtime.InlineUnit;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.Rule;
import runtime.Ruleset;
import runtime.functor.Functor;
import runtime.functor.SymbolFunctor;

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
public class RuleCompiler
{
	/** ����ѥ��뤵���롼�빽¤ */
	RuleStructure rs;

	/**
	 * ����ѥ��뤵���롼����б�����롼�륪�֥�������
	 * ���̿����
	 */
	Rule theRule;

	private List<Instruction> atomMatch;
	private List<Instruction> memMatch;
	private List<Instruction> tempMatch;// Slimcode���ˤΤ߻��Ѥ����(��Ȥ�memMatch)

	List<Instruction> guard;

	private List<Instruction> body;

	private int varcount;			// �����ѿ��ֹ�

	boolean hasISGROUND = true;

	private List<Atom> rhsatoms;
	//private List<Atomic> rhsAtomics;				// �ץ���ʸ̮��ĥ��
	private Map<Atom, Integer>  rhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	//private Map<Atomic, Integer> rhsAtomicPath;		// �ץ���ʸ̮��ĥ��
	private Map<Membrane, Integer>  rhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)
	private Map<LinkOccurrence, Integer>  rhslinkpath;		// ���դΥ�󥯽и�(LinkOccurence) -> �ѿ��ֹ�(Integer)
	//private List rhslinks;		// ���դΥ�󥯽и�(LinkOccurence)�Υꥹ�ȡ������Τߡ� -> computeRHSLinks���֤��ˤ���
	private List<Atomic> lhsatoms;
	private List<Membrane> lhsmems;
	private Map<Atomic, Integer>  lhsatompath;		// ���դΥ��ȥ� (Atomic) -> �ѿ��ֹ� (Integer)
	private Map<Membrane, Integer>  lhsmempath;		// ���դ��� (Membrane) -> �ѿ��ֹ� (Integer)
	private Map<LinkOccurrence, Integer>  lhslinkpath = new HashMap<LinkOccurrence, Integer>();		// ���դΥ��ȥ�Υ�󥯽и� (LinkOccurrence) -> �ѿ��ֹ�(Integer)
	// �㺸�դΥ��ȥ���ѿ��ֹ� (Integer) -> ��󥯤��ѿ��ֹ������ (int[])���䤫���ѹ�

	private HeadCompiler hc, hc2;

	private int lhsmemToPath(Membrane mem) { return lhsmempath.get(mem); }
	private int rhsmemToPath(Membrane mem) { return rhsmempath.get(mem); }
	private int lhsatomToPath(Atomic atom) { return lhsatompath.get(atom); }
	private int rhsatomToPath(Atomic atom) { return rhsatompath.get(atom); }
	private int lhslinkToPath(Atomic atom, int pos) { return lhslinkToPath(atom.args[pos]); }
	private int lhslinkToPath(LinkOccurrence link) { return lhslinkpath.get(link); }

	private String unitName;

	/** �إåɤΥޥå��󥰽�λ��η�³̿����Υ�٥� */
	private InstructionList contLabel;

	/**
	 * ���ꤵ�줿 RuleStructure �ѤΥ롼���Ĥ���
	 */
	public RuleCompiler(RuleStructure rs)
	{
		this(rs, InlineUnit.DEFAULT_UNITNAME);
	}

	public RuleCompiler(RuleStructure rs, String unitName)
	{
		this.unitName = unitName;
		this.rs = rs;
	}

	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤹��
	 */
	public Rule compile() throws CompileException
	{
		liftupActiveAtoms(rs.leftMem);
		simplify();
		//theRule = new Rule(rs.toString());
		theRule = new Rule(rs.leftMem.getFirstAtomName(),rs.toString());
		theRule.name = rs.name;

		hc = new HeadCompiler();//rs.leftMem;
		hc.enumFormals(rs.leftMem);	// ���դ��Ф��벾�����ꥹ�Ȥ���
		hc2 = new HeadCompiler();
		hc2.enumFormals(rs.leftMem);
		//�Ȥꤢ������˥����ɥ���ѥ����Ƥֻ��ˤ��Ƥ��ޤ� by mizuno
		//if (!rs.typedProcessContexts.isEmpty() || !rs.guardNegatives.isEmpty())
		if (true)
		{
			theRule.guardLabel = new InstructionList();
			guard = theRule.guardLabel.insts;
		}
		else
		{
			guard = null;
		}
		theRule.bodyLabel = new InstructionList();
		body = theRule.bodyLabel.insts;
		contLabel = (guard != null ? theRule.guardLabel : theRule.bodyLabel);

		// ������Υ���ѥ���
		compile_l();

		// �����ɤΥ���ѥ���
		compile_g();

		hc = new HeadCompiler();//rs.leftMem;
		hc.enumFormals(rs.leftMem);	// ���դ��Ф��벾�����ꥹ�Ȥ���
		hc.firsttime = false;
		theRule.guardLabel = new InstructionList();
		guard = theRule.guardLabel.insts;
		contLabel = (guard != null ? theRule.guardLabel : theRule.bodyLabel);

		compile_l();

		compile_g();

		// ������Υ���ѥ���
		if (isSwapLinkUsable() && (Env.useSwapLink || Env.useCycleLinks))
		{
			compile_r_swaplink();
		}
		else
		{
			if (Env.useSwapLink || Env.useCycleLinks)
			{
				System.err.println("WARNING: swaplink/cyclelinks was suppressed.");
			}
			compile_r();
		}

		theRule.memMatch  = memMatch;
		theRule.tempMatch = tempMatch;
		theRule.atomMatch = atomMatch;
		theRule.guard     = guard;
		theRule.body      = body;
		if (theRule.name != null)
		{
			 theRule.body.add(1, Instruction.commit(theRule.name, theRule.lineno));
		}
		else
		{
			//�롼��̾������
			StringBuilder ruleName = new StringBuilder("_");
			String orgName = rs.toString();
			for (int i = 0; i < orgName.length(); i++)
			{
				char c = orgName.charAt(i);
				if (isAlphabetOrDigit(c) || c == '_')
				{
					ruleName.append(c);
				}
				//1+4ʸ�����Ǥ��ڤ�
				if (!Env.showlongrulename && ruleName.length() >= 5) break;
			}
			theRule.body.add(1, Instruction.commit(ruleName.toString(), theRule.lineno));
		}

		optimize();
		return theRule;
	}
	
	/**
	 * <p>
	 * swaplink/cyclelinks �����Ѳ�ǽ��Ƚ�ꤷ�ޤ���
	 * �����μ�����̤�б�����ʬ���äƤ��륱�����ˤĤ��ơ��̾�Υѥ��ǥ����������򤹤뤿���Ƚ�Ǥ�ɬ�פǤ���
	 * ������������Ƚ����̲ᤷ�Ƥ��꤯�������������Ǥ��ʤ������������뤫���Τ�ޤ���
	 * </p>
	 */
	private boolean isSwapLinkUsable()
	{
		// 1. ��̵���ץ���ʸ̮��¸�ߤ��ʤ�
		// 2. ���դ��ץ���ʸ̮��¸�ߤ��ʤ�
		// 3. ñ�첽���ȥब¸�ߤ��ʤ�
		return rs.processContexts.isEmpty()
			&& rs.typedProcessContexts.isEmpty()
			&& !containsUnify();
	}

	/**
	 * <p>���������ñ�첽���ȥ� '='/2 ��¸�ߤ��뤫Ĵ�٤ޤ���</p>
	 */
	private boolean containsUnify()
	{
		Set<Atom> ratoms = new HashSet<Atom>();
		getAllAtoms(ratoms, rs.rightMem);
		for (Atom a : ratoms)
		{
			if (a.functor.equals(Functor.UNIFY))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * ������򥳥�ѥ��뤹��
	 */
	private void compile_l()
	{
		theRule.atomMatchLabel = new InstructionList();
		atomMatch = theRule.atomMatchLabel.insts;

		int maxvarcount = 2;	// ���ȥ��Ƴ�ѡʲ���
		for (int firstid = 0; firstid <= hc.atoms.size(); firstid++)
		{
			hc.prepare(); // �ѿ��ֹ������
			hc2.prepare();
			if (firstid < hc.atoms.size())
			{
				if (Env.slimcode || Env.memtestonly) continue;
				// Env.SHUFFLE_DEFAULT �ʤ�С��롼���ȿ����Ψ��ͥ�褹�뤿�ᥢ�ȥ��Ƴ�ƥ��ȤϹԤ�ʤ�

				Atom atom = (Atom)hc.atoms.get(firstid);
				if (!atom.functor.isActive()) continue;

				// ���ȥ��Ƴ
				InstructionList tmplabel = new InstructionList();
				tmplabel.insts = hc.match;
				atomMatch.add(new Instruction(Instruction.BRANCH, tmplabel));

				hc.memPaths.put(rs.leftMem, 0);	// ������ѿ��ֹ�� 0
				hc.atomPaths.put(atom, 1);		// ��Ƴ���륢�ȥ���ѿ��ֹ�� 1
				hc.varCount = 2;
				hc.match.add(new Instruction(Instruction.FUNC, 1, atom.functor));
				Membrane mem = atom.mem;
				if (mem == rs.leftMem)
				{
					hc.match.add(new Instruction(Instruction.TESTMEM, 0, 1));
				}
				else
				{
					hc.match.add(new Instruction(Instruction.GETMEM, hc.varCount, 1, mem.kind, mem.name));
					hc.match.add(new Instruction(Instruction.LOCK,   hc.varCount));
					hc.memPaths.put(mem, hc.varCount++);
					mem = mem.parent;
					while (mem != rs.leftMem)
					{
						hc.match.add(new Instruction(Instruction.GETPARENT,hc.varCount,hc.varCount-1));
						hc.match.add(new Instruction(Instruction.LOCK,     hc.varCount));
						hc.memPaths.put(mem, hc.varCount++);
						mem = mem.parent;
					}
					hc.match.add(new Instruction(Instruction.GETPARENT,hc.varCount,hc.varCount-1));
					hc.match.add(new Instruction(Instruction.EQMEM, 0, hc.varCount++));
				}
				hc.getLinks(1, atom.functor.getArity(), hc.match); //��󥯤ΰ�����(RISC��) by mizuno
				Atom firstatom = (Atom)hc.atoms.get(firstid);
				hc.compileLinkedGroup(firstatom, hc.matchLabel);
				hc.compileMembrane(firstatom.mem, hc.matchLabel);
			}
			else
			{
				// ���Ƴ
				theRule.memMatchLabel = hc.matchLabel;
				if (Env.findatom2)
				{
					tempMatch = hc.tempMatch;
					memMatch = hc.match;
				}
				else
				{
					memMatch = hc.match;
				}
				hc.memPaths.put(rs.leftMem, 0);	// ������ѿ��ֹ�� 0
				hc2.memPaths.put(rs.leftMem, 0);	// ������ѿ��ֹ�� 0
			}
			if (Env.findatom2)
			{
				hc.compileMembraneForSlimcode(rs.leftMem, hc.matchLabel, hasISGROUND);
				hc2.compileMembrane(rs.leftMem, hc.tempLabel);
			}
			else
			{
				hc.compileMembrane(rs.leftMem, hc.matchLabel);
			}

			// ��ͳ�и������ǡ������ȥब�ʤ�����������
			if (!hc.fFindDataAtoms)
			{
				if (Env.debug >= 1)
				{
					for (Atomic a : hc.atoms)
					{
						Atom atom = (Atom)a;
						if (!hc.isAtomLoaded(atom))
						{
							Env.warning("TYPE WARNING: Rule head contains free data atom: " + atom);
						}
					}
				}
				hc.switchToUntypedCompilation();
				hc.setContLabel(contLabel);
				if (Env.findatom2)
				{
					hc.compileMembraneForSlimcode(rs.leftMem, hc.matchLabel, hasISGROUND);
					hc2.compileMembrane(rs.leftMem, hc.tempLabel);
				}
				else
				{
					hc.compileMembrane(rs.leftMem, hc.matchLabel);
				}
			}
			hc.checkFreeLinkCount(rs.leftMem, hc.match); // ��������ѹ��ˤ��ƤФʤ��Ƥ褯�ʤä�����Ϥ�Ƥ�ɬ�פ���

			if (Env.hyperLinkOpt)
			{
				hc.compileSameProcessContext(rs.leftMem, hc.matchLabel);//seiji
			}

			if (hc.match == memMatch)
			{
				hc.match.add(0, Instruction.spec(1, hc.maxVarCount));
				hc.tempMatch.add(0, Instruction.spec(1, hc.maxVarCount));
			}
			else
			{
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
			//hc.match.add( Instruction.inlinereact(theRule, memActuals, atomActuals, varActuals) );
			//int formals = memActuals.size() + atomActuals.size() + varActuals.size();
			//hc.match.add( Instruction.spec(formals, formals) );
			//hc.match.add( hc.getResetVarsInstruction() );
			//List brancharg = new ArrayList();
			//brancharg.add(body);
			//hc.match.add( new Instruction(Instruction.BRANCH, brancharg) );

			// jump̿�ᷲ�����������
			if (maxvarcount < hc.varCount) maxvarcount = hc.maxVarCount;
		}
		atomMatch.add(0, Instruction.spec(2,maxvarcount));
	}

	/**
	 * ���դΥ�󥯤�����ޤ�����������
	 */
	private List<LinkOccurrence> computeRHSLinks()
	{
		List<LinkOccurrence> rhslinks = new ArrayList<LinkOccurrence>();
		rhslinkpath = new HashMap<LinkOccurrence, Integer>();
		int rhslinkindex = 0;
		// ���ȥ�ΰ����Υ�󥯽и�
		for (Atom atom : rhsatoms)
		{
			for (int pos = 0; pos < atom.functor.getArity(); pos++)
			{
				body.add(new Instruction(Instruction.ALLOCLINK,varcount,rhsatomToPath(atom),pos));
				rhslinkpath.put(atom.args[pos], varcount);
				if (!rhslinks.contains(atom.args[pos].buddy) &&
						!(atom.functor.equals(Functor.INSIDE_PROXY) && pos == 0))
				{
					rhslinks.add(rhslinkindex++,atom.args[pos]);
				}
				varcount++;
			}
		}

		// unary���եץ���ʸ̮�Υ�󥯽и�
		for (ProcessContext atom : rhstypedcxtpaths.keySet())
		{
			body.add(new Instruction(Instruction.ALLOCLINK,varcount,rhstypedcxtToPath(atom),0));
			rhslinkpath.put(atom.args[0], varcount);
			if (!rhslinks.contains(atom.args[0].buddy))
			{
				rhslinks.add(rhslinkindex++,atom.args[0]);
			}
			varcount++;
		}

		// ground���եץ���ʸ̮�Υ�󥯽и�
		for (ProcessContext ground : rhsgroundpaths.keySet())
		{
			int linklistpath = rhsgroundToPath(ground);
			for (int i = 0; i < ground.def.lhsOcc.args.length; i++)
			{
				int linkpath = varcount++;
				body.add(new Instruction(Instruction.GETFROMLIST,linkpath, linklistpath, i));
//				int linkpath = rhsgroundToPath(atom);
				rhslinkpath.put(ground.args[i], linkpath);
				if (!rhslinks.contains(ground.args[i].buddy))
				{
					rhslinks.add(rhslinkindex++,ground.args[i]);
				}
			}
		}

		// ���ʤ�

		for (ContextDef def : rs.processContexts.values())
		{
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext())
			{
				ProcessContext atom = (ProcessContext)it2.next();
				for (int pos = 0; pos < atom.getArity(); pos++)
				{
					//LinkOccurrence srclink = atom.def.lhsOcc.args[pos].buddy;
					//int srclinkid;
					//if (!lhslinkpath.containsKey(srclink))
					//{
					//	srclinkid = varcount++;
					//	body.add(new Instruction(Instruction.GETLINK,srclinkid,
					//	lhsatomToPath(srclink.atom), srclink.pos));
					//	lhslinkpath.put(srclink, srclinkid);
					//}
					//srclinkid = lhslinkToPath(srclink);
					//if (!(fUseMoveCells && atom.def.rhsOccs.size() == 1))
					//{
					//	int copiedlink = varcount++;
					//	body.add( new Instruction(Instruction.LOOKUPLINK,
					//	copiedlink, rhspcToMapPath(atom), srclinkid));
					//	srclinkid = copiedlink;
					//}
					//rhslinkpath.put(atom.args[pos], srclinkid);
					if (!rhslinks.contains(atom.args[pos].buddy))
					{
						rhslinks.add(rhslinkindex++,atom.args[pos]);
					}
				}
			}
		}
		return rhslinks;
	}

	private int getLinkPath(LinkOccurrence link)
	{
		if (rhslinkpath.containsKey(link))
		{
			return rhslinkpath.get(link);
		}
		else if (link.atom instanceof ProcessContext && !((ProcessContext)link.atom).def.typed)
		{
			LinkOccurrence srclink = ((ProcessContext)link.atom).def.lhsOcc.args[link.pos].buddy;
			int linkpath = varcount++;
			body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(srclink.atom),srclink.pos));
			if (!(fUseMoveCells && ((ProcessContext)link.atom).def.rhsOccs.size() == 1))
			{
				int copiedlink = varcount++;
				body.add( new Instruction(Instruction.LOOKUPLINK,
						copiedlink, rhspcToMapPath(((ProcessContext)link.atom)), linkpath));
				return copiedlink;
			}
			return linkpath;
		}
		else
		{
			if (!lhslinkpath.containsKey(link))
			{
				int linkpath = varcount++;
				body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(link.atom),link.pos));
				lhslinkpath.put(link, linkpath);
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
	private void compile_r() throws CompileException
	{
		int formals = varcount;
		//body.add( Instruction.commit(theRule) );
		inc_guard();

		rhsatoms    = new ArrayList<Atom>();
		rhsatompath = new HashMap<Atom, Integer>();
		rhsmempath  = new HashMap<Membrane, Integer>();
		int toplevelmemid = lhsmemToPath(rs.leftMem);
		rhsmempath.put(rs.rightMem, toplevelmemid);

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
		if (removeLHSMem(rs.leftMem) >= 2)
		{
			//2011/01/23 slim�Ǥ�ɬ�פʤ��ʤä��Τ��������ʤ��褦�˽��� by meguro
			if (!Env.slimcode)
			{
				body.add(new Instruction(Instruction.REMOVETOPLEVELPROXIES, toplevelmemid));
			}
		}

		recursiveLockLHSNonlinearProcessContextMems();
		insertconnectors();

		// insertconnectors�θ�Ǥʤ���Ф��ޤ������ʤ��ΤǺ�ȯ�� ( 2006/09/15 kudo)
		getGroundLinkPaths();

		// ���դι�¤��$p�����ơ���Ƶ�Ū����������
		// $p������Ū�Ǥʤ���󥯤�Ϥ�

		buildRHSMem(rs.rightMem);
		/* ���դ�$p�����֤��줿ľ�塣���Υ����ߥ󥰤Ǥʤ���Фʤ�ʤ�Ȧ */
		if (!rs.rightMem.processContexts.isEmpty())
		{
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
		if (Env.slimcode)
		{
			if (Env.hyperLink)
			{
				addHyperlink();//seiji
			}
			addCallback();
		}
		addRegAndLoadModules();

		// ���դλĤä��ץ������������
		freeLHSNonlinearProcessContexts();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms();
		freeLHSTypedProcesses();
		//freeLHSSingletonProcessContexts(); // freemem����Ԥ����뤿��3�Ծ�˰�ư���� n-kato 2005.1.13

		// ���unlock����

		recursiveUnlockLHSNonlinearProcessContextMems();
		unlockReusedOrNewRootMem(rs.rightMem);
		//
		body.add(0, Instruction.spec(formals, varcount));

		//if (rs.rightMem.mems.isEmpty() && rs.rightMem.ruleContexts.isEmpty()
		//&& rs.rightMem.processContexts.isEmpty() && rs.rightMem.rulesets.isEmpty()) {
		//body.add(new Instruction(Instruction.CONTINUE));
		//} else
		body.add(new Instruction(Instruction.PROCEED));
	}

	/**
	 * <p>�����쥳��ѥ�������� {@code swaplink/cyclelinks} ��ĥ�ǡ�</p>
	 */
	private void compile_r_swaplink() throws CompileException
	{
		int formals = varcount;
		inc_guard();

		if (rhsatoms == null)
			rhsatoms = new ArrayList<Atom>();
		else
			rhsatoms.clear();

		if (rhsatompath == null)
			rhsatompath = new HashMap<Atom, Integer>();
		else
			rhsatompath.clear();

		/*
		if (rhsAtomics == null)
			rhsAtomics = new ArrayList<Atomic>();
		else
			rhsAtomics.clear();

		if (rhsAtomicPath == null)
			rhsAtomicPath = new HashMap<Atomic, Integer>();
		else
			rhsAtomicPath.clear();
		*/

		if (rhsmempath == null)
			rhsmempath = new HashMap<Membrane, Integer>();
		else
			rhsmempath.clear();

		int toplevelmemid = lhsmemToPath(rs.leftMem);
		rhsmempath.put(rs.rightMem, toplevelmemid);

		Set<Atom> rhsAtomSet = new HashSet<Atom>();
		getAllAtoms(rhsAtomSet, rs.rightMem);
		rhsatoms.addAll(rhsAtomSet);
		//rhsAtomics.addAll(rhsatoms);

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
		removeLHSTypedProcesses();
		if (removeLHSMem(rs.leftMem) >= 2)
		{
			//2011/01/23 slim�Ǥ�ɬ�פʤ��ʤä��Τ��������ʤ��褦�˽��� by meguro
			if (!Env.slimcode)
			{
				body.add(new Instruction(Instruction.REMOVETOPLEVELPROXIES, toplevelmemid));
			}
		}

		recursiveLockLHSNonlinearProcessContextMems();
		insertconnectors();

		// insertconnectors�θ�Ǥʤ���Ф��ޤ������ʤ��ΤǺ�ȯ�� ( 2006/09/15 kudo)
		getGroundLinkPaths();

		// ���դι�¤��$p�����ơ���Ƶ�Ū����������
		// $p������Ū�Ǥʤ���󥯤�Ϥ�

		buildRHSMem(rs.rightMem); // ���դˤ�������ѿ��ֹ����
		/* ���դ�$p�����֤��줿ľ�塣���Υ����ߥ󥰤Ǥʤ���Фʤ�ʤ�Ȧ */
		if (!rs.rightMem.processContexts.isEmpty()) {
			body.add(new Instruction(Instruction.REMOVETEMPORARYPROXIES, toplevelmemid));
		}
		copyRules(rs.rightMem);
		loadRulesets(rs.rightMem);
		buildRHSTypedProcesses();
		
		Set<Atomic> noModified = getInvariantAtomics();
		Map<Atom, Atom> reusable = getReusableAtomics(noModified);
		Set<Atomic> removed = getRemovedAtomics(noModified, reusable);
		Set<Atomic> created = getCreatedAtomics(noModified, reusable);

		removeLHSAtoms_swaplink(removed);
		
		buildRHSAtoms_swaplink(rs.rightMem, created, reusable);
		// ������varcount�κǽ��ͤ����ꤹ�뤳�ȤˤʤäƤ��롣�ѹ�����Ŭ�ڤ˲��˰�ư���뤳�ȡ�


		//���դ�����Ū�ʥ�󥯤�Ž��
		//getLHSLinks();
		if (Env.useCycleLinks)
		{
			compileCycleLinks(removed, created, reusable);
		}
		else
		{
			compileLinkOperations(removed, created, reusable);
		}
		deleteconnectors();

		//���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ�
		enqueueRHSAtoms_swaplink(created, reusable.keySet());

		//����2�Ĥϱ��դι�¤�������ʹߤʤ餤�ĤǤ�褤
		addInline();
		if (Env.slimcode) {
			if (Env.hyperLink) addHyperlink();//seiji
			addCallback();
		}
		addRegAndLoadModules();

		// ���դλĤä��ץ������������
		freeLHSNonlinearProcessContexts();
		freeLHSMem(rs.leftMem);
		freeLHSAtoms_swaplink(removed);
		freeLHSTypedProcesses();

		// ���unlock����

		recursiveUnlockLHSNonlinearProcessContextMems();
		unlockReusedOrNewRootMem(rs.rightMem);
		body.add(0, Instruction.spec(formals, varcount));

		body.add(new Instruction(Instruction.PROCEED));
	}

	/**
	 * <p>��{@code mem}�ʲ��˴ޤޤ�뤹�٤ƤΥ��ȥ��Ƶ�Ū�˼������ޤ���</p>
	 * @param destAtoms �����������ȥ�γ�Ǽ��
	 * @param mem õ��������
	 */
	private void getAllAtoms(Set<Atom> destAtoms, Membrane mem)
	{
		for (Membrane submem : mem.mems) getAllAtoms(destAtoms, submem);
		for (Atom a : mem.atoms) destAtoms.add(a);
	}

	/**
	 * <p>2�ĤΥ��ȥबƱ�����ȥ�Ǥ��뤫��Ĵ�٤ޤ���</p>
	 * <p>2�ĤΥ��ȥबƱ���Ǥ���Ȥϡ��������ȥ��ФˤĤ��ưʲ��ξ��
	 * <ol>
	 * <li>������������</li>
	 * <li>̾����������</li>
	 * </ol>
	 * ����Ω���뤳�Ȥ�ɽ���ޤ���Ʊ���Ǥ��륢�ȥ��Фϥ롼����Ǻ����Ѥ���ޤ���</p>
	 * @param a1 ���ȥ�1
	 * @param a2 ���ȥ�2
	 * @return ���ȥ�{@code a1}�ȥ��ȥ�{@code a2}��Ʊ���Ǥ������{@code true}�������Ǥʤ�����{@code false}���֤��ޤ���
	 */
	private boolean isIsomorphic(Atom a1, Atom a2)
	{
		return a1.getArity() == a2.getArity() && a1.getName().equals(a2.getName());
	}

	/**
	 * <p>���ȥߥå��ν���{@code atomics}�����ĥ�󥯤��ܿ�������ޤ���</p>
	 * TODO: ̵�������ʼ����ʤΤǡ���Ǥ�äȤޤȤ�ʼ�����ͤ��롣
	 */
	private int countLinkOccurrence(Collection<? extends Atomic> atomics)
	{
		int count = 0;
		for (Atomic a : atomics) count += a.getArity();
		return count;
	}

	////////////////////////////////////////////////////////////////
	//
	// �����ɴط�
	//

	/** �إåɤ���ȥ��ȥ���Ф��ơ��������ֹ����Ͽ���� */
	private void genLHSPaths()
	{
		lhsatompath = new HashMap<Atomic, Integer>();
		lhsmempath  = new HashMap<Membrane, Integer>();
		varcount = 0;
		for (int i = 0; i < lhsmems.size(); i++)
		{
			lhsmempath.put(lhsmems.get(i), varcount++);
		}
		for (Atomic atomic : lhsatoms)
		{
			lhsatompath.put(atomic, varcount++);
		}
	}

	/**
	 * �����ɤμ�����
	 */
	private void inc_guard()
	{
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
	private void compile_g() throws CompileException
	{
		lhsmems  = hc.mems;
		lhsatoms = hc.atoms;
		genLHSPaths();
		gc = new GuardCompiler2(this, hc);		/* �ѿ��ֹ�������� */
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
	 *   
	 *   newhlink�ʤ�, ����ܥ륢�ȥ���������륬����̿����ɲä������ᡢ
	 *   �ʲ��Τ褦�ˡ�"���Ƥμ��Ԥ����륬����̿��"�κǸ����Ǥ��ꡢ
	 *   �ɥ���ܥ륢�ȥ�����̿��ɤ���������������� 2011/01/10 seiji
	 *        ....
	 *     [���Ƥμ��Ԥ����륬����̿����]
	 *      [uniq] <-- ��������������
	 *     [newhlink�ʤɤΥ���ܥ륢�ȥ�����̿����]
	 *        ....
	 */
	void fixUniqOrder()
	{
		boolean found = guard.contains(Instruction.UNIQ);
		List<Integer> vars = new ArrayList<Integer>();
		for (Iterator<Instruction> it = guard.iterator(); it.hasNext(); )
		{
			Instruction inst = it.next();
			if (inst.getKind() == Instruction.UNIQ)
			{
				found = true;
				vars.addAll((List<Integer>)inst.getArg(0));
				it.remove();
			}
		}
		
//		if(found) guard.add(new Instruction(Instruction.UNIQ, vars));
		if (found)
		{
			boolean guardallocs = false;
			int i = 0;
			for (Instruction inst : guard)
			{
				// ����ܥ륢�ȥ����������̿��˽в񤦤ޤǥ롼��
				if (inst.getKind() == Instruction.NEWHLINK || inst.getKind() == Instruction.MAKEHLINK)
				{
					guardallocs = true;
					break;
				}
				i++;
			}
			if (guardallocs)
			{
				guard.add(i, new Instruction(Instruction.UNIQ, vars));
			}
			else
			{
				guard.add(new Instruction(Instruction.UNIQ, vars));
			}
		}
	}

	/** ������򥳥�ѥ��뤹�� */
	void compileNegatives() throws CompileException
	{
		Iterator<List<ProcessContextEquation>> it = rs.guardNegatives.iterator();
		while (it.hasNext())
		{
			List<ProcessContextEquation> eqs = it.next();
			HeadCompiler negcmp = hc.getNormalizedHeadCompiler();
			negcmp.varCount = varcount;
			negcmp.compileNegativeCondition(eqs, negcmp.matchLabel);
			guard.add(new Instruction(Instruction.NOT, negcmp.matchLabel));
			if (varcount < negcmp.varCount)  varcount = negcmp.varCount;
		}
	}

	// ���դ��ץ���ʸ̮�ط�

	private GuardCompiler2 gc;
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
	private void genTypedProcessContextPaths()
	{
		for (ContextDef def : gc.typedCxtDefs)
		{
			if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE)
			{
				typedcxtsrcs.put( def, varcount++);
			}
		}
	}

	/** ground���դ��ץ���ʸ̮����ˤĤ��ơ����Ȥʤ��󥯤Υꥹ�Ȥ�������� */
	private void getGroundLinkPaths()
	{
		groundsrcs = new HashMap<ContextDef, Integer>();
		for (ContextDef def : gc.groundSrcs.keySet())
		{
			if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE)
			{
				//ProcessContext lhsOcc = def.lhsOcc
				int linklistpath = varcount++;
				body.add(new Instruction(Instruction.NEWLIST,linklistpath));
				// ���Ƥΰ������Ф���ȯ�Ԥ���
				for (int i = 0; i < def.lhsOcc.args.length; i++)
				{
					int linkpath = varcount++;
					body.add(new Instruction(Instruction.GETLINK,linkpath,lhsatomToPath(def.lhsOcc.args[i].buddy.atom),def.lhsOcc.args[i].buddy.pos));
					body.add(new Instruction(Instruction.ADDTOLIST,linklistpath,linkpath));
					groundsrcs.put(def, linklistpath);
				}
			}
		}
	}

	/*
	public void enumTypedContextDefs()
	{
		Iterator it = rs.typedProcessContexts.values().iterator();
		while (it.hasNext())
		{
			ContextDef def = (ContextDef)it.next();
			typedcxtdefs.add(def);
		}
	}
	*/

	/** ���դη��դ��ץ���ʸ̮������ */
	private void removeLHSTypedProcesses()
	{
		for (ContextDef def : rs.typedProcessContexts.values())
		{
			Context pc = def.lhsOcc;
			if (pc != null) { // �إåɤΤȤ��Τ߽����
				if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE)
				{
					//dequeue����Ƥ��ʤ��ä��Τ��ɲ�(2005/08/27) by mizuno
					body.add(new Instruction( Instruction.DEQUEUEATOM, typedcxtToSrcPath(def) ));
					body.add(new Instruction( Instruction.REMOVEATOM,
							typedcxtToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
				else if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE)
				{
					body.add(new Instruction( Instruction.REMOVEGROUND,
							groundToSrcPath(def), lhsmemToPath(pc.mem) ));
				}
			}
		}
	}
	/** ���դη��դ��ץ���ʸ̮��������� */
	private void freeLHSTypedProcesses()
	{
		for (ContextDef def : rs.typedProcessContexts.values())
		{
			if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE)
			{
				body.add(new Instruction( Instruction.FREEATOM,
						typedcxtToSrcPath(def) ));
			}
			else if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE)
			{
				body.add(new Instruction( Instruction.FREEGROUND,groundToSrcPath(def)));
			}
		}
	}

	/** �������ץ���ʸ̮�κ��սи���������� */
	private void freeLHSNonlinearProcessContexts()
	{
		for (ContextDef def : rs.processContexts.values())
		{
			if (def.rhsOccs.size() != 1) { // �������ΤȤ�1�Ĥ��������Ѥ���褦�ˤ����� size == 0 ��ľ���� -> �����ѤϺ�Ŭ����Ǥ���뤳�Ȥˤ����Τ�����
				body.add(new Instruction( Instruction.DROPMEM,
						lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	/** �������ץ���ʸ̮�κ��սи����Ƶ�Ū�˥�å����� */
	private void recursiveLockLHSNonlinearProcessContextMems()
	{
		for (ContextDef def : rs.processContexts.values())
		{
			if (def.rhsOccs.size() != 1)
			{
				body.add(new Instruction( Instruction.RECURSIVELOCK,
						lhsmemToPath(def.lhsOcc.mem) ));
			}
		}
	}

	/** �������ץ���ʸ̮�κ��սи����Ƶ�Ū�˥�å��������� */
	private void recursiveUnlockLHSNonlinearProcessContextMems()
	{
		for (ContextDef def : rs.processContexts.values())
		{
			if (def.rhsOccs.size() != 1)
			{
				if (false) { // �����Ѥ����Ȥ��Τ� recursiveunlock ����
					body.add(new Instruction( Instruction.RECURSIVEUNLOCK,
							lhsmemToPath(def.lhsOcc.mem) ));
				}
			}
		}
	}

	/** ���դη��դ��ץ���ʸ̮���ۤ��� */
	private void buildRHSTypedProcesses()
	{
		for (ContextDef def : rs.typedProcessContexts.values())
		{
			Iterator it2 = def.rhsOccs.iterator();
			while (it2.hasNext())
			{
				ProcessContext pc = (ProcessContext)it2.next();
				if (gc.typedCxtTypes.get(def) == GuardCompiler.UNARY_ATOM_TYPE)
				{
					int atompath = varcount++;
					body.add(new Instruction( Instruction.COPYATOM, atompath,
							rhsmemToPath(pc.mem),
							typedcxtToSrcPath(pc.def) ));
					rhstypedcxtpaths.put(pc, atompath);
				}
				else if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE)
				{
					int retlistpath = varcount++;
					//System.out.println("cp");
					//int mappath = varcount++;
					body.add(new Instruction( Instruction.COPYGROUND, retlistpath,
							groundToSrcPath(pc.def), // ground�ξ��ϥ�󥯤��ѿ��ֹ�Υꥹ�Ȥ�ؤ��ѿ��ֹ�
							rhsmemToPath(pc.mem) ));
					int groundpath = varcount++;
					body.add(new Instruction( Instruction.GETFROMLIST,groundpath,retlistpath,0));
					int mappath = varcount++;
					body.add(new Instruction(Instruction.GETFROMLIST,mappath,retlistpath,1));
					rhsgroundpaths.put(pc, groundpath);
					rhsmappaths.put(pc, mappath);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////

	/**
	 * �쳬�ز��ˤ��륢���ƥ��֥��ȥ����������Ƭ�����˥��饤�ɰ�ư���롣
	 * slim�Τ���˥����ƥ��֥��ȥࡢ�ǡ������ȥ�(���ͥ��ȥ�ʳ�)��
	 * ���ͥ��ȥ�ν���ѹ������ͥ��ȥ�Ȥ� IntegerFunctor��FloatingFunctor���ե��󥯥��Ǥ���褦�ʥ��ȥ�Τ��ȡ�
	 */
	private static void liftupActiveAtoms(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			liftupActiveAtoms(submem);
		}
		List<Atom> atomlist = new LinkedList<Atom>();
		for (Atom atom : mem.atoms)
		{
			atomlist.add(atom);
		}
		mem.atoms.clear();

		for (Iterator<Atom> it = atomlist.iterator(); it.hasNext();)
		{
			Atom a = it.next();
			if (a.functor.isActive())
			{
				mem.atoms.add(a);
				it.remove();
			}
		}
		for (Iterator<Atom> it = atomlist.iterator(); it.hasNext();)
		{
			Atom a = it.next();
			if (!a.functor.isNumber())
			{
				mem.atoms.add(a);
				it.remove();
			}
		}
		mem.atoms.addAll(atomlist);
	}

	/**
	 * �롼��κ��դȱ��դ��Ф���staticUnify��Ƥ�
	 */
	private void simplify() throws CompileException
	{
		staticUnify(rs.leftMem);
		checkExplicitFreeLinks(rs.leftMem);
		/*
		if (!verifyExplicitFreeLinks(rs.leftMem))
		{
			throw new CompileException("System error");
		}
		*/

		staticUnify(rs.rightMem);

		if (rs.leftMem.atoms.isEmpty() && rs.leftMem.mems.isEmpty() && !rs.fSuppressEmptyHeadWarning)
		{
			Env.warning("WARNING: rule with empty head: " + rs);
		}

		// ��������˴ؤ������ʤ����Ǥ����Τ�����
		// ����¾ unary =/== ground �ν��֤��¤��ؤ���
		List<Atom> typeConstraints = rs.guardMem.atoms;
		List<Atom> unaryList = new ArrayList<Atom>();
		List<Atom> unifyList = new ArrayList<Atom>();
		List<Atom> groundList = new ArrayList<Atom>();
		Iterator<Atom> it = typeConstraints.iterator();
		while (it.hasNext())
		{
			Atom cstr = it.next();
			Functor func = cstr.functor;
			if (func.equals("unary", 1))    { unaryList.add(cstr); it.remove(); }
			if (func.equals(Functor.UNIFY)) { unifyList.add(cstr); it.remove(); }
			if (func.equals("==", 2))       { unifyList.add(cstr); it.remove(); }
			if (func.equals("\\==", 2))     { unifyList.add(cstr); it.remove(); }
			if (func.equals("ground", 1))   { groundList.add(cstr); it.remove(); }
		}
		typeConstraints.addAll(unaryList);
		typeConstraints.addAll(unifyList);
		typeConstraints.addAll(groundList);
	}

	/**
	 * ���ꤵ�줿��Ȥ��λ�¹��¸�ߤ����Ĺ�� =��todo ����Ӽ�ͳ��󥯴������ȥ�ˤ�����
	 * { a(X), b(Y), X=Y } <==> { a(X), b(X) }
	 */
	private void staticUnify(Membrane mem) throws CompileException
	{
		for (Membrane submem : mem.mems)
		{
			staticUnify(submem);
		}

		List<Atom> removedAtoms = new ArrayList<Atom>();
		for (Atom atom : mem.atoms)
		{
			if (atom.functor.equals(Functor.UNIFY))
			{
				LinkOccurrence link1 = atom.args[0].buddy;
				LinkOccurrence link2 = atom.args[1].buddy;

				// ñ�첽���ȥ�Υ���褬ξ���Ȥ�¾����ˤĤʤ��äƤ�����
				if (link1.atom.mem != mem && link2.atom.mem != mem)
				{
					if (mem == rs.leftMem)
					{
						// // <strike> ( X=Y :- p(X,Y) ) �ϰ�̣���ϥ��顼
						// //��=���̾�Υإåɥ��ȥ�ȸ��ʤ������֤�����</strike>
						// error("COMPILE ERROR: head contains body unification");
						// ( X=Y :- p(X,Y) ) �� ( :- p(X,X) ) �ˤʤ�
					}
					else
					{
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
		mem.atoms.removeAll(removedAtoms);
	}

	/**
	 * ���ʤ��ץ���ʸ̮������Ū�ʰ�����Ƶ�Ū�˸������롥
	 * @param mem
	 * @throws CompileException
	 */
	private void checkExplicitFreeLinks(Membrane mem) throws CompileException
	{
		for (Membrane submem : mem.mems)
		{
			checkExplicitFreeLinks(submem);
		}

		// $p[X,X] �ʤɤ򸡽�
		// ���դǤϵ�������ñ�첽�ˡ�
		// ���դǤϡ�������󥯤�¸�ߡפȤ�������򼨤��ȹͤ����뤬������ϰ�̣�����顼��
		for (ProcessContext pc : mem.processContexts)
		{
			if (pc.def.isTyped()) continue;

			Set<String> explicitfreelinks = new HashSet<String>();
			for (int i = 0; i < pc.args.length; i++)
			{
				LinkOccurrence lnk = pc.args[i];
				if (explicitfreelinks.contains(lnk.name))
				{
					systemError("SYNTAX ERROR: explicit arguments of a process context in head must be pairwise disjoint: " + pc.def);
				}
				else
				{
					explicitfreelinks.add(lnk.name);
				}
			}
		}
	}

	/**
	 * ���ʤ��ץ���ʸ̮������Ū�ʰ�����Ƶ�Ū�˸������롥
	 * ���������ʤ���
	 * @param mem
	 * @throws CompileException
	 */
	private boolean verifyExplicitFreeLinks(Membrane mem)
	{
		boolean valid = true;

		for (Membrane submem : mem.mems)
		{
			valid = verifyExplicitFreeLinks(submem) && valid;
		}

		// $p[X,X] �ʤɤ򸡽�
		// ���դǤϵ�������ñ�첽�ˡ�
		// ���դǤϡ�������󥯤�¸�ߡפȤ�������򼨤��ȹͤ����뤬������ϰ�̣�����顼��
		for (ProcessContext pc : mem.processContexts)
		{
			if (pc.def.isTyped()) continue;

			Set<String> occuredNames = new HashSet<String>();
			for (int i = 0; i < pc.args.length; i++)
			{
				LinkOccurrence lnk = pc.args[i];
				if (occuredNames.contains(lnk.name))
				{
					Env.error("Syntax error: explicit arguments of a process context in head must be pairwise disjoint: " + pc.def);
					valid = false;
				}
				else
				{
					occuredNames.add(lnk.name);
				}
			}
		}
		return valid;
	}

	/** ̿������Ŭ������ */
	private void optimize()
	{
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
	private void removeLHSAtoms()
	{
		for (int i = 0; i < lhsatoms.size(); i++)
		{
			Atom atom = (Atom)lhsatoms.get(i);
			body.add( Instruction.removeatom(
					lhsatomToPath(atom), // �� lhsmems.size() + i �˰��פ���
					lhsmemToPath(atom.mem), atom.functor ));
		}
	}

	private void removeLHSAtoms_swaplink(Set<Atomic> removedAtoms)
	{
		for (Atomic a : removedAtoms)
		{
			if (!(a instanceof Atom)) continue;
			body.add(Instruction.removeatom(
				lhsatomToPath(a),
				lhsmemToPath(a.mem), ((Atom)a).functor));
		}
	}

	/** ���դΥ��ȥ��¹ԥ��ȥॹ���å��������롣*/
	private void dequeueLHSAtoms()
	{
		for (int i = 0; i < lhsatoms.size(); i++)
		{
			Atom atom = (Atom)lhsatoms.get(i);
			if (atom.functor.isSymbol())
			{
				body.add(Instruction.dequeueatom(
						lhsatomToPath(atom) // �� lhsmems.size() + i �˰��פ���
				));
			}
		}
	}
	/** ���դ�������¦����Ƶ�Ū�˽���롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int removeLHSMem(Membrane mem)
	{
		int procvarcount = mem.processContexts.size();
		for (Membrane submem : mem.mems)
		{
			int subcount = removeLHSMem(submem);
			body.add(new Instruction(Instruction.REMOVEMEM, lhsmemToPath(submem), lhsmemToPath(mem))); //��2�����ɲ� by mizuno
			if (subcount > 0)
			{
				body.add(new Instruction(Instruction.REMOVEPROXIES, lhsmemToPath(submem)));
			}
			procvarcount += subcount;
		}
		return procvarcount;
	}

	//

	HashMap<ProcessContext, Integer> rhsmappaths = new HashMap<ProcessContext, Integer>();	// ���դ�������$p�и�(ProcessContext) -> map���ѿ��ֹ�(Integer)
	static final int NOTCOPIED = -1;		// rhsmappaths̤��Ͽ������
	private int rhspcToMapPath(ProcessContext pc)
	{
		if (!rhsmappaths.containsKey(pc)) return NOTCOPIED;
		return rhsmappaths.get(pc).intValue();
	}

	//

	private boolean fUseMoveCells = true;	// ����$p���Ф���MOVECELLS��Ȥ���Ŭ�����뤫�ʳ�ȯ�������ѿ���

	/** ��γ��ع�¤����ӥץ���ʸ̮�����Ƥ����¦����Ƶ�Ū���������롣
	 * @return ��mem�������˽и������ץ���ʸ̮�θĿ� */
	private int buildRHSMem(Membrane mem) throws CompileException
	{
		int procvarcount = mem.processContexts.size();
		for (ProcessContext pc : mem.processContexts)
		{
			if (pc.def.lhsOcc.mem == null)
			{
				systemError("SYSTEM ERROR: ProcessContext.def.lhsOcc.mem is not set");
			}
			if (rhsmemToPath(mem) != lhsmemToPath(pc.def.lhsOcc.mem))
			{
				if (fUseMoveCells && /*pc.def.rhsOccs.get(0) == pc*/ pc.def.rhsOccs.size() == 1)
				{
					body.add(new Instruction(Instruction.MOVECELLS,
							rhsmemToPath(mem), lhsmemToPath(pc.def.lhsOcc.mem) ));
				}
				else
				{
					int rethashmap = varcount++;
					body.add(new Instruction(Instruction.COPYCELLS,
							rethashmap, rhsmemToPath(mem), lhsmemToPath(pc.def.lhsOcc.mem) ));
					rhsmappaths.put(pc, rethashmap);
					//else {
					//	systemError("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + pc);
					//}
				}
			}
		}
		for (Membrane submem : mem.mems)
		{
			int submempath = varcount++;
			rhsmempath.put(submem, submempath);
			if (submem.pragmaAtHost != null) { // ���դǡ����ꤵ��Ƥ�����
				if (submem.pragmaAtHost.def == null)
				{
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

	/**
	 * �ץ���ʸ̮�򥢥ȥߥå��Ȥ��ư�����
	 */
	/*
	private int buildRHSMem_AtomicPC(Membrane mem) throws CompileException
	{
		Env.c("RuleCompiler::buildRHSMem" + mem);
		int procvarcount = mem.processContexts.size();
		for (ProcessContext pc : mem.processContexts)
		{
			if (pc.def.lhsOcc.mem == null)
			{
				systemError("SYSTEM ERROR: ProcessContext.def.lhsOcc.mem is not set");
			}
			if (rhsmemToPath(mem) != lhsmemToPath(pc.def.lhsOcc.mem))
			{
				System.err.println("ProcessContext: " + pc + " (copied from " + pc.def.lhsOcc + " in mem " + lhsmemToPath(pc.def.lhsOcc.mem) + ")");
				if (!lhsatoms.contains(pc.def.lhsOcc))
				{
					lhsatoms.add(pc.def.lhsOcc);
					lhsatompath.put(pc.def.lhsOcc, procvarcount++);
				}
				rhsAtomics.add(pc);
				rhsAtomicPath.put(pc, procvarcount++);
				body.add(new Instruction(Instruction.COPYATOM, rhsAtomicPath.get(pc), lhsatompath.get(pc.def.lhsOcc)));
			}
		}
		for (Membrane submem : mem.mems)
		{
			int submempath = varcount++;
			rhsmempath.put(submem, submempath);
			if (submem.pragmaAtHost != null) // ���դǡ����ꤵ��Ƥ�����
			{
				if (submem.pragmaAtHost.def == null) {
					systemError("SYSTEM ERROR: pragmaAtHost.def is not set: " + submem.pragmaAtHost.getQualifiedName());
				}
				int nodedescatomid = typedcxtToSrcPath(submem.pragmaAtHost.def);
				body.add( new Instruction(Instruction.NEWROOT, submempath, rhsmemToPath(mem),
						nodedescatomid, submem.kind) );
			}
			else // �̾�α�����ξ��
			{
				body.add( Instruction.newmem(submempath, rhsmemToPath(mem), submem.kind ) );
			}
			if (submem.name != null)
			{
				body.add(new Instruction( Instruction.SETMEMNAME, submempath, submem.name.intern() ));
			}
			int subcount = buildRHSMem_AtomicPC(submem);
			// ������Υץ��������Ǥʤ���硢insertproxies̿���ȯ��
			if (subcount > 0)
			{
				body.add(new Instruction(Instruction.INSERTPROXIES,
						rhsmemToPath(mem), rhsmemToPath(submem)));
			}
			procvarcount += subcount;
		}
		return procvarcount;
	}
	*/

	/** ���դ�����Υ롼��ʸ̮�����Ƥ��������� */
	private void copyRules(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			copyRules(submem);
		}
		for (RuleContext rc : mem.ruleContexts)
		{
			if (rhsmemToPath(mem) == lhsmemToPath(rc.def.lhsOcc.mem)) continue;
			body.add(new Instruction( Instruction.COPYRULES, rhsmemToPath(mem), lhsmemToPath(rc.def.lhsOcc.mem) ));
		}
	}

	/** ���դ�����Υ롼������Ƥ��������� */
	private void loadRulesets(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			loadRulesets(submem);
		}
		for (Ruleset ruleset : mem.rulesets)
		{
			//if (!mem.rules.isEmpty())
			//{
			body.add(Instruction.loadruleset(rhsmemToPath(mem), ruleset));
			//}
		}
	}

	/** ���դ�����Υ��ȥ���������롣
	 * ñ�첽���ȥ�ʤ��UNIFY̿�����������
	 * ����ʳ��ʤ�б��դΥ��ȥ�Υꥹ��rhsatoms���ɲä��롣 */
	private void buildRHSAtoms(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			buildRHSAtoms(submem);
		}
		for (Atom atom : mem.atoms)
		{
			if (atom.functor.equals(Functor.UNIFY))
			{
				LinkOccurrence link1 = atom.args[0].buddy;
				LinkOccurrence link2 = atom.args[1].buddy;
				body.add(new Instruction( Instruction.UNIFY,
						lhsatomToPath(link1.atom), link1.pos,
						lhsatomToPath(link2.atom), link2.pos, rhsmemToPath(mem) ));
			}
			else
			{
				int atomid = varcount++;
				rhsatompath.put(atom, atomid);
				rhsatoms.add(atom);
				body.add(Instruction.newatom(atomid, rhsmemToPath(mem), atom.functor));
			}
		}
	}

	private void buildRHSAtoms_swaplink(Membrane mem, Set<Atomic> created, Map<Atom, Atom> reused)
	{
		for (Membrane submem : mem.mems)
		{
			buildRHSAtoms_swaplink(submem, created, reused);
		}
		for (Atom atom : mem.atoms)
		{
			if (atom.functor.equals(Functor.UNIFY))
			{
				LinkOccurrence link1 = atom.args[0].buddy;
				LinkOccurrence link2 = atom.args[1].buddy;
				body.add(new Instruction(Instruction.UNIFY,
					lhsatomToPath(link1.atom), link1.pos,
					lhsatomToPath(link2.atom), link2.pos, rhsmemToPath(mem)));
			}
			else
			{
				int atomid;
				if (reused.containsKey(atom))
				{
					atomid = lhsatomToPath(reused.get(atom));
				}
				else
				{
					atomid = varcount++;
				}
				rhsatompath.put(atom, atomid);
				//rhsAtomicPath.put(atom, atomid);
				if (created.contains(atom))
				{
					body.add(Instruction.newatom(rhsatomToPath(atom), rhsmemToPath(mem), atom.functor));
				}
			}
		}
	}

	/**
	 * <p>�롼��κ��դȱ��դ��ȤˤʤäƤ����󥯽и��ν�������롣</p>
	 */
	private Set<LinkOccurrence> getFreeLinkOccurrence()
	{
		Set<LinkOccurrence> freeLinks = new HashSet<LinkOccurrence>();
		for (Atomic a1 : lhsatoms)
		{
			for (Atomic a2 : rhsatoms)
			{
				for (int i = 0; i < a1.getArity(); i++)
				{
					for (int j = 0; j < a2.getArity(); j++)
					{
						if (a1.args[i].buddy == a2.args[j])
						{
							freeLinks.add(a1.args[i]);
							freeLinks.add(a2.args[j]);
							break;
						}
					}
				}
			}
		}
		return freeLinks;
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
	private void insertconnectors()
	{
		for (ContextDef def : rs.processContexts.values())
		{
			if (def.rhsOccs.size() < 2) continue;
			List<Integer> linklist = new ArrayList<Integer>();
			int setpath = varcount++;
			for (int i = 0; i < def.lhsOcc.args.length; i++)
			{
				if (!lhslinkpath.containsKey(def.lhsOcc.args[i]))
				{
					int linkpath = varcount++;
					body.add(new Instruction(Instruction.GETLINK,linkpath,
							lhsatomToPath(def.lhsOcc.args[i].buddy.atom),def.lhsOcc.args[i].buddy.pos));
					lhslinkpath.put(def.lhsOcc.args[i], linkpath);
				}
				int srclink = lhslinkToPath(def.lhsOcc.args[i]);
				linklist.add(srclink);
			}
			body.add(new Instruction( Instruction.INSERTCONNECTORS,
					setpath,linklist,lhsmemToPath(def.lhsOcc.mem) ));
			cxtlinksetpaths.put(def, setpath);

		}
		for (ContextDef def : rs.typedProcessContexts.values())
		{
			if (gc.typedCxtTypes.get(def) != GuardCompiler.GROUND_LINK_TYPE) continue;
			List<Integer> linklist = new ArrayList<Integer>();
			int setpath = varcount++;
			for (int i = 0; i < def.lhsOcc.args.length; i++)
			{
				if (!lhslinkpath.containsKey(def.lhsOcc.args[i]))
				{
					int linkpath = varcount++;
					body.add(new Instruction(Instruction.GETLINK,linkpath,
							lhsatomToPath(def.lhsOcc.args[i].buddy.atom),def.lhsOcc.args[i].buddy.pos));
					lhslinkpath.put(def.lhsOcc.args[i], linkpath);
				}
				int srclink = lhslinkToPath(def.lhsOcc.args[i]);
				linklist.add(srclink);
			}
			body.add(new Instruction(Instruction.INSERTCONNECTORSINNULL,
					setpath,linklist));//,lhsmemToPath(def.lhsOcc.mem)));
			cxtlinksetpaths.put(def, setpath);
		}
	}

	/** ��Ǻ��줿�ޥåפ�������Ƥ���set�ȡ����ȥ��ԡ����˺�ä��ޥåפ�
	 * �����ˤ��ơ�deleteconnectors̿���ȯ�Ԥ��롣
	 *
	 */
	private void deleteconnectors()
	{
		/*
		Iterator it = rs.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
		 */
		for (ContextDef def : rs.processContexts.values())
		{
			Iterator it2 = def.rhsOccs.iterator();
			if (def.rhsOccs.size() < 2)continue;
			while (it2.hasNext())
			{
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
		for (ContextDef def : rs.typedProcessContexts.values())
		{
			if (gc.typedCxtTypes.get(def) == GuardCompiler.GROUND_LINK_TYPE)
			{
				Iterator it2 = def.rhsOccs.iterator();
				while (it2.hasNext())
				{
					ProcessContext pc = (ProcessContext)it2.next();
					body.add(new Instruction(Instruction.DELETECONNECTORS,
							cxtlinksetpaths.get(def).intValue(),
							rhspcToMapPath(pc)));
//					rhsmemToPath(pc.mem)));
				}
			}
		}
	}

	/**
	 * ��󥯤�ĥ���ؤ���������Ԥ�
	 */
	private void updateLinks() throws CompileException
	{
		if (true)
		{
			for (LinkOccurrence link : computeRHSLinks())
			{
				int linkpath = getLinkPath(link);
				int buddypath = getLinkPath(link.buddy);
				Membrane mem = link.atom.mem;
				int mempath = rhsmemToPath(mem);
				body.add(new Instruction(Instruction.UNIFYLINKS,linkpath,buddypath,mempath));
			}
		}
	}

	/**
	 * <p>��󥯸ߴ�̿��{@code swaplink}����Ѥ��ƥ�����򥳥�ѥ��뤷�ޤ���</p>
	 */
	private void compileLinkOperations(Set<Atomic> removed, Set<Atomic> created, Map<Atom, Atom> reusable)
	{
		Env.c("RuleCompiler::compileLinkOperations");
		
		System.err.println("Compiling: " + lhsatoms + " :- " + rhsatoms);
		
		Set<LinkOccurrence> freeLinks = getFreeLinkOccurrence();
		//System.err.println("FreeLinks      : " + freeLinks);
		
		// �ե��󥯥���Ʊ����ΤˤϺ����Ʊ��������������Ƥ�
		//System.err.println("Correspondings : " + reusable);
		//System.err.println("Removed        : " + removed);
		//System.err.println("Created        : " + created);
		
		int linkCount = countLinkOccurrence(reusable.keySet()) / 2
			+ countLinkOccurrence(removed)
			+ countLinkOccurrence(created);
		
		// ���̾ -> �����
		Map<LinkOccurrence, Integer> order = new HashMap<LinkOccurrence, Integer>();
		LinkOccurrence[] links1 = new LinkOccurrence[linkCount];
		LinkOccurrence[] links2 = new LinkOccurrence[linkCount];
		int id = 0;
		for (Map.Entry<Atom, Atom> entry : reusable.entrySet())
		{
			Atom al = entry.getKey(), ar = entry.getValue();
			if (!lhsatoms.contains(al)) continue;
			for (int i = 0; i < al.getArity(); i++)
			{
				LinkOccurrence l1 = al.args[i];
				LinkOccurrence l2 = ar.args[i];
				if (freeLinks.contains(l1))
				{
					links1[id + i] = l1;
					order.put(l1, id + i);
				}
				else
				{
					links1[id + i] = new LinkOccurrence(".", l1.atom, l1.pos);
				}
				if (freeLinks.contains(l2))
				{
					links2[id + i] = l2;
				}
				else
				{
					links2[id + i] = new LinkOccurrence(".", l2.atom, l2.pos);
				}
			}
			id += al.getArity();
		}
		for (Atomic a : removed)
		{
			for (LinkOccurrence link : a.args)
			{
				if (freeLinks.contains(link))
				{
					links1[id] = link;
					order.put(link, id);
				}
				else
				{
					links1[id] = new LinkOccurrence(".", link.atom, link.pos);
				}
				links2[id] = new LinkOccurrence(".", link.atom, link.pos);
				id++;
			}
		}
		for (Atomic a : created)
		{
			for (LinkOccurrence link : a.args)
			{
				links1[id] = new LinkOccurrence(".", link.atom, link.pos);
				order.put(link, id);
				if (freeLinks.contains(link))
				{
					links2[id] = link;
				}
				else
				{
					links2[id] = new LinkOccurrence(".", link.atom, link.pos);
				}
				id++;
			}
		}
		//System.err.println("Links1  : " + Arrays.toString(links1));
		//System.err.println("Links2  : " + Arrays.toString(links2));
		
		// �ִ�
		System.err.println(Arrays.toString(links1));
		for (int i = 0; i < links2.length; i++)
		{
			LinkOccurrence link = links2[i].buddy;
			Integer j = order.get(link);
			if (freeLinks.contains(link) && i != j)
			{
				Atomic a1 = links1[i].atom;
				Atomic a2 = links1[j].atom;
				
				/*
				System.err.printf("swap: %s.%d <-> %s.%d\n", links1[i].atom, links1[i].pos, links1[j].atom, links1[j].pos);
				System.err.println(i + " <-> " + j);
				//*/
				
				if (created.contains(a1))
				{
					body.add(swaplink(
						rhsatompath.get(a1), links1[i].pos,
						lhsatompath.get(a2), links1[j].pos));
				}
				else if (created.contains(a2))
				{
					body.add(swaplink(
						lhsatompath.get(a1), links1[i].pos,
						rhsatompath.get(a2), links1[j].pos));
				}
				else
				{
					body.add(swaplink(
						lhsatompath.get(a1), links1[i].pos,
						lhsatompath.get(a2), links1[j].pos));
				}
				LinkOccurrence buddy1 = links1[i].buddy;
				LinkOccurrence buddy2 = links1[j].buddy;
				
				links1[i].buddy = buddy2;
				if (buddy2 != null) buddy2.buddy = links1[i];
				
				links1[j].buddy = buddy1;
				if (buddy1 != null) buddy1.buddy = links1[j];
				
				String tmpName = links1[i].name;
				links1[i].name = links1[j].name;
				links1[j].name = tmpName;
				
				System.err.println(Arrays.toString(links1));
			}
		}
		//System.err.println("E Links    : " + Arrays.toString(links1));
		for (Atomic a : created)
		{
			for (LinkOccurrence l1 : a.args)
			{
				if (!freeLinks.contains(l1))
				{
					LinkOccurrence l2 = l1.buddy;
					Instruction inst = Instruction.newlink(
						rhsatompath.get(l1.atom), l1.pos,
						rhsatompath.get(l2.atom), l2.pos,
						rhsmempath.get(l1.atom.mem));
					body.add(inst);
					freeLinks.add(l1);
					freeLinks.add(l2);
				}
			}
		}
	}

	/**
	 * <p>��󥯽���ִ�̿��{@code cyclelinks}����Ѥ��ƥ�����򥳥�ѥ��뤷�ޤ���</p>
	 */
	private void compileCycleLinks(Set<Atomic> removed, Set<Atomic> created, Map<Atom, Atom> reusable)
	{
		Env.c("RuleCompiler::compileCycleLinks");
		
		System.err.println(lhsatoms + " :- " + rhsatoms);
		
		Set<LinkOccurrence> freeLinks = getFreeLinkOccurrence();
		
		//System.err.println("Correspondings : " + reusable);
		//System.err.println("Removed        : " + removed);
		//System.err.println("Created        : " + created);
		
		// �롼��ξ�դΥ�󥯽и����½�������ǿ�
		int linkCount = countLinkOccurrence(reusable.keySet()) / 2
			+ countLinkOccurrence(removed)
			+ countLinkOccurrence(created);
		
		// ���̾ -> �����
		Map<LinkOccurrence, Integer> order = new HashMap<LinkOccurrence, Integer>();
		LinkOccurrence[] links1 = new LinkOccurrence[linkCount];
		LinkOccurrence[] links2 = new LinkOccurrence[linkCount];
		int id = 0;
		for (Map.Entry<Atom, Atom> entry : reusable.entrySet())
		{
			Atom al = entry.getKey(), ar = entry.getValue();
			if (!lhsatoms.contains(al)) continue;
			for (int i = 0; i < al.getArity(); i++)
			{
				LinkOccurrence l1 = al.args[i];
				if (freeLinks.contains(l1))
				{
					order.put(l1, id + i);
					links1[id + i] = l1;
				}
				else
				{
					links1[id + i] = new LinkOccurrence(".", l1.atom, l1.pos);
				}
				LinkOccurrence l2 = ar.args[i];
				if (freeLinks.contains(l2))
				{
					links2[id + i] = l2;
				}
				else
				{
					links2[id + i] = new LinkOccurrence(".", l2.atom, l2.pos);
				}
			}
			id += al.getArity();
		}
		for (Atomic a : removed)
		{
			for (LinkOccurrence link : a.args)
			{
				if (freeLinks.contains(link))
				{
					order.put(link, id);
					links1[id] = link;
				}
				else
				{
					links1[id] = new LinkOccurrence(".", link.atom, link.pos);
				}
				links2[id] = new LinkOccurrence(".", link.atom, link.pos);
				id++;
			}
		}
		for (Atomic a : created)
		{
			for (LinkOccurrence link : a.args)
			{
				links1[id] = new LinkOccurrence(".", link.atom, link.pos);
				if (freeLinks.contains(link))
				{
					links2[id] = link;
				}
				else
				{
					links2[id] = new LinkOccurrence(".", link.atom, link.pos);
				}
				id++;
			}
		}
		
		//System.err.println(Arrays.toString(links1));
		//System.err.println(Arrays.toString(links2));
		
		// ����ִ�
		boolean[] checked = new boolean[links1.length];
		for (int i = 0; i < links1.length; i++)
		{
			if (links1[i].name.equals(".") || links1[i].buddy == links2[i] || checked[i]) continue;
			
			//System.err.println(Arrays.toString(checked));
			
			List<Integer> alist = new ArrayList<Integer>();
			List<Integer> plist = new ArrayList<Integer>();
			
			int j = i;
			//System.err.print("( ");
			do
			{
				checked[j] = true;
				//System.err.print(links1[j] + " <- " + links2[j] + " ");
				//System.err.print(links1[j] + " ");
				//System.err.print(j + " ");
				
				if (links1[j].name.equals("."))
				{
					alist.add(rhsatompath.get(links1[j].atom));
				}
				else
				{
					alist.add(lhsatompath.get(links1[j].atom));
				}
				
				plist.add(links1[j].pos);
				
				if (links2[j].name.equals("."))
				{
					for (j = 0; j < links1.length; j++)
					{
						if (!checked[j] && links1[j].name.equals(".") && !links2[j].name.equals("."))
						{
							break;
						}
					}
				}
				else
				{
					j = order.get(links2[j].buddy);
				}
			}
			while (j != i && j < links1.length);
			//System.err.println(")");
			body.add(new Instruction(Instruction.CYCLELINKS, alist, plist));
		}
		
		for (Atomic a : created)
		{
			for (LinkOccurrence l1 : a.args)
			{
				if (!freeLinks.contains(l1))
				{
					LinkOccurrence l2 = l1.buddy;
					body.add(newlink(
						rhsatompath.get(l1.atom), l1.pos,
						rhsatompath.get(l2.atom), l2.pos,
						rhsmempath.get(l1.atom.mem)));
					freeLinks.add(l1);
					freeLinks.add(l2);
				}
			}
		}
	}

	/**
	 * �롼������Ѳ����ʤ����ȥ����롣
	 * ���Υ��르�ꥺ��Ͻ���Ū�ǡ��ܼۤ����ʤ�Τ������ФǤ��ʤ���
	 * �Ĥޤꡢ����褬�롼������Υץ�������³���Ƥ�����Ϲ�θ���ʤ���
	 * ��θ�����硢��³��ΰ�Ϣ�Υץ����ˤĤ���Ʊ����Ƚ���Ԥ�ɬ�פ����롣
	 */
	private Set<Atomic> getInvariantAtomics()
	{
		Set<Atomic> nomodified = new HashSet<Atomic>();
		for (Atomic al : lhsatoms)
		{
			if (!(al instanceof Atom) || !((Atom)al).functor.isSymbol()) continue;
			
			for (Atom ar : rhsatoms)
			{
				if (!ar.functor.isSymbol()) continue;
				
				if (al.getName().equals(ar.getName()) && al.getArity() == ar.getArity())
				{
					boolean eq = true;
					for (int i = 0; i < al.getArity(); i++)
					{
						if (al.args[i].buddy != ar.args[i])
						{
							eq = false;
							break;
						}
					}
					if (eq)
					{
						int m1 = lhsmemToPath(al.mem);
						int m2 = rhsmemToPath(ar.mem);
						if (m1 != m2)
						{
							body.add(Instruction.removeatom(lhsatomToPath(al), m1, ((Atom)al).functor));
							body.add(new Instruction(Instruction.ADDATOM, m2, lhsatomToPath(al)));
						}
						nomodified.add(al);
						nomodified.add(ar);
						break;
					}
				}
			}
		}
		return nomodified;
	}

	/**
	 * <p>�����Ѳ�ǽ�ʥ��ȥ��Ʊ�����ȥ��Сˤ���ޤ���</p>
	 */
	private Map<Atom, Atom> getReusableAtomics(Set<Atomic> noModified)
	{
		Map<Atom, Atom> reusable = new HashMap<Atom, Atom>();
		for (Atomic al : lhsatoms)
		{
			//if (!(al instanceof Atom) || !((Atom)al).functor.isSymbol() || noModified.contains(al)) continue;
			if (!(al instanceof Atom) || noModified.contains(al)) continue;
			
			for (Atom ar : rhsatoms)
			{
				//if (!ar.functor.isSymbol() || noModified.contains(ar)) continue;
				if (noModified.contains(ar)) continue;
				
				if (!reusable.containsValue(ar) && isIsomorphic((Atom)al, ar))
				{
					int m1 = lhsmemToPath(al.mem);
					int m2 = rhsmemToPath(ar.mem);
					if (m1 != m2)
					{
						// TODO: moveatom̿��������������������
						body.add(new Instruction(Instruction.REMOVEATOM, lhsatomToPath(al), m1, ((Atom)al).functor));
						body.add(new Instruction(Instruction.ADDATOM, m2, lhsatomToPath(al)));
					}
					reusable.put((Atom)al, ar);
					reusable.put(ar, (Atom)al);
					break;
				}
			}
		}
		return reusable;
	}

	private Set<Atomic> getRemovedAtomics(Set<Atomic> noModified, Map<Atom, Atom> reused)
	{
		return getChangedAtomics(lhsatoms, noModified, reused);
	}

	private Set<Atomic> getCreatedAtomics(Set<Atomic> noModified, Map<Atom, Atom> reused)
	{
		return getChangedAtomics(rhsatoms, noModified, reused);
	}

	private Set<Atomic> getChangedAtomics(List<? extends Atomic> atoms, Set<Atomic> noModified, Map<Atom, Atom> reused)
	{
		Set<Atomic> set = new HashSet<Atomic>();
		for (Atomic a : atoms)
		{
			if (!noModified.contains(a) && !reused.containsKey(a))
			{
				set.add(a);
			}
		}
		return set;
	}

	private static Instruction swaplink(int a1, int pos1, int a2, int pos2)
	{
		return new Instruction(Instruction.SWAPLINK, a1, pos1, a2, pos2);
	}

	private static Instruction newlink(int a1, int pos1, int a2, int pos2, int memi)
	{
		return new Instruction(Instruction.NEWLINK, a1, pos1, a2, pos2, memi);
	}

	/**
	 * ���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ�
	 */
	private void enqueueRHSAtoms()
	{
		int index = body.size(); // �����Ƶ���Ŭ���θ��̤���粽���뤿�ᡢ�ս���Ѥ�ʥ����ɤ���������
		for(Atom atom : rhsatoms){
			if (atom.functor.isSymbol() && atom.functor.isActive() ) {
				body.add(index, new Instruction(Instruction.ENQUEUEATOM, rhsatomToPath(atom)));
			}
		}
	}
	
	/**
	 * ���դΥ��ȥ��¹ԥ��ȥॹ���å����Ѥ�(swaplink��)
	 */
	private void enqueueRHSAtoms_swaplink(Set<Atomic> created, Set<Atom> reused)
	{
		int index = body.size();
		
		// �������줿���ȥ�
		for(Atomic a : created)
		{
			Atom atom = (Atom)a;
			if (atom.functor.isSymbol() && atom.functor.isActive())
			{
				body.add(index, new Instruction(Instruction.ENQUEUEATOM, rhsatomToPath(a)));
			}
		}
		
		// �����Ѥ��줿���ȥ�
		for(Atom atom : reused)
		{
			if (!lhsatoms.contains(atom)) continue;
			if (atom.functor.isSymbol() && atom.functor.isActive())
			{
				body.add(index, new Instruction(Instruction.ENQUEUEATOM, lhsatomToPath(atom)));
			}
		}
	}

	/**
	 * hyperlink��Ϣ��̿�������������
	 */
	private void addHyperlink()
	{ //seiji
		for (Atom atom : rhsatoms)
		{
			int atomID = rhsatomToPath(atom);
			if (atom.functor.equals(new SymbolFunctor("-", 2)))
			{
				; //body.add( new Instruction(Instruction.REVERSEHLINK, rhsmemToPath(atom.mem), atomID));
			}
			else if (atom.functor.equals(new SymbolFunctor("><", 2)))
			{
				body.add( new Instruction(Instruction.UNIFYHLINKS, rhsmemToPath(atom.mem), atomID));
			}
			else if (atom.functor.equals(new SymbolFunctor(">*<", 2)))
			{
				body.add( new Instruction(Instruction.UNIFYHLINKS, rhsmemToPath(atom.mem), atomID));
			}
			else if (atom.functor.equals(new SymbolFunctor(">+<", 2)))
			{
				body.add( new Instruction(Instruction.UNIFYHLINKS, rhsmemToPath(atom.mem), atomID));
			}
			else if (atom.functor.equals(new SymbolFunctor(">>", 2)))
			{
				; //body.add( new Instruction(Instruction.UNIFYNAMECONAME, rhsmemToPath(atom.mem), atomID));
			}
			else if (atom.functor.equals(new SymbolFunctor("<<", 2)))
			{
				;
			}
		}
	}

	/**
	 * C������Хå���¹Ԥ���̿�����������
	 */
	private void addCallback()
	{
		for (Atom atom : rhsatoms)
		{
			if (atom.getName() == "$callback")
			{
				int atomID = rhsatomToPath(atom);
				body.add( new Instruction(Instruction.CALLBACK, rhsmemToPath(atom.mem), atomID));
			}
		}
	}

	/**
	 * ����饤�󥳡��ɤ�¹Ԥ���̿�����������
	 */
	private void addInline()
	{
		for (Atom atom : rhsatoms)
		{
			int atomID = rhsatomToPath(atom);
			Inline.register(unitName, atom.functor.getName());
			int codeID = Inline.getCodeID(unitName, atom.functor.getName());
			if (codeID == -1) continue;
			body.add( new Instruction(Instruction.INLINE, atomID, unitName, codeID));
		}
	}

	static final Functor FUNC_USE = new SymbolFunctor("use",1);

	/**
	 * �⥸�塼����ɤ߹���
	 */
	private void addRegAndLoadModules()
	{
		for (Atom atom : rhsatoms)
		{
			//REG
			if (atom.functor.getArity()==1 && atom.functor.getName().equals("module"))
			{
				Module.regMemName(atom.args[0].buddy.atom.getName(), atom.mem);
			}

			//LOAD
			if (atom.functor.equals(FUNC_USE))
			{
				body.add( new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem),
						atom.args[0].buddy.atom.getName()) );
			}
			String path = atom.getPath(); // .functor.path;
			if (path!=null && !path.equals(atom.mem.name))
			{
				// ���λ����Ǥϲ��Ǥ��ʤ��⥸�塼�뤬����Τ�̾���ˤ��Ƥ���
				body.add(new Instruction(Instruction.LOADMODULE, rhsmemToPath(atom.mem), path));
			}
		}
	}

	/**
	 * �ʺ����Ѥ��줿��ޤ��ϡ˿������롼������Ф��ơ���¹�줫����֤�UNLOCKMEM��ȯ�Ԥ��롣
	 * ���������ߤμ����Ǥϡ����λ����ǤϤޤ���Ϻ����Ѥ���Ƥ��ʤ���
	 */
	private void unlockReusedOrNewRootMem(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			unlockReusedOrNewRootMem(submem);
		}
		if (mem.pragmaAtHost != null) // ���դǡ����ꤵ��Ƥ�����
		{
			body.add(new Instruction(Instruction.UNLOCKMEM, rhsmemToPath(mem)));
		}
	}

	/**
	 * ���դ�����Ѵ�����
	 */
	private void freeLHSMem(Membrane mem)
	{
		for (Membrane submem : mem.mems)
		{
			freeLHSMem(submem);
			// �����Ѥ��줿���free���ƤϤ����ʤ�
			body.add(new Instruction(Instruction.FREEMEM, lhsmemToPath(submem)));
		}
	}

	/**
	 * ���դΥ��ȥ���Ѵ�����
	 */
	private void freeLHSAtoms()
	{
		for (int i = 0; i < lhsatoms.size(); i++)
		{
			body.add( new Instruction(Instruction.FREEATOM, lhsmems.size() + i ));
		}
	}

	/**
	 * ���դΥ��ȥ���˴�����(swaplink��)
	 */
	private void freeLHSAtoms_swaplink(Set<Atomic> removed)
	{
		for (Atomic a : removed)
		{
			int i = lhsatompath.get(a);
			body.add(new Instruction(Instruction.FREEATOM, i));
		}
	}

	/** �ǥХå���ɽ�� */
	private void showInstructions()
	{
		Env.d("--atomMatches:");
		for (Instruction inst : atomMatch)
		{
			Env.d(inst);
		}

		Env.d("--memMatch:");
		for (Instruction inst : memMatch)
		{
			Env.d(inst);
		}

		Env.d("--body:");
		for (Instruction inst : body)
		{
			Env.d(inst);
		}
	}

	////////////////////////////////////////////////////////////////

	/**
	 * ���顼���ϤȤȤ���㳰��ȯ���롣
	 */
	private void systemError(String text) throws CompileException
	{
		Env.error(text);
		throw new CompileException("SYSTEM ERROR");
	}

	/**
	 * <p>ʸ��{@code c}���ѿ����Ǥ��뤫Ĵ�٤ޤ���</p>
	 * @param c Ĵ�٤�ʸ��
	 * @return ʸ��{@code c}���ѿ����ξ��{@code true}�������Ǥʤ����{@code false}���֤��ޤ���
	 * TODO: ���Υ᥽�åɤϤ����ˤ���٤��ǤϤʤ�
	 */
	private static boolean isAlphabetOrDigit(char c)
	{
		return '0' <= c && c <= '9'
			|| 'A' <= c && c <= 'Z'
			|| 'a' <= c && c <= 'z';
	}
}
