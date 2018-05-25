package org.spr.methodAnalysis;

public class recursion {
    public void a(){
        b();
        c();
    }
    public void b(){
        d();
        e();
    }
    public void c(){
        System.out.println("+++");
    }
    public void d(){

    }
    public void e(){

    }
}
