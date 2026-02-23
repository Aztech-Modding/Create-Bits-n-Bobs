package com.cake.azimuth.behaviour;

/**
 * Common interface for extensions.
 * Extensions are used to selectively add functions to {@link SuperBlockEntityBehaviour}s,
 * specifically to systems where performance is important, and behaviours not requiring this extension are safely ignored.
 */
public interface BehaviourExtension {
}
