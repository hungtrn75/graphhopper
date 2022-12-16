package com.graphhopper.reader.dem;

public class Pair<T, P>{
    final T first;
    final P second;

    public Pair(T first, P second){
        this.first = first;
        this.second = second;
    }
}