package daemon;

import java.io.BufferedWriter;
import java.io.IOException;
//import java.net.InetAddress;
import java.net.Socket;

import runtime.Env;
import runtime.Membrane;
import java.util.HashMap;
import runtime.LMNtalRuntimeManager;

/**
 * ��󥿥��ब�������륪�֥������ȡ�
 * �ǡ����ȤΥ��ͥ��������Ф����������졢��å������μ�����Ԥ���
 * 
 * TODO LMNtalDaemonMessageProcessor�ȶ��̤ν�����LMNtalNode�˰ܴɤ��롣
 * @author nakajima, n-kato
 */
public class LMNtalRuntimeMessageProcessor extends LMNtalNode implements Runnable {
	static boolean DEBUG = true;
	
	/** ����VM�Ǽ¹Ԥ���LMNtalRuntime����°����runtimeGroupID�ʥ��󥹥ȥ饯���������*/
	protected String rgid;
	
	/** ����VM�Ǽ¹Ԥ���LMNtalRuntime��runtimeid��̤���ѡ�*/
	protected String runtimeid;
	
	/** �̾�Υ��󥹥ȥ饯�� */
	public LMNtalRuntimeMessageProcessor(Socket socket, String rgid) {
		super(socket);
		this.rgid = rgid;
	}
	
	/** ������ǡ������Ф��� REGISTERLOCAL ��ȯ�Ԥ����������Ԥ� */
	public boolean sendWaitRegisterLocal(String type) {
		// REGSITERLOCAL MASTER/SLAVE msgid rgid
		String msgid = LMNtalDaemon.makeID();
		String command = "registerlocal " + type + " " + msgid + " " + rgid + "\n";
		if (!sendMessage(command)) return false;
		return waitForResult(msgid);
	}

	////////////////////////////////////////////////////////////////
	
//	/** msgid (String) -> �֥�å����Ƥ��� Object */
//	protected HashMap blockingObjects = new HashMap();

	/** msgid (String) -> ��å�����msgid���Ф���res������ (String) */
	HashMap messagePool = new HashMap();
	
	/** ���ꤷ����å��������Ф����������Ԥäƥ֥�å����롣*/
	synchronized public String waitForResponseText(String msgid) {
		while (!messagePool.containsKey(msgid)) {
			try {
				wait();
			}
			catch (InterruptedException e) {}
		}
		return (String)messagePool.remove(msgid);
	}
	/** ���ꤷ����å��������Ф����̤��Ԥäƥ֥�å����롣*/
	public boolean waitForResult(String msgid) {
		return waitForResponseText(msgid).equalsIgnoreCase("ok");
	}
	
	////////////////////////////////////////////////////////////////
	// todo Cache�ذܴɤ���

//	/*
//	 * ���η׻����ˤ�����Υ����Х��ID�����
//	 */
//	//static HashMap localMemTable = new HashMap();

	////////////////////////////////////////////////////////////////
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (DEBUG) System.out.println("LMNtalDaemonMessageProcessor.run()");

		String input = "";

