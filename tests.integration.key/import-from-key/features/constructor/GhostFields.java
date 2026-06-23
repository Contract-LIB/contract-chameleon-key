package constructor;

public abstract class GhostFields {

    // - Abstract class states

    //@ public instance ghost boolean absValBoolean;
    //@ public instance ghost int absValInteger;


    // - Well-encapsulated properties 

    //@ public instance ghost \locset footprint;
    //@ public instance invariant \subset(this.*, this.footprint);
    //@ public instance accessible \inv : footprint;


    // - Constructors

    /*@  normal_behavior
        requires true;
        ensures \result != null;
        ensures \result.absValBoolean == true;
        ensures \result.absValInteger == 0;
        ensures \fresh(\result);
        accessible \nothing;
        assignable \nothing;
        */
    public static GhostFields init() {
      return null;
    }

    /*@  normal_behavior
        requires source != null;
        ensures \result != null;
        ensures \result.absValBoolean == source.absValBoolean;
        ensures \result.absValInteger == source.absValInteger;
        ensures \fresh(\result);
        accessible source.footprint;
        assignable \nothing;
        */
    public static GhostFields cloneClass(GhostFields source) {
      return null;
    }


    // - Getters

    /*@  normal_behavior
        requires true;
        ensures \result == this.absValBoolean;
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract boolean getBooleanValue();

    /*@  normal_behavior
        requires true;
        ensures \result == this.absValInteger;
        accessible this.footprint;
        assignable \nothing;
        */
    public abstract int getIntValue();


    // - Setters

    /*@  normal_behavior
        requires true;
        ensures newValue == this.absValBoolean;
        assignable this.footprint;
        */
    public abstract void setBooleanValue(boolean newValue);

    /*@  normal_behavior
        requires true;
        ensures newValue == this.absValInteger;
        assignable this.footprint;
        */
    public abstract void setIntValue(int newValue);

    /*@  normal_behavior
        requires buddy != null;
        ensures this.absValBoolean == (\old(this.absValBoolean) & buddy.absValBoolean);
        ensures this.absValInteger == (\old(this.absValInteger) + buddy.absValInteger);
        assignable this.footprint;
        */
    public abstract void merge(GhostFields buddy);
}
