package test.distribute;

/*
 * DaemonTester
 * @author nakajima
 *
 * �Ȥ�����
 *  - cure��LMNtalDaemonTest��¹�
 *  - brie��banon�ʤɤ˥����󡢲��ۥ����ߥʥ��2�Ĥ�����ʰʲ��������ߥʥ�1�ȥ����ߥʥ�2��
 *  - [�����ߥʥ�1-2] cd eclipse/devel/bin/
 *  - scp�ǥ��������~/eclipse/devel/bin/[daemon,test/distribute]�ʲ���ǿ���
 *  - [�����ߥʥ�1] java daemon/LMNtalDaemon
 *  - [�����ߥʥ�2] java test/distribute/DaemonTester
 */

class DaemonTester{
	public static void main(String args[]){
		Thread r1 = new Thread(new DummyRuntime(100));
		r1.start();	
	}
}

