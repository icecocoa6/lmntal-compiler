package type.argument;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import runtime.Functor;
import type.ConstraintSet;
import type.PolarizedPath;
import type.ReceiveConstraint;
import type.RootPath;
import type.TypeConstraintException;
import type.TypeEnv;
import type.UnifyConstraint;

import compile.structure.Atom;
import compile.structure.ContextDef;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

/**
 * �롼�뤴�Ȥˡ����ȥ�����η����⡼�ɤ�������롣
 * ʸ̮������롼�뤴�Ȥ˰����ۤ����Թ礬�ɤ��١�
 * @author kudo
 *
 */
public class ArgumentInferrer {
	private RuleStructure rule;
	
	private Set<ContextDef> defs;
	
	private ConstraintSet constraints;

	/** */
	public ArgumentInferrer(RuleStructure rule, ConstraintSet constraints){
		this.rule = rule;
		this.constraints = constraints;
	}
	/**
	 * �����Х�롼������Ф��ƤΤ߸ƤФ��
	 * @param top
	 */
	public ArgumentInferrer(Membrane top, ConstraintSet constraints){
		RuleStructure tmprule = new RuleStructure(new Membrane(null),"tmp");
		tmprule.leftMem = new Membrane(null);
		tmprule.rightMem = top;
		this.rule = tmprule;
		this.constraints = constraints;
	}

	public void infer() throws TypeConstraintException{
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
			new ArgumentInferrer(itr.next(),constraints).infer();
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
			int out = TypeEnv.outOfPassiveAtom(atom);
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
			LinkOccurrence rightlink = TypeEnv.getRealBuddy(leftlink);
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
			LinkOccurrence b = TypeEnv.getRealBuddy(lo);
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
			LinkOccurrence lhsPartner = TypeEnv.getRealBuddy(lhsOcc.args[i]);
			LinkOccurrence rhsPartner = TypeEnv.getRealBuddy(rhsOcc.args[i]);
			if(rhsPartner.atom instanceof Atom){
				if(TypeEnv.isLHSAtom((Atom)rhsPartner.atom))
					addUnifyConstraint(-1,lhsPartner,rhsPartner);
				else
					addUnifyConstraint(1,lhsPartner,rhsPartner);
			}
			else{ // ���սи����ץ���ʸ̮�ȷѤäƤ���
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
	private void addConstraintAboutLinks(int sign, LinkOccurrence lo, LinkOccurrence b) throws TypeConstraintException{
		int out = TypeEnv.outOfPassiveAtom((Atom)lo.atom);
		if(out == lo.pos){ // �ǡ������ȥ�ν��ϰ���
			if(TypeEnv.outOfPassiveAtom((Atom)b.atom) == b.pos)//!= TypeEnv.ACTIVE)
				throw new TypeConstraintException("MODE ERROR : output arguments connected each other.");
			else addReceiveConstraint(-sign, b, ((Atom)lo.atom).functor);
		}
		else{
			if(TypeEnv.outOfPassiveAtom((Atom)b.atom) == b.pos) //!= TypeEnv.ACTIVE)
				addConstraintAboutLinks(sign, b, lo);
			else addUnifyConstraint(sign, lo, b);
		}
	}

	private void addUnifyConstraint(int sign, LinkOccurrence lo,
			LinkOccurrence b) {
		constraints.add(new UnifyConstraint(new PolarizedPath(1, new RootPath(lo)),
				new PolarizedPath(sign, new RootPath(b))));
	}

	private void addReceiveConstraint(int sign, LinkOccurrence b, Functor f) {
		constraints.add(new ReceiveConstraint(new PolarizedPath(sign, new RootPath(b)), f));
	}


}
