package com.codeforces.commons.network;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Vladislav Bandurin (vbandurin7@gmail.com)
 * Date: 31.08.2023
 */
public class NetworkUtil {
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    public static boolean isIpInSubnet(String ip, String subnet) {
        if (ip == null || subnet == null) {
            throw new IllegalArgumentException(
                    "Expected not null ip and subnet, got ip=" + ip + " subnet=" + subnet + "instead"
            );
        }
        int mask, ipAsInt, subnetAddressInt;
        String[] subnetAndPrefixLength = subnet.split("/");
        if (subnetAndPrefixLength.length == 1) {
            validateIpAndSubnet(ip, subnet);
            return ip.equals(subnet);
        }

        if (subnetAndPrefixLength.length != 2) {
            throw new IllegalArgumentException("Invalid format of subnet");
        }

        try {
            int prefixLength = Integer.parseInt(subnetAndPrefixLength[1]);
            if (prefixLength <= 0) {
                throw new IllegalArgumentException("Prefix length must be positive, got " + prefixLength);
            }
            validateIpAndSubnet(ip, subnetAndPrefixLength[0]);
            mask = (0xFFFFFFFF << (32 - prefixLength));

            ipAsInt = getIpAsInt(ip);
            subnetAddressInt = getIpAsInt(subnetAndPrefixLength[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse subnet prefix length", e);
        }

        return (ipAsInt & mask) == (subnetAddressInt & mask);
    }

    private static int getIpAsInt(String ip) {
        int[] ipParts = Arrays.stream(DOT_PATTERN.split(ip)).mapToInt(Integer::parseInt).toArray();
        return (ipParts[0] << 24) + (ipParts[1] << 16) + (ipParts[2] << 8) + ipParts[3];
    }

    private static void validateIpAndSubnet(String ip, String subnet) {
        InetAddressValidator addressValidator = InetAddressValidator.getInstance();
        if (!addressValidator.isValidInet4Address(ip)) {
            throw new IllegalArgumentException("Invalid format of ip: " + ip);
        }
        if (!addressValidator.isValidInet4Address(subnet)) {
            throw new IllegalArgumentException("Invalid format of subnet: " + subnet);
        }
    }
}
