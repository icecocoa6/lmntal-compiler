package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;



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
		System.out.println("LMNtalDaemonThread2.run()");

		while (true) {
			try {
				String input = in.readLine();
				//�ƥ����ѡ�
				//String input = new String("msgid \"localhost\" runtimegroupid connect\n");
				System.out.println("input: " + input);
				if (input == null) {
					break;
				}

				//���������å�����������ʬ
				/* �����ǽ��������̿�����������ʳ��Τϲ��������뤷�Ƥ�
				 *  res
				 *  registerlocal
				 *  (dumphash)
				 *  
				 */
							
				Integer msgid;
				Integer rgid;
				String fqdn;
				boolean result;
				String[] tmpString = new String[3];

				tmpString = input.split(" ", 3);

				if (tmpString[0].equalsIgnoreCase("res")) {
					//res msgid ���
					//ľ���᤻�Ф褤

					//TODO ñ�Υƥ���
					msgid = new Integer(tmpString[1]);

					LMNtalNode returnNode =
						LMNtalDaemon.getNodeFromMsgId(msgid);

					returnNode.getOutputStream().write(input);
					returnNode.getOutputStream().flush();
				} else if (tmpString[0].equalsIgnoreCase("registerlocal")) {
					//registerlocal rgid
					//rgid�ȥ����åȤ���Ͽ
					rgid = new Integer(tmpString[1]);
					result = LMNtalDaemon.registerLocal(rgid, socket);
					if (result == true) {
						//����
						out.write("ok\n");
						out.flush();
					} else {
						//����
						out.write("fail\n");
						out.flush();
					}
				} else if (tmpString[0].equalsIgnoreCase("dumphash")) {
					//dumphash
					LMNtalDaemon.dumpHashMap();
				} else {
					//msgid����ĤŤ�̿����Ȥߤʤ�
					//msgid "FQDN" rgid ��å�����
					msgid = new Integer(tmpString[0]);
					fqdn = (tmpString[1].split("\"", 3))[1];
					
					//��å���������Ͽ
					LMNtalNode returnNode =
						new LMNtalNode(socket.getInetAddress(), in, out);
					result = LMNtalDaemon.registerMessage(msgid, returnNode);

					if (result == true) {
						//��å�������Ͽ����
						
						//��ʬ���Ȱ����ɤ���Ƚ��
						if (socket.getInetAddress().isAnyLocalAddress()){
							//��ʬ���Ȱ��ʤ顢��ʬ���Ȥǽ�������
							
							/* �����ǽ��������̿�����
							 * 
							 *  begin
							 *  connect
							 *  lock taskid
							 *  terminate
							 */
							
							String command =  (tmpString[2].split(" ", 3))[0];
							if(command.equalsIgnoreCase("connect")){
								//connect�������顢��󥿥������������ ok ���֤�
								
								Thread remoteRuntime = new Thread(new DummyRemoteRuntime());
								remoteRuntime.start();



								out.write("ok\n");
								out.flush();
							} else if(command.equalsIgnoreCase("begin")){
								//��
								out.write("not implemented yet\n");
								out.flush();							 
							} else if(command.equalsIgnoreCase("lock")){
								//��
								out.write("not implemented yet\n");
								out.flush();
							} else if(command.equalsIgnoreCase("terminate")){
								//��
								out.write("not implemented yet\n");
								out.flush();
							} else {
								//̤�ΤΥ��ޥ�� or ����ʳ��β���
								out.write("fail\n");
								out.flush();	
							}						
						} else {
							//¾�Ρ��ɰ��ʤ��å������򤤤��餺�ˤ��Τޤ�ž������
							if (LMNtalDaemon.sendMessage( (tmpString[1].split("\"", 3))[1]  , input  )){
								out.write("ok\n");
								out.flush();
							} else {
								out.write("fail\n");
								out.flush();
							}
						}
					} else {
						//����msgTable����Ͽ����Ƥ���� or �̿����Ի�
						out.write("fail\n");
						out.flush();
					}

				}

			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���! " + e.toString());
				break;
			} catch (ArrayIndexOutOfBoundsException ae) {
				//�����Ƥ�����å�������û��������Ȥ��ʡ������ʻ�
				//'hoge' �Ȥ�����������
				System.out.println("Invalid Message: " + ae.toString());
				break;
			}
		}
	}

}