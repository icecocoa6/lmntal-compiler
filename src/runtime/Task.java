package runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import debug.Debug;

//import unyo.Mediator;
import util.Stack;
import util.Util;

/** ������
 * todo ��������ͥ���٤����ꤹ�롣����ϥ롼�륹��åɤˤ���å����֥�å��󥰤ˤʤ뤫�ɤ����η���˱ƶ������롣
 * ��Ʊ���¹Ԥϥ롼�륹��åɤ���⤤ͥ���٤�Ϳ���롣
 * todo �롼���줬���ĥ������ޤ��ϼ��������Υ롼�륹��åɤˤ�äƥ�å����줿���ɤ����򵭲����롣
 * ����ˤ�ꡢ���ĥ������Υ롼�륹��åɤ���ߤ���ư�����Ʊ���¹Ԥ���ǽ�Ȥʤ롣
 * <p>
 * <font size=+1><b>��ˡ7</b></font><br>
 * 
 * <p>
 * <b>���Ѿ��</b>
 * (�����Ω����Ƥʤ���Фʤ�ʤ�����)
 * <p>
 * ���¹��쥹���å������Ѿ��
 * ��������M(�롼����Ǥʤ���)�ο���ϰʲ��ξ�郎��������Ƥ��롣
 * ������������M���¹��쥹���å����Ѥޤ�Ƥ���Ȥ���
 * ����(1)���¹��쥹���å��Ρ�M�����������Ѥޤ�Ƥ���
 * ����(2)�����μ¹��쥹���å����Ѥޤ�Ƥ���
 * ����(3)����å�����Ƥ���
 * �����������줬���μ¹��쥹���å����Ѥޤ�Ƥ���Ȥ�
 * ����(1)�����μ¹��쥹���å��Ρ�M�����������Ѥޤ�Ƥ���
 * ���������Ѿ��ϡ�����������ͥ�褷�Ƽ¹Ԥ����뤿��ˤ��롣
 * <p>
 * ����Υ�å��˴ؤ������Ѿ��
 * ���줬�ǥåɥ�å����ʤ�����ˡ��ʲ��ξ�郎��������Ƥ��롣
 * ��������Υ�å���������褦�Ȥ��륹��åɤϡ��ɤ���Υ�å���������Ƥ��ʤ�����
 * �������ޤ��ϥ�å���������褦�Ȥ�����ο���Υ�å���������Ƥ��ʤ���Фʤ�ʤ���
 * �������롼�륹��åɤ��ǽ�˥�å������������ϡ���������������������Ǥʤ���Фʤ�ʤ���
 * ����. ��롼�륹��åɤ��ǽ�˥�å������������ϡ��롼����Ǥʤ���Фʤ�ʤ���
 * ����Υ�å��׵᤬û���֤Ǽ������줵���뤿��ˡ��ʲ��ξ�郎��������Ƥ��롣
 * �������롼�륹��åɤϡ���å������׵�������ä���®�䤫�����ƤΥ�å���������ʤ���Фʤ�ʤ���
 * ��������롼�륹��åɤϡ�����������å���û���֤ǲ������ʤ���Фʤ�ʤ���
 * <p>
 *
 * <p>
 * <b>�롼�륹��å�</b>
 * <p>
 * �롼��Ŭ�Ѥ�Ԥ�����Υ���åɤ�롼�륹��åɤȸƤ֡�
 * �������Ϥ��礦��1�ĤΥ롼�륹��åɤˤ�äƼ¹Ԥ���롣
 * ʪ���ޥ��󤴤Ȥ�ʣ���Υ��������������뤳�Ȥ��Ǥ��롣
 * �ƥ������ϸ�ͭ����������åɤ���Ĥ褦�ˤʤäƤ��롣
 * <p>
 * �ƥ���åɤϡ��롼�륹��åɤ��Ф�������׵��ȯ�Ԥ�������뤳�Ȥ��Ǥ��롣
 * �롼�륹��åɤϡ�����׵���Υ���åɤ�¸�ߤ���Ȥ��ϥ롼��Ŭ�Ѥ�Ԥ�ʤ���
 * �롼�륹��åɤϡ�����׵���Τ����ľ���˥롼��Ŭ�Ѥ���ߤ�����ʬ���Ȥ˥����ʥ��ȯ�Ԥ��롣
 * <p>
 * �����Ǥϡ��롼�륹��åɤϡ���Υ�å�������Υ�֥�å��󥰤ǹԤ���
 * ���Τ��ᡢ�줬���Ǥ˥�å�����Ƥ�����硢��������Ф���롼��Ŭ�Ѥ�Ԥ�ʤ���
 * ����ϡ���������ͥ���٤��ߤ��뤳�Ȥˤ��֥�å��󥰤Ǥμ������ǽ�ˤ��뤳�Ȥˤ���褹�٤��Ǥ��롣
 * 
 * <p>
 * <b>�롼�륹��åɤǤʤ�����å�</b>
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ���Υ�å�������֥�å��󥰤ǹԤ���
 * �줬��å�����Ƥ����硢�������Υ롼�륹��åɤ��Ф�������׵��ȯ�Ԥ����塢
 * ���Υ롼�륹��åɤ������ʥ��ȯ�Ԥ���Τ��Ԥäƺ�����Υ�å��������ߤĤŤ��롣
 * <p>
 * �롼�륹��åɤǤʤ�����åɤϡ�û���֤ǥ�å���������٤��Ǥ��롣
 * �롼�륹��åɤϡ����Υ��������Ф��ƥ�å��׵᤬���ä����ˤ�ľ���˼¹Ԥ���ߤ��٤��Ǥ��롣
 * �롼�륹��åɤϡ������������륿�����ʳ��Υ������Υ�å����������ץ���ʸ̮�ΤȤ��Τ�ɬ�ס�
 * ��û���֤ǲ������٤��Ǥ��롣
 * 
 * <p>
 * <b>�ƥ���������γ�����</b>
 * <p>
 * ��Ʊ������Υ�å���������뤳�Ȥˤ��¹��쥹���å��򹹿����뤳�Ȥˤ��¸����롣�����Ѥߡ��ƥ��Ⱥѡ�
 * 
 * <p>
 * <b>�����ƥॳ����</b>
 * <p>
 * ��Ʊ������Υ�å���������뤳�Ȥˤ��¸���ǽ��
 * ����������μ������֥�å��󥰤ˤʤ�褦�˥�������ͥ���٤��ߤ���ɬ�פ�����Ϥ��Ǥ��롣
 * �����ƥॳ����ϸ��ߤϤޤ���������Ƥ��ʤ���
 * 
 * <p>
 * <b>��¾����</b>
 * <p>
 * ����������ߡ��Ƴ����Ԥ���碌�����Τ���ˡ����Υ��饹�Υ��󥹥��󥹤˴ؤ��� synchronized ������Ѥ��롣
 */

