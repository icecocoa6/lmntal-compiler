package type.argument;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import type.TypeException;

/**
 * 
 * @author kudo
 * 
 */
public class UnifySolver {
	/**
	 * �ѥ����鷿�ѿ��ؤΥޥå�
	 */
	private final Map<Path,TypeVar> pathToTV;

	/**
	 * �⡼���ѿ��δ���
	 */
	private final ModeVarSet modeVarSet;

	public UnifySolver() {
		this.pathToTV = new HashMap<Path,TypeVar>();
		this.modeVarSet = new ModeVarSet();
	}

	public void add(UnifyConstraint uc) throws TypeException{
		PolarizedPath pp1 = uc.getPPath1();
		PolarizedPath pp2 = uc.getPPath2();
		Path p1 = pp1.getPath();
		Path p2 = pp2.getPath();
		int sign1 = pp1.getSign();
		int sign2 = pp2.getSign();
		TypeVar tp1 = getTypeVar(p1);
		TypeVar tp2 = getTypeVar(p2);
		tp1.unify(tp2);
		modeVarSet.add(sign1*sign2, p1, p2);
	}
	
	/**
	 * �ѥ��η��ѿ����֤���̵����п��롣
	 * @param p
	 * @return
	 */
	private TypeVar getTypeVar(Path p){
		if(!pathToTV.containsKey(p))
			pathToTV.put(p, new TypeVar());
		return (TypeVar)pathToTV.get(p);
	}
	
	/**
	 * ReceiveConstraint �ξ��󤫤顢���ѿ����⡼���ѿ����ͤ���Ƥ�����
	 * TODO ���ѿ���«���ˤĤ��Ƥϡ��ǡ�������ռ�������Τˤ���
	 * @param receiveConstraintsSet
	 * @throws TypeException �⡼���ѿ���«���κݤ������礬������
	 */
	public void solveTypeAndMode(Collection<Set<ReceiveConstraint>> receiveConstraintsSet)throws TypeException{
		for(Set<ReceiveConstraint> rcs : receiveConstraintsSet){
			for(ReceiveConstraint rc : rcs){
				PolarizedPath pp = rc.getPPath();
				int sign = pp.getSign();
				Path p = pp.getPath();
				TypeVar tv = getTypeVar(p);
				tv.addPassiveFunctor(rc.getFunctor());
				ModeVar mv = modeVarSet.getModeVar(p);
				try{
					mv.bindSign(sign);
				}catch(TypeException e){
					Env.e(rc + " ==> " + e.getMessage());
				}
			}
		}
	}

	public Set<TypeVarConstraint> getTypeVarConstraints() throws TypeException{
		Set<TypeVarConstraint> typeVarConstraints = new HashSet<TypeVarConstraint>();
		for(Path p : pathToTV.keySet()){
			typeVarConstraints.add(new TypeVarConstraint(p, getTypeVar(p), modeVarSet.getModeVar(p)));
		}
		return typeVarConstraints;
	}
}
