package com.codeforces.commons.network;

import com.codeforces.commons.text.Patterns;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.math.BigInteger;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @author Vladislav Bandurin (vbandurin7@gmail.com)
 * Date: 31.08.2023
 */
public class NetworkUtil {
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final InetAddressValidator ADDRESS_VALIDATOR = InetAddressValidator.getInstance();

    private static final Pattern ADDRESS_SPLIT_PATTERN = Patterns.DOT_PATTERN;

    private static volatile boolean workstationIpCached;
    private static String workstationIp;

    private static volatile boolean encryptedWorkstationIpCached;
    private static String encryptedWorkstationIp;

    public static String getWorkstationIp() {
        if (workstationIpCached) {
            return workstationIp;
        }

        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.getDisplayName().toLowerCase().contains("virt")
                        || networkInterface.getDisplayName().toLowerCase().contains("vm")) {
                    continue;
                }

                String result = null;
                int count = 0;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress == null || interfaceAddress.getAddress() == null
                            || interfaceAddress.getAddress().isLoopbackAddress()
                            || interfaceAddress.getAddress().isAnyLocalAddress()) {
                        continue;
                    }

                    if (ADDRESS_SPLIT_PATTERN.split(interfaceAddress.getAddress().getHostAddress()).length == 4) {
                        result = interfaceAddress.getAddress().getHostAddress();
                        ++count;
                    }
                }

                if (count == 1) {
                    workstationIp = result;
                    workstationIpCached = true;
                    return workstationIp;
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }

        workstationIp = "127.0.0.1";
        workstationIpCached = true;

        return workstationIp;
    }

    public static String getEncryptedWorkstationIp() {
        if (encryptedWorkstationIpCached) {
            return encryptedWorkstationIp;
        }

        String ip = getWorkstationIp();
        StringBuilder result = new StringBuilder();

        if (StringUtils.isNotBlank(ip)) {
            for (int i = 0; i < ip.length(); i++) {
                if (Character.isDigit(ip.charAt(i))) {
                    char c = (char) (ip.charAt(i) + 1);
                    if (c > '9') {
                        c = '0';
                    }
                    result.append(c);
                }
            }
        }

        encryptedWorkstationIp = result.toString();
        encryptedWorkstationIpCached = true;
        return encryptedWorkstationIp;
    }

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

            ipAsInt = getIpV4AsInt(ip);
            subnetAddressInt = getIpV4AsInt(subnetAndPrefixLength[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse subnet prefix length", e);
        }

        return (ipAsInt & mask) == (subnetAddressInt & mask);
    }

    private static int getIpV4AsInt(String ip) {
        int[] ipParts = Arrays.stream(DOT_PATTERN.split(ip)).mapToInt(Integer::parseInt).toArray();
        return (ipParts[0] << 24) + (ipParts[1] << 16) + (ipParts[2] << 8) + ipParts[3];
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
}
