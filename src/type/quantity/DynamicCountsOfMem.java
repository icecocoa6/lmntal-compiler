package type.quantity;

import runtime.Env;

import compile.structure.Membrane;

/**
 * ���줾�����ˤĤ��ƺ���롣
 * ưŪ���̾����ɽ�����饹
 * @author kudo
 *
 */
public class DynamicCountsOfMem {
	public final Membrane mem;
	
	/** Ŭ�Ѳ����ɽ���ѿ� */
	public final VarCount applyCount;
	
	/**
	 * ������ν�°�ץ��������ܤ���뤫��ɽ����
	 * 1 : ��ư��(���뤤�ϥ롼���������)
	 * >1 : ʣ�����ޡ���
	 */
	public final int multiple;
	
	/** �������ץ��� */
	public final StaticCountsOfMem removeCounts;
	/** ��������ץ��� */
	public final StaticCountsOfMem generateCounts;

	public DynamicCountsOfMem(StaticCountsOfMem removeCounts, int multiple, StaticCountsOfMem generateCounts, VarCount applyCount){
		this.mem = generateCounts.mem;
		this.multiple = multiple;
		this.applyCount = applyCount;
		this.removeCounts = removeCounts;
		this.generateCounts = generateCounts;
	}

//	public void addAllCounts(DynamicCountsOfMem dom){
//		removeCounts.addAllCounts(dom.removeCounts);
//		generateCounts.addAllCounts(dom.generateCounts);
//	}
	
//	/**
//	 * �����ͤˤ���
//	 * @return
//	 */
//	public FixedCounts solve(){
////		return new FixedCounts(this);
//	}
	
	public void print(){
		Env.p("---dynamic count in " + mem.name + " :");
		Env.p("----remove:");
		removeCounts.print();
		Env.p("----generate:");
		generateCounts.print();
//		Env.p("----atoms of " + TypeEnv.getMemName(mem) + ":");
//		Iterator<Functor> itf = functorToCount.keySet().iterator();
//		while(itf.hasNext()){
//			Functor f = itf.next();
//			Env.p(f + ":" + functorToCount.get(f));
//		}
//		Env.p("----mems of " + TypeEnv.getMemName(mem) + ":");
//		Iterator<String> itm = memnameToCount.keySet().iterator();
//		while(itm.hasNext()){
//			String m = itm.next();
//			Env.p(m + ":" + memnameToCount.get(m));
//		}
	}
	
}

