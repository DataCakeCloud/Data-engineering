package com.ushareit.dstask.utils;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A globally unique identifier for objects.</p>
 * <p/>
 * <p>Consists of 12 bytes, divided as follows:</p>
 * <table border="1">
 * <caption>ObjectID layout</caption>
 * <tr>
 * <td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td>
 * </tr>
 * <tr>
 * <td colspan="4">time</td><td colspan="3">machine</td> <td colspan="2">pid</td><td colspan="3">inc</td>
 * </tr>
 * </table>
 * <p/>
 * <p>Instances of this class are immutable.</p>
 * @author dongjiejie
 */
@Slf4j
public class ObjectId implements Comparable<ObjectId>, java.io.Serializable {

    private final int time;

    private final int machine;

    private final int inc;

    private boolean isNew;

    private static final int GEN_MACHINE;

    private static AtomicInteger nextInc = new AtomicInteger((new Random()).nextInt());

    private static final long serialVersionUID = -4415279469780082174L;

    /**
     * Create a new object id.
     */
    public ObjectId() {
        time = (int) (System.currentTimeMillis() / 1000);
        machine = GEN_MACHINE;
        inc = nextInc.getAndIncrement();
        isNew = true;
    }

    /**
     * Gets a new object id.
     *
     * @return the new id
     */
    public static ObjectId get() {
        return new ObjectId();
    }

    /**
     * Checks if a string could be an {@code ObjectId}.
     *
     * @param s a potential ObjectId as a String.
     *
     * @return whether the string could be an object id
     *
     * @throws IllegalArgumentException if hexString is null
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }

        final int len = s.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Converts this instance into a 24-byte hexadecimal string representation.
     *
     * @return a string representation of the ObjectId in hexadecimal format
     */
    public String toHexString() {
        final StringBuilder buf = new StringBuilder(24);
        for (final byte b : toByteArray()) {
            buf.append(String.format("%02x", b & 0xff));
        }
        return buf.toString();
    }

    /**
     * Convert to a byte array.  Note that the numbers are stored in big-endian order.
     *
     * @return the byte array
     */
    public byte[] toByteArray() {
        byte b[] = new byte[12];
        ByteBuffer bb = ByteBuffer.wrap(b);
        // by default BB is big endian like we need
        bb.putInt(time);
        bb.putInt(machine);
        bb.putInt(inc);
        return b;
    }

    private int compareUnsigned(int i, int j) {
        long li = 0xFFFFFFFFL;
        li = i & li;
        long lj = 0xFFFFFFFFL;
        lj = j & lj;
        long diff = li - lj;
        if (diff < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (diff > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) diff;
    }

    @Override
    public int compareTo(ObjectId id) {
        if (id == null) {
            return -1;
        }

        int x = compareUnsigned(time, id.time);
        if (x != 0) {
            return x;
        }

        x = compareUnsigned(machine, id.machine);
        if (x != 0) {
            return x;
        }

        return compareUnsigned(inc, id.inc);
    }

    /**
     * Gets the timestamp (number of seconds since the Unix epoch).
     *
     * @return the timestamp
     */
    public int getTimestamp() {
        return time;
    }

    /**
     * Gets the timestamp as a {@code Date} instance.
     *
     * @return the Date
     */
    public Date getDate() {
        return new Date(time * 1000L);
    }

    /**
     * Gets the current value of the auto-incrementing counter.
     *
     * @return the current counter value.
     */
    public static int getCurrentCounter() {
        return nextInc.get();
    }

    static {

        try {
            // build a 2-byte machine piece based on NICs info
            int machinePiece;
            {
                try {
                    StringBuilder sb = new StringBuilder();
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        NetworkInterface ni = e.nextElement();
                        sb.append(ni.toString());
                    }
                    machinePiece = sb.toString().hashCode() << 16;
                } catch (Throwable e) {
                    // exception sometimes happens with IBM JVM, use random
                    machinePiece = (new Random().nextInt()) << 16;
                }
            }

            // add a 2 byte process piece. It must represent not only the JVM but the class loader.
            // Since static var belong to class loader there could be collisions otherwise
            final int processPiece;
            {
                int processId = new Random().nextInt();
                try {
                    processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
                } catch (Throwable t) {
                }

                ClassLoader loader = ObjectId.class.getClassLoader();
                int loaderId = loader != null ? System.identityHashCode(loader) : 0;

                StringBuilder sb = new StringBuilder();
                sb.append(Integer.toHexString(processId));
                sb.append(Integer.toHexString(loaderId));
                processPiece = sb.toString().hashCode() & 0xFFFF;
            }

            GEN_MACHINE = machinePiece | processPiece;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectId that = (ObjectId) o;

        return
            Objects.equal(this.time, that.time) &&
                Objects.equal(this.machine, that.machine) &&
                Objects.equal(this.inc, that.inc) &&
                Objects.equal(this.isNew, that.isNew);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serialVersionUID, time, machine, inc, isNew,
            nextInc, GEN_MACHINE);
    }

}