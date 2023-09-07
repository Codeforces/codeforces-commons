package com.codeforces.commons.network;

import com.codeforces.commons.text.StringUtil;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        long mask, ipSubnetAsLong, subnetAddressAsLong;
        try {
            int prefixLength = subnetAndPrefixLength.length == 1 ? 64 : Integer.parseInt(subnetAndPrefixLength[1]);
            if (prefixLength > 64 || prefixLength <= 0) {
                throw new IllegalArgumentException("Subnet prefix length must be in range from 1 to 64");
            }
            mask = (0xFFFFFFFFFFFFFFFFL << (64 - prefixLength));

            ipSubnetAsLong = getSubnetV6AsLong(getIpV6Subnet(ip));
            subnetAddressAsLong = getSubnetV6AsLong(getIpV6Subnet(subnetAndPrefixLength[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse subnet prefix length", e);
        }
        return (ipSubnetAsLong & mask) == (subnetAddressAsLong & mask);
    }

    private static String getIpV6Subnet(String ip) {
        String[] parts = ip.split("::");
        if (ip.contains("::")) {
            int sepCount = StringUtils.countMatches(ip, ':');
            if (sepCount == 2 && parts.length == 0) {
                return "0:0:0:0:0:0:0:0";
            }
            String replacement;
            if (parts.length >= 1 && StringUtil.isEmpty(parts[0])) {
                replacement = Strings.repeat("0000:", 9 - sepCount);
            } else {
                replacement = Strings.repeat(":0000", 8 - sepCount) + ":";
            }
            ip = ip.replace("::", replacement);
        }
        return Arrays.stream(ip.split(":")).limit(4).collect(Collectors.joining(":"));
    }

    private static long getSubnetV6AsLong(String subnet) {
        try {
            long[] ipParts = Arrays.stream(subnet.split(":")).mapToLong(n -> Long.parseLong(n, 16)).toArray();
            return (ipParts[0] << 48) + (ipParts[1] << 32) + (ipParts[2] << 16) + ipParts[3];
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format of subnet", e);
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
