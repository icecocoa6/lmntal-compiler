package runtime;

class TestRemote{
	public static void main(String args[]){

		/*���ͥ����� from FrontEnd.java*/		
		//LMNParser lp = new LMNParser(src);
			
		//compile.structure.Membrane m = lp.parse();

		//Ruleset rs = RulesetCompiler.compileMembrane(m, unitName);
		//((InterpretedRuleset)rs).showDetail();
		//m.showAllRules();
			
		// �¹�
		//LMNtalRuntimeManager.init();
//		MasterLMNtalRuntime rt = new MasterLMNtalRuntime();
//		Membrane root = (Membrane)rt.getGlobalRoot();
//		Env.initGUI(root);
		//root.blockingLock();
//		rs.react(root); 
//		if (Env.gui != null) {
//			Env.gui.lmnPanel.getGraphLayout().calc();
//			Env.gui.onTrace();
//		}
//		//root.blockingUnlock();
//		((Task)root.getTask()).execAsMasterTask();
//		LMNtalRuntimeManager.terminateAll();
		/*�����ޤǡ����ͥ����� from FrontEnd.java*/		

		RemoteLMNtalRuntime rr = (RemoteLMNtalRuntime)(LMNtalRuntimeManager.connectRuntime("banon.ueda.info.waseda.ac.jp"));
		
		if(rr==null){
			//��³����
			System.out.println("failed to connect");
			System.exit(0);
		}
		
		//RemoteMembrane rmem = (RemoteMembrane)RemoteMembrane.newRoot(rr);
		


		
	}
}
