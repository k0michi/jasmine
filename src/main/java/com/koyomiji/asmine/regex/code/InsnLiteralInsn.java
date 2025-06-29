package com.koyomiji.asmine.regex.code;

import com.koyomiji.asmine.common.ArrayListHelper;
import com.koyomiji.asmine.regex.AbstractRegexInsn;
import com.koyomiji.asmine.regex.RegexProcessor;
import com.koyomiji.asmine.regex.RegexThread;
import com.koyomiji.asmine.tree.AbstractInsnNodeHelper;
import com.koyomiji.asmine.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.List;

public class InsnLiteralInsn extends AbstractRegexInsn {
  public AbstractInsnNode literal;

  public InsnLiteralInsn(AbstractInsnNode literal) {
    this.literal = literal;
  }

  @Override
  public List<RegexThread> execute(RegexProcessor processor, RegexThread thread) {
    CodeRegexProcessor codeProcessor = (CodeRegexProcessor) processor;

    if (!codeProcessor.compareCurrentCharToLiteral(literal)) {
      return ArrayListHelper.of();
    }

    thread.advanceProgramCounter();
    return ArrayListHelper.of(thread);
  }

  @Override
  public int getExecutionType() {
    return CONSUMING;
  }
}
