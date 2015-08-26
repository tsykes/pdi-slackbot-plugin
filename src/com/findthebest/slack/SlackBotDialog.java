/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.findthebest.slack;



import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.CCombo;


import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.ControlSpaceKeyAdapter;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.net.ConnectException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class is part of the demo job entry plug-in implementation.
 * It demonstrates the basics of developing a plug-in job entry for PDI.
 *
 * The demo job entry is configurable to yield a positive or negative
 * result. The job logic will follow the respective path during execution.
 *
 * This class is the implementation of JobEntryDialogInterface.
 * Classes implementing this interface need to:
 *
 * - build and open a SWT dialog displaying the job entry's settings (stored in the entry's meta object)
 * - write back any changes the user makes to the job entry's meta object
 * - report whether the user changed any settings when confirming the dialog
 *
 */

public class SlackBotDialog extends JobEntryDialog implements JobEntryDialogInterface{

    /**
     *	The PKG member is used when looking up internationalized strings.
     *	The properties file with localized keys is expected to reside in
     *	{the package of the class specified}/messages/messages_{locale}.properties
     */
    private static Class<?> PKG = SlackBot.class; // for i18n purposes

    public static final String STEP_VERSION = "v0.1";

    // the text box for the job entry name
    private Text wName, customTextInput;

    // output field name
    private Label wlChannel, standardSuccessLabel, standardFailureLabel, customMessageLabel, customTextLabel, wlUpdate,
            wlToken, wlPostType, wlBotName, wlBotIcon;
    private FormData customTextLabelForm, contentGroupForm, standardSuccessButtonForm,
            standardSuccessLabelForm,textCompositeLayoutForm, standardFailureLabelForm, standardFailureButtonForm,
            customMessageButtonForm,
            customMessageLabelForm;
    private FormData customTextInputForm, recipientGroupForm;
    private GridData fdlChannel, fdChannel, fdlUpdate, fdUpdate, fdToken, fdlToken, fdlPostType, fdPostType, fdlBotName,
            fdBotName, fdlBotIcon, fdBotIcon;
    private Button standardSuccessButton, standardFailureButton, wAlert, customMessageButton, wUpdate;
    private Group recipientGroup, contentGroup;
    private Composite wMessageComp;
    private CCombo wChannel, wPostType, wBotIcon;
    private SelectionAdapter selectionAdapter;
    private TextVar wToken, wBotName;

    // the job entry configuration object
    private SlackBot meta;

    // flag saving the changed status of the job entry configuration object
    private boolean changed;

