package type.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.Functor;

import compile.structure.Atom;
import compile.structure.Atomic;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

public class ConnectInferer {
	private Membrane root;

	/**
	 * Functor �ΰ����˷Ҥ����ǽ���Τ��� Functor �ΰ����ؤ� set
	 */
	private FunctorKindAndArgumentMap functorConnect;

	/**
	 * a(X) :- b(X). �Τ褦�ʥ롼��ˤ�����
	 * (a_0, 0) -> (b_0, 0) �Τ褦�ʤ�Τ����ޤä� multimap 
	 */
	private FunctorKindAndArgumentMap functorTrans;


	@SuppressWarnings("unused")
	private ConnectInferer() {}

	public ConnectInferer(Membrane root) {
		this.root = root;
		functorConnect = new FunctorKindAndArgumentMap();
		functorTrans = new FunctorKindAndArgumentMap();

	}

	public void infer(){
		/**
		 * ������ȥ�ȥ롼�������Ū�˷Ҥ��äƤ��륢�ȥ�Ʊ�ΤˤĤ���
		 * functorConnect �� functorTrans������� 
		 */
		makeFunctorConnect(root);

		/**
		 * functorTrans ���Ѥ��ƿ��������׻�����functorConnect �����
		 */
		solveFunctorConnect();
	}

	private void solveFunctorConnect() {
		boolean flag = true;
		while (flag) {
			flag = false;
			for (Map.Entry<FunctorAndArgument, Set<FunctorAndArgument>> mapEntry : 
				functorTrans.entrySet()) {
				FunctorAndArgument leftFaa = mapEntry.getKey();
				for (FunctorAndArgument rightFaa : mapEntry.getValue()) {
					int preCount = functorConnect.getSetSize(rightFaa);
					functorConnect.addAll(rightFaa, functorConnect.getSet(leftFaa));
					int postCount = functorConnect.getSetSize(rightFaa);
					flag = preCount != postCount ? true : flag;
				}
			}
		}
	}

	private Atomic getBuddyAtom(LinkOccurrence lo) {
		Atomic otherSideAtom = lo.buddy.atom;
		int otherSideAtompos = lo.buddy.pos;
		if (otherSideAtom instanceof Atom && ((Atom)otherSideAtom).functor.getName().equals("=")) {
			int j = otherSideAtompos ^ 1;
			return getBuddyAtom(otherSideAtom.args[j]);
		}
		return otherSideAtom;
	}
	private int getBuddyAtomPos(LinkOccurrence lo) {
		Atomic otherSideAtom = lo.buddy.atom;
		int otherSideAtompos = lo.buddy.pos;
		if (otherSideAtom instanceof Atom && ((Atom)otherSideAtom).functor.getName().equals("=")) {
			int j = otherSideAtompos ^ 1;
			return getBuddyAtomPos(otherSideAtom.args[j]);
		}
		return otherSideAtompos;
	}

	private void makeFunctorConnect(Membrane mem) {
		for (Atomic atomic : mem.atoms) {
			if (atomic instanceof ProcessContext) {
				continue;
			}
			if (!((Atom)atomic).functor.isSymbol()) {
				continue;
			}
			for(LinkOccurrence otherSide : atomic.args){
				Atomic a = getBuddyAtom(otherSide);
				int j = getBuddyAtomPos(otherSide);
				if(a instanceof Atom) {
					functorConnect.add(
							new FunctorAndArgument(((Atom) atomic).functor, otherSide.pos),
							new FunctorAndArgument(((Atom) a).functor, j)
					);
				}
			}
		}

		for (Membrane subMem: mem.mems) {
			makeFunctorConnect(subMem);
		}

		for (RuleStructure rule: mem.rules) {
			makeFunctorConnectRule(rule);
		}
	}

	private void makeFunctorConnectRule(RuleStructure rule) {
		/* functorConnect �κ��� */		
		makeFunctorConnectRuleRightMem(rule.rightMem, rule);

		/* functorTrans �κ��� */
		makeFunctorConnectRuleLeftMem(rule.leftMem, rule);	
	}

	private void makeFunctorConnectRuleLeftMem(Membrane leftMem, RuleStructure rule) {
		for (Atomic atomic : leftMem.atoms) {
			if (atomic instanceof ProcessContext) {
				continue;
			}
			if (!((Atom)atomic).functor.isSymbol()) {
				continue;
			}
			Atom atom = (Atom) atomic;
			for (LinkOccurrence otherSide : atomic.args) {

				// ���Ԥ⥳�ͥ������θ����٤���
				if (!isFreeLink(otherSide, rule) && !(getBuddyAtom(otherSide) instanceof Atom)) {
					continue;
				}
				Atom a = (Atom)getBuddyAtom(otherSide);
				int j = getBuddyAtomPos(otherSide);
				functorTrans.add(
						new FunctorAndArgument(atom.functor, otherSide.pos),
						new FunctorAndArgument(a.functor, j)
				);
			}
		}

		for (Membrane subMem : leftMem.mems) {
			makeFunctorConnectRuleLeftMem(subMem, rule);
		}

	}

	private void makeFunctorConnectRuleRightMem(Membrane rightMem, RuleStructure rule) {
		for (Atomic atomic : rightMem.atoms) {
			if (atomic instanceof ProcessContext) {
				continue;
			}
			if (!((Atom)atomic).functor.isSymbol()) {
				continue;
			}
			Atom atom = (Atom) atomic;
			for (LinkOccurrence otherSide : atomic.args){
				if (isFreeLink(otherSide, rule)) {
					continue;
				}
				Atomic a = getBuddyAtom(otherSide);
				int j = getBuddyAtomPos(otherSide);
				if (!(a instanceof Atom)) {
					continue;
				}
				functorConnect.add(
						new FunctorAndArgument(atom.functor, otherSide.pos),
						new FunctorAndArgument(((Atom) a).functor, j)
				);
			}
		}

		for (Membrane subMem : rightMem.mems) {
			makeFunctorConnectRuleRightMem(subMem, rule);
		}

		for (RuleStructure r : rightMem.rules) {
			makeFunctorConnectRule(r);
		}

	}

	private boolean isFreeLink(LinkOccurrence lo, RuleStructure rs){
		return rs.leftMem.freeLinks.containsValue(lo) 
		|| rs.rightMem.freeLinks.containsValue(lo) ;
	}

	public void printLMNSyntax() {
		Env.p("connect{");
		for (Map.Entry<FunctorAndArgument, Set<FunctorAndArgument>> mapEntry : 
			functorTrans.entrySet()) {
			if (mapEntry.getValue().size() != 1) {
				continue;
			}
			for (FunctorAndArgument value : mapEntry.getValue()) {
				if (!value.functor.isInteger()) {
					continue;
				}
				Env.p("\tonly(" + mapEntry.getKey().functor.getName() + ", " 
						 + "integer)") ;
			}
		}
		Env.p("}.");
	}
}
