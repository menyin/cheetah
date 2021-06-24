package com.caisheng.cheetah.api.spi.net;

import java.net.URL;
import java.util.Objects;

public class DnsMapping {
    private int port;
    private String host;

    public DnsMapping(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 通过指定格式字符串转为DnsMapping实例
     * @param addr 形如192.168.1.11::10
     * @return
     */
    public static DnsMapping parse(String addr){
        if(addr.equals("")){throw new RuntimeException("dns address is not empty.");}
        String[] split = Objects.requireNonNull(addr, "dns address is not null.").split(":");

        if(split.length==1){
            return new DnsMapping(80, split[0]);
        }else{
            return new DnsMapping(Integer.parseInt(split[1]), split[0]);
        }
    }

    /**
     * 将制定的URL实例转换为带host:port的url字符串
     * @param url
     * @return
     */
    public String translate(URL url){
        StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol());
        sb.append("://");
        sb.append(this.host+":"+this.port);
        sb.append(url.getPath());
        sb.append("?");
        String query = url.getQuery();
        query = query != null ? "?" + query : "";
        sb.append(query);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = this.host.hashCode();
        return hashCode*31+this.port;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null)return false;
        if(obj.getClass()!=this.getClass()) return false;
        DnsMapping dnsMapping = (DnsMapping) obj;
        if(dnsMapping.getPort()!=this.getPort())return false;
        return dnsMapping.getHost()==this.getHost();
    }

    @Override
    public String toString() {
        return this.host+":"+host;
    }
}
