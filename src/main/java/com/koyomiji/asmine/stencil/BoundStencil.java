package com.koyomiji.asmine.stencil;

public class BoundStencil<T> implements IStencil<T> {
  public Object key;

  public BoundStencil(Object key) {
    this.key = key;
  }

  @Override
  public boolean match(IStencilRegistry registry, T value) {
    return registry.compareBoundToValue(key, value);
  }

  @Override
  public T evaluate(IStencilRegistry registry) throws StencilEvaluationException {
    // There is no way to check the type is T
    return (T) registry.resolveStencil(key);
  }
}
