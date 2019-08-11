 
package com.sift;

import java.util.ArrayList;

 
//集合类
public class SortedLimitedList<E> extends ArrayList<E> {

    
    private static final long serialVersionUID = 5621558689972829345L;
    int max;

    public SortedLimitedList(int maxElements){
        super(maxElements);
        this.max = maxElements;
    }

    @SuppressWarnings("unchecked")
    public boolean add(E e) {       
        int pos = size();
        while (pos > 0 &&  ((Comparable<E>)get(pos - 1)).compareTo(e) >= 0) {
            if (pos < max) setIdx(pos, get(pos - 1));
            pos--;
        }
        if (pos < max) setIdx(pos, e);
        else return false;
        return true;
    }

    private E setIdx(int idx, E e) {
        if (idx < size()) super.set(idx, e);
        else super.add(e);
        return e;
    }
}

