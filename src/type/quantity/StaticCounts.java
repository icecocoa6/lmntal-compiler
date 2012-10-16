package type.quantity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import runtime.Env;
import runtime.functor.Functor;
import type.TypeEnv;

import compile.structure.Atom;
import compile.structure.Membrane;

/**
 * ���줾�����ˤĤ��ƺ���롣
 * ��Ū���̾����ɽ�����饹
 * @author kudo
 *
 */
public class StaticCounts{
	
	public Membrane mem;
	
	/** �ե��󥯥� -> �� */
	public final Map<Functor,Count> functorToCount;
	/** ��̾ -> �� */
	public final Map<String,Count> memnameToCount;

	public StaticCounts(Membrane mem){
		this.mem = mem;

		functorToCount = new HashMap<Functor, Count>();
		memnameToCount = new HashMap<String, Count>();
	}
	
	/**
	 * ���ȥ�˴ؤ����̤�û�
	 * @param atom
	 * @param count
	 */
	public void addAtomCount(Atom atom, Count count){
		addAtomCount(atom.functor, count);
	}
	public void addAtomCount(Functor functor, Count count){
		if(!functorToCount.containsKey(functor))
			functorToCount.put(functor,count);
		else{
			Count atomcount = functorToCount.get(functor);
			functorToCount.put(functor, Count.sum(atomcount,count));
		}
	}
	/**
	 * ��˴ؤ����̤�û�
	 * @param m
	 * @param count
	 */
	public void addMemCount(Membrane m, Count count){
		addMemCount(TypeEnv.getMemName(m), count);
	}
	public void addMemCount(String memname, Count count){
		if(!memnameToCount.containsKey(memname))
			memnameToCount.put(memname,count);
		else{
			Count memcount = memnameToCount.get(memname);
			memnameToCount.put(memname, Count.sum(memcount, count));
		}
	}
	/**
	 * �̤��̥��åȤ������Ʋû�
	 * TODO �ܿ��ˤ��ʬ����
	 * @param com2
	 */
	public void addAllCounts(StaticCounts com2){
		for(Functor f : com2.functorToCount.keySet())
			addAtomCount(f,com2.functorToCount.get(f));
		for(String name : com2.memnameToCount.keySet())
			addMemCount(name,com2.memnameToCount.get(name));
	}
	
//	public void merge(StaticCounts som){
//		for(Functor f : som.functorToCount.keySet()){
//			if(functorToCount.containsKey(f)){
//				functorToCount.get(f).or(som.functorToCount.get(f));
//			}
//			else{
//				
//			}
//		}
//		for(String name : som.memnameToCount.keySet()){
//			
//		}
//	}
	
	/**
	 * ���̤򤳤ζ������Ŭ�Ѥ���
	 * @param dom
	 */
	public void apply(DynamicCounts dom){
		if(dom.multiple > 1)removeUpperBounds();
		addAllCounts(dom.removeCounts);
		addAllCounts(dom.generateCounts);
	}
	
	/**
	 * ��¤��äѤ餦
	 */
	public void removeUpperBounds(){
		VarCount infVar = new VarCount();
		infVar.bind(new IntervalCount(new NumCount(0),Count.INFINITY));
		for(Functor f : functorToCount.keySet())
			functorToCount.get(f).add(1,infVar);
		for(String name : memnameToCount.keySet())
			memnameToCount.get(name).add(1,infVar);
	}
	
	public void solveByCounts(){
		boolean changed = true;
		while(changed){
			changed = false;
			for(Count c : functorToCount.values()){
				changed |= c.constraintOverZero();
			}
			for(Count c : memnameToCount.values()){
				changed |= c.constraintOverZero();
			}
		}
	}
	
	/**
	 * �ѿ�̾���դ��Ѥ���ʣ�����֤�
	 */
	public StaticCounts clone(VarCount oldvar, VarCount newvar){
		StaticCounts cloned = new StaticCounts(mem);
		for(Functor f : functorToCount.keySet())
			cloned.addAtomCount(f, functorToCount.get(f).clone(oldvar, newvar));
		for(String name : memnameToCount.keySet())
			cloned.addMemCount(name, memnameToCount.get(name).clone(oldvar, newvar));
		return cloned;
	}
	
	/**
	 * �����ͤˤ���
	 * @return
	 */
	public FixedCounts solve(){
		return new FixedCounts(this);
	}
	
	public void print(){
		Env.p("----atoms of " + TypeEnv.getMemName(mem) + ":");
		Iterator<Functor> itf = functorToCount.keySet().iterator();
		while(itf.hasNext()){
			Functor f = itf.next();
			Env.p(f + ":" + functorToCount.get(f));
		}
		Env.p("----mems of " + TypeEnv.getMemName(mem) + ":");
		Iterator<String> itm = memnameToCount.keySet().iterator();
		while(itm.hasNext()){
			String m = itm.next();
			Env.p(m + ":" + memnameToCount.get(m));
		}
	}
	
}