public class Task implements Runnable {
	/** ���Υ���������������󥿥��� */
	protected LMNtalRuntime runtime;
	/** ���Υ������Υ롼���� */
	protected Membrane root;
	/** asyncUnlock���줿�Ȥ���true�ˤʤ�
	 * ��true�ʤ�Х����ʥ���������˥ȥ졼��dump�����*/
	protected boolean asyncFlag = false;
	
	/** ��󥿥���μ��� */
	public LMNtalRuntime getMachine() {
		return runtime;
	}
	/** �롼����μ��� */
	public Membrane getRoot() {
		return root;
	}
	
	/** ���Υ������Υ롼�륹��å� */
	protected Thread thread = new Thread(this, "Task");
	/** �¹��쥹���å�*/
	Stack memStack = new Stack();
	/** ���μ¹��쥹���å� */
	Stack bufferedStack = new Stack();
	static final private int MAX_LOOP = 100;
	/** ��������ͥ���١����Τˤϡ����Υ������Υ롼�륹��åɤ�ͥ���١�
	 * <p>��å�������˻��Ѥ���ͽ�ꡣ����Ū�ˤϥ������Υ������塼��󥰤ˤ���Ѥ����ͽ�ꡣ
	 * <p>10�ʾ���ͤǤʤ���Фʤ�ʤ���*/
	int priority = 32;
	
	/**
	 * �����ʵץե饰��
	 * <p>���٤ƤΥ롼�뤬Ŭ��������ä����֤ǡ����Υե饰�����ξ�硤���٤Ƥ������ٳ����������롥
	 */
	// by nakano
//	static private boolean perpetual = false;
	
	/** ���������ʤ��������롼���줪����б����륿�����ʥޥ����������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���*/
	Task(LMNtalRuntime runtime) {
		this.runtime = runtime;
		root = new Membrane(this);
		memStack.push(root);
	}

	/** ���ꤷ���������Ŀ�������å����줿�롼���줪����б����륿�����ʥ��졼�֥������ˤ��������
	 * @param runtime ����������������¹Ԥ����󥿥���ʤĤͤ�Env.getRuntime()���Ϥ���
	 * @param parent ���� */
	Task(LMNtalRuntime runtime, Membrane parent) {
		this(runtime);
		root = new Membrane(this);
		root.lockThread = Thread.currentThread();
//		root.remote = parent.remote;
		root.activate(); 		// ���μ¹��쥹���å����Ѥ�
		parent.addMem(root);	// ����������κ����������ꤷ��
		thread.start();
	}
	/** �����̵���������������Υ�����������������ˤ��롣 */
	public Membrane createFreeMembrane() {
		return new Membrane(this);
	}
	
