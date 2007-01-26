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
	/** ������ -> ����¿���� */
	private final Map<Membrane,StaticCounts> memToGenCounts = new HashMap<Membrane,StaticCounts>();
	/** ��³�졦�����졦�ǳ��� -> ���������ư¿���� */
	private final Map<Membrane,Set<DynamicCounts>> memToInhCountss = new HashMap<Membrane,Set<DynamicCounts>>();
	/** ��̾ -> ���Ƥ���ư¿���� */
	private final Map<String,Set<DynamicCounts>> memnameToAllInhCountss = new HashMap<String,Set<DynamicCounts>>();
	/** ��̾ -> ���̤���ư¿���� */
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
	
	/** �����о� (������ : ����, ��³��/�����/������ : null, �ǳ��� : �롼���°��θ����о�*/
	public final Map<Membrane, Membrane> effectTarget = new HashMap<Membrane, Membrane>();
	
	/**
	 * ��β��Ϸ�̤�ä��Ƥ�����
	 * �����ʳ��Ǥϡ����Ʊ̾�Ǥ⥽��������̤���ʤ���̤���롣
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
		String memname = TypeEnv.getMemName(counts.mem);
		if(common){// ����
			Set<DynamicCounts> doms = memnameToCommonInhCountss.get(memname);
			if(doms == null){
				doms = new HashSet<DynamicCounts>();
				memnameToCommonInhCountss.put(memname, doms);
			}
			doms.add(counts);
		}// ������롼��
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
	
//	Set<DynamicCounts> clonedDynamicCounts = new HashSet<DynamicCounts>();
	
	/** �ġ��ζ����줴�Ȥ��ѿ���դꡢ���̤�Ŭ�Ѥ���������
	 * �ץ�������Ω��������Ƥ�����ˤĤ��Ƥϲ��⤷�ʤ�
	 *  */
	// �ġ��˲򤱤�������ˤĤ��Ƥϲ�
	public void solveIndividual(){
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			/** �ץ�������Ω��������Ƥ����硢���⤷�ʤ� */
			Boolean cpiflg = memnameToCPIFlg.get(memname);
			if(cpiflg != null && cpiflg &&
					Env.quantityInferenceLevel >= Env.COUNT_APPLYANDMERGE){
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
				if(criflg != null && criflg
						){ // �롼�륻�å���Ω��������Ƥ���
					for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
						// DynamicCounts�򥳥ԡ�����Ŭ�Ѥ��롣(���λ��ѿ���ʣ�����졢�̥��֥������Ȥˤʤ�)
						DynamicCounts domclone = dom.clone();
//						clonedDynamicCounts.add(domclone);
						domclone.assignToVar(new IntervalCount(new NumCount(0),Count.INFINITY));
						memToGenCounts.get(mem).apply(domclone);
					}
					// ��
					if(Env.quantityInferenceLevel >= Env.COUNT_APPLYANDMERGEDETAIL)
						memToGenCounts.get(mem).solveByCounts();
					memToFixedCounts.put(mem, memToGenCounts.get(mem).solve());
				}
				else{// �롼�륻�å���Ω��������Ƥʤ�
					Set<DynamicCounts> doms = memToInhCountss.get(mem);
					/** ���ζ�����ؤθ��̤�Ŭ�� */
					if(doms != null){
						for(DynamicCounts dom : doms){
							DynamicCounts domclone = dom.clone();
							domclone.assignToVar(new IntervalCount(new NumCount(0),Count.INFINITY));
							memToGenCounts.get(mem).apply(domclone);
						}
					}
					/** ������̾�ؤζ��̸��̤�Ŭ�� */
					doms = memnameToCommonInhCountss.get(memname);
					if(doms != null){
						for(DynamicCounts dom : doms){
							DynamicCounts domclone = dom.clone();
//							clonedDynamicCounts.add(domclone);
							domclone.assignToVar(new IntervalCount(new NumCount(0),Count.INFINITY));
							memToGenCounts.get(mem).apply(domclone);
						}
					}
					//��
					if(Env.quantityInferenceLevel >= Env.COUNT_APPLYANDMERGEDETAIL)
						memToGenCounts.get(mem).solveByCounts();
					memToFixedCounts.put(mem, memToGenCounts.get(mem).solve());
//					memToFixedCounts.put(mem, memToGenCounts.get(mem).solveByCounts());
				}
				// ��̾�ˤĤ��ơ�Ŭ�ѺѤߤȤ���
				memnameToAlreadyApplyed.put(memname, true);
			}
		}
		mergeFixeds();
	}
	
	public void solveAll(){
//		Map <String, StaticCounts> memnameToGenCount = new HashMap<String, StaticCounts>();
		for(Membrane mem : memToGenCounts.keySet()){
			String memname = TypeEnv.getMemName(mem);
			/** �ץ�������Ω��������Ƥ����� */
			Boolean cpiflg = memnameToCPIFlg.get(memname);
			if(cpiflg != null && cpiflg &&
					Env.quantityInferenceLevel >= Env.COUNT_APPLYANDMERGE){
				if(memnameToMergedFixedCounts.containsKey(memname)){
					for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
						DynamicCounts domclone = dom.clone();
//						clonedDynamicCounts.add(domclone);
						domclone.assignToVar(new IntervalCount(new NumCount(0),Count.INFINITY));
						memToGenCounts.get(mem).apply(domclone);
					}
					memnameToMergedFixedCounts.put(memname, memToGenCounts.get(mem).solve());
//					memnameToGenCount.put(memname, memToGenCounts.get(mem));
				}
				else{
					FixedCounts oldsc = memnameToMergedFixedCounts.get(memname);
					oldsc.addAllCounts(memToGenCounts.get(mem).solve());
				}
//				if(!memnameToMergedCounts.containsKey(memname))
//					memnameToMergedCounts.put(memname,memToGenCounts.get(mem));
//				else{
//					StaticCounts oldsom = memnameToMergedCounts.get(memname);
//					oldsom.merge(memToGenCounts.get(mem));
//				}
			}
		}
		
	}
	
