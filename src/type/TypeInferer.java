package type;

import runtime.Env;
import type.argument.ArgumentInferrer;
import type.occurrence.OccurrenceInferrer;
import type.quantity.QuantityInferrer;

import compile.structure.Membrane;

/**
 * This class infers type constraints from COMPILE STRUCTURE
 * 
 * @author kudo
 * @since 2006/06/03 (Sat.)
 */
public class TypeInferer {

	/** membrane contains all processes */
	private Membrane root;

	/**
	 * @param root
	 */
	public TypeInferer(Membrane root) {
		this.root = root;
	}

	public void infer() throws TypeException {
		TypeEnv.initialize(root);
		
		// �桼�����������������
		
		Membrane typedefmem = null;
		for(Membrane topmem : root.mems){
			if(topmem.name.equals("typedef"))
				typedefmem = topmem;
		}
		
		if(typedefmem != null)
			TypeDefParser.parseFromMembrane(typedefmem);
		
		// �и�������������
		// TODO �Ŀ��������Ǥ���ʤ�����(?)
//		if(Env.flgOccurrenceInference){
//			OccurrenceInferrer oi = new OccurrenceInferrer(root);
//			oi.infer();
//			if(Env.flgShowConstraints)
//				oi.printAll();
//		}

		QuantityInferrer qi = new QuantityInferrer(root);
		// �Ŀ�������������
		if(Env.flgQuantityInference){
			qi.infer();
//			if(Env.flgShowConstraints)
//				qi.printAll();
		}
		
		ArgumentInferrer ai = new ArgumentInferrer(root);
		// ����������������
		if(Env.flgArgumentInference){
			ai.infer();
//			if(Env.flgShowConstraints)
//				ai.printAll();
		}
		
		//������̤���Ϥ���
		if(Env.flgShowConstraints){
			TypePrinter tp;
			if(Env.flgArgumentInference && Env.flgQuantityInference){
				tp = new TypePrinter(ai,qi);
				tp.printAll();
			}
		}
		
	}

}
