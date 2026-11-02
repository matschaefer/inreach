package dev.inreach.lifecycle;

/** Eligibility only: automatic closing remains disabled. */
public final class LifecyclePolicy {
  private LifecyclePolicy() {}

  public static boolean eligible(
      boolean complete, boolean success, int previous, int current, int consecutiveMisses) {
    return complete
        && success
        && previous > 0
        && current > 0
        && current >= Math.ceil(previous * 0.7)
        && consecutiveMisses >= 2;
  }
}
