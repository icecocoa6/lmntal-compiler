package daemon;

import java.util.HashMap;

import runtime.Atom;
import runtime.Functor;

/*
 * ����å��嵡����
 * 
 * @author nakajima
 *
 */
class Cache{
	//���ȥ�ɽ
	HashMap atomTable = new HashMap();
	//�ե��󥯥�ɽ
	HashMap functorTable = new HashMap();
	
	
	/*
	 * ���󥹥ȥ饯����
	 * 
	 * �ƥ롼���줬����å��奪�֥������Ȥ���ġ�
	 * ����Ȥ��LocalLMNtalRuntime�����ġ�
	 */
	Cache(){
	}
	
	/*
	 * ����å���ʸ����򤯤ߤ��Ƥ�
	 * @author nakajima
	 */
	 void encode(){
	 }

	 /*
	  * ����å���ʸ�������ɤ���
	  * @author nakajima
	  */
	 void decode(){
	 }
	 
	 /*
	  * ����å���򹹿�����
	  * @author nakajima
	  *
	  */
	 void update(){
		//����å��幹���λ���insideproxy�ʳ���remove����ι������Ǥ��������ˤ���
	 }
	 
	 /*
	  * atom id -> atom object
	  */
	 Atom getAtom(String atomid){
	 	Atom a = (Atom)atomTable.get(atomid);
	 	if(a == null){
	 		return null;
	 	} else {
	 		return a;
	 	}
	 }
	 
	/*
	 * fucntor id -> functor object
	 */
	Functor getFunctor(String functorid){
	   Functor f = (Functor)functorTable.get(functorid);
	   if(f == null){
		   return null;
	   } else {
		   return f;
	   }
	}
	 
}