package example;

/*@ predicate payload(Object o;); @*/

/*@ fixpoint list<T> addEnd<T>(T val, list<T> ll) {
      switch(ll) {
        case nil: return cons(val, ll);
        case cons(h, t): return cons(h, addEnd(val, t));
      }
    }
@*/

/*@ fixpoint Cell last(list<Cell> ll) {
       switch(ll) {
       case nil: return null;
       case cons(h, t): return t==nil ? h : last(t);
       }
    }
@*/

//@ predicate llist(list<Cell> xs);

public abstract class LinkedCellList {

    //@ predicate llist(list<Cell> xs);

    public static LinkedCellList init()
        //@ requires true;
        //@ ensures result.llist(?result_absVal) &*& result_absVal == nil &*& result != null;
    {
        return new LinkedCellListImpl();
    }

    public abstract void add(LinkedCellList v);
        //@ requires this.llist(?l_old) &*& [_]payload(v);
        //@ ensures this.llist(?l) &*& l == addEnd(v, l_old);

    public abstract Cell getLast();
        //@ requires this.llist(?l_old) &*& !(l_old == nil);
        //@ ensures this.llist(l_old) &*& result == last(l_old) &*& [_]payload(result);

}