	////////////////////////////////////////////////////////////////
	/** ���Υ������Υ롼�륹��åɤ�¹Ԥ��롣
	 * �¹Ԥ���λ����ޤ����ʤ���
	 * <p>�ޥ����������Υ롼�륹��åɤ�¹Ԥ��뤿��˻��Ѥ���롣*/
	public void execAsMasterTask() {
		switch (Env.ndMode) {
		case Env.ND_MODE_D:
			thread.start();
			try {
				thread.join();
			}
			catch (InterruptedException e) {}
			break;
		case Env.ND_MODE_ND_ALL:
			nondeterministicExec();
			break;
		case Env.ND_MODE_ND_ANSCESTOR:
			nondeterministicExec2();
			break;
		case Env.ND_MODE_ND_NOTHING:
			nondeterministicExec3();
			break;
		}
		
	}
	
	private static int count = 1; // ���ֹ�ɽ��@�ȥ졼���⡼�� okabe
	// ���Ϥ� count ����¾����Τ��ᡢstatic synchronized
	synchronized public static void trace(String arrow, String rulesetName, String ruleName) {
		boolean dumpEnable = Env.getExtendedOption("hide").equals("") || !ruleName.matches(Env.getExtendedOption("hide"));
		if(dumpEnable) {
			Membrane memToDump = Env.theRuntime.getGlobalRoot();
			Env.p( Dumper.dump( memToDump ) );
			if(Env.getExtendedOption("dump").equals("1")) {
				Util.println(" ----- " + rulesetName + "/" + ruleName + " ---------------------------------------");
			} else {
				Util.println(" " + arrow + "  #" + (count++) + ": " + rulesetName + "/" + ruleName);
			}
		}
	}
	
	//2006.3.16 by inui
	public static void initTrace() {
		count = 1;
	}
	
	private boolean guiTrace() {
		if (!Env.guiTrace()) return false;
//		/**nakano graphic��*/
//		if (!Env.graphicTrace()){
//			/*������*/
//			Membrane memToDump = ((MasterLMNtalRuntime)getMachine()).getGlobalRoot();
//			Env.p( Dumper.dump( memToDump ) );
//			System.exit(0);
////			return false;
//		}
		return true;
	}
	
	/* ���ȥ��Ƴ�ƥ��Ȥι�׼¹Ի��� */
	long atomtime = 0;
	/* ���Ƴ�ƥ��Ȥι�׼¹Ի��� */
	long memtime = 0;
	/* �¹Ի��ּ����Τ���Υѥ�᡼�� */
	long start,stop;
	
	/** ���Υ�����������Υ롼���¹Ԥ��� */
	void exec(Membrane mem) {
		for(int i=0; i < MAX_LOOP && mem == memStack.peek() && lockRequestCount == 0 ; i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
			if (!exec(mem, false)) break;
		}
	}
	boolean exec(Membrane mem, boolean nondeterministic) {
		Atom a = mem.popReadyAtom();
		Iterator<Ruleset> it = mem.rulesetIterator();
		boolean flag = false;
		
		if(!nondeterministic && Env.shuffle < Env.SHUFFLE_DONTUSEATOMSTACKS && a != null && !Env.memtestonly){ // �¹ԥ��ȥॹ���å������Ǥʤ��Ȥ�
			if(Env.profile == Env.PROFILE_BYDRIVEN){
		        start = Util.getTime();
			}
			while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
				if (it.next().react(mem, a)) {
					flag = true;
					//if (memStack.peek() != mem) break;
					break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
					//�롼�륻�åȤ��ɲä���Ƥ����ǽ���Ϥ��뤬������������Ϥʤ��Τ�
					//���Τޤ޽�����³���Ƥ⤤���Ȼפ��� 2005/12/08 mizuno
				}
			}

//			if(Env.fUNYO){
//				unyo.Mediator.sync(root);
//			}
			if(flag){
				if (Env.debugOption) {//by inui
					if (Debug.isBreakPoint()) Debug.inputCommand();
				} else {
					if(Env.fUNYO){
						//unyo.Mediator.sync(root);
						if(!unyo.Mediator.sync(root)){ return false; }
					}
					if (!guiTrace()) return false;
				}
			} else {
				if(!mem.isRoot()) {mem.getParent().enqueueAtom(a);} 
				// TODO �����ƥॳ���륢�ȥ�ʤ顢���줬�롼����Ǥ����ˤĤߡ�����������
			}
			if(Env.profile == Env.PROFILE_BYDRIVEN){
		        stop = Util.getTime();
		        atomtime+=(stop>start)?(stop-start):0;
			}
		}else{ // �¹ԥ��ȥॹ���å������λ�
			if(Env.profile == Env.PROFILE_BYDRIVEN){
		        start = Util.getTime();
			}
			// ���ΤȤ��������ƥ�롼�륻�åȤ����Ƴ�ƥ��ȤǤ����¹Ԥ���ʤ���
			// ���ۤǤϡ��Ȥ߹��ߤ� + �ϥ���饤��Ÿ�������٤��Ǥ��롣
			flag = SystemRulesets.react(mem, nondeterministic);
			if (!flag) {				
				while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
					if(((Ruleset)it.next()).react(mem, nondeterministic)) {
						flag = true;
						/*
						if(Env.fUNYO){
							//unyo.Mediator.sync(root);
							if(!unyo.Mediator.sync(root)){ return false; }
						}
						*/
						//if (memStack.peek() != mem) break;
						break; // �롼�륻�åȤ��Ѥ�äƤ��뤫�⤷��ʤ�����
					}
				}
			}
			if(flag){
				if (Env.debugOption) {//by inui
					if (Debug.isBreakPoint()) Debug.inputCommand();
				} else {
					if(Env.fUNYO){
						//unyo.Mediator.sync(root);
						if(!unyo.Mediator.sync(root)){ return false; }
					}
					if (!guiTrace()) return false;
				}
			} else if (!nondeterministic){
				memStack.pop(); // �����pop
				if (!mem.isNondeterministic() && !mem.perpetual) {
					// ���줬����stable�ʤ顢�������stable�ˤ��롣
					Iterator<Membrane> it_m = mem.memIterator();
					flag = false;
					while(it_m.hasNext()){
						if(!it_m.next().isStable()) {
							flag = true;
							break;
						}
					}
					if(!flag) {
						mem.toStable();
					}
				}
			}

