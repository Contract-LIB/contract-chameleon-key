
package theories;

public abstract class SeqTheory {

    // - Abstract class states
   
    // (Seq int)
    //@ public instance ghost \seq absValListValue;
    // (Seq Value)
    //@ public instance ghost \seq absValListOwner;
    // (Seq (Ref Value))
    //@ public instance ghost \seq absValListRef;
    
    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Ghost field invariants 

    // Type invaraints for absValListValue
    //@ public instance invariant (\forall int i; 0 <= i < absValListValue.length; absValListValue[i] instanceof int);
    
    // Type invaraints for absValListOwner
    //@ public instance invariant (\forall int i; 0 <= i < absValListOwner.length; absValListOwner[i] instanceof Value);
    //@ public instance invariant (\forall int i; 0 <= i < absValListOwner.length; \invariant_for(((Value)absValListOwner[i])));
    //@ public instance invariant (\forall int i; 0 <= i < absValListOwner.length; \subset(\singleton(((Value)absValListOwner[i]).footprint), footprint));

    // Type invaraints for absValListRef
    //@ public instance invariant (\forall int i; 0 <= i < absValListRef.length; absValListRef[i] instanceof Value);


    // - Constructors

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.absValListValue == \seq_empty;
        ensures \result.absValListOwner == \seq_empty;
        ensures \result.absValListRef == \seq_empty;
        ensures \new_elems_fresh(\result);
        accessible \nothing;
        assignable \nothing;
        */
    public static SeqTheory init() {
      return null;
    }

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.absValListValue == source.absValListValue;
        ensures \result.absValListOwner == source.absValListOwner;
        ensures \result.absValListRef == source.absValListRef;
        ensures \new_elems_fresh(\result);
        accessible source.footprint;
        assignable \nothing;
        */
    public static SeqTheory cloneClass(SeqTheory source) {
      return null;
    }

    // - Getters

    /*@  normal_behavior
        requires this.absValListValue.length > 0;
        ensures \result != null;
        ensures \result != (int) this.absValListValue[0];
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract int getListFirstValue();

    /*@  normal_behavior
        requires this.absValListOwner.length > 0;
        ensures \result != null;
        ensures \result.absValInteger == ((Value) this.absValListOwner[0]).absValInteger;
        ensures \new_elems_fresh(\result);
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract Value getListFirstOwner();

    /*@  normal_behavior
        requires this.absValListRef.length > 0;
        ensures \result != null;
        ensures \result == (Value) this.absValListRef[0];
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract Value getListFirstRef();

    // - Setters
    
    /*@  normal_behavior
        requires this.absValListOwner != \seq_empty;
        ensures this.absValListOwner == \dl_seqSingleton(newEl);
        assignable this.footprint;
        */
    public abstract void setListFirstValue(int newEl);

    /*@  normal_behavior
        requires this.absValListOwner != \seq_empty;
        ensures ((Value) this.absValListOwner[0]).absValInteger == newEl.absValInteger;
        assignable this.footprint;
        */
    public abstract void setListFirstOwner(Value newEl);

    /*@  normal_behavior
        requires this.absValListRef != \seq_empty;
        ensures this.absValListRef == \dl_seqSingleton(newEl);
        assignable this.footprint;
        */
    public abstract void setListFirstRef(Value newEl);

    /*
     * - Methods from Seq Theory
     *
     * (par (A) (seq.empty (Seq A)))
     * (par (A) (seq.unit (Seq A)))
     * (par (A) (seq.at ((Seq A) Int) (Seq A)))
     * (par (A) (seq.extract ((Seq A) Int Int) (Seq A)))
     *
     * (par (A) (seq.len (Seq A) Int))
     * (par (A) (seq.nth ((Seq A) Int) A))
     * (par (A) (seq.indexof ((Seq A) (Seq A) Int) Int))
     * (par (A) (seq.indexof ((Seq A) (Seq A)) Int))     ;without offset
     * (par (A) (seq.extract ((Seq A) Int Int) (Seq A)))
     *
     * (par (A) (seq.contains ((Seq A) (Seq A)) Bool))
     * (par (A) (seq.prefixof ((Seq A) (Seq A)) Bool))
     * (par (A) (seq.suffixof ((Seq A) (Seq A)) Bool))
     *
     * (par (A) (seq.++ ((Seq A) (Seq A)) (Seq A)))
     * (par (A) (seq.update ((Seq A) Int Seq A) (Seq A)))
     * (par (A) (seq.replace ((Seq S) (Seq S) (Seq S)) (Seq S)))
     * (par (A) (seq.replace_all ((Seq S) (Seq S) (Seq S)) (Seq S)))
     *
     */
}
