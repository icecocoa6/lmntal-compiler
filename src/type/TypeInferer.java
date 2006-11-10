package type;

import runtime.Env;
import type.argument.ArgumentInferer;
import type.quantity.QuantityInferer;

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
		boolean typeDefined = false;
		Membrane typedefmem = null;
		for(Membrane topmem : root.mems)
			if(topmem.name.equals("typedef")){
				typedefmem = topmem;
				break; // TODO ������줬2�Ĥ��ä���ɤ�����
			}
		
		TypeChecker tc = new TypeChecker();
		if(typedefmem != null){
			typeDefined = tc.parseTypeDefinition(typedefmem);
			root.mems.remove(typedefmem); // �������ϸ���������ѥ��뤫�鳰��
		}
		
		// �и�������������
		// TODO �Ŀ��������Ǥ���ʤ�����(?)
//		if(Env.flgOccurrenceInference){
//			OccurrenceInferrer oi = new OccurrenceInferrer(root);
//			oi.infer();
//			if(Env.flgShowConstraints)
//				oi.printAll();
//		}

		QuantityInferer qi = new QuantityInferer(root);
		// �Ŀ�������������
		if(Env.flgQuantityInference){
			qi.infer();
//			if(Env.flgShowConstraints)
//				qi.printAll();
		}
		
		ArgumentInferer ai = new ArgumentInferer(root);
		// ����������������
		if(Env.flgArgumentInference){
			ai.infer();
//			if(Env.flgShowConstraints)
//				ai.printAll();
		}
		
		// �������Ϳ�����Ƥ�����������������å�����
		if(typeDefined){
			tc.check(ai, qi);
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