			if(Env.profile == Env.PROFILE_BYDRIVEN){
		        stop = Util.getTime();
		        memtime+=(stop>start)?(stop-start):0;
			}
		}
		
		return true;
	}
	
	/** ���Υ�������ͭ�Υ롼�륹��åɤ��¹Ԥ������ */
	public void run() {
		Membrane root = null; // �롼�ȥ������ΤȤ��Τߥ롼���줬���롣����ʳ���null
		if (runtime instanceof LMNtalRuntime) {
			root = runtime.getGlobalRoot();
			if (root.getTask() != this) root = null;
		}
		
		if (root != null && Env.fTrace) {
			//Env.p( Dumper.dump(root) );
		}
		
		LMNtalRuntime r = runtime;
//		if(Env.fUNYO){
//			unyo.Mediator.sync(root);
//		}
		while (!r.isTerminated()) {
			Membrane mem;
			//������å�
			synchronized(this) {
				//�ۤ��Υ����������å����ꥯ�����Ȥ���Ƥ��뤫��
				//�¹��쥹���å������äݤ�
				//��Υ�å��˼��Ԥ����
				while (lockRequestCount > 0 || (mem = (Membrane)memStack.peek()) == null || !mem.lock()) {
					if (r.isTerminated()) return;
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				running = true;
			}
//			mem.remote = null;
			//��롼�륹��åɤ��ѹ��������Ƥ���Ϥ��롣
			if (root != null && Env.fTrace && asyncFlag) {
				asyncFlag = false;
				Env.p( " ==>* \n" + Dumper.dump(root) );
			}
			//�¹�
			exec(mem);
	        mem.unlock(true);
	        
			//���Υ���������ߤ��ԤäƤ��륹��åɤ����Ƶ�������
			synchronized(this) {
				running = false;
				notifyAll();
			}
			
			//release���ƤӽФ���Ƥ�����UNYO��λ (ayano)
			if(Env.fUNYO){
				if(unyo.Mediator.releasing) break;
			}
			
			//�����Х�롼����Ǥ��ꡢ����Υ롼��Ŭ�Ѥ�λ���Ƥ����顢�¹Խ�λ
			if (root != null && root.isStable()) break;
//			if (root != null && root.isStable() && !perpetual) break;
//			if (root != null && root.isStable() && perpetual){
//				activateAllMem(root);
//				try {
//					this.thread.sleep(10);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

			// Perpetual���줬¸�ߤ���Ȥ���100ms�Ԥäơ�perptual����μ¹Ը�������ľ��(����������)��
			// TODO perpetual ����ʤ��⤦�ҤȤĤΥե饰��Ĥ��äƤ����������������� hara
			if(root!=null && memStack.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				activatePerpetualMem(root);
			}

			// ����Υ롼��Ŭ�Ѥ�λ���Ƥ��ꡢ���줬root�줫�Ŀ������Ĥʤ顢�������������롣
			// ����������å�������˹Ԥ�ɬ�פ����롣
			if(memStack.isEmpty() && mem.isRoot()) {
				Membrane memToActivate = mem.getParent();
				// ���줬���Ǥ�̵���ˤʤäƤ�����硢�������׵��ñ���̵�뤹��Ф褤��
				if (memToActivate != null) {
					doAsyncLock(memToActivate);
				}
			}
		}
	}

	/**
	 * Perpetual �������������롣
	 * hara
	 * @param mem
	 */
	static public void activatePerpetualMem(Membrane mem) {
		if(mem.perpetual) doAsyncLock(mem);
		Iterator<Membrane> it = mem.memIterator();
		while(it.hasNext()) {
			final Membrane m = it.next();
			if(m.perpetual) doAsyncLock(m);
			activatePerpetualMem(m);
		}
	}
	
//	/**
//	 * �����ʵץե饰�Υ��å�
//	 * @param f
//	 */
//	static public void setPerpetual(boolean f){
//		perpetual = f;
//	}
//	
//	/**
//	 * �����ޤ᤿���٤Ƥλ��������������롥
//	 * @param mem
//	 */
//	private void activateAllMem(Membrane mem){
//		Iterator it = mem.memIterator();
//		while(it.hasNext()) {
//			final Membrane m = (Membrane)it.next();
//			doAsyncLock(m);
//			//m.activate();
//			activateAllMem(m);
//		}
//	}
	
	// 060401 okabe
	// Membrane -> AbstractMembrane
	// ¾��缡�ѹ����Ƥ���ͽ��
	static public void doAsyncLock(Membrane mem) {
		final Membrane m = mem;
		new Thread() {
			public void run() {
				if (m.asyncLock()) {
					m.asyncUnlock(false);
				}
			}
		}.start();
	}

	public void outTime(){
		if(Env.majorVersion==1 && Env.minorVersion>4)
			Util.println("threadID="+thread.getId()+" atomtime=" + atomtime/1000000 + "msec memtime=" + memtime/1000000 +"msec totaltime="+(atomtime+memtime)/1000000+"msec");
		else
			Util.println("threadID="+thread.hashCode()+" atomtime=" + atomtime + "msec memtime=" + memtime +"msec totaltime=" + (atomtime+memtime) + "msec");
	}

	////////////////////////////////////////////////////////////////

	// �������Υ롼�륹��åɤ��Ф�������׵�

	/**
	 *  ���Υ������Υ롼�륹��åɤ��Ф�������׵��ȯ�Ԥ��Ƥ��륹��åɤθĿ���
	 *  ʣ���Υ���åɤ����뤿�ᡢTask �˴ؤ��� synchronized ���������ɬ�פ����롣
	 */
	private int lockRequestCount = 0;
	private boolean running = false;
	
	public void suspend() {
		synchronized(this) {
			lockRequestCount++;
			while (running) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
	}
	public void resume() {
		synchronized(this) {
			lockRequestCount--;
			if (lockRequestCount == 0) {
				//�������򵯤�����
				//notifyAll ��ȤäƤϤ��뤬��wait ���Ƥ��륹��åɤϥ롼�륹��åɤΤߤΤϤ��Ǥ��롣
				notifyAll();
			}
		}
	}
	
	////////////////////////////////////
	// non deterministic LMNtal

	public static HashSet<Object[]> states = new HashSet<Object[]>();
	private static final Functor FROM = new SymbolFunctor("from", 1);
	private static final Functor TO = new SymbolFunctor("to", 1);
	private static final Functor FUNCTOR_REDUCE = new SymbolFunctor("reduce", 3);
	/** 
	 * ���ꤵ�줿��˴ؤ�����������󥰥�դ��������롣
	 * ��̤ϡ����ꤵ�줿��ο���ο������������롣
	 * @param memExec2 ��Ʊ���¹Ԥ�����
	 */
	public void nondeterministicExec(Membrane memExec2) {
		AtomSet atoms = memExec2.getAtoms();
		atoms.freeze();
		ArrayList<Membrane> l = new ArrayList<Membrane>();
		l.add(memExec2);
		HashMap<AtomSet,Membrane> memMap = new HashMap<AtomSet,Membrane>();
		memMap.put(atoms, memExec2.getParent());
		while (l.size() > 0) {
			Membrane mem2 = l.remove(l.size() - 1);
			nondeterministicExec(mem2, memMap, l);
		}
	}
	/**
	 * ���ꤵ�줿��������Ū�� 1 �ʳ��¹Ԥ��롣
	 * @param memExec2 �¹Ԥ�����
	 * @param memMap ���Ǥ��������줿�줬���ä��ޥåס��¹Ԥ�����̤�Ʊ��ξ��ˡ�Ʊ���������Ѥ��뤿��Τ�Ρ�
	 * @param newMems ������������������ɲä���ꥹ�ȡ��¹Ԥ�����̤� memMap ��ˤʤ��ä����ˡ��������ɲä���롣
	 *         null�����ꤵ�줿���ϲ��⤷�ʤ���
	 */
	public void nondeterministicExec(Membrane memExec2, HashMap<AtomSet,Membrane> memMap, ArrayList<Membrane> newMems) {
		Membrane memExec = memExec2.getParent();
		Membrane memGraph = memExec.getParent(); // ���������󥰥�դ������������
		states.clear();
		exec(memExec2, true);
		//���줾��Ŭ�Ѥ�����̤����
		Iterator<Object[]> it = states.iterator();
		while (it.hasNext()) {
			//ʣ��
			Membrane memResult = memGraph.newMem();
			Membrane memResult2 = memResult.newMem(Membrane.KIND_ND);
			Map<Atom, Atom> atomMap = memResult2.copyCellsFrom(memExec2); // �����ͤϥ��ԡ����Υ��ȥ�->���ԡ���Υ��ȥ�Ȥ���Map
			memResult2.copyRulesFrom(memExec2);
			//Ŭ��
			// memResult2: ʣ���Ǻ������줿��
			// memExec2:   ʣ����
			String name = react(memResult2, it.next(), memExec2, atomMap);
			AtomSet atoms = memResult2.getAtoms();
			atoms.freeze();
			
			//Ʊ������Ĵ�٤�
			Membrane memOut = memMap.get(atoms);
			boolean flg = memOut == null;
			if (flg) {
				memMap.put(atoms, memResult);
				memOut = memResult;
				if (newMems != null) newMems.add(memResult2);
			} else {
				memGraph.removeMem(memResult);
				memResult.drop();
				if (memOut.lockThread != Thread.currentThread()) 
					memOut.blockingLock();
			}
			//�������
			Atom f = memExec.newAtom(FROM);
			Atom fi = memExec.newAtom(Functor.INSIDE_PROXY);
			Atom fo = memGraph.newAtom(Functor.OUTSIDE_PROXY);
			Atom r = memGraph.newAtom(FUNCTOR_REDUCE);
			Atom n = memGraph.newAtom(new StringFunctor(name));
			Atom to = memGraph.newAtom(Functor.OUTSIDE_PROXY);
			Atom ti = memOut.newAtom(Functor.INSIDE_PROXY);
			Atom t = memOut.newAtom(TO);
			memExec.newLink(f, 0, fi, 1);
			memGraph.newLink(fi, 0, fo, 0);
			memGraph.newLink(fo, 1, r, 0);
			memGraph.newLink(r, 1, to, 1);
			memGraph.newLink(r, 2, n, 0);
			memGraph.newLink(to, 0, ti, 0);
			memOut.newLink(ti, 1, t, 0);
			if (!flg) {
				memOut.unlock();
			}
		}
	}
	/**
	 * �롼�������Ʊ���¹Ԥ������������󥰥�դ�ɸ����Ϥ˽��Ϥ��롣
	 */
	void nondeterministicExec() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		HashMap<AtomSet, Integer> idMap = new HashMap<AtomSet, Integer>();
		int nextId = 0;
		ArrayList<Membrane> st = new ArrayList<Membrane>();
		st.add(getRoot());
		AtomSet atoms = getRoot().getAtoms();
		atoms.freeze();
		idMap.put(atoms, new Integer(nextId++));
//		int i = 0, max_w = 0,t = 0;

		try {
			while (st.size() > 0) {
				Membrane mem = st.remove(st.size() - 1);
				//�롼��Ŭ�Ѥ�����ǽ���򸡺�
				if (mem != getRoot())
					memStack.push(mem);
				root = mem;
				states.clear();
				exec(mem, true);
				memStack.pop();
				//Ŭ�Ѥ�����̤����
				if (!Env.fInteractive)
					Util.println(idMap.get(mem.getAtoms()) + " : " + Dumper.dump(mem));
				if (states.size() > 0) {
					Iterator it = states.iterator();
		//			int w = 0;
		//			t++;
					while (it.hasNext()) {
		//				i++;
		//				w++;
						//ʣ��
						Membrane mem2 = new Membrane(this);
						Map<Atom, Atom> map = mem2.copyCellsFrom(mem);
						//mem2.memToCopyMap = null;
						mem2.copyRulesFrom(mem);
						//Ŭ��
						String ruleName = react(mem2, (Object[])it.next(), mem, map);
						atoms = mem2.getAtoms();
						atoms.freeze();
						
						Integer id;
						if (idMap.containsKey(atoms)) {
							id = (Integer)idMap.get(atoms);
							mem2.drop();
							mem2.free();
						} else {
							id = new Integer(nextId++);
							st.add(mem2);
							idMap.put(atoms, id);
						}
						if (!Env.fInteractive)
							Util.print(" " + id + "(" + ruleName + ")");
					}
				} else if (Env.fInteractive) {
					Util.print(Dumper.dump(mem) + " ? ");
					String str = reader.readLine();
					if (str == null || str.equals("") || str.equals("y")) {
						Env.p("yes");
						idMap.remove(mem.getAtoms());
						mem.drop();
						mem.free();
						return;
					}
				}
	//			if (w > max_w) max_w = w;
				if (!Env.fInteractive)
					System.out.println();
	//			System.out.println(idMap.size() + "/" + st.size() + "/" + max_w);
			}
			if (Env.fInteractive)
				Env.p("no");
	//		System.out.println("state count is " + i + ", unique count is " + nextId);
	//		System.out.println("average width is " + (i / t) + ", max width is " + max_w);
		} catch (IOException e) {
			Env.e("I/O Error");
			Env.d(e);
		}
	}

	private int nextId;
	/**
	 * �롼�������Ʊ���¹Ԥ������������󥰥�դ�ɸ����Ϥ˽��Ϥ��롣
	 * ��ʣ������оݤϡ����ĤȤ��η���Τ�
	 */
	void nondeterministicExec2() {
		HashMap<AtomSet, Integer> idMap = new HashMap<AtomSet, Integer>();
		nextId = 0;

		Membrane mem = (Membrane)getRoot();
		AtomSet atoms = mem.getAtoms();
		atoms.freeze();
		idMap.put(atoms, new Integer(nextId++));

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			boolean ret = nondeterministicExec2(idMap, mem, reader);
			if (Env.fInteractive)
				Env.p(ret ? "yes" : "no");
		} catch (IOException e) {
			Env.e("I/O Error");
			Env.d(e);
		}
	}
	boolean nondeterministicExec2(HashMap<AtomSet, Integer> idMap, Membrane mem, BufferedReader reader) throws IOException {
		//�롼��Ŭ�Ѥ�����ǽ���򸡺�
		if (mem != getRoot())
			memStack.push(mem);
		root = mem;
		states.clear();
		exec(mem, true);
		memStack.pop();
		if (!Env.fInteractive)
			Util.println(idMap.get(mem.getAtoms()) + " : " + Dumper.dump(mem));
		//Ŭ�Ѥ�����̤����
		List<Membrane> children = new ArrayList<Membrane>();
		if (Env.fInteractive && states.size() == 0) {
			Util.print(Dumper.dump(mem) + " ? ");
			String str = reader.readLine();
			if (str == null || str.equals("") || str.equals("y")) {
				idMap.remove(mem.getAtoms());
				mem.drop();
				mem.free();
				return true;
			}
		}
		Iterator it = states.iterator();
		while (it.hasNext()) {
			//ʣ��
			Membrane mem2 = new Membrane(this);
			Map<Atom, Atom> map = mem2.copyCellsFrom(mem);
			//mem2.memToCopyMap = null;
			mem2.copyRulesFrom(mem);
			//Ŭ��
			String ruleName = react(mem2, (Object[])it.next(), mem, map);

			AtomSet atoms2 = mem2.getAtoms();
			atoms2.freeze();
			Integer id;
			if (idMap.containsKey(atoms2)) {
				id = (Integer)idMap.get(atoms2);
				mem2.drop();
				mem2.free();
			} else {
				id = new Integer(nextId++);
				children.add(mem2);
				idMap.put(atoms2, id);
			}
			if (!Env.fInteractive)
				Util.print(" " + id + "(" + ruleName + ")");
		}
		if (!Env.fInteractive)
			Util.println("");
		
		for (int i = 0; i < children.size(); i++) {
			if (nondeterministicExec2(idMap, children.get(i), reader))
				return true;
		}
		idMap.remove(mem.getAtoms());
		mem.drop();
		mem.free();
		return false;
	}
	/**
	 * �롼�������Ʊ���¹Ԥ������������󥰥�դ�ɸ����Ϥ˽��Ϥ��롣
	 */
	void nondeterministicExec3() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int nextId = 1, nowId = 0;
