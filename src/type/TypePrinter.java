package type;

import java.util.List;
import java.util.Map;

import runtime.Functor;
import type.argument.TypeVarConstraint;

/**
 * �����ϡ�������̤���Ϥ���
 * @author kudo
 *
 */
public class TypePrinter {

	Map<String,Map<Functor, List<TypeVarConstraint>>> memnameToFunctorTypes;
	
	public void printAll(){
		
	}
//	public String stringOfActiveAtom(Functor f, Map<Functor, List<TypeVarConstraint>> functorToArgumentTypes){
////		funtorToArgumentTypes.get(f).
//		
//	}
}
