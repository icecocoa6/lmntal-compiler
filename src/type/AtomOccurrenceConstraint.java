package type;

import runtime.Functor;

public class AtomOccurrenceConstraint implements Constraint {
	private String memname;

	private Functor functor;

	public String getMemname(){
		return memname;
	}
	
	public Functor getFunctor(){
		return functor;
	}
	
	public AtomOccurrenceConstraint(String memname, Functor functor) {
		this.memname = (memname == null ? "??" : memname);
		this.functor = functor;
	}

	public boolean equals(Object o) {
		if (o instanceof AtomOccurrenceConstraint) {
			AtomOccurrenceConstraint haac = (AtomOccurrenceConstraint) o;
			return (memname.equals(haac.memname) && functor
					.equals(haac.functor));
		} else
			return false;
	}

	public int hashCode() {
		return (memname.hashCode() * 2) + functor.hashCode();
	}

	public String toString() {
		return "(" + memname + ":" + functor + ")";
	}
}