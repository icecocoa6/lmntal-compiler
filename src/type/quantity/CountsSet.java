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
public class CountsSet {
	/** ����������� -> �̲��Ϸ��(������) */
	Map<Membrane,StaticCounts> memToGenCounts;
	/** ����������� -> �̲��Ϸ��(��³��) */
	Map<Membrane,Set<DynamicCounts>> memToInhCountss;
	/** ��̾ -> �̲��Ϸ��(��³��) */
	Map<String,Set<DynamicCounts>> memnameToAllInhCountss;
	/** ����������� -> �̲��Ϸ��(������/ɾ���Ѥ�) */
	Map<Membrane,FixedCounts> memToFixedCounts;
//	/** ����������� -> �̲��Ϸ��(��³��/ɾ���Ѥ�) */
//	Map<Membrane,FixedCounts> memToInhFixedCounts;
	Map<String, FixedCounts> memnameToMergedFixedCounts;
	/** ��̾ -> �̲��Ϸ��(��³��) */
	Map<String,Set<FixedDynamicCounts>> memnameToFixedCountss;
	public CountsSet(){
		memToGenCounts = new HashMap<Membrane,StaticCounts>();
		memToInhCountss = new HashMap<Membrane,Set<DynamicCounts>>();
		memnameToAllInhCountss = new HashMap<String,Set<DynamicCounts>>();
		memnameToMergedFixedCounts = new HashMap<String, FixedCounts>();
		memnameToFixedCountss = new HashMap<String, Set<FixedDynamicCounts>>();
	}
	/**
	 * ��β��Ϸ�̤�ä��Ƥ�����
	 * �����ʳ��Ǥϡ����Ʊ̾�Ǥ⥽��������̤���ʤ���̤���롣
	 * ���������롼�������ϡ��롼��ν�°�������Ʊ���Ȥ���롣
	 * �������������Ϸ�̤�Ʊ����ˤĤ��Ƥϲû�����롣
	 * TODO addCountsOfMem�Ϥ����������Ǥ���(���֤�)
	 * @param counts
	 */
	public void add(StaticCounts counts){
			if(!memToGenCounts.containsKey(counts.mem))
				memToGenCounts.put(counts.mem,counts);
			else{
				StaticCounts oldcounts = memToGenCounts.get(counts.mem);
				oldcounts.addAllCounts(counts);
			}
	}
	public void add(DynamicCounts counts){
		if(!memToInhCountss.containsKey(counts.mem)){
			Set<DynamicCounts> doms = new HashSet<DynamicCounts>();
			doms.add(counts);
			memToInhCountss.put(counts.mem,doms);
		}
		else{
			Set<DynamicCounts> oldcountss = memToInhCountss.get(counts.mem);
			oldcountss.add(counts);
		}
		String memname = counts.mem.name;
		if(!memnameToAllInhCountss.containsKey(memname)){
			Set<DynamicCounts> doms = new HashSet<DynamicCounts>();
			doms.add(counts);
			memnameToAllInhCountss.put(memname, doms);
		}
		else{
			Set<DynamicCounts> oldcountss = memnameToAllInhCountss.get(memname);
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

	Map<String, Boolean> memnameToAlreadyApplyed = new HashMap<String, Boolean>();
	
	/** �ġ��ζ����줴�Ȥ˸��̤�Ŭ�Ѥ���
	 * ���������ץ�������Ω��������Ƥ�����ˤĤ��Ƥϲ��⤷�ʤ�
	 *  */
	public void applyIndividual(){
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			/** �ץ�������Ω��������Ƥ����硢���⤷�ʤ� */
			if(memnameToCPIFlg.get(memname)){
//				if(!memnameToMergedCounts.containsKey(memname))
//					memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
//				else{
//					StaticCounts oldsom = memnameToMergedCounts.get(memname);
//					oldsom.merge(memToGenCounts.get(mem));
//				}
			}
			/** �롼�����Ω��������Ƥ�����, ¾����θ��̤�Ŭ�� */
			else if(memToCRIFlg.get(mem) || memnameToCRIFlg.get(memname)){
				for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
					// DynamicCounts�򥳥ԡ�����Ŭ�Ѥ��롣
					memToGenCounts.get(mem).apply(dom.clone());
				}
				// ��̾�ˤĤ��ơ�Ŭ�ѺѤߤȤ���
				memnameToAlreadyApplyed.put(memname, true);
			}
			else{
				/** ���ζ�����ؤθ��̤�Ŭ�� */
				for(DynamicCounts dom : memToInhCountss.get(mem)){
					memToGenCounts.get(mem).apply(dom);
				}
				// ��̾�ˤĤ��ơ�Ŭ�ѺѤߤȤ���
				memnameToAlreadyApplyed.put(memname, true);
			}
		}
	}
	
