package daemon;

import java.io.IOException;
import java.net.Socket;

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
 *̿��֥�å��ˤ������ʤ���å�������ʸ�ν�����Ԥ��� 
 * 
 * @author nakajima, n-kato
 */
public class LMNtalDaemonMessageProcessor extends LMNtalNode implements Runnable {
	static boolean DEBUG = true;

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

		String input = "";

		while (true) {
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
			 * input�β�ǽ����
			 * 
			 * ���ޥ�ɤ���Ϥ��ޤ��å�����
			 * cmd msgid fqdn rgid ��å�����
			 *   - fqdn ����ʬ�� 
			 *   - fqdn ��¾�Ͱ�
			 * �Ѣ�BEGIN~END������ʺ��ϤޤȤ�ƽ���������
			 *  
			 */

			/*
			 * ���ޥ�ɤ���Ϥ��ޤ��å������������
			 * 
			 * �����ǽ��������̿�����������ʳ��Τϲ��������뤷�Ƥ�
			 * 
			 * RES                        msgid ��å�������ʸ
			 * REGISTERLOCAL MASTER/SLAVE msgid rgid
			 * dumphash
			 * cmd 
			 */
			String[] parsedInput = input.split(" ", 4);

			if (parsedInput[0].equalsIgnoreCase("res")) {
				//res msgid ��å�������ʸ

				String msgid = parsedInput[1];

				//�᤹��
				LMNtalNode returnNode = LMNtalDaemon.getNodeFromMsgId(msgid);

				if (DEBUG)
					System.out.println(
						"res: returnNode is: " + returnNode.toString());

				if (returnNode == null) {
					//�ᤷ�褬null
					// TODO (n-kato) ���Ԥ�̵�뤹�롣�ޤ��ϡ�msgid�Ǥʤ��̤ο�����msgid���꼺�Ԥ�out�����Τ���
					respondAsFail(msgid);
					continue;
				} else {
					returnNode.sendMessage(input + "\n");
					continue;
				}
			} else if (parsedInput[0].equalsIgnoreCase("registerlocal")) {
				// REGISTERLOCAL MASTER/SLAVE msgid rgid

				//rgid��LMNtalNode����Ͽ
				String type  = parsedInput[1];
				String msgid = parsedInput[2];
				String rgid  = parsedInput[3];
				boolean result = LMNtalDaemon.registerRuntimeGroup(rgid, this);
				respond(msgid, result);
				continue;
			} else if (parsedInput[0].equalsIgnoreCase("dumphash")) {
				//dumphash

				LMNtalDaemon.dumpHashMap();
				continue;
				
				/* ���ޥ�ɤ���Ϥ��ޤ�ʸ����ν����������ޤ� */
			} else if (parsedInput[0].equalsIgnoreCase("cmd")) {
				// todo �Ѹξ������ĥ���Τ��ᡢmsgid �����˶��̥��ޥ��̾���㤨��cmd�ˤ�񤤤������褤
				// ���� nakajima 2004-08-16
				// todo Ⱦ�Ѷ����ʬ�䤹��Τ� " ��񤯰�̣���ʤ��Τ򲿤Ȥ����� 

				/*cmd  msgid "fqdn" rgid ��å����� �ν��� 
				 * 
				 *  ���������å������ϡ�
				 * connect
				 * begin
				 * */
				String msgid = parsedInput[1];
				String fqdn = (parsedInput[2].split("\"", 3))[1];
				String rgid = parsedInput[3];

				//��å���������Ͽ
				LMNtalNode returnNode = this;
				boolean result = LMNtalDaemon.registerMessage(msgid, returnNode);

				if (result == true) {
					//��å�������Ͽ����
					try {
						String[] command = parsedInput[4].split(" ", 3);

						//ž���������Ƥ��������
						String content = input + "\n";
						if (command[0].equalsIgnoreCase("begin")) {
							//BEGIN
							
							StringBuffer buf = new StringBuffer(content);
							// end�����ޤ��Ѥ߹���
							//todo "end"������Ƕ����и������ǽ�������뤫��
							while(true){
								String inputline = in.readLine();
								if (inputline == null) break;
								if (inputline.equalsIgnoreCase("end")) break;
								buf.append(inputline);
								buf.append("\n");
							}
							buf.append("end\n");
							content = buf.toString();
						}

						LMNtalNode targetNode;

						if (command[0].equalsIgnoreCase("connect")) {
							//CONNECT
							
							//��ʬ���Ȱ����ɤ���Ƚ��
							if (isMyself(fqdn)) { //��ʬ���Ȱ�
								if (!LMNtalDaemon.isRuntimeGroupRegistered(rgid)) {	
									/* ��Ͽ����Ƥ��ʤ��� */
									
									//�����˥�󥿥�����롣
									String newCmdLine =
										new String(
											"java daemon/LocalLMNtalRuntimeSessionManager "
												+ msgid
												+ " "
												+ rgid);

									if (DEBUG)
										System.out.println(newCmdLine);

									Process remoteRuntime =
										Runtime.getRuntime().exec(newCmdLine);

									//OK�֤��Τ��������줿��󥿥��ब���롣
									continue;
								} else {
									/* ������Ͽ�Ѥߤλ� */
									targetNode = LMNtalDaemon.getRuntimeGroupNode(rgid);
								}
							}
							else { //¾�Ρ��ɰ��ʤ�connect�򤽤Τޤ�ž������
								LMNtalDaemon.makeRemoteConnection(fqdn); // todo �֥�å����ʤ��褦�ˤ���
								targetNode = LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
							}
						} else {
							// begin, connect�ʳ��ΤȤ�
							// ��ʬ���Ȱ����ɤ���Ƚ��
							if (isMyself(fqdn)) { //��ʬ���Ȱ�
								targetNode = LMNtalDaemon.getRuntimeGroupNode(rgid);
							}
							else {
								targetNode = LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
							}
						}
						
						/*
						 * ��å�����ž��
						 * ��ʬ���Ȥ�����������
						 * ¾�Ρ��ɸ�����
						 */
						if (targetNode != null && targetNode.sendMessage(content)) {
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
			} else {
				//���� or ̤�����ʥ�å�����
				
				//���⤷�ʤ��ʼΤƤ�
			}
		}
	}
}