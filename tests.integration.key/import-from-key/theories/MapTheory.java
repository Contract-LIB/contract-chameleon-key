
package theories;

public abstract class MapTheory {

    // - Abstract class states
   
    // (Map int int)
    //@ public instance ghost \map absValMapValueValue;
    // (Map int Value)
    //@ public instance ghost \map absValMapValueOwner;
    // (Map int (Ref Value))
    //@ public instance ghost \map absValMapValueRef;
    
    // (Map Value int)
    // @ public instance ghost \map absValMapOwnerValue;
    // (Map Value Value)
    // @ public instance ghost \map absValMapOwnerOwner;
    // (Map Value (Ref Value))
    // @ public instance ghost \map absValMapOwnerRef;

    // (Map (Ref Value) int)
    // @ public instance ghost \map absValMapRefValue;
    // (Map (Ref Value) Value)
    // @ public instance ghost \map absValMapRefOwner;
    // (Map (Ref Value) (Ref Value))
    // @ public instance ghost \map absValMapRefRef;

    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;

    // - Ghost field invariants 

    // Type invaraints for absValMapValueValue 
    //@ public instance invariant (\forall int i;  \dl_mapGet(this.absValMapValueValue, i) instanceof int);

    // Type invaraints for absValMapValueOwner
    //@ public instance invariant (\forall int i;  \dl_mapGet(this.absValMapValueOwner, i) instanceof Value);

    // Type invaraints for absValMapValueRef
    //@ public instance invariant (\forall int i;  \dl_mapGet(this.absValMapValueRef, i) instanceof Value);

    // Type invaraints for absValMapOwnerValue
    // @ public instance invariant (\forall Object o;  \dl_mapGet(this.absValMapOwnerValue, o) instanceof int);

    // Type invaraints for absValMapOwnerOwner

    // Type invaraints for absValMapOwnerRef

    // Type invaraints for absValMapRefValue
    // @ public instance invariant (\forall Object o;  \dl_mapGet(this.absValMapRefValue, o) instanceof int);

    // Type invaraints for absValMapRefOwner

    // Type invaraints for absValMapRefRef

    // Type invaraints for
    // @ public instance invariant (\forall int i; 0 <= i < absValMapValueValue.length; absValListOwner[i] instanceof Value);
    // @ public instance invariant (\forall int i; 0 <= i < absValMapValueValue.length; \invariant_for(((Value)absValListOwner[i])));
    // @ public instance invariant (\forall int i; 0 <= i < absValMapValueValue.length; \subset(\singleton(((Value)absValListOwner[i]).footprint), footprint));

    // Type invaraints for absValListRef
    // @ public instance invariant (\forall int i; 0 <= i < absValMapValueValue.length; absValListRef[i] instanceof Value);

    // - Constructors

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.absValMapValueValue == \map_empty;
        ensures \result.absValMapValueOwner == \map_empty;
        ensures \result.absValMapValueRef == \map_empty;
        //ensures \result.absValMapOwnerValue == \map_empty;
        //ensures \result.absValMapOwnerOwner == \map_empty;
        //ensures \result.absValMapOwnerRef == \map_empty;
        //ensures \result.absValMapRefValue== \map_empty;
        //ensures \result.absValMapRefOwner == \map_empty;
        //ensures \result.absValMapRefRef == \map_empty;
        ensures \new_elems_fresh(\result);
        accessible \nothing;
        assignable \nothing;
        */
    public static MapTheory init() {
      return null;
    }

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.absValMapValueValue == source.absValMapValueValue;
        ensures \result.absValMapValueOwner == source.absValMapValueOwner;
        ensures \result.absValMapValueRef == source.absValMapValueRef;
        //ensures \result.absValMapOwnerValue == source.absValMapOwnerValue;
        //ensures \result.absValMapOwnerOwner == source.absValMapOwnerOwner;
        //ensures \result.absValMapOwnerRef == source.absValMapOwnerRef;
        //ensures \result.absValMapRefValue == source.absValMapRefValue;
        //ensures \result.absValMapRefOwner == source.absValMapRefOwner;
        //ensures \result.absValMapRefRef == source.absValMapRefRef;
        ensures \new_elems_fresh(\result);
        accessible source.footprint;
        assignable \nothing;
        */
    public static MapTheory cloneClass(MapTheory source) {
      return null;
    }

    // - Getters

    // Maps

    /*@  normal_behavior
        requires \dl_inDomain(this.absValMapValueValue, key);
        ensures \result != null;
        ensures \result != ((int) \dl_mapGet(this.absValMapValueValue, key));
        ensures \new_elems_fresh(\result);
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract int getMapValueValue(int key);

    /*@  normal_behavior
        requires \dl_inDomain(this.absValMapValueOwner, key);
        ensures \result != null;
        ensures ((Value)\result).absValInteger == ((Value)\dl_mapGet(this.absValMapValueOwner, key)).absValInteger;
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract Value getMapValueOwner(int key);

    /*@  normal_behavior
        requires \dl_inDomain(this.absValMapValueRef, key);
        ensures \result != null;
        ensures ((Value)\result) == ((Value)\dl_mapGet(this.absValMapValueRef, key));
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract Value getMapValueOwner(int key);

    // - Setters
    
    /*@  normal_behavior
        requires true;
        ensures ((int) \dl_mapGet(this.absValMapValueValue, key)) == value;
        assignable this.footprint;
        */
    public abstract void setMapValueValue(int key, int value);

    /*@  normal_behavior
        requires true;
        ensures ((Value) \dl_mapGet(this.absValMapValueOwner, key)).absValInteger == value.absValInteger;
        assignable this.footprint;
        */
    public abstract void setMapValueValue(int key, Value value);

    /*@  normal_behavior
        requires true;
        ensures ((Value) \dl_mapGet(this.absValMapValueRef, key)) == value;
        assignable this.footprint;
        */
    public abstract void setMapValueValue(int key, Value value);
}
