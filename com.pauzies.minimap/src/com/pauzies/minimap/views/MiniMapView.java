package com.pauzies.minimap.views;

import java.lang.reflect.Method;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.pauzies.minimap.preferences.Preferences;

public class MiniMapView extends ViewPart {

    public static final String ID = "com.pauzies.minimap.views.MiniMapView";

    private Color backgroundColor;
    private Color foregroundColor;
    private Color selectionBackgroundColor;
    private Color selectionForegroundColor;
    private Color visibleRegionColor = new Color (Display.getCurrent(), 127, 127, 255);

    private AbstractTextEditor selectedPart;
    private MyTextEditor editor;
    private boolean mouseIsDown;
    private StyledText minimap;

    private float scale = 0.5f;

    class MyTextEditor {
        private final AbstractTextEditor editor;

        public MyTextEditor(AbstractTextEditor editor) {
            System.out.println("MyTextEditor");
            this.editor = editor;
        }

        public ISourceViewer getSourceViewer() {
            try {
                Method m = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
                m.setAccessible(true);
                return (ISourceViewer) m.invoke(editor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //--------------------------------------------------------------------------
    // Listeners
    //--------------------------------------------------------------------------

    private final MouseListener minimapMouseListener = new MouseAdapter() {
        @Override
        public void mouseDown(MouseEvent e) {
            changeEditor(e);
            mouseIsDown = true;
        }

        @Override
        public void mouseUp(MouseEvent e) {
            changeEditor(e);
            mouseIsDown = false;
        }
    };

    private final MouseMoveListener minimapMouseMoveListener = new MouseMoveListener() {
        @Override
        public void mouseMove(MouseEvent e) {
            if (mouseIsDown) {
                changeEditor(e);
            }
        }
    };

    private final MouseWheelListener minimapMouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseScrolled(MouseEvent e) {
            System.out.println("mouseScrolled");
            // NOTE : this is not working, I assume it supposed to scroll the window minimap
            editor.getSourceViewer().getTextWidget().setTopIndex(editor.getSourceViewer().getTextWidget().getTopIndex() + (e.count * -1));
        }
    };

    private final SelectionListener minimapSelectionListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent event) {
            System.out.println("widgetSelected");
            int offset = minimap.getCaretOffset();
            if (offset == event.x) {
                // right to left select
                minimap.setSelection(event.x, event.x);
            } else {
                // left to right select
                minimap.setSelection(event.y, event.y);
            }
        }
    };

    private final ControlListener editorControlListener = new ControlListener() {
        @Override
        public void controlResized(ControlEvent e) {
            System.out.println("controlResized");
            highlightVisibleRegion(minimap, editor);
        }

        @Override
        public void controlMoved(ControlEvent e) {
            System.out.println("controlMoved");
        }
    };

    private final ISelectionListener editorSelectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            System.out.println("selectionChanged -> " + part.getTitle());
            if (part == selectedPart) {
                return;
            }
            if (!(part instanceof AbstractTextEditor)) {
                return;
            }
            if (editor != null) {
                editor.getSourceViewer().getTextWidget().removeControlListener(editorControlListener);
                editor.getSourceViewer().getTextWidget().removeBidiSegmentListener(editorSegmentListener);
            }

            selectedPart = (AbstractTextEditor) part;
            editor = new MyTextEditor((AbstractTextEditor) part);

            setMinimapFont();

            fillMiniMap(minimap, editor);

            editor.getSourceViewer().getTextWidget().addControlListener(editorControlListener);
            editor.getSourceViewer().getTextWidget().addBidiSegmentListener(editorSegmentListener);

            foregroundColor = editor.getSourceViewer().getTextWidget().getForeground();
            backgroundColor = editor.getSourceViewer().getTextWidget().getBackground();
            selectionBackgroundColor = editor.getSourceViewer().getTextWidget().getSelectionBackground();
            selectionForegroundColor = editor.getSourceViewer().getTextWidget().getSelectionForeground();
            if (null != Preferences.getVisibleRegionHighlightColor()) { visibleRegionColor = Preferences.getVisibleRegionHighlightColor(); }

