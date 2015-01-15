package net.tridentsdk.entity.living.ai;

import net.tridentsdk.entity.LivingEntity;

/**
 * Interface for creating AI and pathfinding algorithms
 *
 * <p>Each time an entity attempts to think, it will call
 * {@link net.tridentsdk.entity.living.ai.AiModule#think(net.tridentsdk.entity.LivingEntity)} and give the
 * ai a chance to decide behavior.</p>
 *
 * <p>The module may also provide a path and return a number other than 0 to indicate how many ticks the
 * AI can rest and the path will be followed by the entity. By not setting a path and returning a value other
 * than 0, the entity can effectively be made to "sleep," where it will not move at all.</p>
 */
public interface AiModule {
    /**
     * Called each time an entity is given a chance to think
     * @param entity  the handle that allows this handler to interact with the entity
     * @return an integer representing the number of ticks that this entity may not have to think for
     * i.e. if a path was submitted, this entity may not have to think for a few ticks
     */
    int think(LivingEntity entity);
}