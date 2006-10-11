package type.quantity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import runtime.Functor;

import compile.structure.Atom;
import compile.structure.Membrane;

/**
 * ���줾�����ˤĤ��ƺ����
 * @author kudo
 *
 */
public class CountsOfMem{
	
	Membrane mem;
	
	Map<Functor,Count> functorToCount;
	Map<String,Count> memnameToCount;
	public CountsOfMem(Membrane mem){
		this.mem = mem;
		functorToCount = new HashMap<Functor, Count>();
		memnameToCount = new HashMap<String, Count>();
	}
	
	/**
	 * ���ȥ�˴ؤ����̤�û�
	 * @param atom
	 * @param count
	 */
	public void addAtomCount(Atom atom, Count count){
		addAtomCount(atom.functor, count);
	}
	public void addAtomCount(Functor functor, Count count){
		if(!functorToCount.containsKey(functor))
			functorToCount.put(functor,count);
		else{
			Count atomcount = functorToCount.get(functor);
			functorToCount.put(functor, atomcount.add(count));
		}
	}
	/**
	 * ��˴ؤ����̤�û�
	 * @param m
	 * @param count
	 */
	public void addMemCount(Membrane m, Count count){
		addMemCount(m.name, count);
	}
	public void addMemCount(String memname, Count count){
		if(!memnameToCount.containsKey(memname))
			memnameToCount.put(memname,count);
		else{
			Count memcount = memnameToCount.get(memname);
			memnameToCount.put(memname, memcount.add(count));
		}
	}
	/**
	 * �̤��̥��åȤ������Ʋû�
	 * @param com2
	 */
	public void addAllCounts(CountsOfMem com2){
		Iterator<Functor> itf = com2.functorToCount.keySet().iterator();
		while(itf.hasNext()){
			Functor f = itf.next();
			addAtomCount(f,com2.functorToCount.get(f));
		}
		Iterator<String> itn = com2.memnameToCount.keySet().iterator();
		while(itf.hasNext()){
			String name = itn.next();
			addMemCount(name,com2.memnameToCount.get(name));
		}
	}
	/**
	 * �̤��̥��åȤ�or���
	 */
	public void merge(CountsOfMem com2){
		Iterator<Functor> itf = com2.functorToCount.keySet().iterator();
		while(itf.hasNext()){
			Functor f = itf.next();
			Count c = com2.functorToCount.get(f);
			if(!functorToCount.containsKey(f))
				functorToCount.put(f, new OrCount(c));
			else{
				functorToCount.get(f).merge(c);
			}
		}
		Iterator<String> itn = com2.memnameToCount.keySet().iterator();
		while(itf.hasNext()){
			String name = itn.next();
			Count c = com2.memnameToCount.get(name);
			if(!memnameToCount.containsKey(name))
				memnameToCount.put(name,new OrCount(c));
			else
				memnameToCount.get(name).merge(c);
		}
	}
}
