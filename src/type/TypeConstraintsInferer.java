package type;

import runtime.Env;
import type.argument.ArgumentInferrer;
import type.occurrence.OccurrenceInferrer;
import type.quantity.QuantityInferrer;

import compile.structure.Membrane;

/**
 * This class infers type constrains from COMPILE STRUCTURE
 * 
 * @author kudo
 * @since 2006/06/03 (Sat.)
 */
public class TypeConstraintsInferer {

	/** membrane contains all processes */
	private Membrane root;

//	private ConstraintSet constraints = new ConstraintSet();

	/**
	 * @param root
	 */
	public TypeConstraintsInferer(Membrane root) {
		this.root = root;
	}

	public void infer() throws TypeConstraintException {
		// ���Ƥ���ˤĤ��ơ��롼��κ��պǳ����и����ɤ����ξ��������
		TypeEnv.collectLHSMems(root.rules);
		
		// �и�������������
		// TODO �Ŀ��������Ǥ���ʤ�����(?)
		if(Env.flgOccurrenceInference){
			OccurrenceInferrer oi = new OccurrenceInferrer(root);
			oi.infer();
			if(Env.flgShowConstraints)
				oi.printAll();
		}

		// �Ŀ�������������
		if(Env.flgQuantityInference){
			QuantityInferrer qi = new QuantityInferrer(root);
			qi.infer();
			if(Env.flgShowConstraints)
				qi.printAll();
		}
		
		// ����������������
		if(Env.flgArgumentInference){
			ArgumentInferrer ai = new ArgumentInferrer(root);
			ai.infer();
			if(Env.flgShowConstraints)
				ai.printAll();
		}
	}

}
