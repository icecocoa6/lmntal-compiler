package test.distribute;

import daemon.LMNtalDaemon;

//TODO ��λ����褦�ˤ���
//TODO negtiation���߷ס�����

public class LMNtalDaemonTest {
	public static void main(String args[]) {
		Thread t1 = new Thread(new LMNtalDaemon(60000));
		t1.start();

		//Thread t2 = new Thread(new LMNtalDaemon(60001));
		//t2.start();

	}
}





