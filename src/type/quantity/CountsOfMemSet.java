package type.quantity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import type.TypeEnv;

import compile.structure.Membrane;

/**
 * ��̾���Ȥ���Ū���Ϸ�̤��ݻ�����
 * @author kudo
 *
 */
public class CountsOfMemSet {
	/** ����������� -> �̲��Ϸ��(������) */
	Map<Membrane,StaticCountsOfMem> memToGenCounts;
	/** ����������� -> �̲��Ϸ��(��³��) */
	Map<Membrane,Set<DynamicCountsOfMem>> memToInhCountss;
	/** ��̾ -> �̲��Ϸ��(��³��) */
	Map<String,Set<DynamicCountsOfMem>> memnameToAllInhCountss;
	/** ����������� -> �̲��Ϸ��(������/ɾ���Ѥ�) */
	Map<Membrane,FixedCounts> memToFixedCounts;
//	/** ����������� -> �̲��Ϸ��(��³��/ɾ���Ѥ�) */
//	Map<Membrane,FixedCounts> memToInhFixedCounts;
	Map<String, StaticCountsOfMem> memnameToMergedCounts;
	public CountsOfMemSet(){
		memToGenCounts = new HashMap<Membrane,StaticCountsOfMem>();
		memToInhCountss = new HashMap<Membrane,Set<DynamicCountsOfMem>>();
		memnameToAllInhCountss = new HashMap<String,Set<DynamicCountsOfMem>>();
		memnameToMergedCounts = new HashMap<String, StaticCountsOfMem>();
	}
	/**
	 * ��β��Ϸ�̤�ä��Ƥ�����
	 * �����ʳ��Ǥϡ����Ʊ̾�Ǥ⥽��������̤���ʤ���̤���롣
	 * ���������롼�������ϡ��롼��ν�°�������Ʊ���Ȥ���롣
	 * �������������Ϸ�̤�Ʊ����ˤĤ��Ƥϲû�����롣
	 * TODO addCountsOfMem�Ϥ����������Ǥ���(���֤�)
	 * @param counts
	 */
	public void add(StaticCountsOfMem counts){
			if(!memToGenCounts.containsKey(counts.mem))
				memToGenCounts.put(counts.mem,counts);
			else{
				StaticCountsOfMem oldcounts = memToGenCounts.get(counts.mem);
				oldcounts.addAllCounts(counts);
			}
	}
	public void add(DynamicCountsOfMem counts){
		if(!memToInhCountss.containsKey(counts.mem)){
			Set<DynamicCountsOfMem> doms = new HashSet<DynamicCountsOfMem>();
			doms.add(counts);
			memToInhCountss.put(counts.mem,doms);
		}
		else{
			Set<DynamicCountsOfMem> oldcountss = memToInhCountss.get(counts.mem);
			oldcountss.add(counts);
		}
		String memname = counts.mem.name;
		if(!memnameToAllInhCountss.containsKey(memname)){
			Set<DynamicCountsOfMem> doms = new HashSet<DynamicCountsOfMem>();
			doms.add(counts);
			memnameToAllInhCountss.put(memname, doms);
		}
		else{
			Set<DynamicCountsOfMem> oldcountss = memnameToAllInhCountss.get(memname);
			oldcountss.add(counts);
		}
	}
	
	Map<String, Boolean> memnameToCRIFlg = new HashMap<String, Boolean>();
	/**
	 * ���ꤷ����̾�ˤĤ��Ƥϡ����Ƥ���Υ롼������Ƥ���˱ƶ�����
	 * @param memname
	 */
	public void collapseRulesIndependency(String memname){
		memnameToCRIFlg.put(memname, true);
	}
	Map<Membrane, Boolean> memToCRIFlg = new HashMap<Membrane, Boolean>();
	/**
	 * ���ꤷ����̾�ˤĤ��Ƥϡ����Ƥ���Υ롼������Ƥ���˱ƶ�����
	 * @param memname
	 */
	public void collapseRuleIndependency(Membrane mem){
		memToCRIFlg.put(mem, true);
	}
	Map<String, Boolean> memnameToCPUBFlg = new HashMap<String, Boolean>();
	/**
	 * ���ꤷ����̾�ˤĤ��Ƥϡ��ץ����β��¤�̵����
	 * @param memname
	 */
	public void collapseProcessUnderBounds(String memname){
		memnameToCPUBFlg.put(memname, true);
		collapseProcessIndependency(memname);
	}
	Map<String, Boolean> memnameToCPIFlg = new HashMap<String, Boolean>();
	/**
	 * ���ꤷ����̾�ˤĤ��Ƥϡ����������̤��ʤ�
	 * @param memname
	 */
	public void collapseProcessIndependency(String memname){
		memnameToCPIFlg.put(memname, true);
	}

