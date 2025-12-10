package com.scarasol.tageditor.util;

import net.minecraft.util.Tuple;

/**
 * @author Scarasol
 */
public class TagTuple<A, B> extends Tuple<A, B> {

    public TagTuple(A elementA, B elementB) {
        super(elementA, elementB);
    }

    @Override
    public int hashCode() {
        return getA().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagTuple<?, ?> tagTuple) {
            return tagTuple.getA().equals(getA());
        }
        return false;
    }
}
