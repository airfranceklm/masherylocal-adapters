package com.airfranceklm.amt.yaml;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;

public class TestDumping {
    @Test
    public void testDumping() {
        NestedBean nb = new NestedBean();
        nb.setValue("myValue");

        System.out.println(new Yaml().dump(nb));
    }

    @Test
    public void testDumpingList() {
        NestedBean nb = new NestedBean();
        nb.setValue("myValue");

        NestedBean nb1 = new NestedBean();
        nb1.setValue("myValue1");

        ArrayList<NestedBean> al = new ArrayList<>();
        al.add(nb);
        al.add(nb1);

        System.out.println(new Yaml().dump(al));
    }
}
