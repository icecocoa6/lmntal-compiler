package type.occurrence;

import java.util.Iterator;

import type.AtomOccurrenceConstraint;
import type.ConstraintSet;
import type.MembraneOccurrenceConstraint;
import type.TypeEnv;

import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.RuleStructure;

public class OccurrenceInferrer {
	
	private ConstraintSet constraints;

	public OccurrenceInferrer(ConstraintSet constraints){
		this.constraints = constraints;
	}
	
	/** �и������������� */
	public void inferOccurrenceMembrane(Membrane mem){
		/** �����ƥ��֥��ȥ�ˤĤ��ƽи������ݤ� */
		Iterator<Atom> ita = mem.atoms.iterator();
		while(ita.hasNext()){
			Atom atom = ita.next();
			if(TypeEnv.outOfPassiveAtom(atom) == TypeEnv.ACTIVE)
				constraints.add(new AtomOccurrenceConstraint(mem.name,atom.functor));
		}
		/** ����ˤĤ��ƽи������ݤ������� */
		Iterator<Membrane> itm = mem.mems.iterator();
		while(itm.hasNext()){
			Membrane child = itm.next();
			constraints.add(new MembraneOccurrenceConstraint(mem.name,child.name));
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

}
