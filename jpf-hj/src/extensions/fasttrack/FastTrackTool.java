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


package extensions.fasttrack;

import extensions.fasttrack.util.Epoch;
import extensions.fasttrack.util.VectorClock;
import extensions.util.StructuredParallelRaceDetectorTool;
import gov.nasa.jpf.vm.bytecode.ReadOrWriteInstruction;

import java.util.HashMap;
import java.util.Map;

/*
 * Simple implementation of FastTrack
 * TODO verify that class init times do not need special handling
 * TODO verify object refs work even with static fields
 */
public class FastTrackTool implements StructuredParallelRaceDetectorTool {

  public static final int MAX_TID = 10000;
	private static final int INIT_VECTOR_CLOCK_SIZE = 4;

  Map<Integer, FTThreadState> C = new HashMap<>();
  FTLockState L = new FTLockState(INIT_VECTOR_CLOCK_SIZE);
  Map<Integer, FTVarState> X = new HashMap<>();
  boolean race = false;
  String error = "";

  private static class FastTrackToolState {
    final Map<Integer, FTThreadState> C = new HashMap<>();
    final FTLockState L = new FTLockState(INIT_VECTOR_CLOCK_SIZE);
    final Map<Integer, FTVarState> X = new HashMap<>();
    public FastTrackToolState(Map<Integer, FTThreadState> c, FTLockState l, Map<Integer, FTVarState> x) {
      for (Map.Entry<Integer, FTThreadState> threadState : c.entrySet()) {
        C.put(threadState.getKey(), new FTThreadState(threadState.getValue()));
      }
      L.copy(l);
      for (Map.Entry<Integer, FTVarState> varState : x.entrySet()) {
        X.put(varState.getKey(), new FTVarState(varState.getValue()));
      }
    }
  }

  @Override
  public void resetState(Object state) {
    FastTrackToolState toolState = (FastTrackToolState)state;
    C = toolState.C;
    L = toolState.L;
    X = toolState.X;
  }

  @Override
  public Object getImmutableState() {
    return new FastTrackToolState(C, L, X);
  }

  @Override
  public void handleRead(int tid, int objRef) {
    FTVarState sx = getVarState(objRef, false);
		final int e = getThreadState(tid).get(tid);

    final int r = sx.R;
    if (r == e) {
      // read same epoch
      return;
    } else if (r == Epoch.READ_SHARED && sx.get(tid) == e) {
      // read same epoch
      return;
    }

    final VectorClock tV = getThreadState(tid);
    final int w = sx.W;
    final int wTid = Epoch.tid(w);
    
    if (wTid != tid && !Epoch.leq(w, tV.get(wTid))) {
      reportRace("Write-Read race by " + wTid + " and " + tid);
    } 

    if (r != Epoch.READ_SHARED) {
      final int rTid = Epoch.tid(r);
      if (rTid == tid || Epoch.leq(r, tV.get(rTid))) {
        sx.R = e;
      } else {
        int initSize = Math.max(Math.max(rTid,tid), INIT_VECTOR_CLOCK_SIZE); 
        sx.makeCV(initSize);
        sx.set(rTid, r);
        sx.set(tid, e);
        sx.R = Epoch.READ_SHARED;
      }
    } else {
      sx.set(tid, e);					
    }
  }

  @Override
  public void handleWrite(int tid, int objRef) {
    FTVarState sx = getVarState(objRef, true);
    final int e = getThreadState(tid).get(tid);

    //write same epoch
    final int w = sx.W;
    if (w == e) {
      return;
    }

    final int wTid = Epoch.tid(w);				
    final VectorClock tV = getThreadState(tid);

    if (wTid != tid && !Epoch.leq(w, tV.get(wTid))) {
      reportRace("Write-Write race by " + wTid + " and " + tid);
    }
    
    final int r = sx.R;				
    if (r != Epoch.READ_SHARED) {
      final int rTid = Epoch.tid(r);
      if (rTid != tid && !Epoch.leq(r, tV.get(rTid))) {
        reportRace("Read-Write race by " + rTid + " and " + tid);
      }
    } else {
      // slow check
      if (sx.anyGt(tV)) {
        for (int prevReader = sx.nextGt(tV, 0); prevReader > -1; prevReader = sx.nextGt(tV, prevReader + 1)) {
          reportRace("Read-Write race (slow check) by " + prevReader + " and " + tid);
        }
      }
    }
    sx.W = e;
  }

  @Override
  public void handleAcquire(int tid) {
    getThreadState(tid).max(L);
  }

  @Override
  public void handleRelease(int tid) {
		L.max(getThreadState(tid));
    getThreadState(tid).tick(tid);
  }

  @Override
  public void handleFork(int parent, int child) {
    C.put(child, new FTThreadState(getThreadState(parent)));
    getThreadState(parent).tick(parent);
  }

  @Override
  public void handleJoin(int parent, int child) {
    getThreadState(parent).max(getThreadState(child));
  }

  @Override
  public boolean race() {
    return race;
  }

  @Override
  public String error() {
    return race ? error : null;
  }

  void reportRace(String err) {
    race = true;
    error = err;
  }

  FTVarState getVarState(int objRef, boolean isWrite) {
    if (!X.containsKey(objRef)) {
      //TODO verify whether the initial state is correct
      X.put(objRef, new FTVarState(isWrite, Epoch.ZERO));
    }
    return X.get(objRef);
  }

  FTThreadState getThreadState(int tid) {
    if (!C.containsKey(tid)) {
      //TODO verify whether the initial state is correct
      C.put(tid, new FTThreadState(INIT_VECTOR_CLOCK_SIZE));
    }
    return C.get(tid);
  }
}
