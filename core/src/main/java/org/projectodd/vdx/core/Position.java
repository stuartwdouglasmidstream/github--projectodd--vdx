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