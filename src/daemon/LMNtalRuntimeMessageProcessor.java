package daemon;

import java.io.BufferedWriter;
import java.io.IOException;
//import java.net.InetAddress;
import java.net.Socket;

import runtime.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import runtime.LMNtalRuntimeManager;

/**
 * ��󥿥��ब�������륪�֥������ȡ�
 * �ǡ����ȤΥ��ͥ��������Ф����������졢��å������μ�����Ԥ���
 * 
 * todo LMNtalDaemonMessageProcessor�ȶ��̤ν�����LMNtalNode�˰ܴɤ��롣
 * @author nakajima, n-kato
 */
public class LMNtalRuntimeMessageProcessor extends LMNtalNode implements Runnable {
	static boolean DEBUG = true; //todo Env.debug��Ȥ��褦�ˤ���
	
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
		if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.sendWait()");
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
			out.write("CMD " + msgid + " \"" + fqdn + "\" " + rgid + " " + command + "\n");
			out.flush();
			return waitForResponseObject(msgid);
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalRuntimeMessageProcessor.sendWaitObject(): ");
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
		if(DEBUG)System.out.println("waitForResponseObject()");
		while (!messagePool.containsKey(msgid)) { 
			//todo registlocal���ˤ���while��̵�¥롼�פˤʤ�
			//�� 2004-08-21 nakajima  while���ʸ���ѹ������н�
			
			try {
				if(DEBUG)System.out.println("waitForResponseObject(): waiting...");
				wait(); 
			} catch (InterruptedException e) {	}
		}
		if(DEBUG)System.out.println("waitForResponseObject(): loop quit");
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
				System.out.println("LMNtalRuntimeMessageProcessor: ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}
			if (input == null) {
				System.out.println("LMNtalRuntimeMessageProcessor: �ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}
			if (DEBUG) System.out.println("LMNtalRuntimeMessageProcessor: in.readLine(): " + input);

			/* ��å�����:
			 *   RES msgid ����
			 *   DUMPHASH
			 *   CMD msgid fqdn rgid ���ޥ��
			 *     - fqdn ����ʬ�� 
			 *     - fqdn ��¾�Ͱ�
			 * ����:
			 *   OK | FAIL | UNCHANGED | RAW bytes \n data
			 */
			String[] parsedInput = input.split(" ", 5);

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
				//if(DEBUG)System.out.println(messagePool.toString());
				
//				Object suspended = blockingObjects.remove(msgid);
//				if (suspended == null) {
//					System.out.println(
//						"ERROR: no objects waiting for message id = " + msgid);
//					continue;
//				}
				synchronized(this) {	
					//if(DEBUG)System.out.println("notifyALL");
					notifyAll();
				}
				continue;
			//} else if (parsedInput[0].equalsIgnoreCase("REGISTERLOCAL")) {  //�ֹ��פ��ʤ����פ˰�ư 2004-08-21 nakajima
			//	System.out.println("invalid message: registerlocal");
				//continue;
			} else if (parsedInput[0].equalsIgnoreCase("DUMPHASH")) {
				// DUMPHASH
				LMNtalDaemon.dumpHashMap();
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("CMD")) {
				// CMD msgid fqdn rgid ���ޥ��
				String msgid = parsedInput[1];
				// ��ʬ���Ȱ��ʤΤǡ���ʬ���Ȥǽ�������
				
				/* ���ޥ��:
				 *   BEGIN \n �ܥǥ�̿��... END -> OK
				 *   CONNECT        dst_nodedesc src_nodedesc -> OK | FAIL
				 *   TERMINATE -> OK
				 *   REQUIRERULESET globalrulesetid  ->             RAW bytes \n data | FAIL
				 *   LOCK           globalmemid prio -> UNCHANGED | RAW bytes \n data | FAIL
				 *   BLOCKINGLOCK   globalmemid prio -> UNCHANGED | RAW bytes \n data | FAIL
				 *   ASYNCLOCK      globalmemid      -> OK | FAIL
				 *   RECURSIVELOCK  globalmemid      -> OK | FAIL
				 */

				String[] command = parsedInput[4].split(" ", 3);
				
				if (command[0].equalsIgnoreCase("TERMINATE")) {
					// TERMINATE
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
				} else if (command[0].equalsIgnoreCase("BEGIN")) {
					// BEGIN \n �ܥǥ�̿��... END
					try {
						LinkedList insts = new LinkedList();
						while(true){
							String inputline = readLine();
							if (inputline == null) break;
							if (inputline.equalsIgnoreCase("END")) {
								break;
							}
							insts.add(inputline);
						}
						InstructionBlockProcessor ibp;
						ibp = new InstructionBlockProcessor(this, msgid, insts);						
						new Thread(ibp).run();
					} catch (IOException e) {
						e.printStackTrace();
						respondAsFail(msgid);
					}
				} else if (command[0].equalsIgnoreCase("REQUIRERULESET")) {
					// REQUIRERULESET globalrulesetid
					Ruleset rs = IDConverter.lookupRuleset(command[1]);
					if (rs != null) {
						byte[] data = rs.serialize();
						respondRawData(msgid,data);
						continue;
					}
					respondAsFail(msgid);
				} else onCmd(msgid, command);
			} else {
				//�ɤ�ˤ���פ��ʤ���
				System.out.println("LMNtalRuntimeMessageProcessor.run(): invalid message: " + parsedInput[0]);
				continue;
			}
		}
	}
	
