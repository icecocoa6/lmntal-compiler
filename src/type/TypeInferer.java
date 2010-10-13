package type;

import java.util.ArrayList;
import java.util.List;

import runtime.Env;
import type.argument.ArgumentInferer;
import type.connect.ConnectInferer;
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
		// ����Ū�롼����ˤ�root�Ȥ���̾����Ĥ���
		root.name = "root";
		this.root = root;
	}

	public void infer() throws TypeException {
		// ���������(���սи������������)
		TypeEnv.initialize(root);
		
		// �桼�����������������
		boolean typeDefined = false;
		List<Membrane> typedefmems = new ArrayList<Membrane>();
		for(Membrane topmem : root.mems)
			if(TypeEnv.getMemName(topmem).equals("typedef")){
				typedefmems.add(topmem);
				break; // TODO ������줬2�Ĥ��ä���ɤ����� => �ޡ���
			}
		
		TypeChecker tc = new TypeChecker();
		if(typedefmems.size() > 0){
			typeDefined = tc.parseTypeDefinition(typedefmems);
			root.mems.removeAll(typedefmems); // �������ϸ���������ѥ��뤫�鳰��
		}
		
		// �и�������������
//		if(Env.flgOccurrenceInference){
//			OccurrenceInferrer oi = new OccurrenceInferrer(root);
//			oi.infer();
//			if(Env.flgShowConstraints)
//				oi.printAll();
//		}
		
		ConnectInferer ci = new ConnectInferer(root);
		if (false) {
			ci.infer();
		}

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
			if(false)
				ai.printAll();
		}
		
		// �������Ϳ�����Ƥ�����������������å�����
		if(typeDefined){
			tc.check(ai, qi);
		}

		//������̤���Ϥ���
		if(Env.flgShowConstraints){
			TypePrinter tp;
//			if(Env.flgArgumentInference && Env.flgQuantityInference){
				tp = new TypePrinter(ai, qi, ci);
				// printAll ���Ȥ��Ƥ����� LMNtal Syntax ��ɽ������ printAllLMNSyntax �᥽�åɤ��ڤ��ؤ�
				//tp.printAll();
				tp.printAllLMNSyntax();
//			}
		}
		
		
	}

}
