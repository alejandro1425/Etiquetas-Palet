package com.paletlabels.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class SimpleDocumentListener implements DocumentListener {
    private final Consumer<DocumentEvent> callback;

    private SimpleDocumentListener(Consumer<DocumentEvent> callback) {
        this.callback = callback;
    }

    public static SimpleDocumentListener onChange(Runnable runnable) {
        return new SimpleDocumentListener(event -> runnable.run());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        callback.accept(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        callback.accept(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        callback.accept(e);
    }
}