	public void solveIndividuals(){
		for(Membrane mem : memToGenCounts.keySet()){
			memToFixedCounts.put(mem, memToGenCounts.get(mem).solve());
		}
	}
	public void solveDynamics(){
		for(String memname : memnameToAllInhCountss.keySet()){
			Set<FixedDynamicCounts> fdoms = new HashSet<FixedDynamicCounts>();
			for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
				fdoms.add(dom.solve());
			}
			memnameToFixedCountss.put(memname, fdoms);
		}
	}
	
	// �ޡ������줿����Ф���Ŭ�ѺѤߤΥե饰��Ω�äƤ��ʤ����ˤΤ����롼��Ŭ��
	public void applyCollapseds(){
		for(String memname : memnameToMergedFixedCounts.keySet()){
			Boolean already = memnameToAlreadyApplyed.get(memname);
			if(already != null && already)continue;
			for(FixedDynamicCounts dom : memnameToFixedCountss.get(memname)){
				memnameToMergedFixedCounts.get(memname).apply(dom);
			}
		}
	}
	
	public void mergeFixeds(){
		for(Membrane mem : memToFixedCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			if(!memnameToMergedFixedCounts.containsKey(memname))
				memnameToMergedFixedCounts.put(memname, memToFixedCounts.get(mem));
			else{
				FixedCounts oldfc = memnameToMergedFixedCounts.get(memname);
				oldfc.merge(memToFixedCounts.get(mem));
			}
		}
	}
	
//	/**
//	 * ��̾���Ȥ˥ޡ������Ƹ��̤�Ŭ�Ѥ���
//	 *
//	 */
//	public void applyAllInOne(){
//		for(Membrane mem : memToGenCounts.keySet()){
//			String memname = TypeEnv.getMemName(mem);
//			if(!memnameToMergedCounts.containsKey(memname))
//				memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
//			else{
//				StaticCounts oldsom = memnameToMergedCounts.get(memname);
//				oldsom.addAllCounts(memToGenCounts.get(mem));
//			}
//		}
//	}
	
//	/**
//	 * �롼���ѿ���«�����줿��ΤȤ���������γƸĿ��ˤĤ��Ƽºݤ��ͤ����
//	 */
//	public void solve(){
//		memToFixedCounts = new HashMap<Membrane, FixedCounts>();
//		for(Membrane m : memToGenCounts.keySet())
//			memToFixedCounts.put(m,memToGenCounts.get(m).solve());
//		fixed = true;
//	}

	private boolean fixed = false;
	/**
	 * ���̤ˤ�����롼���ѿ���̵�¤�«��������
	 *
	 */
	public void assignInfinityToVar(){
		for(Set<DynamicCounts> doms : memToInhCountss.values())
			for(DynamicCounts dom : doms)
				if(!dom.applyCount.isBound())dom.applyCount.bind(Count.INFINITY.or0());
	}
	
	/**
	 * ���ȥ����������β��¤�0�Ȥ�����������Ȥ��ƥ롼���ѿ����
	 */
	public void solveByCounts(){
		for(Membrane mem : memToGenCounts.keySet()){
			// �ץ�������Ω�����ݤ���Ƥ��ʤ����̵��
			if(memnameToCPIFlg.get(TypeEnv.getMemName(mem)))
				continue;
			memToGenCounts.get(mem).solveByCounts();
		}
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
			for(StaticCounts com : memToGenCounts.values())
				com.print();
			Env.p("---mem effect on source counts:");
			for(Set<DynamicCounts> doms : memToInhCountss.values())
				for(DynamicCounts dom : doms)
					dom.print();
			Env.p("");
		}
	}

}
