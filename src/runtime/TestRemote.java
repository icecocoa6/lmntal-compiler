package runtime;

/*
 * ʬ�������Υƥ�����
 *
 * @author nakajima
 *
 */
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

		//���Υ����ɤ�Env.theRuntime.runtimeid��null�ʤΤǤ̤��   
		//RemoteLMNtalRuntime rr = (RemoteLMNtalRuntime)(LMNtalRuntimeManager.connectRuntime("banon.ueda.info.waseda.ac.jp"));
		//		if(rr==null){
//			//��³����
//			System.out.println("failed to connect");
//			System.exit(0);
//		}
		
/*
 * ���Υ����ɤ�Env.theRuntime.runtimeid��null�ʤΤǤ̤��   
 *  
 * 		if(LMNtalRuntimeManager.connectToDaemon()){
			System.out.println("success");
		} else {
			System.out.println("orz");
		}*/

		/* 
		 * �ǥե���Ȥ�timeout�ͤ�Ĵ�٤Ƥߤ���default == 0 �Τ褦�Ǥ���
		 * �ʤĤޤ�timeout���ʤ���  
		 * 
		Socket s = new Socket();
		try {
			System.out.println(s.getSoTimeout());
		} catch (SocketException e) {
			// todo Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
		*/
	
		LMNtalRuntimeManager.init();
		Env.debug = 1;
		MasterLMNtalRuntime masterRuntime = new MasterLMNtalRuntime();
		
		Membrane rootMem = (Membrane)masterRuntime.getGlobalRoot();
		
		if(!LMNtalRuntimeManager.connectToDaemon()){
			System.out.println("orz");
			System.exit(1);
		}
		System.out.println("connectToDaemon success");

		RemoteLMNtalRuntime banon = (RemoteLMNtalRuntime)LMNtalRuntimeManager.connectRuntime("banon.ueda.info.waseda.ac.jp");

		if(banon == null){
			System.out.println("orz");
			System.exit(1);
		}
		System.out.println("connectRuntime success");

		//�ޥå���̿�ᡦ�ܥǥ�̿���¹Ԥ��Ƥߤ�ƥ���
		//NEWROOT
		//RemoteTask rt_banon = (RemoteTask)banon.newTask(rootMem);
		//rt_banon.flush();

		//RemoteMembrane rm_banon = new RemoteMembrane(rt_banon, rootMem);
		//	rt_banon.flush();
		
		//NEWATOM
		//Atom atom_hoge_banon = rm_banon.newAtom(new Functor("hoge", 1));
		//Atom atom_fuga_banon = rm_banon.newAtom(new Functor("fuga", 1));
		//Atom atom_moge_banon = rm_banon.newAtom(new Functor("moge", 1));
		//	rt_banon.flush();
		
		//ALTERATOMFUNCTOR
		//rm_banon.alterAtomFunctor(atom_hoge_banon, new Functor("hoge2", 1));
		//	rt_banon.flush();
		
		//REMOVEATOM
		//rm_banon.removeAtom(atom_hoge_banon);
		//	rt_banon.flush();
		
		//ENQUEUEATOM
		//rm_banon.enqueueAtom(atom_hoge_banon);
		//	rt_banon.flush();
		
		//NEWMEM
		//RemoteMembrane komaku_01_banon = (RemoteMembrane)rm_banon.newMem();
		//	rt_banon.flush();
		
		//REMOVEMEM
		//rm_banon.removeMem(komaku_01_banon);
		//	rt_banon.flush();
		
		//NEWLINK
		//rm_banon.newLink(hoge_banon, 1, fuga_banon, 1);
		//	rt_banon.flush();
		
		//RELINKATOMARGS
		//rm_banon.	relinkAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2);
		//	rt_banon.flush();
		
		//INHERITLINK
		//rm_banon.inheritLink(Atom atom1, int pos1, Link link2);
		//	rt_banon.flush();
		
		//UNIFYATOMARGS
		//rm_banon.unifyAtomArgs(Atom atom1, int pos1, Atom atom2, int pos2);
		//	rt_banon.flush();
		
		//UNIFYLINKBUDDIES
		//rm_banon.unifyLinkBuddies(Link link1, Link link2);
		//	rt_banon.flush();
		
		//ACTIVATE
		//rm_banon.activate();
		//	rt_banon.flush();
		
		//MOVECELLSFROM
		//rm_banon.moveCellsFrom(AbstractMembrane srcMem);
		//	rt_banon.flush();
		
		//MOVETO
		//rm_banon.moveTo(AbstractMembrane dstMem);
		//	rt_banon.flush();
		
		//LOCK
		//rm_banon.lock();
		//	rt_banon.flush();

		//UNLOCK
		//rm_banon.unlock();
		//	rt_banon.flush();
		
		//UNLOCK(forced)
		//rm_banon.forceUnlock();
		//	rt_banon.flush();
		
		//BLOCKINGLOCK
		//rm_banon.blockingLock();
		//	rt_banon.flush();

		//ASYNCLOCK
		//rm_banon.asyncLock();
		//	rt_banon.flush();
		
		//ASYNCUNLOCK
		//rm_banon.asyncUnlock();
		//	rt_banon.flush();
		
		//RECURSIVELOCK
		//rm_banon.recursiveLock();
		//	rt_banon.flush();
		
		//RECURSIVEUNLOCK
		//rm_banon.recursiveUnlock();
		//	rt_banon.flush();
		
		//��λ����
//		LMNtalRuntimeManager.disconnectFromDaemon(); //������ȱ�֥Ρ��ɤؤΥ��ͥ������ĥ��äѤʤ���slave runtime������äѤʤ�
		//LMNtalRuntimeManager.terminateAllNeighbors();	 //��äȤ�����ˡ�ʤ�����
		//System.out.println("terminateAllNeighbours() success"); 
		
		System.out.println("now terminating");
		LMNtalRuntimeManager.terminateAllNeighbors();
		LMNtalRuntimeManager.disconnectFromDaemon();
		System.out.println("terminated");

		System.out.println("alive thread(s):");
		Thread[] t = new Thread[Thread.activeCount()];
		Thread.enumerate(t);
		for (int i = 0; i < t.length; i++) {
			if(t[i] == null){
			} else {
				System.out.println(t[i].getName());
			}
			//t[i].getThreadGroup().list();
		}
	
		/*
		 *����åɤ򤤤Ļߤ�뤫��
		 * 
		 * LMNtalDaemon										�ߤ�ʤ�
		 * LMNtalDaemonMessageProcessor		LMNtalDaemon����λ������˻ߤ��ʤĤޤ�ߤ�ʤ�
		 *���̣ͣΣ����ң��������ͣ������У��������ʱ�֥Ρ��ɡˡ�		TERMINATE������λ�����
		 * �̣ͣΣ����ң��������ͣ������У��������ʼ�ʬ���ȡˡ�		LMNtalRuntimeManager.disconnectFromDaemon()����ǡ�������Υǡ����ؤΥ����å�	���Ĥ�������
		 *��InstructionBlockProcessor					����BEGIN-END��������
		 * �ӣ�����̣ͣΣ����ң��������̣��������			��ư����LMNtalRuntimeMessageProcessor����λ���ƥǡ���󤫤����Ǥ���=�����åȤ��Ĥ���줿����TERMINATE
		 * 
		 */
		
		
		
		/*
		 *  ��˼¹Ԥ��Ƥߤ륳���ɡ�
		 * a, a:-b.
		 * 
 Compiled Rule (  :- ( a a :- b ) )
	--atommatch:
		spec           [2, 2]
	--memmatch:
		spec           [1, 1]
		jump           [L165, [0], [], []]
	--body:L165:
		spec           [1, 1]
		commit         [(  :- ( a a :- b ) )]
		loadruleset    [0, @609]
		proceed        []

Compiled Rule ( a a :- b )
	--atommatch:
		spec           [2, 3]
	--memmatch:
		spec           [1, 3]
		findatom    [1, 0, a_0]
		findatom    [2, 0, a_0]
		neqatom        [2, 1]
		jump           [L159, [0], [1, 2], []]
	--body:L159:
		spec           [3, 4]
		commit         [( a a :- b )]
		dequeueatom    [1]
		dequeueatom    [2]
		removeatom     [1, 0, a_0]
		removeatom     [2, 0, a_0]
		newatom     [3, 0, b_0]
		enqueueatom    [3]
		freeatom       [1]
		freeatom       [2]
		proceed        []
		 * 
		 */
	}
}
