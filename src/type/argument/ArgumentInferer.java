package type.argument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import runtime.Functor;
import type.TypeEnv;
import type.TypeException;

import compile.structure.Atom;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

/**
 * �롼�뤴�Ȥˡ����ȥ�����η����⡼�ɤ�������롣
 * @author kudo
 *
 */
public class ArgumentInferer {
	private RuleStructure rule;
	
	/** �ץ�����ʸ̮����ν��� */
	private Set<ContextDef> defs;
	
	private ConstraintSet constraints;
	
	public ConstraintSet getConstraints(){
		return constraints;
	}

	/** */
	public ArgumentInferer(RuleStructure rule, ConstraintSet constraints){
		this.rule = rule;
		this.constraints = constraints;
	}

	/**
	 * �������Х�롼������Ф��ƤΤ߸ƤФ��
	 * @param top
	 */
	public ArgumentInferer(Membrane top){//, ConstraintSet constraints){
		RuleStructure tmprule = new RuleStructure(new Membrane(null),"tmp");
		tmprule.leftMem = new Membrane(null);
		tmprule.rightMem = top;
		this.rule = tmprule;
		this.constraints = new ConstraintSet();//constraints;
	}
	
	public void printAll(){
		constraints.printAllConstraints();
	}

	/**
	 * 
	 * @throws TypeException
	 */
	public void infer() throws TypeException{
		defs = new HashSet<ContextDef>();

		// TODO Active Head Condition ������å�����
		// ���Ƥΰ����ˤĤ��ƥ⡼���ѿ������ѿ��򿶤�
		inferArgumentRule(rule);

		//�ץ�����ʸ̮�ˤĤ��ƽ�������
		processLinksOfProcessContexts();
		
		solvePathes();
		constraints.solveUnifyConstraints();
		
	}
	
	/**
	 * �ץ�����ʸ̮�ΰ����ˤĤ��ƽ�������
	 */
	private void processLinksOfProcessContexts(){
		for(ContextDef def : defs)
			for(ProcessContext rhsOcc : (List<ProcessContext>)def.rhsOccs)
				processExplicitLinks((ProcessContext)def.lhsOcc, rhsOcc);
	}

	/**
	 * @param mem
	 * @param freelinks
	 *            free links already checked
	 * @return free links in the process at mem
	 */
	private Set<LinkOccurrence> inferArgumentMembrane(Membrane mem, Set<LinkOccurrence> freelinks) throws TypeException{

		//�롼��ˤĤ�����������
		for(RuleStructure rs : (List<RuleStructure>)mem.rules){
//			new ArgumentInferer(rs,constraints).infer();
			inferArgumentRule(rs);
		}

		// ����ˤĤ�����������
		for(Membrane child : (List<Membrane>)mem.mems){
			freelinks = inferArgumentMembrane(child, freelinks);
		}

		// ���λ����ǡ���¹��˽и��������ƤΥ��ȥࡿ�ץ�����ʸ̮�ˤĤ��ơ��ɽ��󥯤ν����Ͻ���äƤ���
		
		for(Atom atom : (List<Atom>)mem.atoms){
			if (TypeEnv.outOfPassiveAtom(atom) != TypeEnv.CONNECTOR) // '='/2���ä���̵�뤹��
				freelinks = inferArgumentAtom(atom, freelinks);
		}

		// ���λ����ǡ���¹��˽и��������ƤΥ��ȥࡿ�ץ�����ʸ̮������Ӥ�����Υ��ȥ�ΰ����ν����Ͻ���äƤ���
		// �Ĥޤ�Ĥ�Ϥ�����˽и�����ץ�����ʸ̮�ȡ�������μ�ͳ��󥯤Τ�
		
		//ʸ̮����򽸤��
		
		// �ץ�����ʸ̮
		for(ProcessContext pc : (List<ProcessContext>)mem.processContexts){
			ContextDef def = pc.def;
			if(!defs.contains(def))defs.add(def);
		}
		//���դ��ץ�����ʸ̮
		for(ProcessContext tpc : (List<ProcessContext>)mem.typedProcessContexts){
			ContextDef def = tpc.def;
			if(!defs.contains(def))defs.add(def);
		}
		return freelinks;
	}

