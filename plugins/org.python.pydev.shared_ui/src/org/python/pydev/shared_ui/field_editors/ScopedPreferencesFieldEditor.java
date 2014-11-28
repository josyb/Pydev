package org.python.pydev.shared_ui.field_editors;

import java.io.File;
import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.python.pydev.shared_core.preferences.IScopedPreferences;
import org.python.pydev.shared_core.preferences.ScopedPreferences;
import org.python.pydev.shared_ui.EditorUtils;
import org.python.pydev.shared_ui.dialogs.ProjectSelectionDialog;

public class ScopedPreferencesFieldEditor extends FieldEditor {

    private Composite toolBar;
    private IScopedPreferences iScopedPreferences;
    private WeakReference<ScopedFieldEditorPreferencePage> preferencesPage;

    /**
     * @param name the name of the property
     * @param linkText the text that'll appear to the user
     * @param parent the parent composite
     * @param selectionListener a listener that'll be executed when the linked text is clicked
     */
    public ScopedPreferencesFieldEditor(Composite parent, String pluginName,
            ScopedFieldEditorPreferencePage preferencesPage) {
        init("__UNUSED__", "Some text");
        createControl(parent);
        iScopedPreferences = ScopedPreferences.get(pluginName);
        this.preferencesPage = new WeakReference<ScopedFieldEditorPreferencePage>(preferencesPage);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData) toolBar.getLayoutData();
        gd.horizontalSpan = numColumns;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        toolBar = new Composite(parent, SWT.NONE);
        toolBar.setLayout(new GridLayout(3, true));

        final Button bt = getButtonControl(toolBar, "Save to ...");
        bt.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Menu menu = new Menu(bt);

                MenuItem item1 = new MenuItem(menu, SWT.PUSH);
                item1.setText("User settings");
                item1.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        preferencesPage.get().saveToUserSettings(iScopedPreferences);
                    }
                });

                MenuItem item2 = new MenuItem(menu, SWT.PUSH);
                item2.setText("Project settings ...");
                item2.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        ProjectSelectionDialog dialog = new ProjectSelectionDialog(EditorUtils.getShell(), null, true);
                        dialog.setMessage("Choose the projects to which the preferences should be applied.");
                        if (dialog.open() == Window.OK) {
                            Object[] result = dialog.getResult();
                            IProject[] projects = new IProject[result.length];
                            for (int i = 0; i < result.length; i++) {
                                projects[i] = (IProject) result[i];
                            }
                            preferencesPage.get().saveToProjectSettings(iScopedPreferences, projects);
                        }

                    }
                });

                Point loc = bt.getLocation();
                Rectangle rect = bt.getBounds();

                Point mLoc = new Point(loc.x, loc.y + rect.height);

                menu.setLocation(bt.getShell().getDisplay().map(bt.getParent(), null, mLoc));

                menu.setVisible(true);
            }
        });

        final Button bt2 = getButtonControl(toolBar, "Show from ...");
        bt2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Menu menu = new Menu(bt2);

                MenuItem item1 = new MenuItem(menu, SWT.PUSH);
                item1.setText("User settings");
                item1.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        preferencesPage.get().loadFromUserSettings(iScopedPreferences);
                    }
                });

                MenuItem item2 = new MenuItem(menu, SWT.PUSH);
                item2.setText("Project settings ...");
                item2.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        ProjectSelectionDialog dialog = new ProjectSelectionDialog(EditorUtils.getShell(), null, false);
                        dialog.setMessage("Choose the project from which the preferences should be shown.");
                        if (dialog.open() == Window.OK) {
                            IProject project = (IProject) dialog.getFirstResult();
                            preferencesPage.get().loadFromProjectSettings(iScopedPreferences, project);
                        }
                    }
                });

                Point loc = bt2.getLocation();
                Rectangle rect = bt2.getBounds();

                Point mLoc = new Point(loc.x, loc.y + rect.height);

                menu.setLocation(bt2.getShell().getDisplay().map(bt2.getParent(), null, mLoc));

                menu.setVisible(true);
            }
        });

        final Button bt3 = getButtonControl(toolBar, "Open location ...");
        bt3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Menu menu = new Menu(bt3);

                MenuItem item1 = new MenuItem(menu, SWT.PUSH);
                final File userSettingsLocation = iScopedPreferences.getUserSettingsLocation();
                item1.setText("User settings: " + userSettingsLocation);
                item1.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        EditorUtils.openFile(userSettingsLocation);
                    }
                });

                MenuItem item2 = new MenuItem(menu, SWT.PUSH);
                item2.setText("Project settings ...");
                item2.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        ProjectSelectionDialog dialog = new ProjectSelectionDialog(EditorUtils.getShell(), null, true);
                        dialog.setMessage("Choose the projects from which the preference files should be opened.");
                        if (dialog.open() == Window.OK) {
                            for (Object o : dialog.getResult()) {
                                IProject p = (IProject) o;
                                IFile projectSettingsLocation = iScopedPreferences.getProjectSettingsLocation(p);
                                EditorUtils.openFile(projectSettingsLocation);
                            }
                        }
                    }
                });

                Point loc = bt3.getLocation();
                Rectangle rect = bt3.getBounds();

                Point mLoc = new Point(loc.x, loc.y + rect.height);

                menu.setLocation(bt3.getShell().getDisplay().map(bt3.getParent(), null, mLoc));

                menu.setVisible(true);
            }
        });

        GridData gd = createFillGridData();
        gd.horizontalSpan = numColumns;
        toolBar.setLayoutData(gd);

    }

    private GridData createFillGridData() {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        return gd;
    }

    /**
     * Returns this field editor's link component.
     * <p>
     * The link is created if it does not already exist
     * </p>
     *
     * @param parent the parent
     * @return the label control
     */
    private Button getButtonControl(Composite parent, String text) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setFont(parent.getFont());

        GridData gd = createFillGridData();
        button.setLayoutData(gd);
        return button;
    }

    @Override
    protected void doLoad() {
    }

    @Override
    protected void doLoadDefault() {
    }

    @Override
    protected void doStore() {
    }

    @Override
    public int getNumberOfControls() {
        return 1;
    }

    @Override
    public void setEnabled(boolean enabled, Composite parent) {
        //super.setEnabled(enabled, parent); -- don't call super!
        toolBar.setEnabled(enabled);
    }
}