//long t = 0;
		LinkedList<Membrane> queue = new LinkedList<Membrane>();
		queue.addLast(getRoot());
		try {
			while (queue.size() > 0) {
				Membrane mem = queue.removeFirst();
				//�롼��Ŭ�Ѥ�����ǽ���򸡺�
				if (mem != getRoot())
					memStack.push(mem);
				root = mem;
				states.clear();
				exec(mem, true);
				memStack.pop();
				if (!Env.fInteractive)
					Util.println(nowId++ + " : " + Dumper.dump(mem));
				//Ŭ�Ѥ�����̤����
				if (states.size() > 0) {
					Iterator it = states.iterator();
					while (it.hasNext()) {
						//ʣ��
						Membrane mem2 = new Membrane(this);
						Map<Atom, Atom> map = mem2.copyCellsFrom(mem);
						//mem2.memToCopyMap = null;
						mem2.copyRulesFrom(mem);
						//Ŭ��
						String ruleName = react(mem2, (Object[])it.next(), mem, map);
						
						queue.addLast(mem2);
						if (!Env.fInteractive)
							Util.print(" " + nextId++ + "(" + ruleName + ")");
					}
				} else 	if (Env.fInteractive) {
					Util.print(Dumper.dump(mem) + " ? ");
					String str = reader.readLine();
					if (str == null || str.equals("") || str.equals("y")) {
						Env.p("yes");
						mem.drop();
						mem.free();
						return;
					}
				}
	//long s = System.nanoTime();
				mem.drop();
				mem.free();
	//t += System.nanoTime() - s;
				if (!Env.fInteractive)
					System.out.println();
			}
			Env.p("no");
		} catch (IOException e) {
			Env.e("I/O Error");
			Env.d(e);
		}
