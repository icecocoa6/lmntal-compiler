package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import runtime.LocalLMNtalRuntime;

class SlaveLMNtalRuntimeLauncher {
	static boolean DEBUG = true;
	
	static LocalLMNtalRuntime runtime;
	static int rgid;
	
	public static void main(String[] args){
		try {
			rgid = Integer.parseInt(args[0]);
			int callerMsgid = Integer.parseInt(args[1]);
			
			Socket socket = new Socket("localhost", 60000);

			BufferedWriter out =
				new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader in =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			//REGSITERLOCAL rgid
			String command = new String("registerlocal " + rgid + "\n");

			if(DEBUG)System.out.println("DummyRemoteRuntime.run(): now omitting: " + command);

			out.write(command);
			out.flush();

			String input;

			//�ǽ��1��
			input = in.readLine();
				
			if(input.equalsIgnoreCase("ok")){
				//connect��ȯ�Ԥ��������Ф���res msgid ok���֤�
				command = "res " + callerMsgid + " ok\n";
				out.write(command);
				out.flush();
				
				//LocalLMNtalRuntime��ư
				runtime = new LocalLMNtalRuntime();

				//TODO �����貿�򤹤�Ф����Τ�
				//����������
				
				//�����
				
				//�롼�륻�åȤ���


			} else {
				//connect��ȯ�Ԥ��������Ф���res msgid fail���֤�
				command = "res " + callerMsgid + " fail\n";
				out.write(command);
				out.flush();
			}

		} catch (Exception e) {
			System.out.println("ERROR in DummyRemoteRuntime.run()" + e.toString());
			e.printStackTrace();
		}
	}
}
