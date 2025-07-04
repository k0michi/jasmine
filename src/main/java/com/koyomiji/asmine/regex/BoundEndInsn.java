package com.koyomiji.asmine.regex;

import com.koyomiji.asmine.common.ArrayListHelper;
import com.koyomiji.asmine.tuple.Pair;

import java.util.List;

public class BoundEndInsn extends AbstractRegexInsn {
  @Override
  public List<RegexThread> execute(RegexProcessor processor, RegexThread thread) {
    if (thread.stackSize() == 0) {
      return ArrayListHelper.of();
    }

    Pair<Integer, Integer> range = Pair.of((Integer) thread.pop(), processor.getStringPointer());
    Object key = thread.pop();
    Pair<Integer, Integer> bound = thread.getScopedBound(key);

    if(bound != null && processor.compareSubstrings(bound, range)) {
      // Succeeded to match the previous appearance
      thread.advanceProgramCounter();
      return ArrayListHelper.of(thread);
    } else {
      // Failed to match
      return ArrayListHelper.of();
    }
  }

  @Override
  public int getExecutionType() {
    return TRANSITIVE;
  }
}