//	public void solveIndividuals(){
//		for(Membrane mem : memToGenCounts.keySet()){
//			if()
//			
//			
//			memToFixedCounts.put(mem, memToGenCounts.get(mem).solve());
//		}
//	}
	
//	public void solveDynamics(){
//		for(String memname : memnameToAllInhCountss.keySet()){
//			Set<FixedDynamicCounts> fdoms = new HashSet<FixedDynamicCounts>();
//			for(DynamicCounts dom : memnameToAllInhCountss.get(memname)){
//				fdoms.add(dom.solve());
//			}
//			memnameToFixedDynamicCountss.put(memname, fdoms);
//		}
//	}
//	
//	// �ޡ������줿����Ф���Ŭ�ѺѤߤΥե饰��Ω�äƤ��ʤ����ˤΤ����롼��Ŭ��
//	public void applyCollapseds(){
//		for(String memname : memnameToMergedFixedCounts.keySet()){
//			Boolean already = memnameToAlreadyApplyed.get(memname);
//			if(already != null && already)continue;
//			Set<FixedDynamicCounts> doms = memnameToFixedDynamicCountss.get(memname);
//			if(doms != null){
//				for(FixedDynamicCounts dom : doms){
//					memnameToMergedFixedCounts.get(memname).apply(dom);
//				}
//			}
//		}
//	}
	
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
//	public void assignInfinityToVar(){
//		for(Set<DynamicCounts> doms : memToInhCountss.values())
//			for(DynamicCounts dom : doms)
//				if(!dom.applyCount.isBound())dom.applyCount.bind(new IntervalCount(new NumCount(0),Count.INFINITY));
//		for(DynamicCounts dom : clonedDynamicCounts)
//			if(!dom.applyCount.isBound())dom.applyCount.bind(new IntervalCount(new NumCount(0),Count.INFINITY));
//		for(Set<DynamicCounts> doms : memnameToCommonInhCountss.values())
//			for(DynamicCounts dom : doms)
//				if(!dom.applyCount.isBound())dom.applyCount.bind(new IntervalCount(new NumCount(0),Count.INFINITY));
//	}
	
	public void assignZeroToMinimum(){
		for(FixedCounts fc : memnameToMergedFixedCounts.values()){
			for(Functor f : fc.functorToCount.keySet()){
				IntervalCount c = fc.functorToCount.get(f);
//				if(c instanceof NumCount){
//					if(((NumCount)c).value < 0) fc.functorToCount.put(f,new NumCount(0));
//				}
//				else if(c instanceof InfinityCount){
//					if(((InfinityCount)c).minus)fc.functorToCount.put(f, new NumCount(0));
//				}
//				else if(c instanceof IntervalCount){
					IntervalCount ic = (IntervalCount)c;
					if(ic.min.compare(new NumCount(0))<= 0){
						if(ic.max.compare(new NumCount(0)) <= 0){
							fc.functorToCount.put(f, new IntervalCount(0,0));
						}
						else fc.functorToCount.put(f, new IntervalCount(new NumCount(0), ic.max));
					}
//				}
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