	/**
	 * inferrence
	 * 
	 * @param rule
	 */
	private void inferArgumentRule(RuleStructure rule) throws TypeException{
		// ���ա����դ��줾��ˤĤ��Ʒ����⡼�ɤ��褷��1��и������󥯤򽸤��
		Set<LinkOccurrence> freelinksLeft = inferArgumentMembrane(rule.leftMem, new HashSet<LinkOccurrence>());
		Set<LinkOccurrence> freelinksRight = inferArgumentMembrane(rule.rightMem, new HashSet<LinkOccurrence>());
		for(LinkOccurrence leftlink : freelinksLeft){
			LinkOccurrence rightlink = TypeEnv.getRealBuddy(leftlink);
			if (!freelinksRight.contains(rightlink)) // ��󥯤����ա����սи��Ǥʤ��ʤ�
				throw new TypeException("link occurs once in a rule.");
			addConstraintAboutLinks(1, leftlink, rightlink);
		}
	}

	/**
	 * ���ȥ�ΰ�������������1���ܤνи���freelinks����Ͽ����
	 * 2���ܤνи��Ǥ���жɽ��󥯤Ȥ��������ݤ���
	 * �������ץ�����ʸ̮����³���Ƥ������̵�뤹�롣
	 * @param atom
	 * @param freelinks
	 * @return
	 */
	private Set<LinkOccurrence> inferArgumentAtom(Atom atom, Set<LinkOccurrence> freelinks)throws TypeException{
		for (int i = 0; i < atom.args.length; i++) {
			LinkOccurrence lo = atom.args[i];
			LinkOccurrence b = TypeEnv.getRealBuddy(lo);
			if(b.atom instanceof ProcessContext)continue; // �ץ�����ʸ̮����³���Ƥ���
			if (freelinks.contains(b)) { // �ɽ���
				addConstraintAboutLinks(-1, lo, b);
				freelinks.remove(b);
			} else
				freelinks.add(lo);
		}
		return freelinks;
	}
	
	/** �ץ�����ʸ̮�κ��ա����սи��Τ��줾��ΰ������Ф���Ʊ���Ǥ���Ȥ�������򤫤��� */
	private void processExplicitLinks(ProcessContext lhsOcc, ProcessContext rhsOcc){
		for(int i=0;i<lhsOcc.args.length;i++){
			LinkOccurrence lhsPartner = TypeEnv.getRealBuddy(lhsOcc.args[i]);
			LinkOccurrence rhsPartner = TypeEnv.getRealBuddy(rhsOcc.args[i]);
			if(rhsPartner.atom instanceof Atom){
				if(TypeEnv.isLHSAtom((Atom)rhsPartner.atom))
					addUnifyConstraint(-1,lhsPartner,rhsPartner);
				else
					addUnifyConstraint(1,lhsPartner,rhsPartner);
			}
			else{ // ���սи����ץ�����ʸ̮�ȷѤäƤ���
				// �����Ĥκ��սи���������ȤäƤ���
				LinkOccurrence partnerOfPartner = TypeEnv.getRealBuddy(((ProcessContext)rhsPartner.atom).def.lhsOcc.args[i]);
				addUnifyConstraint(-1,lhsPartner, partnerOfPartner);
			}
		}
	}

