package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class LMNtalDaemonMessageProcessor implements Runnable {
	BufferedReader in;
	BufferedWriter out;
	Socket socket;

	public LMNtalDaemonMessageProcessor(
		Socket tmpSocket,
		BufferedReader inTmp,
		BufferedWriter outTmp) {
		in = inTmp;
		out = outTmp;
		socket = tmpSocket;
	}

	public void run() {
		System.out.println("LMNtalDaemonMessageProcessor.run()");

		String input = "";

		while (true) {
			try {
				input = in.readLine();
			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���!");
				e.printStackTrace();
				break;
			}

			System.out.println("in.readLine(): " + input);
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

				System.out.println("res: returnNode is: " + returnNode.toString());
				if (returnNode == null) {
					//�ᤷ�褬null
					try {
						out.write("fail\n");
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
						continue;
					} catch (IOException e1) {
						e1.printStackTrace();
						continue;
					}
				} else {
					//����
					try {
						out.write("fail\n");
						out.flush();
						continue;
					} catch (IOException e1) {
						e1.printStackTrace();
						continue;
					}
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
						if (InetAddress
							.getLocalHost()
							.getHostAddress()
							.equals(
								InetAddress
									.getByName(fqdn)
									.getHostAddress())) {

							System.out.println(
								"This message is for me: "
									+ InetAddress
										.getLocalHost()
										.getHostAddress());

							//��ʬ���Ȱ��ʤ顢��ʬ���Ȥǽ�������

							/* �����ǽ��������̿�����
							 * 
							 *  begin
							 *  connect
							 *  lock taskid
							 *  terminate
							 */

							String command = (parsedInput[3].split(" ", 3))[0];

							if (command.equalsIgnoreCase("connect")) {
								//connect�������顢��󥿥�����������롣

								LMNtalDaemon.createRemoteRuntime(
									msgid.intValue());

								continue;
								//OK�֤��Τ��������줿�饤�����ब���롣
							} else if (command.equalsIgnoreCase("begin")) {
								//��
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command.equalsIgnoreCase("lock")) {
								//��
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else if (command.equalsIgnoreCase("terminate")) {
								//��
								out.write("not implemented yet\n");
								out.flush();
								continue;
							} else {
								//̤�ΤΥ��ޥ�� or ����ʳ��β���
								out.write("fail\n");
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
								
								if(result){
									targetNode =
										LMNtalDaemon.getLMNtalNodeFromFQDN(fqdn);

									if(targetNode == null){
										out.write("fail\n");
										out.flush();
										continue;
									} else {
										LMNtalDaemon.sendMessage(targetNode, input + "\n");
									}

									continue;
								} else {
									//����Ρ��ɤؤ���³����
									out.write("fail\n");
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
									out.write("fail\n");
									out.flush();
									continue;
								}
							}
						}
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
						//continue;
						break;
					} catch (IOException e1) {
						e1.printStackTrace();
						//continue;
						break;
					}  catch (NullPointerException nurupo){
						System.out.println("�ʡ����ϡ��ˡ㡡�̤��");
						nurupo.printStackTrace();	
						//continue;
						break;
					}
				} else {
					//����msgTable����Ͽ����Ƥ���� or �̿����Ի�
					try {
						out.write("fail\n");
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