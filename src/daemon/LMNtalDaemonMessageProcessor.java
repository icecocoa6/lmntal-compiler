package daemon;

import java.io.IOException;
import java.net.Socket;

import util.StreamDumper;

//import runtime.Env;
//import runtime.Membrane;

/**
 * �ǡ�����������륪�֥������ȡ�
 * ���ͥ�����󤴤Ȥ��������졢��å������μ�����Ԥ���
 * <p>
 * <strike>��å���������Ȥ򸫤ƽ������롣</strike>
 * ����Ū��LMNtalDaemon�Υ����åȤ��������ȡ����줬��������롣
 * �Ĥޤ�ʪ��Ū�ʷ׻���1������ʣ��¸�ߤ����롣
 * 
 * @author nakajima, n-kato
 */
public class LMNtalDaemonMessageProcessor extends LMNtalNode implements Runnable {
	boolean DEBUG = true; //LMNtalDaemon.main()����Ƥ֤Ȥ���Env.debug�ϻȤ��ʤ�...
	
	public LMNtalDaemonMessageProcessor(Socket socket) {
		super(socket);
	}

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
				System.out.println("LMNtalDaemonMessageProcessor.run(): ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}
			if (input == null) {
				System.out.println("LMNtalDaemonMessageProcessor.run(): in.readLine(): �ʡ����ϡ��ˡ㡡input���̤�");
				break;
			}
			if (DEBUG) System.out.println("LMNtalDaemonMessageProcessor.run(): in.readLine(): " + input);

			/* ��å�����:
			 *   RES msgid ����
			 *   REGISTERLOCAL (MASTER|SLAVE) msgid rgid
			 *   DUMPHASH
			 *   CMD msgid fqdn rgid ���ޥ��
			 *     - fqdn ����ʬ�� 
			 *     - fqdn ��¾�Ͱ�
			 * ����:
			 *   OK | FAIL | UNCHANGED | RAW bytes \n data
			 * ���ޥ��:
			 *   BEGIN \n �ܥǥ�̿��... END
			 *   CONNECT dst_nodedesc src_nodedesc
			 *   TERMINATE
			 *   SETENV variable value  //TODO SETENV
			 *   ...
			 */

			String[] parsedInput = input.split(" ", 5);

			if (parsedInput[0].equalsIgnoreCase("RES")) {
				// RES msgid ����
				String msgid = parsedInput[1];
				//ž���������Ƥ��������
				String content = input + "\n";
				byte[] rawData = null;
				if (parsedInput[2].equalsIgnoreCase("RAW")) {
					try {
						//�Х��ȿ�����䡢�����β��Ե����readBytes��ǽ���
						rawData = readBytes();
					}
					catch (Exception e) {
						content = "RES " + msgid + " FAIL\n";
					}
				}
				
				//�᤹��
				LMNtalNode returnNode = LMNtalDaemon.unregisterMessage(msgid);
				
				if (DEBUG) System.out.println("res: returnNode is: " + returnNode);

				if (returnNode == null) {
					//�ᤷ�褬null
					// todo (n-kato) ���Ԥ�̵�뤹�롣�ޤ��ϡ�msgid�Ǥʤ��̤ο�����msgid���꼺�Ԥ�out�����Τ���
					respondAsFail(msgid);
					continue;
				} else {
					returnNode.sendMessage(content, rawData);
					continue;
				}
			} else if (parsedInput[0].equalsIgnoreCase("REGISTERLOCAL")) {
				// REGISTERLOCAL (MASTER|SLAVE) msgid rgid
				// rgid��LMNtalNode����Ͽ
				String type  = parsedInput[1];
				String msgid = parsedInput[2];
				String rgid  = parsedInput[3];
				// todo �����緯�ˤ����Ϥ��Ǥ���Ͽ����Ƥ�����FAIL���֤��Τ��������ߤ����Ǥ��Τ�ľ���ʤ��Ǥ���������
				//(nakajima 2004-10-13) λ�򤷤ޤ�����
				boolean result = LMNtalDaemon.registerRuntimeGroup(rgid, this);
				respond(msgid, result);
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("UNREGISTERLOCAL")){
				//UNREGISTERLOCAL rgid
				
				//rgid ����
				String rgid  = parsedInput[1];
				if(LMNtalDaemon.unregisterRuntimeGroup(rgid)){
					//	��ʬ���Ȥˤ��롢�ޥ�����󥿥���(LMNtalRuntimeMessageProcessor)�Ȥ��̿�ϩ���ڤ�
					close();
				}
				return;
			} else if (parsedInput[0].equalsIgnoreCase("DUMPHASH")) {
				// DUMPHASH
				LMNtalDaemon.dumpHashMap();
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("CMD")) {
				// CMD msgid fqdn rgid ���ޥ��
			
				String msgid = parsedInput[1];
				String fqdn = parsedInput[2].replaceAll("\"","");
				String rgid = parsedInput[3];

				//��å���������Ͽ
				LMNtalNode returnNode = this;
				if (LMNtalDaemon.registerMessage(msgid, returnNode)) {
					//��å�������Ͽ����
					try {
						String[] command = parsedInput[4].split(" ", 3);

						//ž���������Ƥ��������
						String content = input + "\n";
						if (command[0].equalsIgnoreCase("BEGIN")) {
							StringBuffer buf = new StringBuffer(content);
							// end�����ޤ��Ѥ߹���
							while(true){ // BEGIN�������Ƥ��θ夬��ʤ���break���ʤ�������ϻ��ͤǤ��Τ�
								String inputline = readLine();
								if (DEBUG) System.out.println("LMNtalDaemonMessageProcessor.run(): after BEGIN:  " + inputline);
								if (inputline == null) break;
								if (inputline.equalsIgnoreCase("END")) break;
								buf.append(inputline);
								buf.append("\n");
							}
							buf.append("END\n");
							content = buf.toString();
						} 
						
						LMNtalNode targetNode = null;
						
						if (command[0].equalsIgnoreCase("CONNECT")) {
							//��ʬ���Ȱ����ɤ���Ƚ��
							if (isMyself(fqdn)) { //��ʬ���Ȱ�
								if (!LMNtalDaemon.isRuntimeGroupRegistered(rgid)) {	
									/* ��Ͽ����Ƥ��ʤ��� */
									
									//�����˥�󥿥�����롣
									String classpath = System.getProperty("java.class.path");
									String newCmdLine =
										new String(
												"java -classpath"
												+ " "
												+ classpath
												+ " "
												+"daemon.SlaveLMNtalRuntimeLauncher"
												+ " "
												+ msgid
												+ " "
												+ rgid);

									if (DEBUG) System.out.println(newCmdLine);

									Process slave = Runtime.getRuntime().exec(newCmdLine);
									
									Thread dumpErr = new Thread(new StreamDumper("slave runtime.error", slave.getErrorStream()));
									Thread dumpOut = new Thread(new StreamDumper("slave runtime.stdout", slave.getInputStream()));
									dumpErr.start();
									dumpOut.start();
									
									//OK�֤��Τ��������줿��󥿥��ब���롣
									continue;
								} else {
									// ������Ͽ�Ѥߤλ�
									targetNode = LMNtalDaemon.getRuntimeGroupNode(rgid);
								}
							} else { //¾�Ρ��ɰ��ʤ�connect�򤽤Τޤ�ž������
								LMNtalDaemon.makeRemoteConnection(fqdn); // TODO�ʸ�Ψ�����˥֥�å����ʤ��褦�ˤ���
								targetNode = LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
							}
						} else {
							// connect�ʳ��ΤȤ�
							// ��ʬ���Ȱ����ɤ���Ƚ��
							if (isMyself(fqdn)) { //��ʬ���Ȱ�
								targetNode = LMNtalDaemon.getRuntimeGroupNode(rgid);
							}
							else {
								targetNode = LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
							}
						}
						if (targetNode != null && targetNode.sendMessage(content)) {
							if(DEBUG)System.out.println("LMNtalDaemonMessageProcessor.run(): target node is " + targetNode.toString() + " and now sending: " + content);
							continue;
						}
						// ž�����Ԥ����鲼��ȴ����
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NullPointerException nullpo) {
						System.out.println("�ʡ����ϡ��ˡ㡡�̤��");
						nullpo.printStackTrace();
						break;
					}
					LMNtalDaemon.unregisterMessage(msgid);
				}
				//����msgTable����Ͽ����Ƥ���� or �̿�����
				respondAsFail(msgid);
			}
		}
	}
}