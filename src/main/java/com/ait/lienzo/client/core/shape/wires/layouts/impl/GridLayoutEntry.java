package com.ait.lienzo.client.core.shape.wires.layouts.impl;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.layouts.base.LayoutEntry;
import com.ait.lienzo.client.core.types.Point2D;

public class GridLayoutEntry implements LayoutEntry<GridLayoutContainer> {

    private final IPrimitive<?> primitive;
    private final GridEntry gridEntry;

    public GridLayoutEntry(final IPrimitive<?> primitive,
                           final int row,
                           final int column) {
        this.primitive = primitive;
        this.gridEntry = new GridEntry(row,
                                       column);
    }

    public GridLayoutEntry(final WiresShape wiresShape,
                           final int row,
                           final int column) {
        this.primitive = wiresShape.getPath().asPrimitive();
        this.gridEntry = new GridEntry(row,
                                       column);
    }

    public GridLayoutEntry row(final int value) {
        gridEntry.row = value;
        return this;
    }

    public GridLayoutEntry column(final int value) {
        gridEntry.column = value;
        return this;
    }

    @Override
    public void refresh(final GridLayoutContainer container) {
        final double[] cellSize = container.getCellSize();
        final Point2D location = getLocation(gridEntry,
                                             cellSize[0],
                                             cellSize[1]);
        // GWT.log("GridLayoutEntyr - setLocation = [" + location + "]");
        primitive.setLocation(location);
    }

    @Override
    public IPrimitive<?> getPrimitive() {
        return primitive;
    }

    public int getRow() {
        return gridEntry.row;
    }

    public int getColumn() {
        return gridEntry.column;
    }

    private static class GridEntry {

        private int row;
        private int column;

        private GridEntry(final int row,
                          final int column) {
            this.row = row;
            this.column = column;
        }
    }

    private static Point2D getLocation(final GridEntry entry,
                                       final double cellWidth,
                                       final double cellHeight) {
        final int column = entry.column;
        final int row = entry.row;
        final double x = (cellWidth * column);
        final double y = (cellHeight * row);
        return new Point2D(x,
                           y);
    }
}
