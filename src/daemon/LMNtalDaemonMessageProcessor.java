package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import runtime.Env;

/*
 * ��å���������Ȥ򸫤ƽ������롣����Ū��LMNtalDaemon�Υ����åȤ��������ȡ�
 * ���줬��������롣�Ĥޤ�ʪ��Ū�ʷ׻���1������ʣ��¸�ߤ����롣
 * 
 * @author nakajima
 */
public class LMNtalDaemonMessageProcessor implements Runnable {
	/*
	 * ��å�������ver.20040710
	 * 
	 * res msgid msg
	 * dumphash
	 * registerlocal
	 * begin
	 * end
	 * lock
	 * unlock
	 * terminate
	 * 
	 * 
	 * �ʲ���begin-end����ˤΤߤ���
	 * RemoteMembrane.java���ȡ�
	 * 
	 * 
	 */

	static boolean DEBUG = true;

	BufferedReader in;
	BufferedWriter out;
	Socket socket;

	Integer slaveRuntimeRgid;

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

	public void run() {
		if (DEBUG) {
			System.out.println("LMNtalDaemonMessageProcessor.run()");
		}

		String input = "";

		while (true) {
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
			 *  res msgid ��å�������ʸ
			 *  registerlocal
			 *  dumphash - �ǥХå���
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
							 * begin
							 * end
							 * connect
							 * lock taskid
							 * terminate
							 */

							//TODO ʬ���ȴط��ʤ�̿��Ϥɤ����褦��InterpretedRuleset.interpret()�˿��碌�뤫��

							String command = (parsedInput[3].split(" ", 3))[0];

							if (command.equalsIgnoreCase("connect")) {
								//TODO ���Ǥ˥�󥿥��ब����Ϥɤ����褦�������Ƥ��뤫���ǧ����̿���������롩

								//�����˥饤�󥿥�����롣
								//OK�֤��Τ��������줿��󥿥��ब���롣

								slaveRuntimeRgid =
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
							} else if (command.equalsIgnoreCase("begin")) {
								//TODO ����

								//begin�θ��end������ޤ�̿�᤬Ϣ³�Ǥ���...
								//�ɤ��������褦����
								//���Τޤ�LocalLMNtalRuntime�ˤޤ魯�Τ������Τ���
								//���̿���ʬ���Ѥ�̿��Ǥʤ����������Ȥ��Ƽ¹Ԥ����٤�̿�ᡣ

								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command.equalsIgnoreCase("lock")) {
								//TODO ����
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command.equalsIgnoreCase("terminate")) {
								//��terminate�����ѡ�(by n-kato)
								//
								//���٤����Ȥ�Env.theRuntime��terminate
								//�Ǥ�Ƥ٤ʤ�����ɤ����褦��
								//LocalLMNtalRuntime.terminate();
								Env.theRuntime.terminate(); //TODO ����Ǥ����Τ���

								//out.write("not implemented yet\n");
								//out.flush();
								continue;
							} else if (command.equalsIgnoreCase("lock")){
								//TODO ����
								//lock
								
								//��å��׵᤬�����顢����å��嵡����ͭ�������ʤ��Ȥ����ʤ���
								
								
								
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
						e1.printStackTrace();
						//continue;
						break;
					}
				}
			}
		}
	}
}