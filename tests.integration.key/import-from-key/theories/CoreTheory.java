package theories;

public abstract class CoreTheory {

    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Methods

    // Constants / Equality / Distinction

    /*@  normal_behavior
        requires a == true;
        ensures \result == true;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performTrue(boolean a);

    /*@  normal_behavior
        requires a == false;
        ensures \result == false;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performFalse(boolean a);

    /*@  normal_behavior
        requires a == b;
        ensures \result == (a == b);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performEquals(boolean a, boolean b);

    /*@  normal_behavior
        requires a != b;
        ensures \result == (a != b);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performDistinct(boolean a, boolean b);

    // Not 
    
    /*@  normal_behavior
        requires a == !b;
        ensures \result == !b;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performNot(boolean a, boolean b);
   

    // Implication 
    
    /*@  normal_behavior
        requires a ==> b;
        ensures \result == (b ==> a);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performImplies(boolean a, boolean b);


    // And 
    
    /*@  normal_behavior
        requires a & b & c;
        ensures \result == (a & b & c);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performLogicAnd(boolean a, boolean b, boolean c);

    /*@  normal_behavior
        requires a && b && c;
        ensures \result == (a && b && c);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performAnd(boolean a, boolean b, boolean c);


    // Or

    /*@  normal_behavior
        requires a | b | c;
        ensures \result == (a |  b | c);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performLogicOr(boolean a, boolean b, boolean c);

    /*@  normal_behavior
        requires a || b || c;
        ensures \result == (a || b || c);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performOr(boolean a, boolean b, boolean c);
    

    // Xor

    /*@  normal_behavior
        requires a ^ b ^ c;
        ensures \result == (a ^  b ^ c);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performLogicOr(boolean a, boolean b, boolean c);
}
