/*
 * @(#)AlignAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.swing.*;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.undo.CompositeEdit;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Aligns the selected figures based on the specified alignment strategy.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AlignAction extends AbstractSelectedAction {

    // New version of the class
    private static final long serialVersionUID = 1L;
    protected ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

    @FunctionalInterface
    public interface AlignStrategy {
        void align(Collection<Figure> selectedFigures, Rectangle2D.Double selectionBounds);
    }

    private final AlignStrategy strategy;

    /**
     * Creates a new instance with the specified alignment strategy.
     */
    protected AlignAction(DrawingEditor editor, ResourceBundleUtil labels, AlignStrategy strategy) {
        super(editor);
        this.labels = labels;
        this.strategy = strategy;
        updateEnabledState();
    }

    @Override
    public void updateEnabledState() {
        if (getView() != null) {
            setEnabled(getView().isEnabled() && getView().getSelectionCount() > 1);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CompositeEdit edit = new CompositeEdit(labels.getString("edit.align.text"));
        fireUndoableEditHappened(edit);
        strategy.align(getView().getSelectedFigures(), getSelectionBounds());
        fireUndoableEditHappened(edit);
    }

    protected Rectangle2D.Double getSelectionBounds() {
        Rectangle2D.Double bounds = null;
        for (Figure f : getView().getSelectedFigures()) {
            if (bounds == null) {
                bounds = f.getBounds();
            } else {
                bounds.add(f.getBounds());
            }
        }
        return bounds;
    }

    /**
     * Registers the keyboard shortcut for this alignment action.
     */
    public void registerShortcut(JComponent component, KeyStroke keyStroke) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, getValue(Action.NAME));
        component.getActionMap().put(getValue(Action.NAME), this);
    }

    // Align North (Top)
    public static class North extends AlignAction {
        private static final long serialVersionUID = 1L;

        public North(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double y = selectionBounds.y;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(0, y - b.y);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignNorth");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
        }
    }

    // Align South (Bottom)
    public static class South extends AlignAction {
        private static final long serialVersionUID = 1L;

        public South(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double y = selectionBounds.y + selectionBounds.height;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(0, y - b.y - b.height);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignSouth");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
        }
    }

    // Align East (Right)
    public static class East extends AlignAction {
        private static final long serialVersionUID = 1L;

        public East(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double x = selectionBounds.x + selectionBounds.width;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(x - b.x - b.width, 0);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignEast");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK));
        }
    }

    // Align West (Left)
    public static class West extends AlignAction {
        private static final long serialVersionUID = 1L;

        public West(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double x = selectionBounds.x;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(x - b.x, 0);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignWest");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK));
        }
    }

    // Align Horizontal Center
    public static class Horizontal extends AlignAction {
        private static final long serialVersionUID = 1L;

        public Horizontal(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double x = selectionBounds.x + selectionBounds.width / 2;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(x - b.x - b.width / 2, 0);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignHorizontal");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        }
    }

    // Align Vertical Center
    public static class Vertical extends AlignAction {
        private static final long serialVersionUID = 1L;

        public Vertical(DrawingEditor editor, ResourceBundleUtil labels, JComponent component) {
            super(editor, labels, (selectedFigures, selectionBounds) -> {
                double y = selectionBounds.y + selectionBounds.height / 2;
                for (Figure f : selectedFigures) {
                    if (f.isTransformable()) {
                        f.willChange();
                        Rectangle2D.Double b = f.getBounds();
                        AffineTransform tx = new AffineTransform();
                        tx.translate(0, y - b.y - b.height / 2);
                        f.transform(tx);
                        f.changed();
                    }
                }
            });
            labels.configureAction(this, "edit.alignVertical");
            registerShortcut(component, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        }
    }
}
