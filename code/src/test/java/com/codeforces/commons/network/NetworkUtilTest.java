package com.codeforces.commons.network;

import com.codeforces.commons.collection.ListBuilder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NetworkUtilTest {
    @Test
    public void requestAddressInSubnet_addressInSubnet_true() {
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.2", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.16", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.32", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.64", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.128", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.255", "192.0.0.0/24"));
        assertTrue(NetworkUtil.isIpInSubnet("172.17.0.0", "172.16.0.0/12"));
        assertTrue(NetworkUtil.isIpInSubnet("172.16.255.1", "172.16.0.0/12"));
        assertTrue(NetworkUtil.isIpInSubnet("172.16.15.15", "172.16.0.0/12"));
        assertTrue(NetworkUtil.isIpInSubnet("172.24.15.15", "172.16.0.0/12"));
        assertTrue(NetworkUtil.isIpInSubnet("172.20.15.15", "172.16.0.0/12"));
        assertTrue(NetworkUtil.isIpInSubnet("192.0.0.170", "192.0.0.170/32"));
        assertTrue(NetworkUtil.isIpInSubnet("160.0.0.255", "128.0.0.32/1"));
        assertTrue(NetworkUtil.isIpInSubnet("128.0.0.32", "128.0.0.32"));
    }

    @Test
    public void requestAddressInSubnet_addressNotInSubnet_false() {
        assertFalse(NetworkUtil.isIpInSubnet("192.15.0.2", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("192.0.1.16", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("192.0.255.32", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("192.6.0.64", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("192.0.1.0", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("172.15.0.0", "172.16.0.0/12"));
        assertFalse(NetworkUtil.isIpInSubnet("17.164.255.1", "172.16.0.0/12"));
        assertFalse(NetworkUtil.isIpInSubnet("12.11.15.15", "172.16.0.0/12"));
        assertFalse(NetworkUtil.isIpInSubnet("172.16.0.1", "172.16.0.0/32"));
        assertFalse(NetworkUtil.isIpInSubnet("127.0.0.255", "128.0.0.32/1"));
        assertFalse(NetworkUtil.isIpInSubnet("127.0.0.255", "128.0.0.32"));
    }

    @Test
    public void requestAddressInSubnet_incorrectAddress_exceptionThrown() {
        List<String> ips = new ListBuilder<String>()
                .add("first")
                .add("192.0.0.256")
                .add("192.0.0.256")
                .add("192.0.0.256")
                .add("192.0.0.25as")
                .add("192.0.0.251as")
                .add("192.0.0.254")
                .add("192.0.0.254")
                .add("192.0.0.256as")
                .add("192.0.0.251")
                .add(null)
                .add(null)
                .add("test").build();

        List<String> subnets = new ListBuilder<String>()
                .add("second")
                .add("192.0.0.0/24")
                .add("192.0.0.0/0")
                .add("192.0.0.0/-1")
                .add("192.0.0.0/16")
                .add("192.0.0.0/aa")
                .add("192.0.0.0/")
                .add("/")
                .add("192.0.0.0/")
                .add(null)
                .add("192.0.0.0/24")
                .add(null)
                .add("test").build();

        for (int i = 0; i < subnets.size(); i++) {
            try {
                NetworkUtil.isIpInSubnet(ips.get(i), subnets.get(i));
                fail(String.format("Expected IllegalArgumentException on ip='%s' and subnet='%s'", ips.get(i), subnets.get(i)));
            } catch (IllegalArgumentException ignored) {

            }
        }
    }
}