	void onCmd(String msgid, String[] command) {
		AbstractMembrane obj = IDConverter.lookupGlobalMembrane(command[1]);
		if (!(obj instanceof Membrane)) {
			respondAsFail(msgid);
			return;
		}
		Membrane mem = (Membrane)obj;
		
		if (command[0].equalsIgnoreCase("LOCK")
		 || command[0].equalsIgnoreCase("BLOCKINGLOCK")
		 || command[0].equalsIgnoreCase("ASYNCLOCK")) {
			// LOCK globalmemid
			// �����������å�
			boolean result = false;
			if (command[0].equalsIgnoreCase("LOCK"))         result = mem.lock();
			if (command[0].equalsIgnoreCase("BLOCKINGLOCK")) result = mem.blockingLock();
			if (command[0].equalsIgnoreCase("ASYNCLOCK"))    result = mem.asyncLock();
			if (result) { // ��å���������
				if (true) { // ����å�������������å�
					byte[] data = mem.cache();
					respondRawData(msgid,data);
				}
				else {
					respond(msgid, "UNCHANGED");
				}
				return;
			}
//		} else if (command[0].equalsIgnoreCase("UNLOCK")
//				 || command[0].equalsIgnoreCase("ASYNCUNLOCK")
//				 || command[0].equalsIgnoreCase("RECURSIVEUNLOCK")) {
//			// UNLOCK          globalmemid # �����������å�����	
//			// ASYNCUNLOCK     globalmemid # �����������å�����
//			// RECURSIVEUNLOCK globalmemid # �����������������λ�¹���Ƶ�Ū�˥�å�����
//			if (command[0].equalsIgnoreCase("UNLOCK"))          mem.unlock();
//			if (command[0].equalsIgnoreCase("ASYNCUNLOCK"))     mem.asyncUnlock();
//			if (command[0].equalsIgnoreCase("RECURSIVEUNLOCK")) mem.recursiveUnlock();
//			respondAsOK(msgid);
//			return;
		} else if (command[0].equalsIgnoreCase("RECURSIVELOCK")) {
			// RECURSIVELOCK globalmemid
			// ��å����������������������λ�¹���Ƶ�Ū�˥�å��ʥ���å���Ϲ������ʤ���
			if (mem.recursiveLock()) {
				respondAsOK(msgid);
				return;
			}
		}
		respondAsFail(msgid);
	}
}

class InstructionBlockProcessor implements Runnable {
	LMNtalRuntimeMessageProcessor remote;
//	String fqdn;
	String msgid;
	LinkedList insts;
	
