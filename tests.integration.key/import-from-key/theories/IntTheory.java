
package theories;

public abstract class IntTheory {

    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Methods

    // Constants / Equality / Distinction

    /*@  normal_behavior
        requires a == 0;
        ensures \result == 0;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract int performZero(int a);

    /*@  normal_behavior
        requires a == 42;
        ensures \result == 42;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract int performPositiveValue(int a);

    /*@  normal_behavior
        requires a == -13;
        ensures \result == -13;
        accessible \nothing;
        assignable \nothing;
        */
    public abstract int performNegativeValue(int a);

    /*@  normal_behavior
        requires true;
        ensures \result == (a == b);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performEquals(int a, int b);

    /*@  normal_behavior
        requires a != b;
        ensures \result == (a != b);
        accessible \nothing;
        assignable \nothing;
        */
    public abstract boolean performDistinct(int a, int b);

    // Negation

    // TODO:

    // Addition
    // Subtraction 
    // Multiplication 
    // Division 
    // Modulo 
    // Abs

    // Less 
    // Less Equals
    // Greater 
    // Greater Equals
}

