package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/** ����VM�Ǽ¹Ԥ����󥿥���ʵ졧ʪ���ޥ��󡢵졹���׻��Ρ��ɡ�
 * ���Υ��饹�Υ��֥��饹�Υ��󥹥��󥹤ϡ�1�Ĥ� Java VM �ˤĤ��⡹1�Ĥ���¸�ߤ��ʤ���
*/
public class LocalLMNtalRuntime extends AbstractLMNtalRuntime /*implements Runnable */{
	List tasks = new ArrayList();
//	protected Thread thread = new Thread(this);
	LocalLMNtalRuntime(){
		Env.theRuntime = this;
	}

	AbstractTask newTask(AbstractMembrane parent) {
		Task t = new Task(this,parent);
		tasks.add(t);
		return t;
	}
	/** �ʥޥ����������ˤ�äơˤ��Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	protected boolean terminated = false;
	/** ���Υ�󥿥���ν�λ���׵᤹�롣
	 * ����Ū�ˤϡ�����ʪ���ޥ����terminated�ե饰��ON�ˤ���
	 * �ƥ������Υ롼�륹��åɤ˽����褦�˸��� */
	synchronized public void terminate() {
		terminated = true;
		Iterator it = tasks.iterator();
		while (it.hasNext()) {
			((Task)it.next()).signal();
		}
		// TODO join����
		// ���졼�֥�󥿥���ʤ�С�VM��λ���롣
		// if (!(this instanceof MasterLMNtalRuntime)) System.exit(0);
	}
	/** ���Υ�󥿥���ν�λ���׵ᤵ�줿���ɤ��� */
	public boolean isTerminated() {
		return terminated;
	}
//	/** ʪ���ޥ��󤬻��ĥ��������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
//	 *  Tasks���Ѥޤ줿��˼¹Ԥ��롣�ƥ�����ͥ��ˤ��뤿��ˤ�
//	 *  ���������ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
//	 */
//	protected void localExec() {
//		boolean allIdle;
//		do {
//			allIdle = true; // idle�Ǥʤ������������Ĥ��ä���false�ˤʤ롣
//			Iterator it = tasks.iterator();
//			while (it.hasNext()) {
//				Task task = (Task)it.next();
//				if (!task.isIdle()) { // idle�Ǥʤ������������ä���
//					task.exec(); // �ҤȤ�����¹�
//					allIdle = false; // idle�Ǥʤ�������������
//				//	break;
//				}
//			}
//		} while(!allIdle);
//	}
}