    /**
     * The constructor should call super() and make sure that the name of the job entry is set.
     *
     * @param parent		the SWT Shell to use
     * @param jobEntryInt	the job entry settings object to use for the dialog
     * @param rep			the repository currently connected to, if any
     * @param jobMeta		the description of the job the job entry belongs to
     */
    public SlackBotDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta){
        super(parent, jobEntryInt, rep, jobMeta);
        // it is safe to cast the JobEntryInterface object to the object handled by this dialog
        meta = (SlackBot) jobEntryInt;
        // ensure there is a default name for new job entries
        if (this.meta.getName() == null){
            this.meta.setName(BaseMessages.getString(PKG, "SlackBot.Default.Name"));
        }
    }

    /**
     * This method is called by Spoon when the user opens the settings dialog of the job entry.
     * It should open the dialog and return only once the dialog has been closed by the user.
     *
     * If the user confirms the dialog, the meta object (passed in the constructor) must
     * be updated to reflect the new job entry settings. The changed flag of the meta object must
     * reflect whether the job entry configuration was changed by the dialog.
     *
     * If the user cancels the dialog, the meta object must not be updated, and its changed flag
     * must remain unaltered.
     *
     * The open() method must return the met object of the job entry after the user has confirmed the dialog,
     * or null if the user cancelled the dialog.
     */
    public JobEntryInterface open(){

        // SWT code for setting up the dialog
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, meta);

        // save the job entry's changed flag
        changed = meta.hasChanged();

        // The ModifyListener used on all controls. It will update the meta object to
        // indicate that changes are being made.
        ModifyListener lsMod = new ModifyListener(){
            public void modifyText(ModifyEvent e){
                meta.setChanged();
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("Slack Messages " + SlackBotDialog.STEP_VERSION);

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        //Start building UI elements

        // Job entry name line
        Label wlName = new Label(shell, SWT.RIGHT);
        wlName.setText("Step Name: ");
        props.setLook(wlName);
        FormData fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, 0);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        FormData fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);


        // Separator Line
        Label separator1 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        FormData fdSeparator1 = new FormData();
        fdSeparator1.left = new FormAttachment(0, margin);
        fdSeparator1.top = new FormAttachment(wlName, margin*3);
        fdSeparator1.right = new FormAttachment(100, 0);
        separator1.setLayoutData(fdSeparator1);
        props.setLook(separator1);


        selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                meta.setChanged();
            }
        };

        /*
         * Recipient Group
         */

        recipientGroup = new Group(shell,SWT.SHADOW_NONE);
        recipientGroup.setText("Message Settings");
        props.setLook(recipientGroup);
        recipientGroupForm = new FormData();
        recipientGroupForm.left = new FormAttachment(0, 0);
        recipientGroupForm.right = new FormAttachment(100, 0);
        recipientGroupForm.top = new FormAttachment(separator1, margin * 2);
        GridLayout recipientGrid = new GridLayout();
        recipientGrid.numColumns = 2;
        recipientGroup.setLayout(recipientGrid);
        recipientGroup.setLayoutData(recipientGroupForm);

        // token
        wlToken = new Label(recipientGroup, SWT.RIGHT);
        wlToken.setText("Token:");
        wlToken.setToolTipText("Go to https://api.slack.com/web to see your token or create a new one");
        fdlToken = new GridData();
        fdlToken.horizontalAlignment = GridData.END;
        wlToken.setLayoutData(fdlToken);

        wToken = new TextVar(jobMeta, recipientGroup, SWT.SINGLE | SWT.LEFT);
        props.setLook(wToken);
        wToken.addModifyListener(lsMod);
        fdToken = new GridData();
        fdToken.horizontalAlignment = GridData.FILL;
        fdToken.grabExcessHorizontalSpace = true;
        wToken.setLayoutData(fdToken);

        /*
         * channel update button
         */

        wlUpdate = new Label(recipientGroup, SWT.RIGHT);
        wlUpdate.setText("Update Channels:");
        wlUpdate.setToolTipText("Update the list of channels available");
        fdlUpdate = new GridData();
        fdlUpdate.horizontalAlignment = GridData.END;
        wlUpdate.setLayoutData(fdlUpdate);

        wUpdate = new Button(recipientGroup,SWT.PUSH);
        wUpdate.setText("Update");
        fdUpdate = new GridData();
        fdUpdate.horizontalAlignment = GridData.FILL;
        fdUpdate.grabExcessHorizontalSpace = true;
        wUpdate.setLayoutData(fdUpdate);

        // post type
        wlPostType = new Label(recipientGroup, SWT.RIGHT);
        wlPostType.setText("Post Type:");
        wlPostType.setToolTipText("Influences what gets populated in Channel/Group/DM");
        fdlPostType = new GridData();
        fdlPostType.horizontalAlignment = GridData.FILL;
        wlPostType.setLayoutData(fdlPostType);

        wPostType = new CCombo(recipientGroup, SWT.READ_ONLY);
        props.setLook(wPostType);
        wPostType.addModifyListener(lsMod);
        fdPostType = new GridData();
        fdPostType.horizontalAlignment = GridData.FILL;
        fdPostType.grabExcessHorizontalSpace = true;
        wPostType.setLayoutData(fdPostType);
        wPostType.add("Channel");
        wPostType.add("Group");

        // room name drop down
        wlChannel = new Label(recipientGroup, SWT.RIGHT);
        wlChannel.setText("Channel/Group:");
        wlChannel.setToolTipText("The name of the Slack Channel/Group/DM. Populated based on Post Type.");
        fdlChannel = new GridData();
        fdlChannel.horizontalAlignment = GridData.FILL;
        wlChannel.setLayoutData(fdlChannel);

        wChannel = new CCombo(recipientGroup, SWT.DROP_DOWN);
        props.setLook(wChannel);
        wChannel.addModifyListener(lsMod);
        fdChannel = new GridData();
        fdChannel.horizontalAlignment = GridData.FILL;
        fdChannel.grabExcessHorizontalSpace = true;
        wChannel.setLayoutData(fdChannel);

        // Bot Name
        wlBotName = new Label(recipientGroup, SWT.RIGHT);
        wlBotName.setText("Bot Name:");
        wlBotName.setToolTipText("Optional Name to use when posting to slack");
        fdlBotName = new GridData();
        fdlBotName.horizontalAlignment = GridData.END;
        wlBotName.setLayoutData(fdlBotName);

        wBotName = new TextVar(jobMeta, recipientGroup, SWT.SINGLE | SWT.LEFT);
        props.setLook(wBotName);
        wBotName.addModifyListener(lsMod);
        fdBotName = new GridData();
        fdBotName.horizontalAlignment = GridData.FILL;
        fdBotName.grabExcessHorizontalSpace = true;
        wBotName.setLayoutData(fdBotName);


        // bot icon drop down
        wlBotIcon = new Label(recipientGroup, SWT.RIGHT);
        wlBotIcon.setText("Bot Icon:");
        wlBotIcon.setToolTipText("Icon to use when posting to slack");
        fdlBotIcon = new GridData();
        fdlBotIcon.horizontalAlignment = GridData.FILL;
        wlBotIcon.setLayoutData(fdlBotIcon);

        wBotIcon = new CCombo(recipientGroup, SWT.DROP_DOWN);
        props.setLook(wBotIcon);
        wBotIcon.addModifyListener(lsMod);
        fdBotIcon = new GridData();
        fdBotIcon.horizontalAlignment = GridData.FILL;
        fdBotIcon.grabExcessHorizontalSpace = true;
        wBotIcon.setLayoutData(fdBotIcon);



        /*
         * Message section
         */

        contentGroup = new Group(shell,SWT.SHADOW_NONE);
        contentGroup.setText("Message");
        props.setLook(contentGroup);
        contentGroupForm = new FormData();
        contentGroupForm.left = new FormAttachment(0, 0);
        contentGroupForm.right = new FormAttachment(100, 0);
        contentGroupForm.top = new FormAttachment(recipientGroup, margin * 3);
        FormLayout contentLayout = new FormLayout();
        contentGroup.setLayout(contentLayout);
        contentGroup.setLayoutData(contentGroupForm);

        Composite textComposite = new Composite(contentGroup, SWT.NONE);
        FormLayout textCompositeLayout = new FormLayout();
        textCompositeLayoutForm = new FormData();
        textCompositeLayoutForm.left = new FormAttachment(0,0);
        textCompositeLayoutForm.right = new FormAttachment(100,0);
        textCompositeLayoutForm.top = new FormAttachment(0,margin);
        textComposite.setLayout(textCompositeLayout);
        textComposite.setLayoutData(textCompositeLayoutForm);

        standardSuccessButton = new Button(contentGroup, SWT.RADIO);
        standardSuccessButton.setSelection(false);
        standardSuccessButton.setSize(100, standardSuccessButton.getSize().y);
        standardSuccessButtonForm = new FormData();
        standardSuccessButtonForm.left = new FormAttachment(0, 0);
        standardSuccessButtonForm.right = new FormAttachment(10, 0);
        standardSuccessButtonForm.top = new FormAttachment(0,margin + 2);
        standardSuccessButton.setLayoutData(standardSuccessButtonForm);

        standardSuccessLabel = new Label(contentGroup, SWT.LEFT);
        standardSuccessLabel.setToolTipText("Send a templated success messages indicating the job has run successfully");
        standardSuccessLabel.setText("Send Standard Success Message");
        standardSuccessLabelForm = new FormData();
        standardSuccessLabelForm.left = new FormAttachment(10, 0);
        standardSuccessLabelForm.right = new FormAttachment(100, 0);
        standardSuccessLabelForm.top = new FormAttachment(0, margin + 2);
        standardSuccessLabel.setLayoutData(standardSuccessLabelForm);

        standardFailureButton = new Button(contentGroup, SWT.RADIO);
        standardFailureButton.setSelection(true);
        standardFailureButton.setSize(100, standardFailureButton.getSize().y);
        standardFailureButtonForm = new FormData();
        standardFailureButtonForm.left = new FormAttachment(0, 0);
        standardFailureButtonForm.right = new FormAttachment(10, 0);
        standardFailureButtonForm.top = new FormAttachment(standardSuccessLabel,margin);
        standardFailureButton.setLayoutData(standardFailureButtonForm);

        standardFailureLabel = new Label(contentGroup, SWT.LEFT);
        standardFailureLabel.setToolTipText("Send a templated failure messages indicating the job has not run successfully");
        standardFailureLabel.setText("Send Standard Failure Message");
        standardFailureLabelForm = new FormData();
        standardFailureLabelForm.left = new FormAttachment(10, 0);
        standardFailureLabelForm.right = new FormAttachment(100, 0);
        standardFailureLabelForm.top = new FormAttachment(standardSuccessLabel, margin);
        standardFailureLabel.setLayoutData(standardFailureLabelForm);

        customMessageButton = new Button(contentGroup, SWT.RADIO);
        customMessageButton.setSelection(false);
        customMessageButton.setSize(100, customMessageButton.getSize().y);
        customMessageButtonForm = new FormData();
        customMessageButtonForm.left = new FormAttachment(0, 0);
        customMessageButtonForm.right = new FormAttachment(10, 0);
        customMessageButtonForm.top = new FormAttachment(standardFailureLabel,margin);
        customMessageButton.setLayoutData(customMessageButtonForm);

        Listener lsCustom = new Listener(){
            public void handleEvent(Event e){
                setDialogStatus(true);
            }
        };

        Listener lsStock = new Listener(){
            public void handleEvent(Event e){
                setDialogStatus(false);
            }
        };

        customMessageButton.addListener(SWT.Selection, lsCustom);
        customMessageButton.addSelectionListener(selectionAdapter);
        standardFailureButton.addListener(SWT.Selection, lsStock);
        standardFailureButton.addSelectionListener(selectionAdapter);
        standardSuccessButton.addListener(SWT.Selection, lsStock);
        standardSuccessButton.addSelectionListener(selectionAdapter);

        wUpdate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    List<String> vars = jobMeta.getUsedVariables();
                    SlackConnection slack = new SlackConnection(meta.environmentSubstitute(wToken.getText()));
                    if (!slack.getAuthStatus()) {
                        throw new ConnectException("Couldn't connect to Slack");
                    }
                    int roomType;
                    String listName;
                    if (wPostType.getText() == null) {
                        roomType = SlackConnection.CHANNEL;
                        listName = "channels";
                    } else if (wPostType.getText().equals("Group")) {
                        roomType = SlackConnection.GROUP;
                        listName = "groups";
                    } else {
                        roomType = SlackConnection.CHANNEL;
                        listName = "channels";
                    }
                    String result = slack.getRoomList(roomType);
                    JsonElement parsed = new JsonParser().parse(result);
                    JsonObject jObject = parsed.getAsJsonObject();
                    String status = jObject.get("ok").toString();
                    if (!status.equals("true")) {
                        new ConnectException("Couldn't get list");
                    }
                    JsonArray jarray = jObject.getAsJsonArray(listName);
                    List<String> options = new LinkedList<String>();
                    Iterator<JsonElement> jelement = jarray.iterator();
                    while (jelement.hasNext()) {
                        options.add(jelement.next().getAsJsonObject().get("name").getAsString());
                    }
                    wChannel.setItems(options.toArray(new String[options.size()]));
                } catch (Exception ex) {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                    mb.setText("Slack Connection Error");
                    mb.setMessage("Unable to connect to Slack to update list. Please enter channel manually or try again.");
                    mb.open();
                }

            }
        });


        customMessageLabel = new Label(contentGroup, SWT.LEFT);
        customMessageLabel.setText("Send Custom Message");
        customMessageLabel.setToolTipText("Send a customized message you write below");
        customMessageLabelForm = new FormData();
        customMessageLabelForm.left = new FormAttachment(10, 0);
        customMessageLabelForm.right = new FormAttachment(100, 0);
        customMessageLabelForm.top = new FormAttachment(standardFailureLabel, margin);
        customMessageLabel.setLayoutData(customMessageLabelForm);


        // Separator Line between checkboxes and custom message input
        Label separator2 = new Label(contentGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
        FormData fdSeparator2 = new FormData();
        fdSeparator2.left = new FormAttachment(0, margin);
        fdSeparator2.top = new FormAttachment(customMessageLabel, margin);
        fdSeparator2.right = new FormAttachment(100, 0);
        separator2.setLayoutData(fdSeparator2);
        props.setLook(separator2);

        customTextLabel = new Label(contentGroup, SWT.LEFT);
        customTextLabel.setText("Input Custom Message Below");
        customTextLabelForm = new FormData();
        customTextLabelForm.left = new FormAttachment(0, 0);
        customTextLabelForm.right = new FormAttachment(100,0);
        customTextLabelForm.top = new FormAttachment(separator2, margin);
        customTextLabel.setLayoutData(customTextLabelForm);

        customTextInput = new Text(contentGroup , SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(customTextInput);
        customTextInput.addModifyListener(lsMod);
        customTextInputForm = new FormData();
        customTextInputForm.left = new FormAttachment(0, 0);
        customTextInputForm.right = new FormAttachment(100, 0);
        customTextInputForm.top = new FormAttachment(customTextLabel, margin);
        customTextInputForm.bottom = new FormAttachment(customTextLabel, 150);
        customTextInput.setLayoutData(customTextInputForm);
        customTextInput.addKeyListener(new ControlSpaceKeyAdapter(jobMeta, customTextInput));

        /*
         * Ok and Cancel buttons
         */

        Button wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        Button wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        // at the bottom
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Add listeners
        Listener lsCancel = new Listener(){
            public void handleEvent(Event e){cancel();}
        };

        Listener lsOK = new Listener(){
            public void handleEvent(Event e){ok();}
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // default listener (for hitting "enter")
        SelectionAdapter lsDef = new SelectionAdapter(){
            public void widgetDefaultSelected(SelectionEvent e){ok();}
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener(new ShellAdapter(){
            public void shellClosed(ShellEvent e){
                cancel();
            }
        });

        // populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners fire during dialog population
        meta.setChanged(changed);

        // restore dialog size and placement, or set default size if none saved yet
        BaseStepDialog.setSize(shell, 100, 100, false);
        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()){
            if (!display.readAndDispatch()){
                display.sleep();
            }
        }
        // at this point the dialog has closed, so either ok() or cancel() have been executed
        return meta;
    }

    /**
     * This helper method is called once the dialog is closed. It saves the placement of
     * the dialog, so it can be restored when it is opened another time.
     */
    private void dispose(){
        // save dialog window placement to use when reopened
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        // close dialog window
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void populateDialog(){

        // setting the name of the job entry
        if (meta.getName() != null){
            wName.setText(meta.getName());
            wToken.setText(meta.getToken());
            wChannel.setText(meta.getSelectedChannel());
            wPostType.setText(meta.getPostType());
            wBotName.setText(meta.getBotName());
            wBotIcon.setItems(meta.getIconList().toArray(new String[meta.getIconList().size()]));
            wBotIcon.setText(meta.getBotIcon());
            standardSuccessButton.setSelection(meta.isSuccessMsg());
            standardFailureButton.setSelection(meta.isFailureMsg());
            customMessageButton.setSelection(meta.isCustomMsg());
            if (meta.isSuccessMsg() || meta.isFailureMsg()) {
                setDialogStatus(false);
            } else {
                setDialogStatus(true);
            }
            customTextInput.setText(meta.getCustomText());
        }
        wName.selectAll();

    }

    /**
     * This method is called once the dialog has been canceled.
     */
    private void cancel(){
        // restore changed flag on the meta object, any changes done by the modify listener
        // are being revoked here
        meta.setChanged(changed);
        // this variable will be returned by the open() method, setting it to null, as open() needs
        // to return null when the dialog is cancelled
        meta = null;
        // close dialog window and clean up
        dispose();
    }

    /**
     * This method is called once the dialog is confirmed. It may only close the window if the
     * job entry has a non-empty name.
     */
    private void ok(){

        // make sure the job entry name is set properly, return with an error message if that is not the case
        if(Const.isEmpty(wName.getText())) {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
            mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
            mb.open();
            return;
        }

        // update the meta object with the entered dialog settings
        meta.setName(wName.getText());
        meta.setToken(wToken.getText());
        meta.setSelectedChannel(wChannel.getText());
        meta.setPostType(wPostType.getText());
        meta.setBotName(wBotName.getText());
        meta.setBotIcon(wBotIcon.getText());
        meta.setSuccessMsg(standardSuccessButton.getSelection());
        meta.setFailureMsg(standardFailureButton.getSelection());
        meta.setCustomMsg(customMessageButton.getSelection());
        meta.setCustomText(customTextInput.getText());

        // close dialog window and clean up
        dispose();
    }

    private void setDialogStatus(boolean status) {
        customTextInput.setEditable(status);
        customTextInput.setEnabled(status);
    }
}
