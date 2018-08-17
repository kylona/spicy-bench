/******************************************************************************

Copyright (c) 2016, Cormac Flanagan (University of California, Santa Cruz)
                    and Stephen Freund (Williams College) 

All rights reserved.  

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

 * Neither the names of the University of California, Santa Cruz
      and Williams College nor the names of its contributors may be
      used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 ******************************************************************************/

package fasttrack.util;

import extensions.fasttrack.FastTrackTool;

public final class Epoch {

	/*
	 * We use a variable number of bits for ids, based on the maxTid configured
	 * on the command line.
	 */
	public static final int TID_BITS =
    Integer.SIZE - Integer.numberOfLeadingZeros(FastTrackTool.MAX_TID);

	static {
    //Epochs can only have 1-16 bits for tids, check 0 < maxTid <= 2^16
		assert (TID_BITS > 0 && TID_BITS <= 16); 
	}

	public static final int CLOCK_BITS = Integer.SIZE - TID_BITS;
	public static final int MAX_CLOCK = ( ((int)1) << CLOCK_BITS) - 1;
	public static final int MAX_TID = (1 << TID_BITS) - 1;

	public static final int ZERO = 0;
	public static final int READ_SHARED = -1;

	public static int tid(int epoch) {
		return (int)(epoch >>> CLOCK_BITS);
	}

	public static int clock(int epoch) {
		return epoch & MAX_CLOCK;
	}

	// clock should not have a tid -- it should be a raw clock value.
	public static int make(int tid, int clock) {
		return (((int)tid) << CLOCK_BITS) | clock;
	}


	public static int tick(int epoch) {
    //Check for epoch clock overflow
		assert (clock(epoch) <= MAX_CLOCK - 1);
		return epoch + 1;
	}
	
	public static int tick(int epoch, int amount) {
    //Check for epoch clock overflow
		assert (clock(epoch) <= MAX_CLOCK - amount);
		return epoch + amount;
	}
	
	
	public static boolean leq(int e1, int e2) {
		// assert (tid(e1) == tid(e2));
		return clock(e1) <= clock(e2);
	}

	public static int max(int e1, int e2) {
		// assert (tid(e1) == tid(e2));
		return Epoch.make(tid(e1), Math.max(clock(e1), clock(e2)));
	}

	public static String toString(int epoch) {
		if (epoch == READ_SHARED) {
			return "SHARED";
		} else {
			return String.format("(%d:%X)", tid(epoch), clock(epoch));
		}
	}

	public static void main(String args[]) {
		{
			int e = make(3,11);
			System.out.println(toString(e));
		}
		{
			int e = make(MAX_TID-1,11);
			System.out.println(toString(e));
			System.out.println(toString(tick(e)));
		}
		{
			int e = make(MAX_TID+1,11);
			System.out.println(toString(e));
		}
	}

}
