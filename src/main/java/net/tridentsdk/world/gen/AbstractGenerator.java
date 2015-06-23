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

package net.tridentsdk.world.gen;

import net.tridentsdk.world.ChunkLocation;

/**
 * The base class for implementing world generation extensions
 *
 * @author The TridentSDK Team
 */
public abstract class AbstractGenerator {
    /**
     * Populates the block ids for a chunk
     *
     * <p>The first array index is the section number, the
     * second index is the position in that section, i.e.
     * x << 8 + y << 4 + z</p>
     *
     * <p>Should only be invoked by TridentChunk</p>
     *
     * @param location the chunk's location
     * @return a double array containing the extended ID data of the chunk
     */
    public abstract char[][] generateChunkBlocks(ChunkLocation location);

    /**
     * Populates block data for a chunk
     * @param location the location of the chunk
     * @return the data of each block in the chunk
     */
    public abstract byte[][] generateBlockData(ChunkLocation location);
}
