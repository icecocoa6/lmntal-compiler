package type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import runtime.Functor;

import compile.structure.Atom;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

/**
 * This class infers type constrains from COMPILE STRUCTURE
 * 
 * @author kudo
 * @since 2006/06/03 (Sat.)
 */
public class TypeConstraintsInferer {

	/** membrane contains all processes */
	private Membrane root;

	private ConstraintSet constraints = new ConstraintSet();

	/**
	 * @param root
	 */
	public TypeConstraintsInferer(Membrane root) {
		this.root = root;
	}

	/**
	 * distinguish active atom from passive atom
	 * 
	 * @param atom
	 * @return
	 */
	private int outOfPassiveAtom(Atom atom) {
		return TypeEnv.outOfPassiveAtom(atom);
	}
	private boolean isActiveAtom(Atom atom){
		return outOfPassiveAtom(atom) == TypeEnv.ACTIVE;
	}

	public void infer() throws TypeConstraintException {
		// ���Ƥ���ˤĤ��ơ��롼��κ��պǳ����и����ɤ����ξ��������
		collectLHSMems();
		
		// TODO Active Head Condition ������å�����
		
		// �Ŀ�������������
		new QuantityInferrer().inferQuantity();
		
		// �и�������������
		inferOccurrence();
		// ���Ƥΰ����ˤĤ��ƥ⡼���ѿ������ѿ��򿶤�
		inferArgument();
		solvePathes();
		constraints.solveUnifyConstraints();
	}

	/** �����줪��Ӻ��սи���ν��� */
	private Set<Membrane> lhsmems = new HashSet<Membrane>();

	/**
	 * ���սи����$lhsmems����Ͽ����
	 * @param mem
	 */
	private void collectLHSMems(){
		Iterator<RuleStructure> it = root.rules.iterator();
		while(it.hasNext()){
			collectLHSMems(it.next());
		}
	}
	/**
	 * ���սи����$lhsmems����Ͽ����
	 * @param rule
	 */
	private void collectLHSMems(RuleStructure rule){
		collectLHSMem(rule.leftMem);
//		 ���դ˥롼��Ͻи����ʤ�
		Iterator<RuleStructure> it = rule.rightMem.rules.iterator();
		while(it.hasNext()){
			collectLHSMems(it.next());
		}
	}
	/**
	 * ���սи����$lhsmems����Ͽ����
	 * @param mem ���սи���
	 */
	private void collectLHSMem(Membrane mem){
		lhsmems.add(mem);
		Iterator it = mem.mems.iterator();
		while(it.hasNext()){
			Membrane cmem = (Membrane)it.next();
			collectLHSMem(cmem);
		}
	}
	
	/** ���դΥ��ȥफ�ɤ������֤� */
	private boolean isLHSAtom(Atom atom) {
		return isLHSMem(atom.mem);
	}

	/** ���դ��줫�ɤ������֤� */
	private boolean isLHSMem(Membrane mem) {
		return lhsmems.contains(mem);
	}

	/** �и������������� */
	private void inferOccurrence(){
		inferOccurrenceMembrane(root);
	}
	/** �и������������� */
	private void inferOccurrenceMembrane(Membrane mem){
		/** �����ƥ��֥��ȥ�ˤĤ��ƽи������ݤ� */
		Iterator<Atom> ita = mem.atoms.iterator();
		while(ita.hasNext()){
			Atom atom = ita.next();
			if(isActiveAtom(atom))
				add(new AtomOccurrenceConstraint(mem.name,atom.functor));
		}
		/** ����ˤĤ��ƽи������ݤ������� */
		Iterator<Membrane> itm = mem.mems.iterator();
		while(itm.hasNext()){
			Membrane child = itm.next();
			add(new MembraneOccurrenceConstraint(mem.name,child.name));
			inferOccurrenceMembrane(child);
		}
		/** �롼��κ��ա����դ����� */
		Iterator<RuleStructure> itr = mem.rules.iterator();
		while(itr.hasNext()){
			RuleStructure rule = itr.next();
			inferOccurrenceMembrane(rule.leftMem);
			inferOccurrenceMembrane(rule.rightMem);
		}
	}

