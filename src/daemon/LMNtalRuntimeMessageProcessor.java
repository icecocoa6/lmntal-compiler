package daemon;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import runtime.AbstractLMNtalRuntime;
import runtime.AbstractMembrane;
import runtime.Atom;
import runtime.Functor;
import runtime.LMNtalRuntimeManager;
import runtime.Membrane;
import runtime.RemoteLMNtalRuntime;
import runtime.RemoteMembrane;
import runtime.RemoteTask;
import runtime.Ruleset;
import util.HybridOutputStream;

/**
 * ��󥿥��ब�������륪�֥������ȡ�
 * �ǡ����ȤΥ��ͥ��������Ф����������졢��å������μ�����Ԥ���
 * 
 * todo LMNtalDaemonMessageProcessor�ȶ��̤ν�����LMNtalNode�˰ܴɤ��롣
 * @author nakajima, n-kato
 */
public class LMNtalRuntimeMessageProcessor extends LMNtalNode implements Runnable {
	boolean DEBUG = true;
	
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
		// REGSITERLOCAL (MASTER|SLAVE) msgid rgid
		String msgid = LMNtalDaemon.makeID();
		String command = "registerlocal " + type + " " + msgid + " " + rgid + "\n";
		if (!sendMessage(command)) return false;
		return waitForResult(msgid);
	}
	
	/** ������ǡ������Ф���UN REGISTERLOCAL ��ȯ�Ԥ��롣�ʸ�����UNREGISTERLOCAL�μ����Ǥ��ֻ��Ϥ��ʤ���*/
	public void sendWaitUnregisterLocal() {
		// UNREGSITERLOCAL rgid
		String command = "UNREGISTERLOCAL " + rgid + "\n";
		sendMessage(command);
	}
	
	////////////////////////////////
	// ������
	
	/** ����Υۥ��Ȥ˥�å����������������������Ԥġ�
	 * @return ������OK���ɤ��� */
	public boolean sendWait(String fqdn, String command){
		return sendWaitText(fqdn, command).equalsIgnoreCase("OK");
	}
	/** ����Υۥ��Ȥ˥�å����������������������Ԥġ�
	 * @return �����˴ޤޤ��ʸ���� */
	public String sendWaitText(String fqdn, String command){
		//if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.sendWaitText()");
		Object obj = sendWaitObject(fqdn, command);
		if (obj instanceof String) {
			return (String)obj;
		}
		return "FAIL";
	}
	/** ����Υۥ��Ȥ˥�å����������������������Ԥġ�
	 * @return �����˴ޤޤ�륪�֥������� */
	public Object sendWaitObject(String fqdn, String command){
		try {
			HybridOutputStream out = getOutputStream();
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
		//if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.waitForResponseObject(" + msgid + ")");
		while (!messagePool.containsKey(msgid)) { 
			try {
				//if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.waitForResponseObject(): waiting...");
				wait(); 
			} catch (InterruptedException e) {	}
		}
		//if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.waitForResponseObject(): loop quit");
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
		if (DEBUG) System.out.println("LMNtalRuntimeMessageProcessor.run()");
		String input;
		while (true) {
			//�����åȤ��Ĥ���Ȥ��������ʳ����Ĥ���Ƥ������readLine()�ޤǹԤä��ԤäƤ�����Τۤ����ͤ����롣
/*			if(isSocketClosed()){
				return;
			}*/

			try {
				input = readLine();
			} catch (IOException e) {
				//todo disconnectFromDaemon()������ˡ�ɬ�������㳰��ȯ������Τ��ɤ�
				//�� 2004-08-24 nakajima

				if(true){//TODO ������Ƚ�ꤹ��
					System.out.println("program finished successfully");
					break;
				} else {
					//System.out.println("LMNtalRuntimeMessageProcessor.run(): ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
					e.printStackTrace(); 
				}
			
				break;
			}
			if (input == null) {
				System.out.println("LMNtalRuntimeMessageProcessor.run(): �ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}
			if (DEBUG) System.out.println("LMNtalRuntimeMessageProcessor.run(): in.readLine(): " + input);

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
				Object res;
				if (content.equalsIgnoreCase("RAW")) {
					try {
						//�Х��ȿ�����䡢�����β��Ե����readBytes��ǽ���
						res = readBytes();
					} catch (Exception e) {
						res = "FAIL";
					}
				}
				else res = content;
				synchronized(this) {	
					messagePool.put(msgid, res);
					//if (DEBUG) System.out.println(res.toString());
					//if (DEBUG) System.out.println(messagePool.toString());
					
//					Object suspended = blockingObjects.remove(msgid);
//					if (suspended == null) {
//						System.out.println(
//							"ERROR: no objects waiting for message id = " + msgid);
//						continue;
//					}
					
					//if (DEBUG) System.out.println("notifyALL");
					notifyAll();
				}
				continue;
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
				 *  DISCONNECTRUNTIME
				 *   REQUIRERULESET globalrulesetid  ->             RAW bytes \n data | FAIL
				 *   LOCK           globalmemid prio -> UNCHANGED | RAW bytes \n data | FAIL
				 *   BLOCKINGLOCK   globalmemid prio -> UNCHANGED | RAW bytes \n data | FAIL
				 *   ASYNCLOCK      globalmemid      -> OK | FAIL
				 *   RECURSIVELOCK  globalmemid      -> OK | FAIL
				 */

				String[] command = parsedInput[4].split(" ", 3);
				
				if (command[0].equalsIgnoreCase("TERMINATE")) {  
					// TERMINATE
					
					Thread t1 = new Thread(new TerminateProcessor(msgid, this));
					t1.start();
										
					continue;
				} else if (command[0].equalsIgnoreCase("DISCONNECTRUNTIME")) { 
					//DISCONNECTRUNTIME
					
					Thread t1 = new Thread(new DisconnectProcessor());
					t1.start();
					
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
						new Thread(ibp,"ibp").start();
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
		AbstractMembrane obj = IDConverter.lookupGlobalMembrane(command[1]); //TODO globalid�����������˴ְ�ä�ID��������Ƥ��롩  or ��Ͽ����Ƥ��ʤ����ʤ��ä������Ĵ�٤��
		if (!(obj instanceof Membrane)) {
			respondAsFail(msgid);
			if(true)System.out.println("LMNtalRuntimeMessageProcessor.onCmd(" + command[1] + " is not found!)"); //TODO Env.debug
			return;
		}
		Membrane mem = (Membrane)obj;
		if(true)System.out.println("LMNtalRuntimeMessageProcessor.onCmd(" + command[1] + " is found.)"); //TODO Env.debug
		
		if (command[0].equalsIgnoreCase("LOCK")
		 || command[0].equalsIgnoreCase("BLOCKINGLOCK")
		 || command[0].equalsIgnoreCase("ASYNCLOCK")) {
			Thread t1 = new Thread(new LockProcessor(command[0], this, mem, msgid));
			t1.start();
			return;
		} else if (command[0].equalsIgnoreCase("RECURSIVELOCK")) { 
			// RECURSIVELOCK globalmemid
			// ��å����������������������λ�¹���Ƶ�Ū�˥�å��ʥ���å���Ϲ������ʤ���
			Thread t1 = new Thread(new RecursiveLockProcessor(command[0],this,mem,msgid));
			t1.start();
			return;
		}
	}
}

class InstructionBlockProcessor implements Runnable {
	LMNtalRuntimeMessageProcessor remote;
//	String fqdn;
	String msgid;
	LinkedList insts;
	
	static boolean DEBUG = true;  //todo Env.debug��Ȥ�
	
	InstructionBlockProcessor(LMNtalRuntimeMessageProcessor remote, 
//		String fqdn, 
		String msgid, LinkedList insts) {
		this.remote = remote;
//		this.fqdn = fqdn;
		this.msgid = msgid;
		this.insts = insts;
	}
	//
	
	/** ��⡼��ž��ɽ��remote�����ѡ�: RemoteTask -> RemoteTask */
	HashMap remoteTable = new HashMap();
	
	////////////////////////////////////////////////////////////////	

	/** �����Х���ID (String) -> AbstractMembrane */
	HashMap newMemTable = new HashMap();
//	/** �����륢�ȥ�ID�ޤ���NEW_ (String) -> Atom */
//	HashMap newAtomTable = new HashMap();

	/** ���ꤵ�줿���ɽ����Ͽ���� */
	public void registerNewMembrane(String globalMemID, AbstractMembrane mem) {
		newMemTable.put(globalMemID, mem);
		
	}
	/** �����Х���ID�ޤ���NEW_���б��������õ��
	 * @return Membrane�ʸ��Ĥ���ʤ��ä�����null��*/
	public AbstractMembrane lookupMembrane(String memid) {
		if(DEBUG)System.out.println("LMNtalRuntimeMessageProcessor.lookupMembrane(" + memid + ")");
		
		Object obj = newMemTable.get(memid);
		if (obj instanceof AbstractMembrane) return (AbstractMembrane)obj;
		return IDConverter.lookupGlobalMembrane(memid);
	}

//	/** ���ꤵ�줿���ȥ��ɽ����Ͽ���� */
//	public void registerNewAtom(String atomID, Atom atom) {
//		newAtomTable.put(atomID, atom);
//	}
//	/** ���ȥ�ID���б����륢�ȥ��õ��
//	 * @param mem ��°��
//	 * @return Atom�ʸ��Ĥ���ʤ��ä�����null��*/
//	public Atom lookupAtom(AbstractMembrane mem, String atomid) {
//		Object obj = newAtomTable.get(atomid);
//		if (obj instanceof Atom) return (Atom)obj;
//		if (mem instanceof Membrane) {
//			return (Atom)((Membrane)mem).lookupAtom(atomid);
//		}
//		return null;
//	}
	
	//
	public void run() {
		/* �ܥǥ�̿��:
		 * 
		 * [1] �롼������
		 *   CLEARRULES       dstmemid
		 *   LOADRULESET      dstmemid rulesetid
		 * 
		 * [2] ���ȥ�����
		 *   NEWATOM          srcmemid NEW_atomid func
		 *   NEWFREELINK      srcmemid NEW_atomid        // ���ߤ� NEWATOM - - $inside ��Ʊ��
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
		 *  QUIETUNLOCK srcmemid
		 */
		
		String result = "";	// ���������줪���inside_proxy���Ф���ID��������Ѥ߹���
		Iterator it = insts.iterator();
		while (it.hasNext()) {
			String input = (String)it.next();
			try {
				String[] command = input.split(" ",6); // RemoteMembrane.send()�ΰ����θĿ��򻲾Ȥ���
				command[0] = command[0].toUpperCase();
				
				if(DEBUG){
					System.out.println("InstructionBlockProcessor.run(): command is : ");
					for(int i = 0; i < command.length; i ++){
						System.out.println("command[" + i + "] is:" + command[i]);
					}
				}
				
//				//todo �ʾ���� ������̿���񤯤ΤǤϤʤ��ơ�Instruction.java��̿���ֹ������Ƥ��롣
//				//�������Ѵ�ɽ��Ҥ��롣
//				//��: new InstructionList�򤹤롣
//				
//				//�ơ�BEGIN����END�ޤǽФƤ�����������NEW���Ĥ��ʤ���Τ�ưŪ�˲������ꥹ�Ȥˤ���Ƥ���
//				//InterpretedRulset�Υ����ɤ��Ȥ���Τǡ��������롩

				String memid = command[1];
				AbstractMembrane m = lookupMembrane(memid);

				if (m == null) {
					// ̤�Τ���ξ�硢������κ������ߤ�
					String fqdn = memid.split(":",2)[0];
					AbstractLMNtalRuntime rt = LMNtalRuntimeManager.connectRuntime(fqdn);
					if (rt instanceof RemoteLMNtalRuntime) {
						RemoteLMNtalRuntime rrt = (RemoteLMNtalRuntime)rt;
						m = rrt.createPseudoMembrane(memid);
						registerNewMembrane(memid,m);
						IDConverter.registerGlobalMembrane(memid,m);
					}
					if (m == null) {
						throw new RuntimeException("cannot lookup membrane: " + memid);
					}
				}
				
	// === ��⡼������Ф���ܥǥ�̿��ξ�� ===
	
				if (m instanceof RemoteMembrane) {
					// �����Ĥ�������������������Υۥ��Ȥ�ž�����������
					
					// [����1] ���Υۥ��Ȥ��Ф���NEWROOT̿��ʥ�⡼����m�Ͽ������롼����ο����
					// (1) ����m���Ф���newRoot�᥽�åɤ�ȯ��
					// (2) ��⡼�ȥۥ���H���Ф��ơ�NEWROOT m H ̿�������
					// (3) �ۥ���H�Ǥϡ�m�򵼻���Ȥ��ƺ�������newRoot�᥽�åɤ�ȯ��
					if (command[0].equals("NEWROOT") && m.remote == null) {
						String nodedesc = command[3];
						String fqdn = LMNtalRuntimeManager.nodedescToFQDN(nodedesc);
						if (LMNtalNode.isMyself(fqdn)) {
							String tmpID = command[2];
							AbstractMembrane newmem = m.newRoot(nodedesc);
							registerNewMembrane(tmpID,newmem);
							IDConverter.registerGlobalMembrane(newmem.getGlobalMemID(),newmem);
							result += tmpID + "=" + newmem.getGlobalMemID() + ";";
							continue;
						}
					}
					
					// [���㽪���]
					
					// ž���� m.remote ����ꤹ��
					RemoteTask tmpremote = (RemoteTask)remoteTable.get(m.getTask());
					if (tmpremote == null) { // ��⡼�Ȥ�̤����ξ��
						// - ���ĤǺǽ�Υ�⡼�ȥ������˥�⡼�Ȥ�¸�ߤ����硢���Υ�⡼�Ȥ�Ѿ�����
						AbstractMembrane tmpmem = m.getTask().getRoot().getParent();
						while (tmpmem instanceof Membrane) {
							tmpmem = tmpmem.getTask().getRoot().getParent();
						}
						if (tmpmem != null) {
							tmpremote = (RemoteTask)remoteTable.get(tmpmem.getTask());
						}
						// - ¸�ߤ��ʤ���硢m��������륿�������⡼�Ȥ����ꤹ��
						if (tmpremote == null) {
							tmpremote = (RemoteTask)m.getTask();
							tmpremote.init();
						}
						remoteTable.put(m.getTask(), tmpremote);
					}
					m.remote = tmpremote;	
									
					// ž������
					m.remote.send(input);
					continue;
				}
								
	// === ����������Ф���ܥǥ�̿��ξ�� ===

				Membrane mem = (Membrane)m;

				if (command[0].equals("END")) { // �ºݤˤϵ�����ʤ�
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
						if (rs == null) {
							throw new RuntimeException("cannot lookup ruleset: " + rulesetid);
						}
						IDConverter.registerRuleset(rulesetid, rs);
					}
					mem.loadRuleset(rs);
	// [2] ���ȥ�����
				} else if (command[0].equals("NEWATOM")) {
					String tmpID = command[2];
					Functor func = Functor.deserialize(command[3]);
					Atom newatom = mem.newAtom(func);
					mem.registerAtom(tmpID,newatom);
					//idconv.registerNewAtom(tmpID,newatom);
					if (func.equals(Functor.INSIDE_PROXY)) {
						result += tmpID + "=" + mem.getAtomID(newatom) + ";";
					}
				} else if (command[0].equals("NEWFREELINK")) {
					String tmpID = command[2];
					Functor func = Functor.INSIDE_PROXY;
					Atom newatom = mem.newAtom(func);
					mem.registerAtom(tmpID,newatom);
					result += tmpID + "=" + mem.getAtomID(newatom) + ";";
				} else if (command[0].equals("ALTERATOMFUNCTOR")) {
					Atom atom = mem.lookupAtom(command[2]);
					mem.alterAtomFunctor(atom,Functor.deserialize(command[3]));
				} else if (command[0].equals("REMOTEATOM")) {
					mem.removeAtom(mem.lookupAtom(command[2]));
				} else if (command[0].equals("ENQUEUEATOM")) {
					mem.enqueueAtom(mem.lookupAtom(command[2]));
	// [3] ��������
				} else if (command[0].equals("NEWMEM")) {
					String tmpID = command[2];
					AbstractMembrane newmem = mem.newMem();
					registerNewMembrane(tmpID,newmem);
					IDConverter.registerGlobalMembrane(newmem.getGlobalMemID(), newmem);
					result += tmpID + "=" + newmem.getGlobalMemID() + ";";
				} else if (command[0].equals("REMOVEMEM")) {
					mem.removeMem(lookupMembrane(command[2]));
				} else if (command[0].equals("NEWROOT")) { 
					String tmpID = command[2];
					AbstractMembrane newmem = mem.newRoot(command[3]); // ��������⡼�Ȥ�����������
					if (newmem instanceof RemoteMembrane) {
						remoteTable.put(newmem.getTask(), newmem.getTask()); // ��������⡼�Ȥ���Ͽ����
					}
					registerNewMembrane(tmpID,newmem);
					result += tmpID + "=" + newmem.getGlobalMemID() + ";";
	// [4] ��󥯤����
				} else if (command[0].equals("NEWLINK")
						 || command[0].equals("RELINKATOMARGS")
						 || command[0].equals("UNIFYATOMARGS")) {
					Atom atom1 = mem.lookupAtom(command[2]);
					int pos1 = Integer.parseInt(command[3]);
					Atom atom2 = mem.lookupAtom(command[4]);
					int pos2 = Integer.parseInt(command[5]);
					if (command[0].equals("NEWLINK")) {
						mem.newLink(atom1,pos1,atom2,pos2);
					} else if (command[0].equals("RELINKATOMARGS")) {
						mem.relinkAtomArgs(atom1,pos1,atom2,pos2);
					} else if (command[0].equals("UNIFYATOMARGS")) {
						mem.unifyAtomArgs(atom1,pos1,atom2,pos2);
					}
	// [5] �켫�Ȥ��ư�˴ؤ������ 
				} else if (command[0].equals("ACTIVATE")) { // ENQUEUEMEM�ܥǥ�̿����б���todo ̾���ѹ�����
					mem.activate();
				} else if (command[0].equals("MOVECELLSFROM")) {
					// todo �ڼ�����command[2]����⡼����ξ�硢���Ƥ�������ʤ���Фʤ�ʤ���
					mem.moveCellsFrom(lookupMembrane(command[2]));
				} else if (command[0].equals("MOVETO")) {
					mem.moveTo(lookupMembrane(command[2]));
					// todo �ڸ��ڡ�flush������ط������������ɤ�����ǧ����
	// [6] ��å��������
				} else if (command[0].equals("UNLOCK")) {
					mem.unlock();
				} else if (command[0].equals("ASYNCUNLOCK")) {
					mem.asyncUnlock();
				} else if (command[0].equals("RECURSIVEUNLOCK")) {
					mem.recursiveUnlock();
				} else if (command[0].equals("QUIETUNLOCK")) {
					mem.quietUnlock();
				} else { //̤�Τ�̿��
					System.out.println("InstructionBlockProcessor.run(): unknown body method: "
						+ command[0] + "\n\tin CMD = " + input);
					result = "FAIL;" + result;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("CMD = " + input);
				result = "FAIL;" + result;
			}
		}
		flush();
		// ����
		if (result.length() > 0) result = result.substring(0, result.length() - 1);
		remote.respond(msgid,result);
	}
	/** ��⡼�Ȥ�̿��֥�å���ž���������������ޤǥ֥�å����� */
	void flush() {
		Iterator it = remoteTable.keySet().iterator();
		while (it.hasNext()) {
			RemoteTask innerremote = (RemoteTask)remoteTable.get(it.next());
			innerremote.flush();
		}
		remoteTable.clear();

		// m.remote��null�˽��������ʤ��Τ����꤫�⤷��ʤ�
	}
}

/**
 * LOCK, ASYNCLOCK, BLOCKINGLOCK����ο�
  * @author nakajima
  */
class LockProcessor implements Runnable{
	String command;
	LMNtalNode node;
	Membrane mem;
	String msgid;
	
	LockProcessor(String command, LMNtalNode node, Membrane mem, String msgid){
		this.command = command;
		this.node = node;
		this.mem = mem;
		this.msgid = msgid;
	}	
	
	public void run() {
		// LOCK globalmemid
		// �����������å�
		boolean result = false;
		if (command.equalsIgnoreCase("LOCK"))         result = mem.lock();
		if (command.equalsIgnoreCase("BLOCKINGLOCK")) result = mem.blockingLock();
		if (command.equalsIgnoreCase("ASYNCLOCK"))    result = mem.asyncLock();
		if (result) { // ��å���������
			if (true) { // ����å�������������å�
				byte[] data = mem.cache();
				node.respondRawData(msgid,data);
			}	else {
				node.respond(msgid, "UNCHANGED");
			}
		} else {
			node.respondAsFail(msgid);
		}
	}
}

/**
 * RECURSIVELOCK����ο�
 * @author nakajima
 *
 */
class RecursiveLockProcessor implements Runnable{
	String command;
	LMNtalNode node;
	Membrane mem;
	String msgid;	

	RecursiveLockProcessor(String command, LMNtalNode node, Membrane mem, String msgid){
		this.command = command;
		this.node = node;
		this.mem = mem;
		this.msgid = msgid;
	}
	
	public void run(){
		if (mem.recursiveLock()) {
			node.respondAsOK(msgid);
		} else {
			node.respondAsFail(msgid);			
		}
	}
}

/**
 * TERMINATE����ο�
 * @author nakajima
 *
 */
class TerminateProcessor implements Runnable {
	String msgid;
	LMNtalNode node;
	
	TerminateProcessor(String msgid, LMNtalNode node){
		this.msgid = msgid;
		this.node = node;
	}

	public void run(){
		if(LMNtalRuntimeManager.terminateAll()){
			node.respondAsOK(msgid);
		} else {
			//node.respondAsFail(msgid);
		}
	}
}

/**
 * DISCONNECTRUNTIME����ο�
 * @author nakajima
 *
 */
class DisconnectProcessor implements Runnable {
	public void run(){
		LMNtalRuntimeManager.disconnectAll();
	}
}