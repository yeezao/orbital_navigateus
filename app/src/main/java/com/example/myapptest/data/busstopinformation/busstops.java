package com.example.myapptest.data.busstopinformation;

import java.io.Serializable;
import java.util.List;

public class busstops implements Serializable {

    private List<StopDetails> busstops;

    public busstops() {}

    public List<StopDetails> getbusstops() {
        return busstops;
    }
}
