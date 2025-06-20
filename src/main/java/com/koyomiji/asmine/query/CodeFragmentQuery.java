package com.koyomiji.asmine.query;

import com.koyomiji.asmine.common.ArrayHelper;
import com.koyomiji.asmine.common.ArrayListHelper;
import com.koyomiji.asmine.regex.RegexMatcher;
import com.koyomiji.asmine.regex.code.CodeMatchResult;
import com.koyomiji.asmine.stencil.StencilEvaluationException;
import com.koyomiji.asmine.stencil.insn.AbstractInsnStencil;
import com.koyomiji.asmine.tree.AbstractInsnNodeHelper;
import com.koyomiji.asmine.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeFragmentQuery<T> extends AbstractQuery<T> {
  protected CodeManipulator codeManipulator;
  protected CodeMatchResult matchResult;
  protected Map<Object, List<Pair<CodeCursor, CodeCursor>>> stringBinds;
  protected List<Pair<CodeCursor, CodeCursor>> selected;

  public CodeFragmentQuery(T parent, CodeManipulator codeManipulator, CodeMatchResult matchResult) {
    super(parent);
    this.codeManipulator = codeManipulator;
    this.matchResult = matchResult;
    this.stringBinds = new HashMap<>();
    this.selected = new ArrayList<>();

    if (matchResult != null) {
      for (Map.Entry<Object, List<Pair<Integer, Integer>>> entry : matchResult.getBounds().entrySet()) {
        for (Pair<Integer, Integer> range : entry.getValue()) {
          if (!stringBinds.containsKey(entry.getKey())) {
            stringBinds.put(entry.getKey(), ArrayListHelper.of());
          }

          stringBinds.get(entry.getKey()).add(Pair.of(codeManipulator.getCursor(range.first), codeManipulator.getCursor(range.second)));
        }
      }

      this.selected = stringBinds.get(RegexMatcher.BOUNDARY_KEY);
    }
  }

  public CodeFragmentQuery(T parent, CodeManipulator codeManipulator, CodeMatchResult matchResult, Map<Object, List<Pair<CodeCursor, CodeCursor>>> stringBinds, List<Pair<CodeCursor, CodeCursor>> selected) {
    super(parent);
    this.codeManipulator = codeManipulator;
    this.matchResult = matchResult;
    this.stringBinds = stringBinds;
    this.selected = selected;
  }

  private List<AbstractInsnNode> instantiate(List<AbstractInsnStencil> insns) throws StencilEvaluationException {
    List<AbstractInsnNode> insnList = new ArrayList<>();

    for (AbstractInsnStencil insn : insns) {
      insnList.add(insn.evaluate(matchResult));
    }

    return insnList;
  }

  public CodeFragmentQuery<CodeFragmentQuery<T>> selectBound(Object key) {
    List<Pair<CodeCursor, CodeCursor>> newSelected = new ArrayList<>();

    for (Pair<CodeCursor, CodeCursor> range : stringBinds.get(key)) {
      for (Pair<CodeCursor, CodeCursor> selectedRange : selected) {
        int start = range.first.getFirstIndex();
        int end = range.second.getLastIndex();
        int selectedStart = selectedRange.first.getFirstIndex();
        int selectedEnd = selectedRange.second.getLastIndex();

        if (start >= selectedStart && end <= selectedEnd) {
          newSelected.add(Pair.of(range.first, range.second));
        }
      }
    }

    return new CodeFragmentQuery<>(this, codeManipulator, matchResult, stringBinds, newSelected);
  }

  public CodeFragmentQuery<T> insertBefore(AbstractInsnStencil... insns) {
    return insertBefore(ArrayListHelper.of(insns));
  }

  public CodeFragmentQuery<T> insertBefore(List<AbstractInsnStencil> insns) {
    for (Pair<CodeCursor, CodeCursor> range : selected) {
      Pair<Integer, Integer> indices = codeManipulator.getIndicesForCursors(range);

      if (indices == null) {
        continue;
      }

      try {
        codeManipulator.insertBefore(
                indices.first,
                instantiate(insns)
        );
      } catch (StencilEvaluationException e) {
        throw new RuntimeException(e);
      }
    }

    return this;
  }

  public CodeFragmentQuery<T> insertAfter(AbstractInsnStencil... insns) {
    return insertAfter(ArrayListHelper.of(insns));
  }

  public CodeFragmentQuery<T> insertAfter(List<AbstractInsnStencil> insns) {
    for (Pair<CodeCursor, CodeCursor> range : selected) {
      Pair<Integer, Integer> indices = codeManipulator.getIndicesForCursors(range);

      if (indices == null) {
        continue;
      }

      try {
        codeManipulator.insertAfter(
                indices.second - 1,
                instantiate(insns)
        );
      } catch (StencilEvaluationException e) {
        throw new RuntimeException(e);
      }
    }

    return this;
  }

  public CodeFragmentQuery<T> addFirst(AbstractInsnStencil... insns) {
    return addFirst(ArrayHelper.toList(insns));
  }

  public CodeFragmentQuery<T> addFirst(List<AbstractInsnStencil> insns) {
    try {
      codeManipulator.addFirst(instantiate(insns));
    } catch (StencilEvaluationException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  public CodeFragmentQuery<T> addLast(AbstractInsnStencil... insns) {
    return addLast(ArrayHelper.toList(insns));
  }

  public CodeFragmentQuery<T> addLast(List<AbstractInsnStencil> insns) {
    try {
      codeManipulator.insertBefore(codeManipulator.getMethodNode().instructions.size(), instantiate(insns));
    } catch (StencilEvaluationException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  public CodeFragmentQuery<T> replaceWith(AbstractInsnStencil... insns) {
    return replaceWith(ArrayHelper.toList(insns));
  }

  public CodeFragmentQuery<T> replaceWith(List<AbstractInsnStencil> insns) {
    for (Pair<CodeCursor, CodeCursor> range : selected) {
      Pair<Integer, Integer> indices = codeManipulator.getIndicesForCursors(range);

      if (indices == null) {
        continue;
      }

      try {
        codeManipulator.replace(
                indices.first,
                indices.second,
                instantiate(insns)
        );
      } catch (StencilEvaluationException e) {
        throw new RuntimeException(e);
      }
    }

    return this;
  }

  public CodeFragmentQuery<T> remove() {
    for (Pair<CodeCursor, CodeCursor> range : selected) {
      Pair<Integer, Integer> indices = codeManipulator.getIndicesForCursors(range);

      if (indices == null) {
        continue;
      }

      codeManipulator.remove(
              indices.first,
              indices.second
      );
    }

    return this;
  }

  public CodeFragmentQuery<T> before() {
    List<Pair<CodeCursor, CodeCursor>> newSelected = new ArrayList<>();

    for (Pair<CodeCursor, CodeCursor> selectedRange : selected) {
      int selectedStart = selectedRange.first.getFirstIndex();

      do {
        selectedStart--;
      } while (selectedStart >= 0 && AbstractInsnNodeHelper.isPseudo(codeManipulator.getMethodNode().instructions.get(selectedStart)));

      newSelected.add(Pair.of(codeManipulator.getCursor(selectedStart), codeManipulator.getCursor(selectedStart + 1)));
    }

    return new CodeFragmentQuery<>(parent, codeManipulator, matchResult, stringBinds, newSelected);
  }

  public CodeFragmentQuery<T> after() {
    List<Pair<CodeCursor, CodeCursor>> newSelected = new ArrayList<>();

    for (Pair<CodeCursor, CodeCursor> selectedRange : selected) {
      int selectedEnd = selectedRange.second.getLastIndex();

      while (selectedEnd < codeManipulator.getMethodNode().instructions.size() && AbstractInsnNodeHelper.isPseudo(codeManipulator.getMethodNode().instructions.get(selectedEnd))) {
        selectedEnd++;
      }

      newSelected.add(Pair.of(codeManipulator.getCursor(selectedEnd), codeManipulator.getCursor(selectedEnd + 1)));
    }

    return new CodeFragmentQuery<>(parent, codeManipulator, matchResult, stringBinds, newSelected);
  }

  public boolean isPresent() {
    return selected.size() > 0;
  }
}