            minimap.setBackground(backgroundColor);
            minimap.setForeground(foregroundColor);
            minimap.setSelectionBackground(selectionBackgroundColor);
            minimap.setSelectionForeground(selectionForegroundColor);
            highlightVisibleRegion(minimap, editor);
        }
    };

    private final BidiSegmentListener editorSegmentListener = new BidiSegmentListener() {
        @Override
        public void lineGetSegments(BidiSegmentEvent event) {
            System.out.println("lineGetSegments");
            highlightVisibleRegion(minimap, editor);
        }
    };

    //	IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
    ////			/*
    ////			 * @see IPropertyChangeListener.propertyChange()
    ////			 */
    ////			public void propertyChange(PropertyChangeEvent event) {
    ////
    ////				if (event
    ////					.getProperty()
    ////					.equals(BadWordCheckerPlugin.HIGHLIGHT_PREFERENCE)) {
    ////					//Update the colors by clearing the current color,
    ////					//updating the view and then disposing the old color.
    ////					Color oldForeground = foreground;
    ////					foreground = null;
    ////					setBadWordHighlights(text.getText());
    ////					oldForeground.dispose();
    ////				}
    ////				if (event
    ////					.getProperty()
    ////					.equals(BadWordCheckerPlugin.BAD_WORDS_PREFERENCE))
    ////					//Only update the text if only the words have changed
    ////					setBadWordHighlights(text.getText());
    ////			}
    //
    //			@Override
    //      public void propertyChange(PropertyChangeEvent event) {
    //				System.out.println("propertyChange");
    //	      // TODO Auto-generated method stub
    //				if (event.getProperty().equals(Preferences.VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE)) {
    //
    //					System.out.println("VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE changed");
    //
    //					//Update the colors by clearing the current color,
    //					//updating the view and then disposing the old color.
    ////					Color oldForeground = foreground;
    ////					foreground = null;
    ////					setBadWordHighlights(text.getText());
    ////					oldForeground.dispose();
    //
    ////					event.getNewValue()
    //
    ////					visibleRegionColor = ??;
    //					// probably redraw
    //				}
    //      }
    //		};

    // WorkbenchPart overrides //////////////////////////////////////////////////
    @Override
    public void createPartControl(Composite parent) {
        System.out.println("createPartControl");

        minimap = new StyledText(parent, SWT.NONE);
        minimap.setEditable(false);
        // minimap.setEnabled(true);
        // minimap.setCursor(null);
        minimap.setCaret(null);

        minimap.addMouseListener(minimapMouseListener);
        minimap.addMouseMoveListener(minimapMouseMoveListener);
        minimap.addMouseWheelListener(minimapMouseWheelListener);
        // Disable Selection
        minimap.addSelectionListener(minimapSelectionListener);

        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(editorSelectionListener);

        //		Preferences.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
    }

    @Override
    public void setFocus() {
        System.out.println("setFocus");
    }

    @Override
    public void dispose() {
        super.dispose();
        System.out.println("dispose");
        editor.getSourceViewer().getTextWidget().removeControlListener(editorControlListener);
        editor.getSourceViewer().getTextWidget().removeBidiSegmentListener(editorSegmentListener);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(editorSelectionListener);
    }

    // private functions ////////////////////////////////////////////////////////
    private static void fillMiniMap(StyledText minimap, MyTextEditor editor) {
        System.out.println("fillMiniMap");
        StyledText text = editor.getSourceViewer().getTextWidget();
        minimap.setContent(text.getContent());
        minimap.setStyleRanges(editor.getSourceViewer().getTextWidget().getStyleRanges());
    }

    // NOTE : this works EXCEPT when code is folded because the minimap matches the contents (foldedness) but uses the line numbers visible to hihglight
    private void highlightVisibleRegion(StyledText minimap, MyTextEditor editor) {
        System.out.print("highlightVisibleRegion ");
        int startLine = editor.getSourceViewer().getTopIndex();
        int endLine = editor.getSourceViewer().getBottomIndex();
        // reset background color for whole document
        minimap.setLineBackground(0, minimap.getLineCount() - 1, backgroundColor);
        // set visible area colour
        minimap.setLineBackground(startLine, endLine - startLine + 1, visibleRegionColor);
        System.out.println("from line# " + startLine + " to line# " + endLine);
    }

    private void changeEditor(MouseEvent e) {
        System.out.println("changeEditor");
        StyledText text = (StyledText) e.getSource();
        int line = text.getLineAtOffset(text.getCaretOffset());
        // editor.getSourceViewer().getTextWidget().setCaretOffset(text.getCaretOffset());
        //		editor.getSourceViewer().getTextWidget().setTopIndex(line);
    }

    private void setMinimapFont() {
        System.out.println("setMinimapFont");
        // create font for minimap that matches original and set to scale
        Font origFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        FontData[] fontData = origFont.getFontData();
        int origFontSize = 0;
        for (int i = 0; i < fontData.length; i++) {
            System.out.println("fontdata " + i);
            origFontSize = fontData[i].getHeight();
            fontData[i].setHeight((int) (origFontSize * scale));
        }

        final Font miniFont = new Font(Display.getCurrent(), fontData);
        minimap.setFont(miniFont);

        // dispose font
        minimap.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                miniFont.dispose();
            }
        });
    }
}

