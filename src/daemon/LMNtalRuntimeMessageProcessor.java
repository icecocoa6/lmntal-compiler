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
	
	////////////////////////////////
	// ������
	
	/** ����Υۥ��Ȥ˥�å����������������������Ԥġ�
	 * @return ������OK���ɤ��� */
	public boolean sendWait(String fqdn, String command){
		Object obj = sendWaitObject(fqdn, command);
		if (obj instanceof String) {
			return ((String)obj).equalsIgnoreCase("OK");
		}
		return false;
	}
	/** ����Υۥ��Ȥ˥�å����������������������Ԥġ�
	 * @return �����˴ޤޤ�륪�֥������� */
	public Object sendWaitObject(String fqdn, String command){
		try {
			BufferedWriter out = getOutputStream();
			String msgid = LMNtalDaemon.makeID();
			out.write(msgid + " \"" + fqdn + "\" " + rgid + " " + command + "\n");
			out.flush();
			return waitForResponseObject(msgid);
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
			return null;
		}
	}
	
	////////////////////////////////////////////////////////////////
	
//	/** msgid (String) -> �֥�å����Ƥ��� Object */
//	protected HashMap blockingObjects = new HashMap();

	/** msgid (String) -> ��å�����msgid���Ф���res������ (String �ޤ��� byte[]) */
	HashMap messagePool = new HashMap();
	
	/** ���ꤷ����å��������Ф����������Ԥäƥ֥�å����롣
	 * @return ��������Ǽ���줿���֥������� */
	synchronized public Object waitForResponseObject(String msgid) {
		while (messagePool.containsKey(msgid)) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		return messagePool.remove(msgid);
	}	
	/** ���ꤷ����å��������Ф����������Ԥäƥ֥�å����롣
	 * @return �����˴ޤޤ��ʸ���� */
	public String waitForResponseText(String msgid) {
		Object obj = waitForResponseObject(msgid);
		if (obj instanceof String) return (String)obj;
		return "fail";
	}
	/** ���ꤷ����å��������Ф����̤��Ԥäƥ֥�å����롣
	 * @return ������OK���ɤ��� */
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
		String input;
		while (true) {
			try {
				input = readLine();
			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}
			if (input == null) {
				System.out.println("�ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}
			if (DEBUG) System.out.println("in.readLine(): " + input);

			/* ��å�����:
			 *   RES msgid ����
			 *   DUMPHASH
			 *   CMD msgid fqdn rgid ���ޥ��
			 *     - fqdn ����ʬ�� 
			 *     - fqdn ��¾�Ͱ�
			 * ����:
			 *   OK | FAIL | UNCHANGED | RAW bytes \n data
			 */
			String[] parsedInput = input.split(" ", 4);

			if (parsedInput[0].equalsIgnoreCase("RES")) {
				// RES msgid (OK | FAIL | UNCHANGED | RAW bytes \n data)
				String msgid = parsedInput[1];
				String content = parsedInput[2];
				if (content.equalsIgnoreCase("RAW")) {
					try {
						int bytes = Integer.parseInt(parsedInput[3]);
						byte[] data = readBytes(bytes);
						readLine();	// ����ʸ�����ɤ����Ф�
						messagePool.put(msgid, data);
					} catch (Exception e) {
						messagePool.put(msgid, "FAIL");
						continue;
					}
				}
				else messagePool.put(msgid, content);
				
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
			} else if (parsedInput[0].equalsIgnoreCase("REGISTERLOCAL")) {
				System.out.println("invalid message: registerlocal");
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("DUMPHASH")) {
				// DUMPHASH
				LMNtalDaemon.dumpHashMap();
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("CMD")) {
				// CMD msgid fqdn rgid ���ޥ��
				String msgid = parsedInput[0];
				// ��ʬ���Ȱ��ʤΤǡ���ʬ���Ȥǽ�������
				
				/* ���ޥ��:
				 *   BEGIN \n �ܥǥ�̿��... END -> OK
				 *   CONNECT        dst_nodedesc src_nodedesc -> OK | FAIL
				 *   TERMINATE -> OK
				 *   REQUIRERULESET globalrulesetid -> RAW bytes \n data | FAIL
				 *   LOCK           globalmemid prio -> UNCHANGED | RAW bytes \n data | FAIL
				 *   BLOCKINGLOCK   globalmemid prio -> UNCHANGED | RAW bytes \n data
				 *   RECURSIVELOCK  globalmemid -> OK | FAIL
				 *   UNLOCK         globalmemid -> OK | FAIL
				 *   BLOCKINGUNLOCK globalmemid -> OK | FAIL
				 *   ASYNCLOCK      globalmemid -> OK | FAIL
				 *   ASYNCUNLOCK    globalmemid -> OK | FAIL
				 */

				String[] command = parsedInput[3].split(" ", 3);
				
				if (command[0].equalsIgnoreCase("TERMINATE")) {
					Env.theRuntime.terminate();
					LMNtalRuntimeManager.terminateAllNeighbors();
					respondAsOK(msgid);
					return;
				} else if (command[0].equalsIgnoreCase("CONNECT")) {
					// CONNECT dst_nodedesc src_nodedesc
					String nodedesc = command[2];
					LMNtalRuntimeManager.connectedFromRemoteRuntime(nodedesc);
					respondAsOK(msgid);
					continue;
				} else {
					if (command[0].equalsIgnoreCase("BEGIN")) {
						// end�����ޤǽ���
						try {
							while(true){
								String inputline = readLine();
								if (inputline == null) break;
								if (inputline.equalsIgnoreCase("END")) {
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
		else if (command[0].equalsIgnoreCase("LOCK")
			   || command[0].equalsIgnoreCase("BLOCKINGLOCK")
			   || command[0].equalsIgnoreCase("ASYNCLOCK")) {
			// LOCK globalmemid
			// �����������å�
			Membrane mem = IDConverter.lookupLocalMembrane(command[1]);
			if (mem != null) {
				boolean result = false;
				if (command[0].equalsIgnoreCase("LOCK"))         result = mem.lock();
				if (command[0].equalsIgnoreCase("BLOCKINGLOCK")) result = mem.blockingLock();
				if (command[0].equalsIgnoreCase("ASYNCLOCK"))    result = mem.asyncLock();
				if (result) { // ��å���������
					if (true) { // ����å�������������å�
						byte[] data = mem.cache();
						// todo ʸ������Ǥ����Τ�Ĵ�٤�
						respond(msgid, "RAW " + data.length + "\n" + data);
					}
					else {
						respond(msgid, "UNCHANGED");
					}
					return;
				}
			}
		} else if (command[0].equalsIgnoreCase("UNLOCK")
				 || command[0].equalsIgnoreCase("RECURSIVEUNLOCK")) {
			// UNLOCK          globalmemid # �����������å�����			
			// RECURSIVEUNLOCK globalmemid # �����������������λ�¹���Ƶ�Ū�˥�å�����
			Membrane mem = IDConverter.lookupLocalMembrane(command[1]);
			if (mem != null) {
				if (command[0].equalsIgnoreCase("UNLOCK"))          mem.unlock();
				if (command[0].equalsIgnoreCase("RECURSIVEUNLOCK")) mem.recursiveUnlock();
				respondAsOK(msgid);
				return;
			}
		} else if (command[0].equalsIgnoreCase("RECURSIVELOCK")) {
			// RECURSIVELOCK globalmemid
			// ��å����������������������λ�¹���Ƶ�Ū�˥�å��ʥ���å���Ϲ������ʤ���
			Membrane mem = IDConverter.lookupLocalMembrane(command[1]);
			if (mem != null) {
				mem.recursiveLock();
				respondAsOK(msgid);
				return;
			}
		}
		respondAsFail(msgid);
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
		BufferedWriter out = getOutputStream();

		beginEndLoop:while(true){
			try {
				input = readLine();
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

}