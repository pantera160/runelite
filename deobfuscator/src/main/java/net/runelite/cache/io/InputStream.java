/*
 * Copyright (c) 2016, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by Adam <Adam@sigterm.info>
 * 4. Neither the name of the Adam <Adam@sigterm.info> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Adam <Adam@sigterm.info> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Adam <Adam@sigterm.info> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.cache.io;

import net.runelite.cache.io.Stream;

public final class InputStream extends Stream {
   private static final int[] BIT_MASK = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, '\uffff', 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, Integer.MAX_VALUE, -1};

   public void initBitAccess() {
      this.bitPosition = this.offset * 8;
   }

   public void finishBitAccess() {
      this.offset = (7 + this.bitPosition) / 8;
   }

   public int readBits(int bitOffset) {
      int bytePos = this.bitPosition >> 1779819011;
      int i_8_ = -(7 & this.bitPosition) + 8;
      this.bitPosition += bitOffset;

      int value;
      for(value = 0; ~bitOffset < ~i_8_; i_8_ = 8) {
         value += (BIT_MASK[i_8_] & this.buffer[bytePos++]) << -i_8_ + bitOffset;
         bitOffset -= i_8_;
      }

      if(~i_8_ == ~bitOffset) {
         value += this.buffer[bytePos] & BIT_MASK[i_8_];
      } else {
         value += this.buffer[bytePos] >> -bitOffset + i_8_ & BIT_MASK[bitOffset];
      }

      return value;
   }

   public InputStream(int capacity) {
      this.buffer = new byte[capacity];
   }

   public InputStream(byte[] buffer) {
      this.buffer = buffer;
      this.length = buffer.length;
   }

   public void checkCapacity(int length) {
      if(this.offset + length >= this.buffer.length) {
         byte[] newBuffer = new byte[(this.offset + length) * 2];
         System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
         this.buffer = newBuffer;
      }

   }

   public int read24BitInt() {
      return (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public void skip(int length) {
      this.offset += length;
   }

   public void setLength(int length) {
      this.length = length;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public int getRemaining() {
      return this.offset < this.length?this.length - this.offset:0;
   }

   public void addBytes(byte[] b, int offset, int length) {
      this.checkCapacity(length - offset);
      System.arraycopy(b, offset, this.buffer, this.offset, length);
      this.length += length - offset;
   }

   public int readPacket() {
      return this.readUnsignedByte();
   }

   public int readByte() {
      return this.getRemaining() > 0?this.buffer[this.offset++]:0;
   }

   public void readBytes(byte[] buffer, int off, int len) {
      for(int k = off; k < len + off; ++k) {
         buffer[k] = (byte)this.readByte();
      }

   }

   public void readBytes(byte[] buffer) {
      this.readBytes(buffer, 0, buffer.length);
   }

   public int readSmart2() {
      int i = 0;

      int i_33_;
      for(i_33_ = this.readUnsignedSmart(); ~i_33_ == -32768; i += 32767) {
         i_33_ = this.readUnsignedSmart();
      }

      i += i_33_;
      return i;
   }

   public int readUnsignedByte() {
      return this.readByte() & 255;
   }

   public int readByte128() {
      return (byte)(this.readByte() - 128);
   }

   public int readByteC() {
      return (byte)(-this.readByte());
   }

   public int read128Byte() {
      return (byte)(128 - this.readByte());
   }

   public int readUnsignedByte128() {
      return this.readUnsignedByte() - 128 & 255;
   }

   public int readUnsignedByteC() {
      return -this.readUnsignedByte() & 255;
   }

   public int readUnsigned128Byte() {
      return 128 - this.readUnsignedByte() & 255;
   }

   public int readShortLE() {
      int i = this.readUnsignedByte() + (this.readUnsignedByte() << 8);
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int readShort128() {
      int i = (this.readUnsignedByte() << 8) + (this.readByte() - 128 & 255);
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int readShortLE128() {
      int i = (this.readByte() - 128 & 255) + (this.readUnsignedByte() << 8);
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int read128ShortLE() {
      int i = (128 - this.readByte() & 255) + (this.readUnsignedByte() << 8);
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int readShort() {
      int i = (this.readUnsignedByte() << 8) + this.readUnsignedByte();
      if(i > 32767) {
         i -= 65536;
      }

      return i;
   }

   public int readUnsignedShortLE() {
      return this.readUnsignedByte() + (this.readUnsignedByte() << 8);
   }

   public int readUnsignedShort() {
      return (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public int readUnsignedShort128() {
      return (this.readUnsignedByte() << 8) + (this.readByte() - 128 & 255);
   }

   public int readUnsignedShortLE128() {
      return (this.readByte() - 128 & 255) + (this.readUnsignedByte() << 8);
   }

   public int readInt() {
      return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
   }

   public int readIntV1() {
      return (this.readUnsignedByte() << 8) + this.readUnsignedByte() + (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16);
   }

   public int readIntV2() {
      return (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 24) + this.readUnsignedByte() + (this.readUnsignedByte() << 8);
   }

   public int readIntLE() {
      return this.readUnsignedByte() + (this.readUnsignedByte() << 8) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 24);
   }

   public long readLong() {
      long l = (long)this.readInt() & 4294967295L;
      long l1 = (long)this.readInt() & 4294967295L;
      return (l << 32) + l1;
   }

   public String readString() {
      String s;
      int b;
      for(s = ""; (b = this.readByte()) != 0; s = s + (char)b) {
         ;
      }

      return s;
   }

   public String readJagString() {
      this.readByte();

      String s;
      int b;
      for(s = ""; (b = this.readByte()) != 0; s = s + (char)b) {
         ;
      }

      return s;
   }

   public int readBigSmart() {
      return this.buffer[this.offset] >= 0?this.readUnsignedShort():Integer.MAX_VALUE & this.readInt();
   }

   public int readUnsignedSmart() {
      int i = 255 & this.buffer[this.offset];
      return i >= 128?-32768 + this.readUnsignedShort():this.readUnsignedByte();
   }
}
