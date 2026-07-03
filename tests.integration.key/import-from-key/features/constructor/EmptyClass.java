package constructor;

public abstract class EmptyClass {

    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Constructors

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \fresh(\result.footprint);
        accessible \nothing;
        assignable \nothing;
        */
    public static EmptyClass init() {
      return null;
    }

    // - Methods

    /*@  normal_behavior
        requires true;
        ensures true;
        assignable \nothing;
        */
    public abstract void performEmpty();

    /*@  normal_behavior
        requires true;
        ensures true;
        assignable this.footprint;
        */
    public abstract void performEmptySelfAccess();
}