	InstructionBlockProcessor(LMNtalRuntimeMessageProcessor remote, 
//		String fqdn, 
		String msgid, LinkedList insts) {
		this.remote = remote;
//		this.fqdn = fqdn;
		this.msgid = msgid;
		this.insts = insts;
	}
	public void run() {
		/* �ܥǥ�̿��:
		 * 
		 * [1] �롼������
		 *   CLEARRULES       dstmemid
		 *   LOADRULESET      dstmemid rulesetid
		 * 
		 * [2] ���ȥ�����
		 *   NEWATOM          srcmemid NEW_atomid
		 *   ALTERATOMFUNCTOR srcmemid atomid func
		 *   ENQUEUEATOM      srcmemid atomid
		 *   REMOVEATOM       srcmemid atomid
		 * 
		 * [3] ��������
		 *   NEWMEM           srcmemid NEW_memid
		 *   REMOVEMEM        srcmemid
		 *   NEWROOT          parentmemid NEW_memid nodedesc
		 * 
		 * [4] ��󥯤���� 
		 *   NEWLINK          srcmemid atomid1 pos1 atomid2 pos2
		 *   RELINKATOMARGS   srcmemid atomid1 pos1 atomid2 pos2
		 *   UNIFYATOMARGS    srcmemid atomid1 pos1 atomid2 pos2
		 * 
		 * [5] �켫�Ȥ��ư�˴ؤ������ 
		 *   ACTIVATE         srcmemid
		 *   MOVECELLSFROM    dstmemid srcmemid
		 *   MOVETO           srcmemid dstmemid
		 * 
		 * [6] ��å��������
		 *   UNLOCK           srcmemid
		 *   ASYNCUNLOCK      srcmemid
		 *   RECURSIVEUNLOCK  srcmemid
		 */
		
		IDConverter idconv = new IDConverter();
		boolean result = true;
		Iterator it = insts.iterator();
		while (it.hasNext()) {
			String input = (String)it.next();
			try {
				String[] command = input.split(" ",6); // RemoteMembrane.send()�ΰ����θĿ��򻲾Ȥ���
				command[0] = command[0].toUpperCase();
		
				//todo �ʾ���� ������̿���񤯤ΤǤϤʤ��ơ�Instruction.java��̿���ֹ������Ƥ��롣
				//�������Ѵ�ɽ��Ҥ��롣
				//��: new InstructionList�򤹤롣
				
				//�ơ�BEGIN����END�ޤǽФƤ�����������NEW���Ĥ��ʤ���Τ�ưŪ�˲������ꥹ�Ȥˤ���Ƥ���
				//InterpretedRulset�Υ����ɤ��Ȥ���Τǡ��������롩

				String memid = command[1];
				AbstractMembrane mem = idconv.lookupMembrane(memid);
				if (mem == null) {
					String fqdn = memid.split(":",2)[0];
					AbstractLMNtalRuntime rt = LMNtalRuntimeManager.connectRuntime(fqdn);
					if (rt instanceof RemoteLMNtalRuntime) {
						RemoteLMNtalRuntime rrt = (RemoteLMNtalRuntime)rt;
						mem = rrt.createPseudoMembrane();
						idconv.registerNewMembrane(memid,mem);
					}
				}

				if (command[0].equals("END")) {
					//����
					break;
	// [1] �롼������
				} else if (command[0].equals("CLEARRULES")) {
					mem.clearRules();
				} else if (command[0].equals("LOADRULESET")) {
					String rulesetid = command[2];
					Ruleset rs = IDConverter.lookupRuleset(rulesetid);
					if (rs == null) {
						String fqdn = rulesetid.split(":",2)[0]; // TODO �ʸ�Ψ�����˰��긵�˼��ˤ����褦�ˤ���
						Object obj = remote.sendWaitObject(fqdn, "REQUIRERULESET " + rulesetid);
						if (obj instanceof byte[]) {
							rs = Ruleset.deserialize((byte[])obj);
						}
						if (rs == null) throw new RuntimeException("cannot lookup ruleset");
						IDConverter.registerRuleset(rulesetid, rs);
					}
					mem.loadRuleset(rs);
	// [2] ���ȥ�����
				} else if (command[0].equals("NEWATOM")) {
					Functor func = Functor.deserialize(command[3]);
					idconv.registerNewAtom(command[2], mem.newAtom(func));
				} else if (command[0].equals("ALTERATOMFUNCTOR")) {
					Atom atom = idconv.lookupAtom(mem, command[2]);
					mem.alterAtomFunctor(atom,Functor.deserialize(command[3]));
				} else if (command[0].equals("REMOTEATOM")) {
					mem.removeAtom(idconv.lookupAtom(mem, command[2]));
				} else if (command[0].equals("ENQUEUEATOM")) {
					mem.enqueueAtom(idconv.lookupAtom(mem, command[2]));
	// [3] ��������
				} else if (command[0].equals("NEWMEM")) {
					idconv.registerNewMembrane(command[2],mem.newMem());
				} else if (command[0].equals("REMOVEMEM")) {
					mem.removeMem(idconv.lookupMembrane(command[2]));
				} else if (command[0].equals("NEWROOT")) {
					idconv.registerNewMembrane(command[2], mem.newRoot(command[3]));
	// [4] ��󥯤����
				} else if (command[0].equals("NEWLINK")
						 || command[0].equals("RELINKATOMARGS")
						 || command[0].equals("UNIFYATOMARGS")) {
					Atom atom1 = idconv.lookupAtom(mem,command[2]);
					int pos1 = Integer.parseInt(command[3]);
					Atom atom2 = idconv.lookupAtom(mem,command[4]);
					int pos2 = Integer.parseInt(command[5]);
					if (command[0].equals("NEWLINK")) {
						mem.newLink(atom1,pos1,atom2,pos2);
					} else if (command[0].equals("RELINKATOMARGS")) {
						mem.relinkAtomArgs(atom1,pos1,atom2,pos2);
					} else if (command[0].equals("UNIFYATOMARGS")) {
						mem.unifyAtomArgs(atom1,pos1,atom2,pos2);
					}
	// [5] �켫�Ȥ��ư�˴ؤ������ 
				} else if (command[0].equals("ACTIVATE")) { // ENQUEUEMEM�ܥǥ�̿����б�
					mem.activate();
				} else if (command[0].equals("MOVECELLSFROM")) {
					mem.moveCellsFrom(idconv.lookupMembrane(command[2]));
				} else if (command[0].equals("MOVETO")) {
					mem.moveTo(idconv.lookupMembrane(command[2]));
	// [6] ��å��������
				} else if (command[0].equals("UNLOCK")) {
					mem.unlock();
				} else if (command[0].equals("ASYNCUNLOCK")) {
					mem.asyncUnlock();
				} else if (command[0].equals("RECURSIVEUNLOCK")) {
					mem.recursiveUnlock();
				} else { //̤�Τ�̿��
					System.out.println("unknown body method: " + command[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("CMD = " + input);
				result = false;
			}
		}
		remote.respond(msgid,result);
	}
}