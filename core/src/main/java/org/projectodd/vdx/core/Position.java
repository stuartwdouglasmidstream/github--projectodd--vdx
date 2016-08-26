/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.vdx.core;

public class Position implements Comparable<Position> {
    public final int line;
    public final int col;

    public Position(int line, int col) {
        this.line = line;
        this.col = col;
    }

    @Override
    public int compareTo(Position that) {
        if (this.line < that.line) return -1;
        if (this.line == that.line) return this.col - that.col;

        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Position)) return false;

        final Position that = (Position)obj;

        return this.line == that.line &&
                this.col == that.col;
    }
}
