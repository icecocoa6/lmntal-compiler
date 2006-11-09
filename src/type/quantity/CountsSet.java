package type.quantity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.Functor;
import type.TypeEnv;

import compile.structure.Membrane;

/**
 * ��̾���Ȥ���Ū���Ϸ�̤��ݻ�����
 * @author kudo
 *
 */
public class CountsSet {
	/** ����������� -> �̲��Ϸ��(������) */
	private final Map<Membrane,StaticCounts> memToGenCounts = new HashMap<Membrane,StaticCounts>();
	/** ����������� -> �̲��Ϸ��(��³��) */
	private final Map<Membrane,Set<DynamicCounts>> memToInhCountss = new HashMap<Membrane,Set<DynamicCounts>>();
	/** ��̾ -> �̲��Ϸ��(��³��) */
	private final Map<String,Set<DynamicCounts>> memnameToAllInhCountss = new HashMap<String,Set<DynamicCounts>>();
	/** ��̾ -> �̲��Ϸ��(����ؤη�³��) */
	private final Map<String,Set<DynamicCounts>> memnameToCommonInhCountss = new HashMap<String, Set<DynamicCounts>>();
	/** ����������� -> �̲��Ϸ��(������/ɾ���Ѥ�) */
	private final Map<Membrane,FixedCounts> memToFixedCounts = new HashMap<Membrane, FixedCounts>();
//	/** ����������� -> �̲��Ϸ��(��³��/ɾ���Ѥ�) */
//	Map<Membrane,FixedCounts> memToInhFixedCounts;
	private final Map<String, FixedCounts> memnameToMergedFixedCounts = new HashMap<String, FixedCounts>();
	/** ��̾ -> �̲��Ϸ��(��³��) */
	private final Map<String,Set<FixedDynamicCounts>> memnameToFixedDynamicCountss = new HashMap<String, Set<FixedDynamicCounts>>();

	public CountsSet(){
	}
	
	/**
	 * ��β��Ϸ�̤�ä��Ƥ�����
	 * �����ʳ��Ǥϡ����Ʊ̾�Ǥ⥽��������̤���ʤ���̤���롣
	 * ���������롼�������ϡ��롼��ν�°�������Ʊ���Ȥ���롣
	 * �������������Ϸ�̤�Ʊ����ˤĤ��Ƥϲû�����롣
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
	/**
	 * 
	 * @param counts
	 */
	public void add(DynamicCounts counts, boolean common){
		String memname = counts.mem.name;
		if(common){
			Set<DynamicCounts> doms = memnameToCommonInhCountss.get(memname);
			if(doms == null){
				doms = new HashSet<DynamicCounts>();
				memnameToCommonInhCountss.put(memname, doms);
			}
			doms.add(counts);
		}
		else{
			if(!memToInhCountss.containsKey(counts.mem)){
				Set<DynamicCounts> doms = new HashSet<DynamicCounts>();
				doms.add(counts);
				memToInhCountss.put(counts.mem,doms);
			}
			else{
				Set<DynamicCounts> oldcountss = memToInhCountss.get(counts.mem);
				oldcountss.add(counts);
			}
		}
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
	
	Set<DynamicCounts> clonedDynamicCounts = new HashSet<DynamicCounts>();
	
	/** �ġ��ζ����줴�Ȥ˸��̤�Ŭ�Ѥ���
	 * ���������ץ�������Ω��������Ƥ�����ˤĤ��Ƥϲ��⤷�ʤ�
	 *  */
	public void applyIndividual(){
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			/** �ץ�������Ω��������Ƥ����硢���⤷�ʤ� */
			Boolean cpiflg = memnameToCPIFlg.get(memname);
			if(cpiflg != null && cpiflg){
//				if(!memnameToMergedCounts.containsKey(memname))
//					memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
//				else{
//					StaticCounts oldsom = memnameToMergedCounts.get(memname);
//					oldsom.merge(memToGenCounts.get(mem));
//				}
			}
			else{
				/** �롼�����Ω��������Ƥ�����, ¾����θ��̤�Ŭ�� */
				Boolean criflg = memToCRIFlg.get(mem);
				if(criflg == null || !criflg)criflg = memnameToCRIFlg.get(memname);
				if(criflg != null && criflg){
					for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
						// DynamicCounts�򥳥ԡ�����Ŭ�Ѥ��롣
						DynamicCounts domclone = dom.clone();
						clonedDynamicCounts.add(domclone);
						memToGenCounts.get(mem).apply(domclone);
					}
					// ��̾�ˤĤ��ơ�Ŭ�ѺѤߤȤ���
					memnameToAlreadyApplyed.put(memname, true);
				}
				else{
					Set<DynamicCounts> doms = memToInhCountss.get(mem);
					/** ���ζ�����ؤθ��̤�Ŭ�� */
					if(doms != null){
						for(DynamicCounts dom : doms){
							memToGenCounts.get(mem).apply(dom);
						}
					}
					/** ������̾�ؤζ��̸��̤�Ŭ�� */
					doms = memnameToCommonInhCountss.get(memname);
					if(doms != null){
						for(DynamicCounts dom : doms){
							DynamicCounts domclone = dom.clone();
							clonedDynamicCounts.add(domclone);
							memToGenCounts.get(mem).apply(domclone);
						}
					}
					
					// ��̾�ˤĤ��ơ�Ŭ�ѺѤߤȤ���
					memnameToAlreadyApplyed.put(memname, true);
				}
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
			memnameToFixedDynamicCountss.put(memname, fdoms);
		}
	}
	
