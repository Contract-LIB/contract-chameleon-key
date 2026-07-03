package example_stack;

public abstract class Stack {

    //@ public instance ghost \locset footprint;

    //@ public instance invariant \subset(this.*, this.footprint);

    //@ public instance accessible \inv : footprint;
}
