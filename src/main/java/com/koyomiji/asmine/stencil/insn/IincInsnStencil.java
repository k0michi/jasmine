package com.koyomiji.asmine.stencil.insn;

import com.koyomiji.asmine.stencil.IStencil;
import com.koyomiji.asmine.stencil.IStencilRegistry;
import com.koyomiji.asmine.stencil.ConstStencil;
import com.koyomiji.asmine.stencil.StencilEvaluationException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;

public class IincInsnStencil extends AbstractInsnStencil {
  public IStencil<Integer> var;
  public IStencil<Integer> incr;

  public IincInsnStencil(IStencil<Integer> var, IStencil<Integer> incr) {
    super(new ConstStencil<>(Opcodes.IINC));
    this.var = var;
    this.incr = incr;
  }

  @Override
  public boolean match(IStencilRegistry registry, AbstractInsnNode insn) {
    return super.match(registry, insn)
        && insn instanceof IincInsnNode
        && var.match(registry, ((IincInsnNode) insn).var)
        && incr.match(registry, ((IincInsnNode) insn).incr);
  }

  @Override
  public AbstractInsnNode evaluate(IStencilRegistry registry) throws StencilEvaluationException {
    return new IincInsnNode(
        this.var.evaluate(registry),
        this.incr.evaluate(registry)
    );
  }

  @Override
  public boolean isReal() {
    return true;
  }
}
