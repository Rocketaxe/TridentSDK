/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2014 The TridentSDK Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tridentsdk.event.block;

import net.tridentsdk.base.Block;
import net.tridentsdk.event.Cancellable;

/**
 * Called when a block is destroyed due to fire
 *
 * @author The TridentSDK Team
 */
public class BlockBurnEvent extends BlockEvent implements Cancellable {
    private volatile boolean cancelled;

    /**
     * The block being burnt in this event
     *
     * @param block Block associated with this event
     */
    public BlockBurnEvent(Block block) {
        super(block);
    }

    @Override
    public boolean isIgnored() {
        return cancelled;
    }

    @Override
    public void cancel(boolean cancelled) {
        this.cancelled = cancelled;
    }
}