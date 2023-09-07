package com.codeforces.commons.network;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Vladislav Bandurin (vbandurin7@gmail.com)
 * Date: 31.08.2023
 */
public class NetworkUtil {
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final InetAddressValidator ADDRESS_VALIDATOR = InetAddressValidator.getInstance();

    public static boolean isIpInSubnet(String ip, String subnet) {
        if (ip == null || subnet == null) {
            throw new IllegalArgumentException("Expected not null ip and subnet, got ip=" + ip + " subnet=" + subnet + "instead");
        }

        String[] subnetAndPrefixLength = subnet.split("/");
        int slashCount = StringUtils.countMatches(subnet, '/');

        boolean isValidSubnetFormat = subnetAndPrefixLength.length == 2 && slashCount == 1
                || subnetAndPrefixLength.length == 1 && slashCount == 0;

        if (isValidSubnetFormat && ADDRESS_VALIDATOR.isValidInet4Address(ip)
                && ADDRESS_VALIDATOR.isValidInet4Address(subnetAndPrefixLength[0])) {
            return isIpInSubnetV4(ip, subnetAndPrefixLength);
        } else if (isValidSubnetFormat && ADDRESS_VALIDATOR.isValidInet6Address(ip)
                && ADDRESS_VALIDATOR.isValidInet6Address(subnetAndPrefixLength[0])) {
            return isIpInSubnetV6(ip, subnetAndPrefixLength);
        } else if (isValidSubnetFormat &&
                (ADDRESS_VALIDATOR.isValidInet6Address(ip) && ADDRESS_VALIDATOR.isValidInet4Address(subnetAndPrefixLength[0]) ||
                        ADDRESS_VALIDATOR.isValidInet4Address(ip) && ADDRESS_VALIDATOR.isValidInet6Address(subnetAndPrefixLength[0]))) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid format of arguments: ip=" + ip + " subnet=" + subnet);
        }
    }

    private static boolean isIpInSubnetV6(String ip, String[] subnetAndPrefixLength) {
        BigInteger mask, ipAsBigInteger, subnetAsBigInteger;
        try {
            int prefixLength = subnetAndPrefixLength.length == 1 ? 128 : Integer.parseInt(subnetAndPrefixLength[1]);
            if (prefixLength <= 0 || prefixLength > 128) {
                throw new IllegalArgumentException("Subnet prefix length must be in range from 1 to 64");
            }

            mask = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE)
                    .shiftRight(128 - prefixLength).shiftLeft(128 - prefixLength);

            ipAsBigInteger = getIpV6AsBigInteger(getCanonicalIpV6(ip));
            subnetAsBigInteger = getIpV6AsBigInteger(getCanonicalIpV6(subnetAndPrefixLength[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse subnet prefix length", e);
        }
        return (ipAsBigInteger.and(mask)).equals(subnetAsBigInteger.and(mask));
    }

    private static String getCanonicalIpV6(String ip) {
        if (ip.contains("::")) {
            int sepCount = StringUtils.countMatches(ip, ':');

            int repeatCount = 8 - sepCount;
            if (ip.charAt(0) == ':') {
                repeatCount++;
            }
            if (ip.charAt(ip.length() - 1) == ':') {
                repeatCount++;
            }

            String replacement = Strings.repeat(":0000", repeatCount) + ":";
            ip = ip.replace("::", replacement).replaceAll("^:+|:+$", "");
        }

        return ip;
    }

    private static BigInteger getIpV6AsBigInteger(String ip) {
        try {
            long[] ipParts = Arrays.stream(ip.split(":")).mapToLong(n -> Long.parseLong(n, 16)).toArray();
            BigInteger result = BigInteger.ZERO;
            for (long ipPart : ipParts) {
                if (ipPart < 0 || ipPart > 65535) {
                    throw new RuntimeException("Invalid format of IpV6: " + ip);
                }
                result = result.shiftLeft(16).add(BigInteger.valueOf(ipPart));
            }
            return result;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format of IpV6: " + ip, e);
        }
    }

    private static boolean isIpInSubnetV4(String ip, String[] subnetAndPrefixLength) {
        if (subnetAndPrefixLength.length == 1) {
            return ip.equals(subnetAndPrefixLength[0]);
        }

        if (subnetAndPrefixLength.length != 2) {
            throw new IllegalArgumentException("Invalid format of subnet");
        }

        int mask, ipAsInt, subnetAddressInt;
        try {
            int prefixLength = Integer.parseInt(subnetAndPrefixLength[1]);
            if (prefixLength <= 0 || prefixLength > 32) {
                throw new IllegalArgumentException("Prefix length must be between 0 and 32, got " + prefixLength);
            }
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
}
