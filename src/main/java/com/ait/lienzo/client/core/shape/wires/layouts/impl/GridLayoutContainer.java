package com.ait.lienzo.client.core.shape.wires.layouts.impl;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.layouts.ILayoutContainer;
import com.ait.lienzo.client.core.shape.wires.layouts.base.DelegateLayoutContainer;
import com.ait.lienzo.client.core.shape.wires.layouts.base.LayoutEntriesContainer;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.tooling.common.api.java.util.function.Consumer;

public class GridLayoutContainer
        extends DelegateLayoutContainer<GridLayoutContainer> {

    private final LayoutEntriesContainer<GridLayoutEntry> layoutContainer;
    private final Grid grid;
    private double[] cellSize;

    public GridLayoutContainer(int rows,
                               int columns) {
        this.layoutContainer = new LayoutEntriesContainer<>(new Consumer<GridLayoutEntry>() {
            @Override
            public void accept(final GridLayoutEntry entry) {
                entry.refresh(GridLayoutContainer.this);
            }
        });
        this.grid = new Grid(columns,
                             rows);
        this.cellSize = null;
    }

    public GridLayoutContainer add(final ILayoutContainer<?> container,
                                   final int row,
                                   final int column) {
        getDelegate().add(new GridLayoutContainerEntry(container,
                                                       row,
                                                       column));
        return this;
    }

    public GridLayoutContainer add(final WiresShape child,
                                   final int row,
                                   final int column) {
        getDelegate().add(new GridLayoutEntry(child,
                                              row,
                                              column));
        return this;
    }

    public GridLayoutContainer add(final IPrimitive<?> child,
                                   final int row,
                                   final int column) {
        getDelegate().add(new GridLayoutEntry(child,
                                              row,
                                              column));
        return this;
    }

    public GridLayoutContainer set(final IPrimitive<?> child,
                                   final int row,
                                   final int column) {
        final GridLayoutEntry entry = getEntry(child);
        return set(entry,
                   row,
                   column);
    }

    public GridLayoutContainer set(final ILayoutContainer<?> layoutContainer,
                                   final int row,
                                   final int column) {
        final GridLayoutContainerEntry entry = getContainerEntry(layoutContainer);
        return set(entry,
                   row,
                   column);
    }

    private GridLayoutContainer set(final GridLayoutEntry entry,
                                    final int row,
                                    final int column) {
        entry.row(row);
        entry.column(column);
        entry.refresh(this);
        return this;
    }

    public GridLayoutEntry getEntry(final IPrimitive<?> child) {
        return getDelegate().getEntry(child);
    }

    public GridLayoutContainerEntry getContainerEntry(final ILayoutContainer<?> layoutContainer) {
        return (GridLayoutContainerEntry) getEntry((IPrimitive<?>) layoutContainer.get());
    }

    @Override
    protected void onRefreshBounds() {
        super.onRefreshBounds();
        cellSize = null;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        grid = grid;
    }

    BoundingBox getBoundingBox() {
        return getDelegate().getBoundingBox();
    }

    double[] getCellSize() {
        if (null == cellSize) {
            final BoundingBox bb = getBoundingBox();
            final Grid grid = getGrid();
            final double cellWidth = bb.getWidth() / grid.getColumns();
            final double cellHeight = bb.getHeight() / grid.getRows();
            cellSize = new double[]{cellWidth, cellHeight};
        }
        return cellSize;
    }

    @Override
    protected LayoutEntriesContainer<GridLayoutEntry> getDelegate() {
        return layoutContainer;
    }
}