	// �ޡ������줿����Ф���Ŭ�ѺѤߤΥե饰��Ω�äƤ��ʤ����ˤΤ����롼��Ŭ��
	public void applyCollapseds(){
		for(String memname : memnameToMergedFixedCounts.keySet()){
			Boolean already = memnameToAlreadyApplyed.get(memname);
			if(already != null && already)continue;
			Set<FixedDynamicCounts> doms = memnameToFixedDynamicCountss.get(memname);
			if(doms != null){
				for(FixedDynamicCounts dom : doms){
					memnameToMergedFixedCounts.get(memname).apply(dom);
				}
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
	
//	private boolean fixed = false;
	/**
	 * ���̤ˤ�����롼���ѿ���̵�¤�«��������
	 *
	 */
	public void assignInfinityToVar(){
		for(Set<DynamicCounts> doms : memToInhCountss.values())
			for(DynamicCounts dom : doms)
				if(!dom.applyCount.isBound())dom.applyCount.bind(Count.INFINITY.or0());
		for(DynamicCounts dom : clonedDynamicCounts)
			if(!dom.applyCount.isBound())dom.applyCount.bind(Count.INFINITY.or0());
		for(Set<DynamicCounts> doms : memnameToCommonInhCountss.values())
			for(DynamicCounts dom : doms)
				if(!dom.applyCount.isBound())dom.applyCount.bind(Count.INFINITY.or0());
	}
	
	public void assignZeroToMinimum(){
		for(FixedCounts fc : memnameToMergedFixedCounts.values()){
			for(Functor f : fc.functorToCount.keySet()){
				FixedCount c = fc.functorToCount.get(f);
				if(c instanceof NumCount){
					if(((NumCount)c).value < 0) fc.functorToCount.put(f,new NumCount(0));
				}
				else if(c instanceof InfinityCount){
					if(((InfinityCount)c).minus)fc.functorToCount.put(f, new NumCount(0));
				}
				else if(c instanceof IntervalCount){
					IntervalCount ic = (IntervalCount)c;
					if(ic.min.compare(new NumCount(0))<= 0){
						if(ic.max.compare(new NumCount(0)) <= 0){
							fc.functorToCount.put(f, new NumCount(0));
						}
						else fc.functorToCount.put(f, new IntervalCount(new NumCount(0), ic.max));
					}
				}
			}
		}
	}
	
	/**
	 * ���ȥ����������β��¤�0�Ȥ�����������Ȥ��ƥ롼���ѿ����
	 */
	public void solveByCounts(){
		for(Membrane mem : memToGenCounts.keySet()){
			// �ץ�������Ω�����ݤ���Ƥ��ʤ����̵��
			Boolean cpiflg = memnameToCPIFlg.get(TypeEnv.getMemName(mem));
			if(cpiflg != null && cpiflg)
//			if(memnameToCPIFlg.get(TypeEnv.getMemName(mem)))
				continue;
			memToGenCounts.get(mem).solveByCounts();
		}
	}
	
	public Map<String, FixedCounts> getMemNameToFixedCountsSet(){
		return memnameToMergedFixedCounts;
	}
	
	public void printAll(){
//		if(fixed){
			Env.p("--QUANTITY ANALYSIS");
//			Env.p("---mem on source counts:");
//			for(FixedCounts fc : memToFixedCounts.values())
//				fc.print();
//			Env.p("");
			Env.p("---mem on source counts:");
			for(FixedCounts fc : memnameToMergedFixedCounts.values())
				fc.print();
			Env.p("");
//		}
//		else{
//			Env.p("--QUANTITY ANALYSIS");
//			Env.p("---mem on source counts:");
//			for(StaticCounts com : memToGenCounts.values())
//				com.print();
//			Env.p("---mem effect on source counts:");
//			for(Set<DynamicCounts> doms : memToInhCountss.values())
//				for(DynamicCounts dom : doms)
//					dom.print();
//			Env.p("");
//		}
	}

}