	/** �ġ��ζ����줴�Ȥ˸��̤�Ŭ�Ѥ���
	 * ���������ץ�������Ω��������Ƥ�����̾�ˤĤ��Ƥ�merge����
	 *  */
	public void applyIndividual(){
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			if(memnameToCPIFlg.get(memname)){
				if(!memnameToMergedCounts.containsKey(memname))
					memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
				else{
					StaticCountsOfMem oldsom = memnameToMergedCounts.get(memname);
					oldsom.addAllCounts(memToGenCounts.get(mem));
				}
			}
			/** ���ζ�����ؤθ��̤�Ŭ�� */
			for(DynamicCountsOfMem dom : memToInhCountss.get(mem)){
				memToGenCounts.get(mem).apply(dom);
			}
			/** ¾����θ��̤�Ŭ�� */
			if(memToCRIFlg.get(mem) || memnameToCRIFlg.get(memname)){
				for(DynamicCountsOfMem dom : memnameToAllInhCountss.get(memname)){
					memToGenCounts.get(mem).apply(dom);
				}
			}
		}
	}
	
	/**
	 * ��̾���Ȥ˥ޡ������Ƹ��̤�Ŭ�Ѥ���
	 *
	 */
	public void applyAllInOne(){
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			if(!memnameToMergedCounts.containsKey(memname))
				memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
			else{
				StaticCountsOfMem oldsom = memnameToMergedCounts.get(memname);
				oldsom.addAllCounts(memToGenCounts.get(mem));
			}
		}
	}
	
	/**
	 * �롼���ѿ���«�����줿��ΤȤ���������γƸĿ��ˤĤ��Ƽºݤ��ͤ����
	 */
	public boolean solve(){
		memToFixedCounts = new HashMap<Membrane, FixedCounts>();
		for(Membrane m : memToGenCounts.keySet())
			memToFixedCounts.put(m,memToGenCounts.get(m).solve());
		return true;
	}

	private boolean fixed = false;
	/**
	 * ���̤ˤ�����롼���ѿ���̵�¤�«��������
	 *
	 */
	public void solveRVAsInfinity(){
		for(Set<DynamicCountsOfMem> doms : memToInhCountss.values())
			for(DynamicCountsOfMem dom : doms)
				if(!dom.applyCount.isBound())dom.applyCount.bind(Count.INFINITY.or0());
		fixed = solve();
	}
	
	/**
	 * ���ȥ����������β��¤�0�Ȥ�����������Ȥ��ƥ롼���ѿ����
	 * @return
	 */
	public void solveByCounts(){
		for(Membrane mem : memToGenCounts.keySet()){
			// �ץ�������Ω�����ݤ���Ƥ��ʤ����̵��
			if(memnameToCPIFlg.get(TypeEnv.getMemName(mem)))
				continue;
			memToGenCounts.get(mem).solveByCounts();
		}
		// �򤭤��ä����ɤ����狼��ʤ��ΤǻĤ��[0,#inf]�Ȥ���
		solveRVAsInfinity();
	}
	
	public void printAll(){
		if(fixed){
			Env.p("--QUANTITY ANALYSIS");
			Env.p("---mem on source counts:");
			for(FixedCounts fc : memToFixedCounts.values())
				fc.print();
			Env.p("");
		}
		else{
			Env.p("--QUANTITY ANALYSIS");
			Env.p("---mem on source counts:");
			for(StaticCountsOfMem com : memToGenCounts.values())
				com.print();
			Env.p("---mem effect on source counts:");
			for(Set<DynamicCountsOfMem> doms : memToInhCountss.values())
				for(DynamicCountsOfMem dom : doms)
					dom.print();
			Env.p("");
		}
	}

}