	/** ���������������� */
	private void inferArgument() throws TypeConstraintException{
		new ArgumentInferrer(root).infer();
	}

	/**
	 * If $lo or $b is output-argument, add receive-passive-constraint. Else,
	 * add unify-constraint.
	 * 
	 * @param sign
	 * @param lo
	 * @param b
	 */
	private void addConstraintAboutLinks(int sign, LinkOccurrence lo, LinkOccurrence b) throws TypeConstraintException{
		int out = outOfPassiveAtom((Atom)lo.atom);
		if(out == lo.pos){ // �ǡ������ȥ�ν��ϰ���
			if(outOfPassiveAtom((Atom)b.atom) == b.pos)//!= TypeEnv.ACTIVE)
				throw new TypeConstraintException("MODE ERROR : output arguments connected each other.");
			else addReceiveConstraint(-sign, b, ((Atom)lo.atom).functor);
		}
		else{
			if(outOfPassiveAtom((Atom)b.atom) == b.pos) //!= TypeEnv.ACTIVE)
				addConstraintAboutLinks(sign, b, lo);
			else addUnifyConstraint(sign, lo, b);
		}
	}

	private void addUnifyConstraint(int sign, LinkOccurrence lo,
			LinkOccurrence b) {
		add(new UnifyConstraint(new PolarizedPath(1, new RootPath(lo)),
				new PolarizedPath(sign, new RootPath(b))));
	}

	private void addReceiveConstraint(int sign, LinkOccurrence b, Functor f) {
		add(new ReceiveConstraint(new PolarizedPath(sign, new RootPath(b)), f));
	}

	/**
	 * get real buddy through =/2, $out, $in
	 * 
	 * @param lo
	 * @return
	 */
	private LinkOccurrence getRealBuddy(LinkOccurrence lo) {
		if (lo.buddy.atom instanceof Atom) {
			Atom a = (Atom) lo.buddy.atom;
			int o = outOfPassiveAtom(a);
			if (o == TypeEnv.CONNECTOR)
				return getRealBuddy(a.args[1 - lo.buddy.pos]);
			else
				return lo.buddy;
		} else
			return lo.buddy;
	}

	private void add(Constraint c) {
		constraints.add(c);
	}

	/**
	 * change RootPath into ActiveAtomPath or TracingPath.
	 * 
	 */
	private void solvePathes() {
		Set unSolvedRPCs = constraints.getReceivePassiveConstraints();
		Iterator it = unSolvedRPCs.iterator();
		while (it.hasNext()) {
			ReceiveConstraint rpc = (ReceiveConstraint) it.next();
			rpc.setPPath(solvePolarizedPath(rpc.getPPath()));
		}
		constraints.refreshReceivePassiveConstraints(unSolvedRPCs);
		Set unSolvedUCs = constraints.getUnifyConstraints();
		it = unSolvedUCs.iterator();
		while (it.hasNext()) {
			UnifyConstraint uc = (UnifyConstraint) it.next();
			uc.setPPathes(solvePolarizedPath(uc.getPPath1()),
					solvePolarizedPath(uc.getPPath2()));
		}
	}

