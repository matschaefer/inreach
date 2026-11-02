package dev.inreach;

import static org.junit.jupiter.api.Assertions.*;

import dev.inreach.lifecycle.LifecyclePolicy;
import org.junit.jupiter.api.Test;

class FoundationTest {
  @Test
  void closingRequiresTwoCompleteHealthyChecks() {
    assertTrue(LifecyclePolicy.eligible(true, true, 100, 90, 2));
    assertFalse(LifecyclePolicy.eligible(false, true, 100, 90, 2));
    assertFalse(LifecyclePolicy.eligible(true, false, 100, 90, 2));
    assertFalse(LifecyclePolicy.eligible(true, true, 100, 0, 2));
    assertFalse(LifecyclePolicy.eligible(true, true, 100, 50, 2));
    assertFalse(LifecyclePolicy.eligible(true, true, 100, 90, 1));
  }
}
