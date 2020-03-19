package com.ledger.u2f;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Atomic, Increment-only counter.
 *
 * This class implements an unsigned 24-bit counter that always counts up
 * and cannot be decremented. Once it reaches the value 0x00FFFFFF, all
 * future operations throw ISOException with a value of
 * ISO7816.SW_FILE_FULL.
 *
 * Some mild wear-leveling is implemented, which should extend the
 * lifetime of single-byte-addressable EEPROM secure elements. On
 * page-based flash systems it should make no difference.
 *
 * The maximum number of increments is 2^24, or more than 16 million. If
 * you authenticated 1000 times a day, this will last over 43 years,
 * which is longer than the guaranteed read lifetime of most of these
 * devices. This is more than enough for practically all security token
 * use cases.
 *
 * As a bonus, the system is atomic without the use of transactions. This
 * speeds up increment operations considerably on older hardware.
 *
 * This implementation ensures that no byte is overwritten more than
 * 131,072 times over the course of 2^24 increment operations.
 *
 * Once the counter has reached the maximum value (0xFFFFFF), then all
 * future increment operations will throw an exception. However, the
 * value may still be read.
 */
public class Counter {
    private final static byte SLOT_MASK = 0x7F;
    private final static short SLOT_COUNT = 128;

    private short ms;
    private byte[] ls;

    Counter() {
        ms = 0;
        ls = new byte [SLOT_COUNT];
    }

    private short getSlot() {
        return (short)(ms & SLOT_MASK);
    }

    /**
     * Atomically increments the counter by 1.
     *
     * If that is not possible because it would cause the counter
     * to overflow, an ISOException is thrown with a value of
     * ISO7816.SW_FILE_FULL.
     */
    public void inc() {
        short slot = getSlot();

        if (ls[slot] == (byte)0xFF) {
            // The value of our slot is about to roll over.
            short next_ms = (short)(ms + 1);

            if (next_ms == (short)0) {
                // ms would roll over if we incremented.
                // Throw an exception instead.
                ISOException.throwIt(ISO7816.SW_FILE_FULL);
            }

            // Set our upcoming slot to zero. Note that if we
            // tear immediately after this line then we haven't
            // actually changed the count yet, this is just a
            // prep step.
            ls[(short)(next_ms & SLOT_MASK)] = 0;

            // Increment the most significant two bytes of our
            // counter. Since the JCRE ensures that all assignments
            // are atomic, our entire operation is assured to be
            // atomic---assuming there is no command reordering
            // going on (and there shouldn't be on a secure element).
            ms = next_ms;

        } else {
            // We are not going to roll over, just increment.
            ls[slot]++;
        }
    }

    /**
     * Writes the value as a 32-bit number to the destination,
     * in big endian order.
     */
    public short writeValue(byte[] dest, short destOffset) {
        // Since we are a 24-bit counter, the first byte is
        // always zero.
        dest[destOffset++] = 0;

        // Write out the most significant two bytes.
        destOffset = Util.setShort(dest, destOffset, ms);

        // The value of the last byte comes from the
        // current slot.
        dest[destOffset++] = ls[getSlot()];

        return destOffset;
    }
}