//System.err.println(t / 1000000);
	}

	/**
	 * exec(origMem, true) �ˤ�ä�����줿����򸵤ˡ��ºݤ� 1 �ʳ��Υ롼��Ŭ�Ѥ�Ԥ���
	 * @param mem �¹Ԥ���ʽ񤭴�������оݤ���
	 * @param state exec(origMem, true) �μ¹Ի��� states ����������Ŭ�Ѿ��� 
	 * @param origMem exec ���Ϥ����ʥޥå��󥰸������оݤȤʤä����졣state ��� origMem �ϡ�mem �˽񤭴������롣
	 * @param atomMap origMem ��Υ��ȥफ�� mem ��Υ��ȥ�ؤΥޥåס�state ��Υ��ȥ�Ϥ��Υޥåפˤ������äƽ񤭴������롣
	 * @return Ŭ�Ѥ����롼���̾��
	 */
	static String react(Membrane mem, Object[] state, Membrane origMem, Map<Atom, Atom> atomMap) {
		Ruleset rs = (Ruleset)state[0];
		String name = (String)state[1];
		String label = (String)state[2];
		Object[] st = (Object[])state[3];
		
		Object[] argsTemp = new Object[st.length];
		if (atomMap != null){
			for(int i = 0; i < st.length; i++){
				// atomMap �˴ޤޤ�Ƥ�����
				argsTemp[i] = atomMap.get(st[i]);
				if (origMem == st[i]){
					argsTemp[i] = mem;
				}
				// atomMap �˴ޤޤ�Ƥ��ʤ����ʥ����ɤ��ɲä��줿�ץ�����
				if(argsTemp[i]==null) {
					if(st[i] instanceof Atom) {
						argsTemp[i] = new Atom(((Atom)st[i]).getMem(),((Atom)st[i]).getFunctor());
					} else {
						// ¿ʬ���ꤨ�ʤ����������Ȼפ���
						Util.println("Error: st["+i+"] is not an instance of Atom.");
						System.exit(1);
					}
				}
			}
		}
//		if (origMem == st){
//			argsTemp[i] = mem;
//		}
		
		Class[] parameterTypes = new Class[2];
		parameterTypes[0] = Object[].class;
		parameterTypes[1] = boolean.class;
		Object[] args = new Object[2];
		args[0] = argsTemp;
		args[1] = Boolean.FALSE;
		
		try {
			Method m = rs.getClass().getMethod("exec" + label, parameterTypes);
			m.invoke(rs, args);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return name;
	}
//	static String react(Membrane mem, Object[] state, Membrane origMem, Map atomMap) {
//		Ruleset rs = (Ruleset)state[0];
//		String name = (String)state[1];
//		String label = (String)state[2];
//		Class[] parameterTypes = new Class[state.length - 3 + 1];
//		Object[] args = new Object[state.length - 3 + 1];
//		int i;
//		for (i = 0; i < state.length - 3; i++) {
////			parameterTypes[i] = Object.class;
//			args[i] = state[i+3];
//			if (args[i] instanceof Atom && atomMap != null && atomMap.containsKey(args[i]))
//				args[i] = atomMap.get(args[i]);
//			if (origMem == args[i])
//				args[i] = mem;
//		}
//		parameterTypes[0] = Object[].class;
//		parameterTypes[1] = boolean.class;
//		args[i] = Boolean.FALSE;
//		try {
//			Method m = rs.getClass().getMethod("exec" + label, parameterTypes);
//			System.out.println(m);
//			m.invoke(rs, args);
//		} catch (NoSuchMethodException e) {
//			throw new RuntimeException(e);
//		} catch (InvocationTargetException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		}
//		return name;
//	}
}