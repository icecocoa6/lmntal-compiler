package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import runtime.Env;
import runtime.Membrane;

/*
 * ��å���������Ȥ򸫤ƽ������롣����Ū��LMNtalDaemon�Υ����åȤ��������ȡ�
 * ���줬��������롣�Ĥޤ�ʪ��Ū�ʷ׻���1������ʣ��¸�ߤ����롣
 * 
 * @author nakajima
 */
public class LMNtalDaemonMessageProcessor implements Runnable {
	static boolean DEBUG = true;

	// �����å�
	BufferedReader in;
	BufferedWriter out;
	Socket socket;


	/*
	 * ���󥹥ȥ饯����
	 * 
	 * @param socket �����줿�����åȡ�
	 * @param in ���ϡ�BufferedReader��
	 * @param out ���ϡ�BufferedWriter��
	 */
	public LMNtalDaemonMessageProcessor(
		Socket tmpSocket,
		BufferedReader inTmp,
		BufferedWriter outTmp) {
		in = inTmp;
		out = outTmp;
		socket = tmpSocket;
	}

	/*
	 * �ۥ���fqdn����ʬ���Ȥ�Ƚ�ꡣ
	 * 
	 * @param fqdn Fully Qualified Domain Name
	 * @return ��ʬ���Ȥ˳�꿶���Ƥ���IP���ɥ쥹����ۥ���̾������ơ�fqdn��string����ӡ�Ʊ�����ä���true������ʳ���false��
	 */
	public static boolean isMyself(String fqdn) {
		try {
			return InetAddress.getLocalHost().getHostAddress().equals(
				InetAddress.getByName(fqdn).getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (DEBUG) {
			System.out.println("LMNtalDaemonMessageProcessor.run()");
		}

		String input = "";

		outsideloop:while (true) {
			try {
				input = in.readLine();
			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}

			if (DEBUG) {
				System.out.println("in.readLine(): " + input);
			}

			if (input == null) {
				System.out.println("�ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}

			/* 
			 * input�β�ǽ����
			 * 
			 * ���ޥ�ɤ���Ϥ��ޤ��å�����
			 * msgid fqdn rgid ��å����� 
			 *   - fqdn ����ʬ��
			 *   - fqdn ��¾�Ͱ�
			 * BEGIN~END������
			 * 
			 */

			/* ���ޥ�ɤ���Ϥ��ޤ��å������������
			 * 
			 * �����ǽ��������̿�����������ʳ��Τϲ��������뤷�Ƥ�
			 * 
			 *  res msgid ��å�������ʸ
			 *  registerlocal
			 *  dumphash - �ǥХå���
			 * 
			 */
			Integer msgid;
			Integer rgid;
			String fqdn;
			boolean result;
			String[] parsedInput = new String[4];
			parsedInput = input.split(" ", 4);

			if (parsedInput[0].equalsIgnoreCase("res")) {
				//res msgid ���
				msgid = new Integer(parsedInput[1]);

				//�᤹��
				LMNtalNode returnNode = LMNtalDaemon.getNodeFromMsgId(msgid);

				if (DEBUG)
					System.out.println(
						"res: returnNode is: " + returnNode.toString());

				if (returnNode == null) {
					//�ᤷ�褬null
					try {
						out.write("res " + msgid.toString() + " fail\n");
						out.flush();
						continue;
					} catch (IOException e1) {
						e1.printStackTrace();
						continue;
					}
				} else {
					//System.out.println(returnNode.getOutputStream().toString());
					//System.out.println(input);
					try {
						returnNode.out.write(input + "\n");
						returnNode.out.flush();
						continue;
					} catch (IOException e1) {
						e1.printStackTrace();
						continue;
					}
				}
			} else if (parsedInput[0].equalsIgnoreCase("registerlocal")) {
				//registerlocal rgid
				//rgid�ȥ����åȤ���Ͽ
				rgid = new Integer(parsedInput[1]);
				result = LMNtalDaemon.registerLocal(rgid, socket);
				if (result == true) {
					//����
					try {
						out.write("ok\n");
						out.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					continue;
				} else {
					//����
					try {
						out.write("fail\n");
						out.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					continue;
				}
			} else if (parsedInput[0].equalsIgnoreCase("dumphash")) {
				//dumphash
				LMNtalDaemon.dumpHashMap();
				continue;
			} else {
				//msgid����ĤŤ�̿����Ȥߤʤ�
				//msgid "FQDN" rgid ��å�����
				msgid = new Integer(parsedInput[0]);
				fqdn = (parsedInput[1].split("\"", 3))[1];

				//��å���������Ͽ
				LMNtalNode returnNode =
					new LMNtalNode(socket.getInetAddress(), in, out);
				result = LMNtalDaemon.registerMessage(msgid, returnNode);

				if (result == true) {
					//��å�������Ͽ����

					//��ʬ���Ȱ����ɤ���Ƚ��
					try {
						if (isMyself(fqdn)) {
							if (DEBUG)
								System.out.println(
									"This message is for me: "
										+ InetAddress
											.getLocalHost()
											.getHostAddress());

							//��ʬ���Ȱ��ʤ顢��ʬ���Ȥǽ�������

							/* �����ǽ��������̿�����
							 * connect
							 * begin
							 * 
							 * lock
							 * blockinglock
							 * asynclock
							 * recursivelock
							 * 
							 * unlock
							 * blockingunlock
							 * asyncunlock
							 * recursiveunlock
							 * 
							 * terminate
							 * 
							 */

							//TODO ʬ���ȴط��ʤ�̿��Ϥɤ����褦��InterpretedRuleset.interpret()�˿��碌�뤫��

							String[] command = new String[3];
							command = parsedInput[3].split(" ", 3);
							String srcmem, dstmem, parentmem, atom1, atom2, pos1, pos2, ruleset, func;
							Membrane realMem;


							if (command[0].equalsIgnoreCase("connect")) {
								//TODO ���Ǥ˥�󥿥��ब����Ϥɤ����褦�������Ƥ��뤫���ǧ����̿���������롩

								//�����˥饤�󥿥�����롣
								//OK�֤��Τ��������줿��󥿥��ब���롣

								Integer slaveRuntimeRgid =
									new Integer(LMNtalDaemon.makeID());

								String newCmdLine =
									new String(
										"java daemon/SlaveLMNtalRuntimeLauncher "
											+ slaveRuntimeRgid.toString()
											+ " "
											+ msgid.toString());

								if (DEBUG)
									System.out.println(newCmdLine);

								try {
									Process remoteRuntime =
										Runtime.getRuntime().exec(newCmdLine);
								} catch (IOException e) {
									e.printStackTrace();
									out.write(
										"res " + msgid.toString() + " fail\n");
									out.flush();
								}
								continue;
							} else if (command[0].equalsIgnoreCase("begin")) {
								/*
								 * �����ǽ��������̿�ᡧ
								 * (�ǿ��Ǥ�RemoteMembrane���饹���ȡ�)
								 * 
								 * end
								 * 
								 * �롼������
								 * clearrules dstmem
								 * loadruleset dstmem globalRulesetID
								 * 
								 * ���ȥ�����
								 * newatom srcmem atomid
								 * alteratomfunctor srcmem atomid func.getName()
								 * removeatom srcmem atomid
								 * enqueueatom srcmem atomid
								 * 
								 * ��������
								 * newmem srcmem dstmem
								 * removemem srcmem parentmem
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
								 * requireruleset globalRulesetID
								 * newroot mem
								 */
								
								String[] commandInsideBegin = new String[5]; //RemoteMembrane.send()�ΰ����θĿ��򻲾Ȥ���

								beginEndLoop:while(true){
									input = in.readLine();
									commandInsideBegin = input.split(" ",5);


									if(commandInsideBegin[0].equalsIgnoreCase("end")){
										//����
										continue outsideloop;
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
										
										//realMem = IDConverter.getMem(srcmem).newMem();
										//if(IDConverter.registerMem(dstmem,realMem)){
										//	continue beginEndLoop;
										//} else {
										//     //todo: ���Ի��ˤʤˤ�����
										//     //System.out.println("failed to register new membrane");
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
										//AbstractTask task = (IDConverter.getMem(srcmem)).task.runtime.newTask(this);
										
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
										
										//Membrane dstmemobj = IDConverter.getMem(dstmem);
										//Membrane srcmemobj = IDConverter.getMem(srcmem);
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
									//} else if (commandInsideBegin[0].equalsIgnoreCase("requireruleset")){
										//TODO ����
									} else {
										//̤�Τ�̿��
										out.write("not implemented yet\n");
										out.flush();
										continue beginEndLoop;
									}
								}
							} else if (command[0].equalsIgnoreCase("lock")) {
								//TODO ����
								
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
								//out.write("res " + msgid.toString() + " fail\n");
								//out.flush();
								
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("blockinglock")) {
								//IDConverter.getMem(command[1]).blockingLock();

								//����å��幹��

								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("asynclock")) {

								//IDConverter.getMem(command[1]).asyncLock();

								//����å��幹��
																
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("unlock")) {
								
								//if(IDConverter.getMem(command[1]).unlock()){
								    //unlock����
									//continue;
								//} else {
								    //������
								    //System.out.println("UNLOCK failed");
								    //out.write("res " + msgid.toString() + " fail\n");
								    //out.flush();
								    //continue;
								//}
								
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("blockingunlock")) {
								//IDConverter.getMem(command[1]).blockingUnlock();
								
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("asyncunlock")) {

								//if(IDConverter.getMem(command[1]).asyncUnlock()){
									//unlock����
									//continue;
								//} else {
									//������
									//System.out.println("ASYNCUNLOCK failed");
									//out.write("res " + msgid.toString() + " fail\n");
									//out.flush();
									//continue;
								//}

								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("recursivelock")) {
								//IDConverter.getMem(command[1]).recursiveLock();

								//����å��幹��
																
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("recursiveunlock")) {
								
								//IDConverter.getMem(command[1]).recursiveUnlock();
								
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command[0].equalsIgnoreCase("terminate")) {
								//��terminate�����ѡ�(by n-kato)
								//
								//���٤����Ȥ�Env.theRuntime��terminate
								//�Ǥ�Ƥ٤ʤ�����ɤ����褦��
								//LocalLMNtalRuntime.terminate();
								Env.theRuntime.terminate(); //TODO ����Ǥ����Τ���

								//out.write("not implemented yet\n");
								//out.flush();
								continue;
							} else {
								//̤�ΤΥ��ޥ�� or ����ʳ��β���
								out.write(
									"res " + msgid.toString() + " fail\n");
								out.flush();
								continue;
							}
						} else {
							//¾�Ρ��ɰ��ʤ��å������򤤤��餺�ˤ��Τޤ�ž������

							//����Ρ��ɤϴ��Τ���
							LMNtalNode targetNode =
								LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
							if (targetNode == null) {
								//����Ρ��ɤ���³����Τ����Ƥξ��
								result = LMNtalDaemon.connect(fqdn);

								if (result) {
									targetNode =
										LMNtalDaemon.getLMNtalNodeFromFQDN(
											fqdn);

									if (targetNode == null) {
										//��³����
										out.write(
											"res "
												+ msgid.toString()
												+ " fail\n");
										out.flush();
										continue;
									} else {
										LMNtalDaemon.sendMessage(
											targetNode,
											input + "\n");
									}

									continue;
								} else {
									//����Ρ��ɤؤ���³����
									out.write(
										"res " + msgid.toString() + " fail\n");
									out.flush();
									continue;
								}
							} else {
								//����Ρ��ɤ����Τξ��
								if (LMNtalDaemon
									.sendMessage(targetNode, input + "\n")) {
									//OK���֤��Τϡ�ž���褬���ΤǤ����Ǥ�OK���֤��ʤ�
									continue;
								} else {
									//ž������
									out.write(
										"res " + msgid.toString() + " fail\n");
									out.flush();
									continue;
								}
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						//continue;
						break;
					} catch (NullPointerException nurupo) {
						System.out.println("�ʡ����ϡ��ˡ㡡�̤��");
						nurupo.printStackTrace();
						//continue;
						break;
					}
				} else {
					//����msgTable����Ͽ����Ƥ���� or �̿����Ի�
					try {
						out.write("res " + msgid.toString() + " fail\n");
						out.flush();
						//continue;
						break;
					} catch (IOException e1) {
						//todo: �̿����Ի��ϲ����褦��
						
						e1.printStackTrace();
						break;
					}
				}
			}
		}
	}
}