package example_counter;

public abstract class Counter {

    //@ public instance ghost \locset footprint;

    //@ public instance invariant \subset(this.*, this.footprint);

    //@ public instance accessible \inv : footprint;

    //@ ghost public instance int absVal;
    
    /*@  normal_behavior
        requires true;
        ensures \result.absVal == 0;
        ensures \fresh(\result.footprint);
        accessible \nothing;
        assignable \nothing;
        */
    public static Counter init() {
      return null;
    }

    /*@  normal_behavior
        requires true;
        ensures \result == this.absVal;
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract int value();

    /*@  normal_behavior
        requires true;
        ensures this.absVal == \old(this.absVal) + 1;
        assignable this.footprint;
        */
    public abstract void increment();

    /*@  normal_behavior
        requires 0 < this.absVal;
        ensures 0 < this.absVal ==> this.absVal == \old(this.absVal) - 1;
        assignable this.footprint;
        */
    public abstract void decrement();

    /*@  normal_behavior
        requires true;
        ensures value >= 0 ==> (this.absVal == value);
        assignable this.footprint;
        */
    public abstract void set(int value);
}
