/*
 * @(#)TextCreationTool.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.figure.TextHolderFigure;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.text.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * A tool to create figures which implement the {@code TextHolderFigure}
 * interface, such as {@code TextFigure}. The figure to be created is specified
 * by a prototype.
 * <p>
 * To create a figure using this tool, the user does the following mouse
 * gestures on a DrawingView:
 * <ol>
 * <li>Press the mouse button over an area on the DrawingView on which there
 * isn't a text figure present. This defines the location of the figure.</li>
 * </ol>
 * When the user has performed this mouse gesture, the TextCreationTool overlays
 * a text field over the drawing where the user can enter the text for the Figure.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Framework</em><br>
 * The text creation and editing tools and the {@code TextHolderFigure}
 * interface define together the contracts of a smaller framework inside of the
 * JHotDraw framework for structured drawing editors.<br>
 * Contract: {@link TextHolderFigure}, {@link TextCreationTool},
 * {@link TextAreaCreationTool}, {@link TextEditingTool},
 * {@link TextAreaEditingTool}, {@link FloatingTextField},
 * {@link FloatingTextArea}.
 *
 * <p>
 * <em>Prototype</em><br>
 * The text creation tools create new figures by cloning a prototype
 * {@code TextHolderFigure} object.<br>
 * Prototype: {@link TextHolderFigure}; Client: {@link TextCreationTool},
 * {@link TextAreaCreationTool}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */

public class TextCreationTool extends CreationTool implements ActionListener {

    private static final long serialVersionUID = 1L;

    // Marked as transient since it's a non-serializable UI component
    private transient FloatingTextField textField;

    private TextHolderFigure typingTarget;

    // Refactored constructor for clarity
    public TextCreationTool(TextHolderFigure prototype) {
        super(prototype);
    }

    public TextCreationTool(TextHolderFigure prototype, Map<AttributeKey<?>, Object> attributes) {
        super(prototype, attributes);
    }

    @Override
    public void deactivate(DrawingEditor editor) {
        endEdit();
        super.deactivate(editor);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (typingTarget != null) {
            endEdit();
            if (isToolDoneAfterCreation()) {
                fireToolDone();
            }
        } else {
            super.mousePressed(e);
            TextHolderFigure textHolder = (TextHolderFigure) getCreatedFigure();
            getView().clearSelection();
            getView().addToSelection(textHolder);
            beginEdit(textHolder);
            updateCursor(getView(), e.getPoint());
        }
    }

    protected void beginEdit(TextHolderFigure textHolder) {
        if (textField == null) {
            textField = new FloatingTextField();
            textField.addActionListener(this);
        }
        if (textHolder != typingTarget && typingTarget != null) {
            endEdit();
        }
        textField.createOverlay(getView(), textHolder);
        textField.requestFocus();
        typingTarget = textHolder;
    }

    protected void endEdit() {
        if (typingTarget != null) {
            typingTarget.willChange();
            final TextHolderFigure editedFigure = typingTarget;
            final String oldText = typingTarget.getText();
            final String newText = textField.getText();

            if (!newText.isEmpty()) {
                typingTarget.setText(newText);
            } else {
                handleEmptyText();
            }

            createUndoableEdit(editedFigure, oldText, newText);
            typingTarget.changed();
            typingTarget = null;
            textField.endOverlay();
        }
    }

    private void handleEmptyText() {
        if (createdFigure != null) {
            getDrawing().remove(getAddedFigure());
        } else {
            typingTarget.setText("");
            typingTarget.changed();
        }
    }

    private void createUndoableEdit(TextHolderFigure figure, String oldText, String newText) {
        UndoableEdit edit = new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getString("attribute.text.text");
            }

            @Override
            public void undo() {
                super.undo();
                figure.willChange();
                figure.setText(oldText);
                figure.changed();
            }

            @Override
            public void redo() {
                super.redo();
                figure.willChange();
                figure.setText(newText);
                figure.changed();
            }
        };
        getDrawing().fireUndoableEditHappened(edit);
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE || isToolDoneAfterCreation()) {
            fireToolDone();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        endEdit();
        if (isToolDoneAfterCreation()) {
            fireToolDone();
        }
    }

    @Override
    protected void creationFinished(Figure createdFigure) {
        beginEdit((TextHolderFigure) createdFigure);
        updateCursor(getView(), new Point(0, 0));
    }

    public boolean isEditing() {
        return typingTarget != null;
    }

    @Override
    public void updateCursor(DrawingView view, Point p) {
        if (view.isEnabled()) {
            view.setCursor(Cursor.getPredefinedCursor(isEditing() ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
        } else {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }
}
