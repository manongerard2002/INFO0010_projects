/* (PD) 2006 The Bitzi Corporation
 *
 * 1. Authorship. This work and others bearing the above
 * label were created by, or on behalf of, the Bitzi
 * Corporation. Often other public domain material by
 * other authors is incorporated; this should be clear
 * from notations in the source code. If other non-
 * public-domain code or libraries are included, this is
 * is done under those works' respective licenses.
 *
 * 2. Release. The Bitzi Corporation places its portion
 * of these labelled works into the public domain,
 * disclaiming all rights granted us by copyright law.
 *
 * Bitzi places no restrictions on your freedom to copy,
 * use, redistribute and modify this work, though you
 * should be aware of points (3), (4), and (5) below.
 *
 * 3. Trademark Advisory. The Bitzi Corporation reserves
 * all rights with regard to any of its trademarks which
 * may appear herein, such as "Bitzi", "Bitcollider", or
 * "Bitpedia". Please take care that your uses of this
 * work do not infringe on our trademarks or imply our
 * endorsement. For example, you should change labels
 * and identifier strings in your derivative works where
 * appropriate.
 *
 * 4. Licensed portions. Some code and libraries may be
 * incorporated in this work in accordance with the
 * licenses offered by their respective rightsholders.
 * Further copying, use, redistribution and modification
 * of these third-party portions remains subject to
 * their original licenses.
 *
 * 5. Disclaimer. THIS SOFTWARE IS PROVIDED BY THE AUTHOR
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Please see http://bitzi.com/publicdomain or write
 * info@bitzi.com for more info.
 *
 * $Id: Base32.java,v 1.2 2006/07/14 04:58:39 gojomo Exp $
 */

/**
 * Base32 - encodes and decodes RFC3548 Base32
 * (see http://www.faqs.org/rfcs/rfc3548.html )
 *
 * @author Robert Kaye
 * @author Gordon Mohr
 */
public class Base32 {
    private static final String base32Chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] base32Lookup =
            { 0xFF,0xFF,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F,
                    0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
                    0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06,
                    0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,
                    0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,
                    0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF,
                    0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06,
                    0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,
                    0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,
                    0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF
            };

    /**
     * Encodes byte array to Base32 String.
     *
     * @param bytes Bytes to encode.
     * @return Encoded byte array <code>bytes</code> as a String.
     *
     */
    static public String encode(final byte[] bytes) {
        int i = 0, index = 0, digit = 0;
        int currByte, nextByte;
        StringBuffer base32
                = new StringBuffer((bytes.length + 7) * 8 / 5);

        while (i < bytes.length) {
            currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256);

            /* Is the current digit going to span a byte boundary? */
            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    nextByte = (bytes[i + 1] >= 0)
                            ? bytes[i + 1] : (bytes[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0)
                    i++;
            }
            base32.append(base32Chars.charAt(digit));
        }

        return base32.toString();
    }

    /**
     * Decodes the given Base32 String to a raw byte array.
     *
     * @param base32
     * @return Decoded <code>base32</code> String as a raw byte array.
     */
    static public byte[] decode(final String base32) {
        int i, index, lookup, offset, digit;
        byte[] bytes = new byte[base32.length() * 5 / 8];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - '0';

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= base32Lookup.length) {
                continue;
            }

            digit = base32Lookup[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length)
                        break;
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }
}