package daemon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

//import runtime.Env;
//import runtime.Membrane;

/**
 * �ǡ�����������륪�֥������ȡ�
 * ���ͥ�����󤴤Ȥ��������졢��å������μ�����Ԥ���
 * <p>
 * ��å���������Ȥ򸫤ƽ������롣
 * ����Ū��LMNtalDaemon�Υ����åȤ��������ȡ����줬��������롣
 * �Ĥޤ�ʪ��Ū�ʷ׻���1������ʣ��¸�ߤ����롣
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
		if (DEBUG){System.out.println("LMNtalDaemonMessageProcessor.run()");

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
			 * input�β�ǽ����
			 * 
			 * ���ޥ�ɤ���Ϥ��ޤ��å�����
			 *  msgid fqdn rgid ��å�����
			 *   - fqdn ����ʬ�� 
			 *   - fqdn ��¾�Ͱ�
			 * BEGIN~END������
			 *  
			 */

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
			String[] parsedInput = new String[4];
			parsedInput = input.split(" ", 4);

			if (parsedInput[0].equalsIgnoreCase("res")) {
				//res msgid ���
				msgid = parsedInput[1];

				//�᤹��
				LMNtalNode returnNode = LMNtalDaemon.getNodeFromMsgId(msgid);

				if (DEBUG)
					System.out.println(
						"res: returnNode is: " + returnNode.toString());

				if (returnNode == null) {
					//�ᤷ�褬null
					// TODO (n-kato) ���Ԥ�̵�뤹�롣�ޤ��ϡ�msgid�Ǥʤ��̤ο�����msgid���꼺�Ԥ�out�����Τ���
					LMNtalDaemon.respondAsFail(out, msgid);
					continue;
				} else {
					try {  //todo LMNtalDaemon�˰ܤ�
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
				rgid = parsedInput[1];

				result = LMNtalDaemon.registerLocal(rgid, socket);
				if (result == true) {  //todo LMNtalDaemon�����ݤ򸫤�
					try {
						//����
						out.write("ok\n");
						out.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					continue;
				} else {
					try {
						//����
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
				
				/* ���ޥ�ɤ���Ϥ��ޤ�ʸ����ν����������ޤ� */
				
			} else {
				// TODO �Ѹξ������ĥ���Τ��ᡢmsgid �����˶��̥��ޥ��̾���㤨��cmd�ˤ�񤤤������褤
				/* msgid "fqdn" rgid ��å����� �ν��� */
			
				//msgid����ĤŤ�̿����Ȥߤʤ�
				msgid = parsedInput[0];
				fqdn = (parsedInput[1].split("\"", 3))[1];
				rgid = parsedInput[2];

				//��å���������Ͽ
				LMNtalNode returnNode = this;
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

							/*
							 * �����ǽ��������̿�����
							 * 
							 *  connect
							 *  begin
							 *  beginrule
							 * 
							 * lock
							 * blockinglock
							 * asynclock recursivelock
							 * 
							 * unlock
							 * blockingunlock
							 * asyncunlock
							 * recursiveunlock
							 * 
							 * terminate
							 *  
							 */

							String[] command = new String[3];
							command = parsedInput[3].split(" ", 3);
							//String srcmem, dstmem, parentmem, atom1, atom2, pos1, pos2, ruleset, func;
							//Membrane realMem;

							if (command[0].equalsIgnoreCase("connect")) {
								
								if (!LMNtalDaemon.isRuntimeGroupRegistered(rgid)) {
									
									/** ��Ͽ����Ƥ��ʤ��� */
									
									//�����˥�󥿥�����롣
									//OK�֤��Τ��������줿��󥿥��ब���롣
									
									//������å�������msgid���Ȥ���Ƚ�Ǥ���registerRemote��Ƥ�
									
									String newCmdLine =
										new String(
											"java daemon/SlaveLMNtalRuntimeLauncher "
												+ rgid
												+ " "
												+ msgid.toString());

									if (DEBUG)
										System.out.println(newCmdLine);

									try {
										Process remoteRuntime =
											Runtime.getRuntime().exec(newCmdLine);
									} catch (IOException e) {
										e.printStackTrace();
										LMNtalDaemon.respondAsFail(out,msgid);
									}
									
									continue;
									/** ��Ͽ����Ƥ��ʤ����������ޤ� */

//									//�����ʤɤ�Ȥäƥ�å������η�³��Ͽ���롣
//									//res��������Ȥ��η�³��									
//									//daemon����Ͽ��
//									LMNtalDaemon.registerRemote(rgid, socket);
									
								} else {
									/** ������Ͽ�Ѥߤλ� */
									Socket localSocket = LMNtalDaemon.getLocalSocket(rgid);
									
									if(localSocket == null){
										LMNtalDaemon.respondAsFail(out,msgid); 
									} else {
										//TODO  ����Ʊ�ΤǤ��Ȥꤹ���å������Υե����ޥåȤ�ͤ��롣�Ȥꤢ���������Ǥϻ�ʤʤ���ΤȻפä����֤���
										
										//LMNtalDaemon.sendLocal(out, rgid, "CONNECT");
										
										//OK���֤��Τϥ������󥿥��ब�Ԥ���todo	  �����Ρ��ºݤ�SlaveLMNtalRuntimeLauncher�ʤ�ͽ��
										/** ������Ͽ�Ѥߤλ��������ޤ� */
									}
								}

								continue;
							} else {
								Socket localSocket = LMNtalDaemon.getLocalSocket(rgid);
								// TODO rgid���Ф���socket�ǤϤʤ�Writer���������
								BufferedWriter localout = null; // localSocket.out;
								localout.write(input + "\n");
								if (command[0].equalsIgnoreCase("begin")) {
									// end�����ޤ�ž��
									try {
										while(true){
											String inputline = in.readLine();
											if (inputline == null) break;
											if (inputline.equalsIgnoreCase("end")) {
												break;
											}
											localout.write(inputline + "\n");
										}
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									localout.write("end\n");
								}
								localout.flush();
								continue;
							}
						} else {
							//¾�Ρ��ɰ��ʤ��å������򤤤��餺�ˤ��Τޤ�ž������

							//����Ρ��ɤϴ��Τ���
							LMNtalNode targetNode =
								LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);
				
							if (targetNode == null) {
								//����Ρ��ɤ���³����Τ����Ƥξ��
								result = LMNtalDaemon.connect(fqdn, rgid);

								if (result) {
									targetNode =
										LMNtalDaemon.getLMNtalNodeFromFQDN(
											fqdn);

									if (targetNode == null) {
										//��³����
										LMNtalDaemon.respondAsFail(out,msgid);
										continue;
									} else {
										LMNtalDaemon.sendMessage(
											targetNode,
											input + "\n");
									}
									continue;
								} else {
									//����Ρ��ɤؤ���³����
									LMNtalDaemon.respondAsFail(out,msgid);
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
									LMNtalDaemon.respondAsFail(out,msgid);
									continue;
								}
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						//continue;
						break;
					} catch (NullPointerException nullpo) {
						System.out.println("�ʡ����ϡ��ˡ㡡�̤��");
						nullpo.printStackTrace();
						//continue;
						break;
					}
				} else {
					//����msgTable����Ͽ����Ƥ���� or �̿�����
					LMNtalDaemon.respondAsFail(out,msgid);
				}
				}
			}
		}
	}
	


}