# Alias Problem

The following example look at some special cases of the alias problem,
the semantics in `ContractLIB` and
how they can (or cannot) be translated to the tools.

Abstractions can appear in three modes in a contract,
which bring the following conditions along.

## Base Examples

This is our first base abstractions,
working as a `Int` wrapper.

```lisp
(declare-abstractions
  (
    (IntAbstraction 0)
  )
  (
    ((IntAbstraction (absVal Int)))
  )
)
; Constructor
(define-contract alias.IntAbstraction.init
    (
      (result (out IntAbstraction))
    )
    ((
      true
      (= (absVal result) 0)
    ))
)

; Getter
(define-contract alias.IntAbstraction.get
    (
      (this (in IntAbstraction))
      (result (out Int))
    )
    ((
      true
      (= (absVal this) result)
    ))
)

; Setter 
(define-contract alias.IntAbstraction.set
    (
      (this (inout IntAbstraction))
      (value (in Int))
    )
    ((
      true
      (= (absVal this) value)
    ))
)
```

Our second base abstraction is a wrapper for a reference,
or an alias.
We require that the inner type of the reference
is also an abstraction.

One might notice that the getter and setter contracts
are similar between the `RefAbstraction` and the `IntAbstraction`.
This results from the idea,
that a `Ref T` is rather a value type
(possibly `null`, or the unique object it points to)
then a transparent reference.
This however, comes at the cost,
that we cannot access the fields or state from the abstraction `T`
from a `Ref T` variable or argument.

```lisp
(declare-abstractions
  (
    (RefAbstraction 0)
  )
  (
    ((RefAbstraction (absVal (Ref IntAbstraction))))
  )
)
; Constructor
(define-contract alias.RefAbstraction.init
    (
      (result (out IntAbstraction))
    )
    ((
      true
      (= (absVal result) null)
    ))
)

; Getter
(define-contract alias.RefAbstraction.get
    (
      (this (in RefAbstraction))
      (result (out (Ref IntAbstraction)))
    )
    ((
      true
      (= (absVal this) result)
    ))
)

; Setter
(define-contract alias.RefAbstraction.set
    (
      (this (inout RefAbstraction))
      (value (in (Ref IntAbstraction)))
    )
    ((
      true
      (= (absVal this) value)
    ))
)
```

## Two Abstraction Contracts

We are able to define contracts,
that change their state and the one of the second given abstraction.
We also take a look at the different modes,
under which the abstractions might appear
and their consequences seen from a client perspective.

```lisp
(define-contract alias.IntAbstraction.copyStateFrom
    (
      (this (inout IntAbstraction))
      (from (in IntAbstraction))
    )
    ((
      true
      (= (absVal this) (absVal this))
    ))
)
```

```lisp
(define-contract alias.IntAbstraction.copyStateTo
    (
      (this (in IntAbstraction))
      (from (inout IntAbstraction))
    )
    ((
      true
      (= (absVal this) (absVal this))
    ))
)
```

Let's also look at some erroneous definitions,
that look valid on the first glimpse,
but miss what we wanted to express.

```lisp
(define-contract alias.IntAbstraction.copyStateToWrongA
    (
      (this (inout IntAbstraction))
      (from (inout IntAbstraction))
    )
    ((
      true
      ; We ensure that both abstractions have the same state.
      ; However, this state is not connected to any of the 'old' states
      ; of the abstractions under which the me contract was called.
      ; Asigning an aritrary value to both abstractions,
      ; would be a correct implementation of this contract.
      (= (absVal this) (absVal this))
    ))
)
```

## Special, Special Case

As we declare abstractions,
and we say that `T` in `Ref T` only must be an abstraction,
one might come to the idea to let an abstraction own a reference of the self type.
Or create contracts where an abstraction is passed a reference to itself.
As this breaks the encapsulation requirement of the arguments on first sight,
I will argue with the following example,
that this is not the case.

