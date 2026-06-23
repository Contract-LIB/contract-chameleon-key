
package theories;

public abstract class SetTheory {

    // - Abstract class states

    // (Set int)
    //@ public instance ghost \map setValue;

    // (Set Value) - This does not (easily) work in KeY (would be a set over abstractions)
    // @ public instance ghost \map setOwned;

    // (Set (Ref Value))
    //@ public instance ghost \map setRef;

    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Ghost field invariants 

    //@ public instance invariant (\forall int i;  \dl_mapGet(this.setValue, i) instanceof boolean);
    // @ public instance invariant (\forall int i;  \dl_mapGet(this.setOwned, i) instanceof boolean);
    //@ public instance invariant (\forall int i;  \dl_mapGet(this.setRef, i) instanceof boolean);
    
    // - Constructors

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.setValue == \map_empty;
        //ensures \result.setOwned == \map_empty;
        ensures \result.setRef == \map_empty;
        ensures \new_elems_fresh(\result);
        */
    public static SetTheory init() {
      return null;
    }

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.setValue == source.setValue;
        //ensures \result.setOwned == source.setOwned;
        ensures \result.setRef == source.setRef;
        ensures \new_elems_fresh(\result);
        accessible source.footprint;
        assignable \nothing;
        */
    public static SetTheory cloneClass(SetTheory source) {
      return null;
    }


    // - Getter

    /*@  normal_behavior
        requires true;
        ensures \result == \dl_inDomain(this.setValue, value);
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract boolean inSet(int value);
    
    /*@  normal_behavior
        requires true;
        ensures \result == \dl_inDomain(this.setRef, value);
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract boolean inSet(Value value);

    // - Setter
    
    /*@  normal_behavior
        requires true;
        ensures \dl_inDomain(this.setValue, value);
        assignable this.footprint;
        */
    public abstract void addToSet(int value);
    
    /*@  normal_behavior
        requires value != null;
        ensures \dl_inDomain(this.setRef, value);
        assignable this.footprint;
        */
    public abstract void addToSet(Value value);
}
