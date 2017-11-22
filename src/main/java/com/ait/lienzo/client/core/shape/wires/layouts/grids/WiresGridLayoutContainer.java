/*
   Copyright (c) 2017 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.client.core.shape.wires.layouts.grids;

import java.util.HashMap;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.event.AttributesChangedEvent;
import com.ait.lienzo.client.core.event.AttributesChangedHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.wires.layouts.ILayoutContainer;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.tooling.common.api.java.util.UUID;
import com.ait.tooling.nativetools.client.collection.NFastArrayList;
import com.ait.tooling.nativetools.client.event.HandlerRegistrationManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Basic layout container implementation.
 * It hold in memory the required child attributes that are added into layouts to perform the further coordinate calculations.
 * Next -> Use an incremental/differential approach to avoid keeping in memory children attributes.
 */
public class WiresGridLayoutContainer implements ILayoutContainer<Grid>
{
    private static final GridLayoutBuilder GRID_LAYOUT = new GridLayoutBuilder();

    private final Group group;

    private Point2D offset;

    private Grid grid;

    private double width;

    private double height;

    private final NFastArrayList<GridChildEntry> children;

    protected HandlerRegistrationManager attrHandlerRegs = new HandlerRegistrationManager();

    protected HashMap<ObjectAttribute, HandlerRegistration> registrations = new HashMap<ObjectAttribute,HandlerRegistration>();

    private final AttributesChangedHandler ShapeAttributesChangedHandler = new AttributesChangedHandler()
    {
        @Override
        public void onAttributesChanged(AttributesChangedEvent event)
        {
            refresh();
        }
    };


    public WiresGridLayoutContainer(final int columns, final int rows)
    {
        this.group = new Group().setDraggable(false);
        this.offset = new Point2D(0, 0);
        this.grid = new Grid(columns, rows);
        this.width = 0;
        this.height = 0;
        this.children = new NFastArrayList<GridChildEntry>();
    }


    @Override
    public ILayoutContainer<Grid> setOffset(final Point2D offset)
    {
        this.offset = offset;
        return this;
    }

    @Override
    public WiresGridLayoutContainer setSize(final double width, final double height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public Point2D getOffset() {
        return offset;
    }

    @Override
    public Grid getLayout()
    {
        return grid;
    }

    public WiresGridLayoutContainer add(final IPrimitive<?> child)
    {

        return add(child, -1, -1);
    }

    public WiresGridLayoutContainer add(final IPrimitive<?> child, final int column, final int line)
    {
        if (null == child)
        {
            throw new NullPointerException("Child cannot be null.");
        }

        if (null == child.getID())
        {
            child.setID(UUID.uuid());
        }

        addChild(child);

        final GridChildEntry entry = new GridChildEntry(child, column, line);
        children.add(entry);
        for (Attribute attribute : child.getTransformingAttributes()) {
            HandlerRegistration reg = child.addAttributesChangedHandler(attribute,ShapeAttributesChangedHandler);
            registrations.put(new ObjectAttribute(child,attribute), reg);
            attrHandlerRegs.register(reg);
        }

        doPositionChild(child, true);

        return this;
    }

    public void addChild(final IPrimitive<?> child)
    {
        group.add(child);
    }

    public WiresGridLayoutContainer remove(final IPrimitive<?> child)
    {
        final GridChildEntry entry = getChildEntry(child.getID());

        if (null != entry)
        {
            children.remove(entry);

            for (Attribute attribute : child.getTransformingAttributes()) {
                ObjectAttribute key = new ObjectAttribute(child,attribute);
                attrHandlerRegs.deregister(registrations.remove(key));
            }
        }

        group.remove(child);

        return this;
    }

    public ILayoutContainer<Grid> execute()
    {
        for (IPrimitive<?> child : group.getChildNodes())
        {
            doPositionChild(child, false);
        }

        if (null != getGroup().getLayer())
        {
            getGroup().getLayer().batch();
        }

        return this;
    }

    public ILayoutContainer<Grid> refresh()
    {
        for (final GridChildEntry entry : children)
        {
            entry.refresh();
        }
        return this;
    }

    public WiresGridLayoutContainer clear()
    {
        children.clear();
        group.removeAll();
        registrations.clear();
        attrHandlerRegs.clear();
        return this;
    }

    @Override
    public void destroy()
    {
        clear();
        attrHandlerRegs.destroy();
        group.removeFromParent();
    }

    public Group getGroup()
    {
        return group;
    }

    private GridChildEntry getChildEntry(final String key)
    {
        for (final GridChildEntry entry : children)
        {
            if (entry.getUUID().equals(key))
            {
                return entry;
            }
        }
        return null;
    }

    private void doPositionChild(final IPrimitive<?> child, final boolean batch)
    {
        final String id = child.getID();
        final GridChildEntry entry = getChildEntry(id);

        if (null != entry)
        {
            final double[] initial = entry.getInitialCoordinates();
            final double c[] = GRID_LAYOUT.getCoordinates(entry, this);

            if (c != null)
            {
                final double x = c[0] + initial[0] + offset.getX();
                final double y = c[1] + initial[1] + offset.getY();

                child.setX(x);
                child.setY(y);
                child.moveToTop();

                if (batch && null != getGroup().getLayer())
                {
                    getGroup().getLayer().batch();
                }
            }
        }
    }

    private final static class ObjectAttribute
    {
        private final Object obj;
        private final Attribute attr;

        private ObjectAttribute(Object obj, Attribute attr)
        {
            this.obj = obj;
            this.attr = attr;
        }

        @Override
        public final int hashCode()
        {
            return obj.hashCode() ^ attr.hashCode();
        }

        @Override
        public final boolean equals(Object o)
        {
            if (o instanceof ObjectAttribute)
            {
                ObjectAttribute other = (ObjectAttribute) o;
                return obj.equals(other.obj) && attr.equals(other.attr);
            }
            return false;
        }
    }
}
