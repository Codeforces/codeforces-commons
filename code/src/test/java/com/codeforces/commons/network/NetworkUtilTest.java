package com.codeforces.commons.network;

import com.codeforces.commons.collection.ListBuilder;
import org.apache.commons.lang3.StringUtils;
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
    public void isIpInSubnet_Ipv6InSubnet_true() {
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertTrue(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a5", "00:6b8:0892:ad61:59a2:3149:c5a0:67a4/127"));
        assertTrue(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a5", "00:6b8:0892:ad61:59a2:3149:c5a0:67a0/124"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8::59a2:3149:c5a0:67a4", "2a02:6b8:0:0:59a2:3149:c5a0:67a4/64"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0892:ad61:59:0::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0892:ad61:59:0:1:2", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:089f:ad61::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:089f:ad61:1:2::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:089f:ad61:1:2::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12"));
        assertTrue(NetworkUtil.isIpInSubnet("2a0f:1:2:4::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12"));
        assertTrue(NetworkUtil.isIpInSubnet("2a0f:1:2:4::", "2a02:6b8::/12"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0:0::", "2a02:6b8::"));
        assertTrue(NetworkUtil.isIpInSubnet("2a02:6b8:0::", "2a02:6b8::"));
        assertTrue(NetworkUtil.isIpInSubnet("1::", "2a02:6b8::/1"));
        assertTrue(NetworkUtil.isIpInSubnet("::", "::/64"));
        assertTrue(NetworkUtil.isIpInSubnet("::5:6:7:8", "0000:0000::/64"));
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
        assertFalse(NetworkUtil.isIpInSubnet("127.0.0.255", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
    }

    @Test
    public void isIpInSubnet_Ipv6InSubnet_false() {
        assertFalse(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a8", "00:6b8:0892:ad61:59a2:3149:c5a0:67a0/125"));
        assertFalse(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a4", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertFalse(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a4", "00:6b8:0892:ad61:59a2:3149:c5a0:67a5"));
        assertFalse(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a5", "00:6b8:0892:ad61:59a2:3149:c5a0:67a4"));
        assertFalse(NetworkUtil.isIpInSubnet("00:6b8:0892:ad61:59a2:3149:c5a0:67a4", "192.0.0.0/24"));
        assertFalse(NetworkUtil.isIpInSubnet("2a02:6b8:0892:555:59:0::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertFalse(NetworkUtil.isIpInSubnet("::59:0:1:2", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64"));
        assertFalse(NetworkUtil.isIpInSubnet("2a02:6b8:099f:ad61::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertFalse(NetworkUtil.isIpInSubnet("2a02:6b8:079f:ad61:1:2::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertFalse(NetworkUtil.isIpInSubnet("21:6b8:089f::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/40"));
        assertFalse(NetworkUtil.isIpInSubnet("2a13::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12"));
        assertFalse(NetworkUtil.isIpInSubnet("2a4e:1:2:4::", "2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12"));
        assertFalse(NetworkUtil.isIpInSubnet("1b0f:1:2:4::", "2a02:6b8::/12"));
        assertFalse(NetworkUtil.isIpInSubnet("2a02:6b8:0:1::", "2a02:6b8::"));
        assertFalse(NetworkUtil.isIpInSubnet("2a02:6b9:0::", "2a02:6b8::"));
        assertFalse(NetworkUtil.isIpInSubnet("::", "fa02:6b8::/1"));
        assertFalse(NetworkUtil.isIpInSubnet("0:0:1::", "::/64"));
        assertFalse(NetworkUtil.isIpInSubnet("::1:5:6:7:8", "0000:0000::/64"));
    }


    @Test
    public void requestAddressInSubnet_incorrectAddress_exceptionThrown() {
        List<String> ips = new ListBuilder<String>()
                .add("first")
                .add("192.0.0.0")
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
                .add("test")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:gg")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0::")
                .add("")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0::")
                .add("-1abc::")
                .add("")
                .add("2a02:6b8::ad61:59a2:3149:c5a0::")
                .add("2a02:6b8:11111:ad61:59a2:3149:c5a0::")
                .build();

        List<String> subnets = new ListBuilder<String>()
                .add("second")
                .add("192.0.0.0/ ")
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
                .add("test")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/64")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/78")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/-1")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/4")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12")
                .add("2a02:6b8:0892:ad61:59a2:3149:c5a0:67a4/12")
                .build();

        for (int i = 0; i < subnets.size(); i++) {
            try {
                NetworkUtil.isIpInSubnet(ips.get(i), subnets.get(i));
                fail(String.format("Expected IllegalArgumentException on ip='%s' and subnet='%s'", ips.get(i), subnets.get(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void testGetWorkstationIp() {
        String workstationIp = NetworkUtil.getWorkstationIp();
        assertNotNull(workstationIp);
        assertFalse(StringUtils.isBlank(workstationIp));
        assertEquals(3, StringUtils.countMatches(workstationIp, '.'));
    }

    @Test
    public void testGetEncryptedWorkstationIp() {
        String encryptedWorkstationIp = NetworkUtil.getEncryptedWorkstationIp();
        assertNotNull(encryptedWorkstationIp);
        assertFalse(StringUtils.isBlank(encryptedWorkstationIp));
        assertTrue(encryptedWorkstationIp.length() >= 4);
    }
}