		outsideloop:while (true) {
			try {
				input = in.readLine();
			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}

			if (DEBUG)System.out.println("in.readLine(): " + input);

			if (input == null) {
				System.out.println("�ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}

			/*
			 * ���ޥ�ɤ���Ϥ��ޤ��å������������
			 * 
			 * �����ǽ��������̿�����������ʳ��Τϲ��������뤷�Ƥ�
			 * 
			 * res msgid ��å�������ʸ
			 * registerlocal rgid
			 * dumphash
			 *  
			 */
			String msgid;
			String rgid;
			String fqdn;
			boolean result;
			String[] parsedInput = input.split(" ", 4);

			if (parsedInput[0].equalsIgnoreCase("res")) {
				//res msgid ���
				msgid = parsedInput[1];
				String content = parsedInput[2];
				messagePool.put(msgid, content);
				
//				Object suspended = blockingObjects.remove(msgid);
//				if (suspended == null) {
//					System.out.println(
//						"ERROR: no objects waiting for message id = " + msgid);
//					continue;
//				}
				synchronized(this) {
					notifyAll();
				}
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("registerlocal")) {
				System.out.println("invalid message: registerlocal");
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("dumphash")) {
				//dumphash
				LMNtalDaemon.dumpHashMap();
				continue;
				
				/* ���ޥ�ɤ���Ϥ��ޤ�ʸ����ν����������ޤ� */
				
			} else {
				// todo �Ѹξ������ĥ���Τ��ᡢmsgid �����˶��̥��ޥ��̾���㤨��cmd�ˤ�񤤤������褤
				/* msgid "fqdn" rgid ��å����� �ν��� */
			
				//msgid����ĤŤ�̿����Ȥߤʤ�
				msgid = parsedInput[0];
				fqdn = (parsedInput[1].split("\"", 3))[1];
				rgid = parsedInput[2];

				// ��ʬ���Ȱ��ʤΤǡ���ʬ���Ȥǽ�������

				/*
				 * �����ǽ��������̿�����
				 * 
				 *  CONNECT dst_nodedesc src_nodedesc
				 *  BEGIN
				 *  beginrule
				 * 
				 * LOCK           globalmemid -> UNCHANGED | CHANGED bytes content | FAIL
				 * BLOCKINGLOCK   globalmemid -> UNCHANGED | CHANGED bytes content
				 * asynclock
				 * recursivelock
				 * 
				 * 
				 * UNLOCK         globalmemid -> OK | FAIL
				 * BLOCKINGUNLOCK globalmemid -> OK | FAIL
				 * asyncunlock
				 * recursiveunlock
				 * 
				 * terminate
				 *  
				 */

				String[] command = parsedInput[3].split(" ", 3);
				//String srcmem, dstmem, parentmem, atom1, atom2, pos1, pos2, ruleset, func;
				Membrane realMem;

				if (command[0].equalsIgnoreCase("connect")) {
					// connect "my_fqdn" "remote_fqdn"
					String nodedesc = command[2];
					LMNtalRuntimeManager.connectedFromRemoteRuntime(nodedesc);
					respondAsOK(msgid);
					continue;
				} else {
					if (command[0].equalsIgnoreCase("begin")) {
						// end�����ޤǽ���
						try {
							while(true){
								String inputline = in.readLine();
								if (inputline == null) break;
								if (inputline.equalsIgnoreCase("end")) {
									break;
								}
								// doSomething(inputline);
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else onCmd(msgid, command);
					continue;
				}
				//respondAsFail(msgid);
			}
		}
	}	

	void onCmd(String msgid, String[] command) {
		//
		if (command[0].equalsIgnoreCase("begin")) {
			onBegin();
			return;
		}
		else if (command[0].equalsIgnoreCase("lock")) {
									
			//��å��о�����å�
			//realMem = IDConverter.getMem(command[1]);
			//if(realMem.lock()){
			////��å�����
			//
			////����å��幹�������å�
			//   ����å��奪�֥�������.update();
			//
			//
									
			//////��������Ƥ����饭��å�����ֿ�����
	
			//////��������Ƥ��ʤ��ä���ֹ�������Ƥ��ޤ����å�������
									
			//��å�����
			//out.write("res " + msgid.toString() + "
			// fail\n");
			//out.flush();
									
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("blockinglock")) {
			//IDConverter.getMem(command[1]).blockingLock();
	
			//����å��幹��
	
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("asynclock")) {
	
			//IDConverter.getMem(command[1]).asyncLock();
	
			//����å��幹��
	
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("unlock")) {
									
			//if(IDConverter.getMem(command[1]).unlock()){
				//unlock����
				//continue;
			//} else {
				//������
				//System.out.println("UNLOCK failed");
				//out.write("res " + msgid.toString() + "
				// fail\n");
				//out.flush();
				//continue;
			//}
									
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("blockingunlock")) {
			//IDConverter.getMem(command[1]).blockingUnlock();
									
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("asyncunlock")) {
	
			//if(IDConverter.getMem(command[1]).asyncUnlock()){
				//unlock����
				//continue;
			//} else {
				//������
				//System.out.println("ASYNCUNLOCK failed");
				//out.write("res " + msgid.toString() + "
				// fail\n");
				//out.flush();
				//continue;
			//}
	
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("recursivelock")) {
			//IDConverter.getMem(command[1]).recursiveLock();
	
			//����å��幹��
																	
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("recursiveunlock")) {
									
			//IDConverter.getMem(command[1]).recursiveUnlock();
			runtime_errorNotImplemented();	
			return;
		} else if (command[0].equalsIgnoreCase("terminate")) {
			//��terminate�����ѡ�(by n-kato)
			//
			//���٤����Ȥ�Env.theRuntime��terminate
			//�Ǥ�Ƥ٤ʤ�����ɤ����褦��
			//LocalLMNtalRuntime.terminate();
			Env.theRuntime.terminate(); //TODO
															  // ����Ǥ����Τ���
	
			//out.write("not implemented yet\n");
			//out.flush();
			return;
		} else {
			//̤�ΤΥ��ޥ�� or ����ʳ��β���
			runtime_respondAsFail(out,msgid);
			return;
		}
	}
	void runtime_errorNotImplemented() {
//		out.write("not implemented yet\n");
//		out.flush();
	}
	void runtime_respondAsFail(BufferedWriter out, String msgid) {
//		LMNtalDaemon.respondAsFail(out,msgid);
	}


	void onBegin() {
		/*
		 * �����ǽ��������̿�ᡧ (�ǿ��Ǥ�RemoteMembrane���饹���ȡ�)
		 * 
		 * end
		 * 
		 * �롼������
		 *  clearrules  dstmem
		 *  loadruleset  dstmem ruleset
		 * 
		 * ���ȥ����� 
		 * newatom srcmem atomid 
		 * alteratomfunctor srcmem atomid func.getName() 
		 * removeatom
		 * srcmem atomid 
		 * enqueueatom srcmem atomid
		 * 
		 * ��������
		 *  newmem srcmem dstmem 
		 * removemem srcmem
		 * parentmem
		 * 
		 * ��󥯤���� 
		 * newlink mem atomid1 pos1 atomid2 pos2
		 * relinkatomargs mem atomid1 pos1 atomid2 pos2
		 * unifyatomargs mem atomid1 pos1 atomid2 pos2
		 * 
		 * �켫�Ȥ��ư�˴ؤ������ 
		 * movecells dstmem srcmem 
		 * moveto srcmem dstmem
		 * 
		 * ����
		 *  requireruleset globalRulesetID 
		 *  newroot mem
		 */
		
		String input;
		String srcmem, dstmem, parentmem, atom1, atom2, pos1, pos2, ruleset, func;

		String[] commandInsideBegin = new String[5]; //RemoteMembrane.send()�ΰ����θĿ��򻲾Ȥ���
	
		beginEndLoop:while(true){
			try {
				input = in.readLine();
				commandInsideBegin = input.split(" ",5);
		
				//TODO ������̿���񤯤ΤǤϤʤ��ơ�Instruction.java��̿���ֹ������Ƥ��롣
				//�������Ѵ�ɽ��Ҥ��롣
				//��: new InstructionList�򤹤롣
				
				//�ơ�BEGIN����END�ޤǽФƤ�����������NEW���Ĥ��ʤ���Τ�ưŪ�˲������ꥹ�Ȥˤ���Ƥ���
				//InterpretedRulset�Υ����ɤ��Ȥ���Τǡ��������롩
				
				if(commandInsideBegin[0].equalsIgnoreCase("end")){
					//����
					return;
				} else if (commandInsideBegin[0].equalsIgnoreCase("clearrules")){
					dstmem = commandInsideBegin[1];
					
					//dstmem.clearRules()��Ƥ�
					//(IDConverter.getMem(dstmem)).clearRules();
		
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("loadruleset")){
					dstmem = commandInsideBegin[1];
					ruleset = commandInsideBegin[2];
		
					//mem.loadRulesest(Ruleset)��Ƥ�
					//(IDConverter.getMem(dstmem)).loadRuleset(Env.theRuntime.getRuleset(ruleset));
															
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("newatom")){
					srcmem = commandInsideBegin[1]; //�����Х���ID
					atom1 = commandInsideBegin[2]; //NEW_1�Ȥ�
					
					//NEW_1��atomid���Ѵ�
					//����å��夫��atomid���б�����Atom���֥������Ȥ��äƤ���
		
					//srcmem.addAtom(atom1)�ƤӽФ�
					//(IDConverter.getMem(srcmem)).addAtom(Cache.getAtom(atom1));
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("alteratomfunctor")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					func = commandInsideBegin[3];
					
					//(IDConverter.getMem(srcmem)).alterAtomFunctor(Cache.getAtom(atom1),Cache.getFunctor(func));
					
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("removeatom")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					
					//(IDConverter.getMem(srcmem)).removeAtom(Cache.getAtom(atom1));
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("enqueueatom")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					
					//(IDConverter.getMem(srcmem)).enqueueAtom(Cache.getAtom(atom1));
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("newmem")){
					srcmem = commandInsideBegin[1];
					dstmem = commandInsideBegin[2];
					
					//realMem =
					// IDConverter.getMem(srcmem).newMem();
					//if(IDConverter.registerMem(dstmem,realMem)){
					//	continue beginEndLoop;
					//} else {
					//     //todo: ���Ի��ˤʤˤ�����
					//     //System.out.println("failed to
					// register new membrane");
					//     continue beginEndLoop;
					//}
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("removemem")){
					srcmem = commandInsideBegin[1];
					parentmem = commandInsideBegin[2];
					
					//IDConverter.getMem(parentmem).removeMem(IDConverter.getMem(srcmem));
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("newroot")){
					srcmem = commandInsideBegin[1];
					
					//TODO ����
					
					//create new LMNtalLocalRuntime
					
					//create new Task & root membrane
					//AbstractTask task =
					// (IDConverter.getMem(srcmem)).task.runtime.newTask(this);
					
					//todo ���θ�ɤ�����Τ���
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("newlink")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					pos1  = commandInsideBegin[3];
					atom2 = commandInsideBegin[4];
					pos2  = commandInsideBegin[5];
					
					//Cache.getAtom(atom1).mem.newLink(Cache.getAtom(atom1),pos1,Cache.getAtom(atom2),pos2);
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("relinkatomargs")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					pos1  = commandInsideBegin[3];
					atom2 = commandInsideBegin[4];
					pos2  = commandInsideBegin[5];
					
					//Cache.getAtom(atom1).mem.relinkAtomArgs(Cache.getAtom(atom1),pos1,Cache.getAtom(atom2),pos2);
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("unifyatomargs")){
					srcmem = commandInsideBegin[1];
					atom1 = commandInsideBegin[2];
					pos1  = commandInsideBegin[3];
					atom2 = commandInsideBegin[4];
					pos2  = commandInsideBegin[5];
					
					//IDConverter.getMem(srcmem).uniftyAtomArgs(Cache.getAtom(atom1),pos1,Cache.getAtom(atom2),pos2);
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("movecells")){
					dstmem = commandInsideBegin[1];
					srcmem = commandInsideBegin[2];
					
					//Membrane dstmemobj =
					// IDConverter.getMem(dstmem);
					//Membrane srcmemobj =
					// IDConverter.getMem(srcmem);
		//
		//										if (dstmemobj == srcmemobj) continue beginEndLoop;
		//
		//										dstmemobj.mems.addAll(srcmemobj.mems);
		//										Iterator it = srcmemobj.atomIterator();
		//										while (it.hasNext()) {
		//											dstmemobj.addAtom((Atom)it.next());
		//										}
		//										it = srcmemobj.memIterator();
		//										while (it.hasNext()) {
		//											((Membrane)it.next()).parent = dstmemobj;
		//										}
		//										if (srcmemobj.task != dstmemobj.task) {
		//											srcmemobj.setTask(dstmemobj.task);
		//										}
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				} else if (commandInsideBegin[0].equalsIgnoreCase("moveto")){
					srcmem = commandInsideBegin[1];
					dstmem = commandInsideBegin[2];
					
					//IDConverter.getMem(srcmem).moveTo(IDConverter.getMem(dstmem));
					
					out.write("not implemented yet\n");
					out.flush();
		
					continue beginEndLoop;
				//} else if
				// (commandInsideBegin[0].equalsIgnoreCase("requireruleset")){
					//TODO ����
				} else {
					//̤�Τ�̿��
					out.write("not implemented yet\n");
					out.flush();
					continue beginEndLoop;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				//continue;
				break;
			}
		}
	}
	//
	/** ����Υۥ��Ȥ��������롣*/
	public boolean sendWait(String fqdn, String command){
		try {
			String msgid = LMNtalDaemon.makeID();
			out.write(msgid + " \"" + fqdn + "\" " + rgid + " " + command + "\n");
			out.flush();
			return waitForResult(msgid);
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
			return false;
		}
	}
}