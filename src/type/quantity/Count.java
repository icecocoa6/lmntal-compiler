package type.quantity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Count {

	public final static InfinityCount INFINITY = new InfinityCount(false);
	public final static InfinityCount M_INFINITY = new InfinityCount(true);	

	public final  Map<VarCount, Integer> varToMultiple;
	
	public Count(VarCount vc){
		this(new HashMap<VarCount, Integer>());
		add(1, vc);
	}
	
	public Count(Map<VarCount, Integer> varToMultiple){
		this.varToMultiple = varToMultiple;
	}
	public String toString(){
		int size = varToMultiple.keySet().size();
		if(size == 0)return "0";
		else{
			Iterator<VarCount> itv = varToMultiple.keySet().iterator();
			VarCount vc = itv.next();
			Integer i = varToMultiple.get(vc);
			String ret = "(" + i + "*" + vc;
			while(itv.hasNext()){
				vc = itv.next();
				i = varToMultiple.get(vc);
				ret += " + " + i + "*" + vc;
			}
			return ret + ")";
		}
	}
	public void add(int multiple, VarCount vc){
		if(!varToMultiple.containsKey(vc))
			varToMultiple.put(vc,multiple);
		else
			varToMultiple.put(vc,varToMultiple.get(vc)+multiple);
	}
	public static Count sum(Count sc1, Count sc2){
		Set<VarCount> vs = new HashSet<VarCount>();
		vs.addAll(sc1.varToMultiple.keySet());
		vs.addAll(sc2.varToMultiple.keySet());
		Map<VarCount, Integer> newmap = new HashMap<VarCount, Integer>();
		for(VarCount v : vs){
			Integer i1 = sc1.varToMultiple.get(v);
			Integer i2 = sc2.varToMultiple.get(v);
			if(i1 == null)newmap.put(v,i2);
			else if(i2 == null)newmap.put(v,i1);
			else newmap.put(v, i1+i2);
		}
		return new Count(newmap);
	}
	
	public static Count mul(int m, Count sc){
		Map<VarCount, Integer> newmap = new HashMap<VarCount, Integer>();
		for(VarCount vc : sc.varToMultiple.keySet()){
			newmap.put(vc,sc.varToMultiple.get(vc)*m);
		}
		return new Count(newmap);
	}
	
	public FixedCount evaluate(){
		FixedCount fc = new NumCount(0);
		for(VarCount vc : varToMultiple.keySet()){
			int m = varToMultiple.get(vc);
			if(m==0)continue;
			FixedCount f = vc.evaluate();
			f = f.mul(m);
			fc = fc.add(f);
		}
		return fc;
	}
	
	public Count clone(VarCount oldvar, VarCount newvar){
		Count cloned = new Count(new HashMap<VarCount, Integer>());
		for(VarCount vc : varToMultiple.keySet()){
			if(vc == oldvar){
				cloned.add(varToMultiple.get(vc), newvar);
			}
			else cloned.add(varToMultiple.get(vc), vc);
		}
		return cloned;
	}
	
	// ����ͤ�0�ʾ���Ȥ������Ȥ����Ѥ��Ƶ���
	public boolean constraintOverZero(){
		int min = 0; // �Ǿ���
		Set<VarCount> vars = new HashSet<VarCount>();
		for(VarCount vc : varToMultiple.keySet()){
			int m = varToMultiple.get(vc);
			if(m == 0) continue;
			if(vc.bound instanceof NumCount){
				min += ((NumCount)vc.bound).value * m;
			}
			else if(vc.bound instanceof InfinityCount){
				// ̵���ͤ˸��ꤵ��Ƥ��ޤ��Τǲ򤱤ʤ�
				return false;
			}
			else if(vc.bound instanceof IntervalCount){
				IntervalCount ic = (IntervalCount)vc.bound;
				if(m > 0){
					// ��椬+�Ǿ峦���ʤ���в򤱤ʤ�
					if(ic.max instanceof InfinityCount)return false;
					if(ic.max instanceof NumCount){
						min += ((NumCount)ic.max).value * m;
					}
				}
				else if(m < 0){
					// ��椬-�ǲ������ʤ���в򤱤ʤ� ( ��äȤ⤳��Ϥ��ꤨ�ʤ��� )
					if(ic.min instanceof InfinityCount)return false;
					if(ic.min instanceof NumCount){
						min += ((NumCount)ic.min).value * m;
					}
					vars.add(vc);
				}
			}
		}
		boolean changed = false;
		for(VarCount vc : vars){
			IntervalCount ic = (IntervalCount)vc.bound;
			int newmax = min / (-varToMultiple.get(vc));
			if(ic.max.compare(new NumCount(newmax))>0){
				vc.bind(new IntervalCount(ic.min, new NumCount( newmax )));
				changed = true;
			}
		}
		return changed;
	}
}
