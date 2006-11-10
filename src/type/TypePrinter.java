package type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import runtime.Env;
import runtime.Functor;
import type.argument.ActiveAtomPath;
import type.argument.ArgumentInferer;
import type.argument.ConstraintSet;
import type.argument.Path;
import type.argument.TypeVarConstraint;
import type.quantity.FixedCount;
import type.quantity.FixedCounts;
import type.quantity.QuantityInferer;

/**
 * �����ϡ�������̤���Ϥ���
 * @author kudo
 *
 */
public class TypePrinter {

	private final Map<String,Map<Functor, TypeVarConstraint[]>> memnameToFunctorTypes;
	private final TreeSet<String> sortedMemNames;
	/** ǰ�Τ��ᡢ�ե��󥯥���̾����->��������ǥ����Ȥ��ƴ�������(��̣�ʤ�����) */
	private final TreeSet<Functor> sortedFunctors;

	/** ��̾ -> ���ȥࡿ����λҿ��Υ��å� */
	private final Map<String, FixedCounts> memnameToCounts;
	
	public TypePrinter(ArgumentInferer ai, QuantityInferer qi){
		
		//�ޤ������η�������󤹤�
		ConstraintSet cs = ai.getConstraints();
		Set<TypeVarConstraint> tvcs = cs.getTypeVarConstraints();
		memnameToFunctorTypes = new HashMap<String,Map<Functor,TypeVarConstraint[]>>();
		sortedMemNames = new TreeSet<String>();
		sortedFunctors = new TreeSet<Functor>(new FunctorComparator());
		for(TypeVarConstraint tvc : tvcs){
			Path p = tvc.getPath();
			// TODO TracingPath�ˤĤ��ƤϤȤꤢ����̵��
			if(!(p instanceof ActiveAtomPath))continue;
			ActiveAtomPath aap = (ActiveAtomPath)p;
			String memname = aap.getMemName();
			sortedMemNames.add(memname);
			if(!memnameToFunctorTypes.containsKey(memname))
				memnameToFunctorTypes.put(memname, new HashMap<Functor,TypeVarConstraint[]>());
			Map<Functor, TypeVarConstraint[]> functorToArgumentTypes = memnameToFunctorTypes.get(memname);
			Functor f = aap.getFunctor();
			sortedFunctors.add(f);
			if(!functorToArgumentTypes.containsKey(f))
				functorToArgumentTypes.put(f, new TypeVarConstraint[f.getArity()]);
			TypeVarConstraint[] argtypes = functorToArgumentTypes.get(f);
			argtypes[aap.getPos()] = tvc;
		}
		
		//���˸Ŀ�����������ե��󥯥�̾���󤹤�
		memnameToCounts = qi.getMemNameToFixedCountsSet();
		for(String memname : memnameToCounts.keySet()){
			sortedMemNames.add(memname);
			FixedCounts fcs = memnameToCounts.get(memname);
			for(Functor f : fcs.functorToCount.keySet()){
				sortedFunctors.add(f);
			}
		}
	}
	
	public void printAll(){
		Env.p("Type Information : ");
		for(String memname : sortedMemNames){
			FixedCounts fcs = memnameToCounts.get(memname);
			Env.p(memname + "{");
			// �����ƥ��֥��ȥ�ξ�������
			for(Functor f : sortedFunctors){
				
				// �ǡ������ȥࡢ���ͥ�����̵�뤹��
				if(TypeEnv.outOfPassiveFunctor(f) != TypeEnv.ACTIVE)continue;
				
				Map<Functor, TypeVarConstraint[]> functorToArgumentTypes = memnameToFunctorTypes.get(memname);
				
				if(fcs.functorToCount.containsKey(f)){

					StringBuffer texp = new StringBuffer("");
					texp.append("\t" + f.getQuotedAtomName() + "(");

					// �����η���ɽ��
					TypeVarConstraint[] argtypes = functorToArgumentTypes.get(f);
					for(int i=0;i<argtypes.length;i++){
						if(i!=0)texp.append(", ");
						texp.append(argtypes[i].shortString());
					}
					texp.append(")");
					
					FixedCount fc = fcs.functorToCount.get(f);
					texp.append(" : " + fc);
					
					Env.p(texp);
				}
			}
			
			// ����ξ�������
			for(String childname : sortedMemNames){
				
				if(fcs.memnameToCount.containsKey(childname)){
					FixedCount fc = fcs.memnameToCount.get(childname);
					Env.p("\t" + childname + "{} : " + fc);
				}
			}
			
			Env.p("}");
		}
	}

	/**
	 * �ե��󥯥���̾����(����)->��������ǥ����Ȥ���
	 */
	class FunctorComparator implements Comparator<Functor>{
		public int compare(Functor f1, Functor f2){
			int nc = f1.getName().compareTo(f2.getName());
			if(nc != 0)return nc;
			else{
				return f1.getArity() - f2.getArity();
			}
		}
	}
}
