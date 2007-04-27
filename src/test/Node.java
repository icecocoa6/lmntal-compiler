/* 
 �����ȡ����Υ饤�֥����Ȥ߹�碌��
 on-the-fly Model Checking ��Ԥ����Ȥ���ɸ�Ȥ��롥
 --Java �ǽ񤤤��С������--
 1. translate LTL into Automata
 2. generate system Automata
 3. DDFS

 struct node = (ID: int, Incoming: Membrane, Old: Membrane, New: Membrane, Next: Membrane)
 */

/*
 % example
 a = until(a,until(b,c)).

 % put the formula into negational normal form
 // F �� �� True U ��
 H = future(Psi) :- H = until(true,Psi).

 // G �� �� False R ��
 H = global(Psi) :- H = release(false,Psi).

 // ��(�� U ��) = (�̦�) R (�̦�)
 H = not(until(Mu,Eta)) :- H = release(not(Mu,Eta)).

 // ��(�� R ��) = (�̦�) U (�̦�)
 H = not(release(Mu,Eta)) :- H = until(not(Mu),not(Eta)).

 // ��X�� = X�̦�
 H = not(next(Mu)) :- H = next(not(Mu)).

 % translate LTL into Automata
 */

//[:/*inline_define*/
package test;

import java.util.HashSet;
import java.util.Iterator;
import runtime.Link;

//struct node
public class Node {
	public final int id;

	public HashSet<Integer> incoming;

	HashSet<Link> older;

	HashSet<Link> newer;

	HashSet<Link> next;

	public Node(int id, HashSet<Integer> incoming, HashSet<Link> old,
			HashSet<Link> newer, HashSet<Link> next) {
		this.id = id;
		this.incoming = incoming;
		this.older = old;
		this.newer = newer;
		this.next = next;
	}

	public void print() {
		System.out.println("id: " + this.id);
		System.out.println("Incoming: " + this.incoming);
		System.out.println("Old: " + this.older);
		System.out.println("New: " + this.newer);
		System.out.println("Next: " + this.next);
	}

	public void printv() {
		System.out.println("Id: " + this.id);
		System.out.println("Incoming: " + this.incoming);
		Iterator<Link> oi = this.older.iterator();
		System.out.print("Old:");
		while (oi.hasNext())
			System.out.print(" " + oi.next().getAtom());
		System.out.print("\n");
		Iterator<Link> newi = this.newer.iterator();
		System.out.print("New:");
		while (newi.hasNext())
			System.out.print(" " + newi.next().getAtom());
		System.out.print("\n");
		Iterator<Link> nexti = this.next.iterator();
		System.out.print("Next:");
		while (nexti.hasNext())
			System.out.print(" " + nexti.next().getAtom());
		System.out.print("\n");
	}
}