```lisp
(declare-abstractions
  (
    (alias.SpecialAbstraction 0)
  )
  (
    ((SpecialAbstraction (absValRef (Ref SpecialAbstraction)) (absVal Int)))
  )
)
; Constructor
(define-contract alias.SpecialAbstraction.init
    (
      (result (out SpecialAbstraction))
    )
    ((
      true
      (and
        (= (absValRef result) null)
        (= (absVal result) 0)
      )
    ))
)
; Getter Ref
(define-contract alias.SpecialAbstraction.getRef
    (
      (this (in SpecialAbstraction))
      (result (out (Ref SpecialAbstraction)))
    )
    ((
      true
      (= (absValRef this) result)
    ))
)
; Getter Value
(define-contract alias.SpecialAbstraction.getValue
    (
      (this (in SpecialAbstraction))
      (result (out Int))
    )
    ((
      true
      (= (absVal this) result)
    ))
)
; Setter Ref
(define-contract alias.SpecialAbstraction.setRef
    (
      (this (inout SpecialAbstraction))
      (value (in (Ref SpecialAbstraction)))
    )
    ((
      true
      (= (absValRef this) value)
    ))
)
; Setter Value
(define-contract alias.SpecialAbstraction.setValue
    (
      (this (inout SpecialAbstraction))
      (value (in Int))
    )
    ((
      true
      (= (absVal this) value)
    ))
)
```

### Breaking Example?

```lisp
(define-contract alias.SpecialAbstraction.looseInfo
    (
      (this (inout SpecialAbstraction))
      (valueA (in (Ref SpecialAbstraction)))
      (valueB (in (Ref SpecialAbstraction)))
    )
    ((
      true
      true
    ))
)
```

```java
    /*@  normal_behavior
        requires true;
        ensures true;
        ensures \new_elems_fesh(footprint);
        assignable this.footprint;
        */
    public abstract void looseInfo(Counter valueA, Counter valueB) {
    }
```

```java
/*@ normal_behavior
  requires true;
  ensures true;
*/
void client() {
  SpecialAbstraction a = SpecialAbstraction.init(null, 0);
  SpecialAbstraction b = SpecialAbstraction.init(null, 0);
  a.setRef(b);
  a.doSthA(a);
  //loses information about the reference it holds

  SpecialAbstraction c = a.getRef();
  if (c == null) {
    c = SpecialAbstraction.init(null, 0);
  }

  // This would not verify in VF
  //as we don't know the predicate to set the value
  c.setValue(10);

  //@ assert c.getValue == 10
  //@ assert b.getValue == 0

}
```

## Notes

```java
    //(this (inout StoreOwned))
    //(input (in Counter))

    /*@  normal_behavior
        requires \disjoint(this.footprint, input.footprint);
        ensures (this.absVal) == (\seq_concat(\old(this.absVal), \seq_singleton(input)))));
        ensures \disjoint(this.footprint, input.footprint);
        ensures \new_elems_fesh(footprint);
        accessible this.footprint;
        accessible input.footprint;
        assignable this.footprint;
        */
    public abstract int addOwned(Counter input) {}


    //TODO: Check for VF
    //(this (inout StoreOwned))
    //(input (in (Ref Counter)))

    /*@  normal_behavior
        requires true;
        ensures true;
        ensures \new_elems_fesh(footprint);
        accessible this.footprint;
        assignable this.footprint;
        */
    public abstract int addRef(Counter input) {
      this.box = input.gotBox()
    }

    //(this (inout StoreOwned))
    //(inputA (in (Ref Counter)))
    //(inputB (in (Ref Counter)))

    /*@  normal_behavior
        requires true;
        --requires \disjointX(this.fp, inputA.fp, intputB.fp);
        ensures true;
        --ensures \disjointX(this.fp, inputA.fp, intputB.fp);
        ensures \new_elems_fesh(footprint);
        accessible this.footprint;
        assignable this.footprint;
        */
    public abstract int addTwoRef(Counter inputA, Counter inputB) {}


    //s1 = new Counter();
    //s2 = new Counter();
    //s1.addTwoRef(s1, s2);

    //setValue(int x) { }

    //addRef(input) 
    //setValue(x)

    //a = Box [5]
    //b = a
    //b.cntent = 6
    //a.content = 6

    //    requires \disjoint(footprint, input.footprint)
    //    --ensures \fresh(footprint - old(footprint));
    //    ensures \disjoint(footprint, input.footprint)
```
