package com.pauzies.minimap.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

//public class Preferences extends AbstractUIPlugin {
public class Preferences extends PreferencePage implements IWorkbenchPreferencePage {

    private static Preferences	plugin;
    private ColorFieldEditor	 colorEditor;
    public static final int	   DEFAULT_VISIBLE_REGION_HIGHLIGHT_COLOR	    = SWT.COLOR_BLUE;
    public static final String	VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE	= "visible_region_highlight_color";

    @Override
    public void init(IWorkbench workbench) {
        System.out.println("init");
        setPreferenceStore(Preferences.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(Composite parent) {
        System.out.println("createContents");
        Composite entryTable = new Composite(parent, SWT.NULL);

        // Create a data that takes up the extra space in the dialog .
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        entryTable.setLayoutData(data);

        GridLayout layout = new GridLayout();
        entryTable.setLayout(layout);

        Composite colorComposite = new Composite(entryTable,SWT.NONE);

        colorComposite.setLayout(new GridLayout());

        // Create a data that takes up the extra space in the dialog.
        colorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        colorEditor = new ColorFieldEditor(VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE, "Visible Region Highlight Color", colorComposite);


        // Set the editor up to use this page
        colorEditor.setPreferencePage(this);
        colorEditor.setPreferenceStore(getPreferenceStore());
        colorEditor.load();

        return entryTable;
    }

    public Preferences() {
        System.out.println("Preferences");
        plugin = this;
        //		Color color = Display.getDefault().getSystemColor(DEFAULT_VISIBLE_REGION_HIGHLIGHT_COLOR);
        //		PreferenceConverter.setDefault(plugin.getPreferenceStore(),  VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE, color.getRGB());
    }

    public static Preferences getDefault() {
        System.out.println("getDefault");
        return plugin;
    }

    //	/**
    //	 * Initializes a preference store with default preference values
    //	 * for this plug-in.
    //	 */
    //	protected void initializeDefaultPreferences(IPreferenceStore store) {
    //		System.out.println("initializeDefaultPreferences");
    ////		store.setDefault(VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE, DEFAULT_VISIBLE_REGION_HIGHLIGHT_COLOR);
    //		Color color = Display.getDefault().getSystemColor(DEFAULT_VISIBLE_REGION_HIGHLIGHT_COLOR);
    //		PreferenceConverter.setDefault(store,  VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE, color.getRGB());
    //	}

    @Override
    protected void performDefaults() {
        super.performDefaults();
        System.out.println("performDefaults");
        colorEditor.loadDefault();
    }

    @Override
    public boolean performOk() {
        System.out.println("performOk");
        colorEditor.store();
        return super.performOk();
    }

    public static Color getVisibleRegionHighlightColor() {
        return new Color(Display.getCurrent(), PreferenceConverter.getColor(plugin.getPreferenceStore(), VISIBLE_REGION_HIGHLIGHT_COLOR_PREFERENCE));
    }

}
