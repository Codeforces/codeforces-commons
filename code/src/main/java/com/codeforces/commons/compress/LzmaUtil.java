package com.codeforces.commons.compress;

import com.codeforces.commons.io.ByteArrayOutputStream;
import lzma.sdk.lzma.Decoder;
import lzma.sdk.lzma.Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mike Mirzayanov
 */
public class LzmaUtil {
    private LzmaUtil() {
        throw new UnsupportedOperationException();
    }

    public static byte[] compress(byte[] plainBytes) throws IOException {
        InputStream in = new ByteArrayInputStream(plainBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Encoder encoder = new Encoder();

        encoder.setDictionarySize(1 << 23);
        encoder.setEndMarkerMode(true);
        encoder.setMatchFinder(Encoder.EMatchFinderTypeBT4);
        encoder.setNumFastBytes(0x20);

        encoder.writeCoderProperties(out);
        long fileSize = plainBytes.length;
        for (int i = 0; i < 8; ++i) {
            //noinspection NumericCastThatLosesPrecision
            out.write((int) (fileSize >>> (8 * i)) & 0xFF);
        }

        encoder.code(in, out, -1, -1, null);

        in.close();

        return out.toByteArray();
    }

    public static byte[] decompress(byte[] compressedBytes) throws IOException {
        InputStream in = new ByteArrayInputStream(compressedBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int propertiesSize = 5;
        byte[] properties = new byte[propertiesSize];
        if (in.read(properties, 0, propertiesSize) != propertiesSize) {
            throw new IOException("LZMA-input is too short.");
        }

        Decoder decoder = new Decoder();
        if (!decoder.setDecoderProperties(properties)) {
            throw new IOException("Incorrect stream properties.");
        }

        long outSize = 0;
        for (int i = 0; i < 8; i++) {
            int v = in.read();
            if (v < 0) {
                throw new IOException("Can't read stream size.");
            }
            //noinspection IntegerMultiplicationImplicitCastToLong
            outSize |= (long) v << (8 * i);
        }

        if (!decoder.code(in, out, outSize)) {
            throw new IOException("Error in data stream.");
        }

        in.close();

        return out.toByteArray();
    }
}
