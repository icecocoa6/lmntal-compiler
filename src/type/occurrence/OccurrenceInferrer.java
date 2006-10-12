package type.occurrence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import type.TypeEnv;

import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.RuleStructure;

public class OccurrenceInferrer {
	
	private Membrane root;

	private Map<String, Set<AtomOccurrence>> atomOccurrenceConstraintsMap = new HashMap<String, Set<AtomOccurrence>>();

	private Map<String, Set<MembraneOccurrence>> membraneOccurrenceConstraintsMap = new HashMap<String, Set<MembraneOccurrence>>();

	/**
	 * �롼�������ꤷ���и���������
	 * @param root
	 */
	public OccurrenceInferrer(Membrane root){
		this.root = root;
	}
	
	public void infer(){
		inferOccurrenceMembrane(root);
	}
	
	public void printAll(){
		Env.p("--OCCURRENCE ANALYSIS");
		Env.p("---atom :");
		Iterator<Set<AtomOccurrence>> itas = atomOccurrenceConstraintsMap.values().iterator();
		while (itas.hasNext()) {
			Iterator<AtomOccurrence> itac = itas.next().iterator();
			while (itac.hasNext()) {
				Env.p(itac.next());
			}
		}
		Env.p("---membrane : ");
		Iterator<Set<MembraneOccurrence>> itms = membraneOccurrenceConstraintsMap.values().iterator();
		while (itms.hasNext()) {
			Iterator<MembraneOccurrence> itmc = itms.next().iterator();
			while (itmc.hasNext()) {
				Env.p(itmc.next());
			}
		}
		Env.p("");
		
	}
	
	/** �и������������� */
	public void inferOccurrenceMembrane(Membrane mem){
		/** �����ƥ��֥��ȥ�ˤĤ��ƽи������ݤ� */
		Iterator<Atom> ita = mem.atoms.iterator();
		while(ita.hasNext()){
			Atom atom = ita.next();
			if(TypeEnv.outOfPassiveAtom(atom) == TypeEnv.ACTIVE)
				addAtomOccurrence(new AtomOccurrence(TypeEnv.getMemName(mem),atom.functor));
		}
		/** ����ˤĤ��ƽи������ݤ������� */
		Iterator<Membrane> itm = mem.mems.iterator();
		while(itm.hasNext()){
			Membrane child = itm.next();
			addMembraneOccurrence(new MembraneOccurrence(TypeEnv.getMemName(mem),TypeEnv.getMemName(child)));
			inferOccurrenceMembrane(child);
		}
		/** �롼��κ��ա����դ����� */
		Iterator<RuleStructure> itr = mem.rules.iterator();
		while(itr.hasNext()){
			RuleStructure rule = itr.next();
			// TODO ���դ���������ɬ�פ����뤫�ɤ�������̯
			inferOccurrenceMembrane(rule.leftMem);
			inferOccurrenceMembrane(rule.rightMem);
		}
	}

	public void addAtomOccurrence(AtomOccurrence ao){
		if (!atomOccurrenceConstraintsMap.containsKey(ao.getMemname())) {
			atomOccurrenceConstraintsMap.put(ao.getMemname(),
					new HashSet<AtomOccurrence>());
		}
		atomOccurrenceConstraintsMap.get(ao.getMemname()).add(ao);
	}
	public void addMembraneOccurrence(MembraneOccurrence mo){
		if (!membraneOccurrenceConstraintsMap.containsKey(mo.getParentName())) {
			membraneOccurrenceConstraintsMap.put(mo.getParentName(),
					new HashSet<MembraneOccurrence>());
		}
		membraneOccurrenceConstraintsMap.get(mo.getParentName()).add(mo);
	}
}
