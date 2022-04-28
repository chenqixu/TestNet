package com.cqx.netty.example.echo;

/**
 * EchoBean
 *
 * @author chenqixu
 */
public class EchoBean {
    private int id;

    public EchoBean(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "" + getId();
    }
}