	private PolarizedPath solvePolarizedPath(PolarizedPath pp) {
		Path p = pp.getPath();
		if (!(p instanceof RootPath)) {
			System.out.println("fatal error in solving path.");
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
		int out = outOfPassiveAtom(atom);
		if (out == TypeEnv.ACTIVE) {
			return new PolarizedPath(1, new ActiveAtomPath(atom.mem.name,
					atom.functor, lo.pos));
		} else if (out == TypeEnv.CONNECTOR) {
			System.out.println("fatal error in getting path.");
			return null;
		} else {
			LinkOccurrence tl = getRealBuddy(atom.args[out]);
			if (!(tl.atom instanceof Atom))
				return new PolarizedPath(1, new RootPath(tl));
			Atom ta = (Atom) tl.atom;
			PolarizedPath pp = getPolarizedPath(tl);
			if (isLHSAtom(atom) == isLHSAtom(ta))
				return new PolarizedPath(pp.getSign(), new TracingPath(pp
						.getPath(), atom.functor, lo.pos));
			else
				return new PolarizedPath(-pp.getSign(), new TracingPath(pp
						.getPath(), atom.functor, lo.pos));
		}
	}

	public void printAllConstraints() {
		constraints.printAllConstraints();
	}

	/**
	 * �롼�뤴�Ȥˡ����ȥ�����η����⡼�ɤ�������롣
	 * ʸ̮������롼�뤴�Ȥ˰����ۤ����Թ礬�ɤ��١�
	 * @author kudo
	 *
	 */
	class ArgumentInferrer{
		RuleStructure rule;
		
		Set<ContextDef> defs;

		/** */
		ArgumentInferrer(RuleStructure rule){
			this.rule = rule;
		}
		/**
		 * �����Х�롼������Ф��ƤΤ߸ƤФ��
		 * @param top
		 */
		ArgumentInferrer(Membrane top){
			RuleStructure tmprule = new RuleStructure(new Membrane(null),"tmp");
			tmprule.leftMem = new Membrane(null);
			tmprule.rightMem = top;
			this.rule = tmprule;
		}
		void infer() throws TypeConstraintException{
			defs = new HashSet<ContextDef>();
			inferArgumentRule(rule);
		}
		/**
		 * @param mem
		 * @param freelinks
		 *            free links already checked
		 * @return free links in the process at mem
		 */
		private Set<LinkOccurrence> inferArgumentMembrane(Membrane mem, Set<LinkOccurrence> freelinks) throws TypeConstraintException{

			//�롼��ˤĤ�����������
			Iterator<RuleStructure> itr = mem.rules.iterator();
			while (itr.hasNext()) {
				new ArgumentInferrer(itr.next()).infer();
			}

			// ����ˤĤ�����������
			Iterator<Membrane> itm = mem.mems.iterator();
			while (itm.hasNext()) {
				freelinks = inferArgumentMembrane(itm.next(), freelinks);
			}

			// ���λ����ǡ���¹��˽и��������ƤΥ��ȥࡿ�ץ���ʸ̮�ˤĤ��ơ��ɽ��󥯤ν����Ͻ���äƤ���
			
			Iterator<Atom> ita = mem.atoms.iterator();
			while (ita.hasNext()) {
				Atom atom = ita.next();
				int out = outOfPassiveAtom(atom);
				if (out == TypeEnv.CONNECTOR) continue; // '='/2���ä���̵�뤹��
				else
					freelinks = inferArgumentAtom(atom, freelinks);
			}

			// ���λ����ǡ���¹��˽и��������ƤΥ��ȥࡿ�ץ���ʸ̮������Ӥ�����Υ��ȥ�ΰ����ν����Ͻ���äƤ���
			// �Ĥޤ�Ĥ�Ϥ�����˽и�����ץ���ʸ̮�ȡ�������μ�ͳ��󥯤Τ�
			
			// �ץ���ʸ̮
			Iterator<ProcessContext> itp = mem.processContexts.iterator();
			while(itp.hasNext()){
				ContextDef def = itp.next().def;
				if(!defs.contains(def))defs.add(def);
			}
			//���դ��ץ���ʸ̮
			Iterator<ProcessContext> ittp = mem.typedProcessContexts.iterator();
			while(ittp.hasNext()){
				ContextDef def = ittp.next().def;
				if(!defs.contains(def))defs.add(def);
			}
			return freelinks;
		}

		/**
		 * inferrence
		 * 
		 * @param rule
		 */
		private void inferArgumentRule(RuleStructure rule) throws TypeConstraintException{
			// ���ա����դ��줾��ˤĤ��Ʒ����⡼�ɤ��褷��1��и������󥯤򽸤��
			Set<LinkOccurrence> freelinksLeft = inferArgumentMembrane(rule.leftMem, new HashSet<LinkOccurrence>());
			Set<LinkOccurrence> freelinksRight = inferArgumentMembrane(rule.rightMem, new HashSet<LinkOccurrence>());
			Iterator<LinkOccurrence> it = freelinksLeft.iterator();
			while (it.hasNext()) {
				LinkOccurrence leftlink = it.next();
				LinkOccurrence rightlink = getRealBuddy(leftlink);
				if (!freelinksRight.contains(rightlink)){ // ��󥯤����ա����սи��ʤ�
					throw new TypeConstraintException("link occurs once in a rule.");
				}
				addConstraintAboutLinks(1, leftlink, rightlink);
			}
			//�ץ���ʸ̮�ˤĤ��ƽ�������
			Iterator<ContextDef> itd = defs.iterator();
			while(itd.hasNext()){
				ContextDef def = itd.next();
				Iterator<ProcessContext> itp = def.rhsOccs.iterator();
				while(itp.hasNext()){
					processExplicitLinks((ProcessContext)def.lhsOcc, itp.next());
				}
			}
		}

		/**
		 * ���ȥ�ΰ�������������1���ܤνи���freelinks����Ͽ����
		 * 2���ܤνи��Ǥ���жɽ��󥯤Ȥ��������ݤ���
		 * �������ץ���ʸ̮����³���Ƥ������̵�뤹�롣
		 * @param atom
		 * @param freelinks
		 * @return
		 */
		private Set<LinkOccurrence> inferArgumentAtom(Atom atom, Set<LinkOccurrence> freelinks)throws TypeConstraintException{
			for (int i = 0; i < atom.args.length; i++) {
				LinkOccurrence lo = atom.args[i];
				LinkOccurrence b = getRealBuddy(lo);
				if(b.atom instanceof ProcessContext)continue; // �ץ���ʸ̮����³���Ƥ���
				if (freelinks.contains(b)) { // �ɽ���
					addConstraintAboutLinks(-1, lo, b);
					freelinks.remove(b);
				} else
					freelinks.add(lo);
			}
			return freelinks;
		}
		
		/** �ץ���ʸ̮�κ��ա����սи��Τ��줾��ΰ������Ф���Ʊ���Ǥ���Ȥ�������򤫤��� */
		private void processExplicitLinks(ProcessContext lhsOcc, ProcessContext rhsOcc){
			for(int i=0;i<lhsOcc.args.length;i++){
				LinkOccurrence lhsPartner = getRealBuddy(lhsOcc.args[i]);
				LinkOccurrence rhsPartner = getRealBuddy(rhsOcc.args[i]);
				if(rhsPartner.atom instanceof Atom){
					if(isLHSAtom((Atom)rhsPartner.atom))
						addUnifyConstraint(-1,lhsPartner,rhsPartner);
					else
						addUnifyConstraint(1,lhsPartner,rhsPartner);
				}
				else{ // ���սи����ץ���ʸ̮�ȷѤäƤ���
					// �����Ĥκ��սи���������ȤäƤ���
					LinkOccurrence partnerOfPartner = getRealBuddy(((ProcessContext)rhsPartner.atom).def.lhsOcc.args[i]);
					addUnifyConstraint(-1,lhsPartner, partnerOfPartner);
				}
			}
		}
	}

	/**
	 * ��Ū����(?)
	 * @author kudo
	 *
	 */
	class QuantityInferrer{
		QuantityInferrer(){
			
		}
		void inferQuantity(){
			
		}
		
		/** ���ȥ�ˤĤ��ƤΡ��̡פ�������륯�饹 */
		class AtomQuantity{
//			Functor f;
			int min = 0;
			int max = 0;
			AtomQuantity(){}
			AtomQuantity merge(AtomQuantity aq1, AtomQuantity aq2){
				AtomQuantity ret = new AtomQuantity();
				ret.min = (aq1.min<aq2.min)?aq1.min:aq2.min;
				ret.max = (aq1.max>aq2.max)?aq1.max:aq2.max;
				return ret;
			}
		}
		class Quantitys{
			Map<Functor,AtomQuantity> functorToQuantity;
			Quantitys(){
				functorToQuantity = new HashMap();
			}
			void putAtomQuantity(Functor f, AtomQuantity aq){
				if(!functorToQuantity.containsKey(f)){
					functorToQuantity.put(f,aq);
				}
				else{
					AtomQuantity aqold = functorToQuantity.get(f);
					functorToQuantity.put(f,aq.merge(aqold, aq));
				}
			}
		}
	}
}