	/**
	 * If $lo or $b is output-argument, add receive-passive-constraint. Else,
	 * add unify-constraint.
	 * 
	 * @param sign
	 * @param lo
	 * @param b
	 */
	private void addConstraintAboutLinks(int sign, LinkOccurrence lo, LinkOccurrence b) throws TypeException{
		int out = TypeEnv.outOfPassiveAtom((Atom)lo.atom);
		if(out == lo.pos){ // �ǡ������ȥ�ν��ϰ���
			if(TypeEnv.outOfPassiveAtom((Atom)b.atom) == b.pos)//!= TypeEnv.ACTIVE)
				throw new TypeException("MODE ERROR : output arguments connected each other.");
			else addReceiveConstraint(-sign, b, ((Atom)lo.atom).functor);
		}
		else{
			if(TypeEnv.outOfPassiveAtom((Atom)b.atom) == b.pos) //!= TypeEnv.ACTIVE)
				addConstraintAboutLinks(sign, b, lo);
			else addUnifyConstraint(sign, lo, b);
		}
	}

	private void addUnifyConstraint(int sign, LinkOccurrence lo, LinkOccurrence b) {
		constraints.add(new UnifyConstraint(new PolarizedPath(1, new RootPath(lo)),
				new PolarizedPath(sign, new RootPath(b))));
	}

	private void addReceiveConstraint(int sign, LinkOccurrence b, Functor f) {
		constraints.add(new ReceiveConstraint(new PolarizedPath(sign, new RootPath(b)), f));
	}

	/**
	 * change RootPath into ActiveAtomPath or TracingPath.
	 * 
	 */
	private void solvePathes() {
		Set<ReceiveConstraint> unSolvedRPCs = constraints.getReceivePassiveConstraints();
		for(ReceiveConstraint rpc : unSolvedRPCs){
			rpc.setPPath(solvePolarizedPath(rpc.getPPath()));
		}
		constraints.refreshReceivePassiveConstraints(unSolvedRPCs);
		for(UnifyConstraint uc : constraints.getUnifyConstraints()){
			uc.setPPathes(solvePolarizedPath(uc.getPPath1()),
					solvePolarizedPath(uc.getPPath2()));
		}
	}

	private PolarizedPath solvePolarizedPath(PolarizedPath pp) {
		Path p = pp.getPath();
		if (!(p instanceof RootPath)) {
			System.out.println("fatal error in solving path.");
			if(p instanceof ActiveAtomPath)
				System.out.println("\tactive atom path");
			else if(p instanceof TracingPath)
				System.out.println("\ttracing path");
			return pp;
		}
		LinkOccurrence lo = ((RootPath) p).getTarget();
		if (!(lo.atom instanceof Atom))
			return pp;
		PolarizedPath tp = getPolarizedPath(lo);
		return new PolarizedPath(pp.getSign() * tp.getSign(), tp.getPath());
	}

	/**
	 * get the path to the active head atom.
	 * 
	 * @param lo :
	 *            argument of Atom (not Atomic)
	 * @return
	 */
	private PolarizedPath getPolarizedPath(LinkOccurrence lo) {
		Atom atom = (Atom) lo.atom;
		int out = TypeEnv.outOfPassiveAtom(atom);
		if (out == TypeEnv.ACTIVE) {
			return new PolarizedPath(1, new ActiveAtomPath(TypeEnv.getMemName(atom.mem),
					atom.functor, lo.pos));
		} else if (out == TypeEnv.CONNECTOR) {
			System.out.println("fatal error in getting path.");
			return null;
		} else {
			LinkOccurrence tl = TypeEnv.getRealBuddy(atom.args[out]);
			if (!(tl.atom instanceof Atom))
				return new PolarizedPath(1, new RootPath(tl));
			Atom ta = (Atom) tl.atom;
			PolarizedPath pp = getPolarizedPath(tl);
			if (TypeEnv.isLHSAtom(atom) == TypeEnv.isLHSAtom(ta))
				return new PolarizedPath(pp.getSign(), new TracingPath(pp
						.getPath(), atom.functor, lo.pos));
			else
				return new PolarizedPath(-pp.getSign(), new TracingPath(pp
						.getPath(), atom.functor, lo.pos));
		}
	}

}