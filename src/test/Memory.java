package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import runtime.Atom;
import runtime.AtomSet;
import runtime.Functor;
import runtime.MasterLMNtalRuntime;
import runtime.Membrane;


public class Memory {
	private static final int N = 1000;
	public static void main(String[] args) {
		Object[] data = new Object[N];
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			data[i] = new Integer(i);
		}
		show(N);
		
		Map map = new HashMap();
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			map.put(data[i], data[i]);
		}
		show(N);
		
		Set set2= new HashSet();
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			set2.add(data[i]);
		}
		show(N);
		
		ArrayList list = new ArrayList();
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			list.add(data[i]);
		}
		show(N);
		
		
		Atom[] a = new Atom[N];
		Functor f = new Functor("a", 0);
		
		//���ȥ����
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			a[i] = new Atom(null, f);
		}
		show(N);

		//AtomSet�Ǵ���
		AtomSet set = new AtomSet();
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			set.add(a[i]);
		}
		show(N);

		//��˺���
		MasterLMNtalRuntime rt = new MasterLMNtalRuntime();
		Membrane root = rt.getGlobalRoot();
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			a[i] = root.newAtom(f);
		}
		show(N);
		
		//activate
		for (int i = 0; i < N; i++) {
			if (i % 100 == 0)
				show(i);
			root.enqueueAtom(a[i]);
		}
	}
	private static void show(int n) {
		Runtime r = Runtime.getRuntime();
		r.gc();
		System.out.println(n + "\t" + (r.totalMemory() - r.freeMemory())); 
	}
}
