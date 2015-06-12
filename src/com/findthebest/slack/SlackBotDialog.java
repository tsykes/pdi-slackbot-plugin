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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.CCombo;


import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


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
    private Text wName, customTextInput, channelInput;
    // the combo box for the outcomes
    private CCombo wOutcome;

    // output field name
    private TextVar emailInput, passwordInput, appIDInput,
            listDelimiterInput;

    private Label emailLabel, passwordLabel, channelUpdateButtonLabel,
            appIDLabel, dataFieldLabel, listDelimiterLabel, insertNoMatchLabel,
            updateExistingLabel, updateByIDLabel, doNotReplaceLabel,
            wipeTableLabel, deleteUnchangedLabel, emailOnSuccessLabel, channelLabel, directLabel, privateLabel,
            standardSuccessLabel, standardFailureLabel, errorLineLabel, customMessageLabel, customTextLabel, attachLogLabel,
            attachErrorLabel, attachWarningsLabel, customAttachmentLabel, customAttachLabel;
    private FormData outputLabelForm, outputInputForm, emailLabelForm,
            emailInputForm, appIDLabelForm, dataFieldLabelForm,
            listDelimiterLabelForm, insertNoMatchLabelForm,
            updateExistingLabelForm, updateByIDLabelForm,
            doNotReplaceLabelForm, wipeTableLabelForm, customTextLabelForm,
            deleteUnchangedLabelForm, emailOnSuccessLabelForm, channelUpdateButtonForm, recipientGroupForm, contentGroupForm, standardSuccessButtonForm,
            standardSuccessLabelForm,textCompositeLayoutForm, attachCompositeLayoutForm, standardFailureLabelForm, standardFailureButtonForm, errorLineButtonForm, customMessageButtonForm,
            customMessageLabelForm, errorLineLabelForm, attachLogLabelForm, attachErrorLabelForm, attachWarningsLabelForm, customAttachmentLabelForm,
            customAttachLabelForm;
    private FormData passwordInputForm, passwordLabelForm, tokenInputForm,
            channelUpdateButtonLabelForm, appIDInputForm, listDelimiterInputForm,
            insertNoMatchInputForm, updateExistingInputForm,
            updateByIDInputForm, doNotReplaceInputForm, wipeTableInputForm,
            deleteUnchangedInputForm, fields, emailOnSuccessInputForm, customTextInputForm,
            attachLogButtonForm, attachErrorButtonForm, attachWarningsButtonForm, customAttachmentButtonForm, tabFolderForm;
    private GridData channelLabelGrid, channelInputGrid, directInputGrid, directLabelGrid, privateLabelGrid, privateInputGrid,
            tokenLabelGrid, tokenVerifyGrid;
    private String[] incoming_fields = null;
    private Combo field_options, directInput, privateInput;
    private Button insertNoMatchButton, updateExistingButton, updateByIDButton,
            doNotReplaceButton, wipeTableButton, deleteUnchangedButton,
            emailOnSuccessButton, channelUpdateButton, textButton, attachmentButton, standardSuccessButton, standardFailureButton,
            errorLineButton, customMessageButton, attachLogButton, attachErrorButton, attachWarningsButton, customAttachmentButton;
    private Group recipientGroup, contentGroup;
    private Composite textComposite, attachComposite;
    private TabFolder tabFolder;
    private TabItem text, attachment;

    private JobMeta jobMeta;
    private String sname;

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

        /*
         * channel update button

        channelUpdateButtonLabel = new Label(shell, SWT.RIGHT);
        channelUpdateButtonLabel.setText("Update Channel List");
        channelUpdateButtonLabel
                .setToolTipText("Update the list of channels available");
        props.setLook(channelUpdateButtonLabel);
        channelUpdateButtonLabelForm = new FormData();
        channelUpdateButtonLabelForm.left = new FormAttachment(0, 0);
        channelUpdateButtonLabelForm.right = new FormAttachment(middle, -margin);
        channelUpdateButtonLabelForm.top = new FormAttachment(separator1, margin*3);
        channelUpdateButtonLabel.setLayoutData(channelUpdateButtonLabelForm);

        channelUpdateButton = new Button(shell,SWT.PUSH);
        channelUpdateButton.setText("Update");
        channelUpdateButtonForm = new FormData();
        channelUpdateButtonForm.left = new FormAttachment(middle, 10);
        channelUpdateButtonForm.right = new FormAttachment(80, 0);
        channelUpdateButtonForm.top = new FormAttachment(separator1, margin - 2);
        channelUpdateButton.setLayoutData(channelUpdateButtonForm);
        */

        /*
         * Recipient Group
         */

        recipientGroup = new Group(shell,SWT.SHADOW_NONE);
        recipientGroup.setText("To Whom?");
        props.setLook(recipientGroup);
        recipientGroupForm = new FormData();
        recipientGroupForm.left = new FormAttachment(0, 0);
        recipientGroupForm.right = new FormAttachment(100, 0);
        recipientGroupForm.top = new FormAttachment(separator1, margin * 2);
        GridLayout recipientGrid = new GridLayout();
        recipientGrid.numColumns = 2;
        recipientGroup.setLayout(recipientGrid);
        recipientGroup.setLayoutData(recipientGroupForm);

        channelLabel = new Label(recipientGroup, SWT.RIGHT);
        channelLabel.setText("Channel:");
        channelLabel.setToolTipText("The name of the Slack channel");
        channelLabelGrid = new GridData();
        channelLabelGrid.horizontalAlignment = GridData.FILL;
        channelLabelGrid.grabExcessHorizontalSpace = true;
        channelLabel.setLayoutData(channelLabelGrid);

        //Fill in Drop Down Box
        channelInput = new Text(recipientGroup, SWT.SINGLE | SWT.LEFT);
        props.setLook(channelInput);
        channelInput.addModifyListener(lsMod);
        channelInputGrid = new GridData();
        channelInputGrid.horizontalAlignment = GridData.FILL;
        channelInputGrid.grabExcessHorizontalSpace = true;
        channelInput.setLayoutData(channelLabelGrid);

        /*
         * Message section
         */

        contentGroup = new Group(shell,SWT.SHADOW_NONE);
        contentGroup.setText("What to Send?");
        props.setLook(contentGroup);
        contentGroupForm = new FormData();
        contentGroupForm.left = new FormAttachment(0, 0);
        contentGroupForm.right = new FormAttachment(100, 0);
        contentGroupForm.top = new FormAttachment(recipientGroup, margin * 3);
        FormLayout contentLayout = new FormLayout();
        contentGroup.setLayout(contentLayout);
        contentGroup.setLayoutData(contentGroupForm);


        /*tabFolder = new TabFolder(contentGroup, SWT.NONE);
        FormData tabFolderForm = new FormData();
        tabFolderForm.left = new FormAttachment(0,0);
        tabFolderForm.right = new FormAttachment(100,0);
        tabFolderForm.top = new FormAttachment(0,margin);
        tabFolder.setLayoutData(tabFolderForm);
        */

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
        props.setLook(standardSuccessButton);
        standardSuccessButtonForm = new FormData();
        standardSuccessButtonForm.left = new FormAttachment(0, 0);
        standardSuccessButtonForm.right = new FormAttachment(10, 0);
        standardSuccessButtonForm.top = new FormAttachment(0,margin + 2);
        standardSuccessButton.setLayoutData(standardSuccessButtonForm);

        standardSuccessLabel = new Label(contentGroup, SWT.LEFT);
        standardSuccessLabel.setToolTipText("Send a templated success messages indicating the job has run successfully");
        standardSuccessLabel.setText("Send Standard Success Message");
        props.setLook(standardSuccessLabel);
        standardSuccessLabelForm = new FormData();
        standardSuccessLabelForm.left = new FormAttachment(10, 0);
        standardSuccessLabelForm.right = new FormAttachment(100, 0);
        standardSuccessLabelForm.top = new FormAttachment(0, margin + 2);
        standardSuccessLabel.setLayoutData(standardSuccessLabelForm);

        standardFailureButton = new Button(contentGroup, SWT.RADIO);
        standardFailureButton.setSelection(true);
        standardFailureButton.setSize(100, standardFailureButton.getSize().y);
        props.setLook(standardFailureButton);
        standardFailureButtonForm = new FormData();
        standardFailureButtonForm.left = new FormAttachment(0, 0);
        standardFailureButtonForm.right = new FormAttachment(10, 0);
        standardFailureButtonForm.top = new FormAttachment(standardSuccessLabel,margin);
        standardFailureButton.setLayoutData(standardFailureButtonForm);

        standardFailureLabel = new Label(contentGroup, SWT.LEFT);
        standardFailureLabel.setToolTipText("Send a templated failure messages indicating the job has not run successfully");
        standardFailureLabel.setText("Send Standard Failure Message");
        props.setLook(standardFailureLabel);
        standardFailureLabelForm = new FormData();
        standardFailureLabelForm.left = new FormAttachment(10, 0);
        standardFailureLabelForm.right = new FormAttachment(100, 0);
        standardFailureLabelForm.top = new FormAttachment(standardSuccessLabel, margin);
        standardFailureLabel.setLayoutData(standardFailureLabelForm);

        customMessageButton = new Button(contentGroup, SWT.RADIO);
        customMessageButton.setSelection(false);
        customMessageButton.setSize(100, customMessageButton.getSize().y);
        props.setLook(customMessageButton);
        customMessageButtonForm = new FormData();
        customMessageButtonForm.left = new FormAttachment(0, 0);
        customMessageButtonForm.right = new FormAttachment(10, 0);
        customMessageButtonForm.top = new FormAttachment(standardFailureLabel,margin);
        customMessageButton.setLayoutData(customMessageButtonForm);

        /*final int customMessage = new int;

        Listener customListener = new Listener(){
            public void handleEvent(Event event){

                if(event.widget == customMessageButton) {
                    customMessage = 1;
                }
                else{
                    customMessage = 0;
                }
            }
        }*/

        customMessageLabel = new Label(contentGroup, SWT.LEFT);
        customMessageLabel.setToolTipText("Send a customized message you write below");
        customMessageLabel.setText("Send Custom Message");
        props.setLook(customMessageLabel);
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
        customTextLabel.setText("Input Custom Text Here:");
        props.setLook(customTextLabel);
        customTextLabelForm = new FormData();
        customTextLabelForm.left = new FormAttachment(0, 0);
        customTextLabelForm.right = new FormAttachment(100,0);
        customTextLabelForm.top = new FormAttachment(separator2, margin);
        customTextLabel.setLayoutData(customTextLabelForm);

        customTextInput = new Text(contentGroup , SWT.MULTI | SWT.LEFT | SWT.WRAP);
        props.setLook(customTextInput);
        customTextInput.addModifyListener(lsMod);
        customTextInputForm = new FormData();
        customTextInputForm.left = new FormAttachment(0, 0);
        customTextInputForm.right = new FormAttachment(100, 0);
        customTextInputForm.top = new FormAttachment(customTextLabel, margin);
        customTextInputForm.bottom = new FormAttachment(customTextLabel, 150);
        customTextInput.setLayoutData(customTextInputForm);

        /*//Place all the above into tab item "Text"
        TabItem text = new TabItem(tabFolder, SWT.NONE);
        text.setText("Text");
        text.setToolTipText("This tab controls the text in the message");
        text.setControl(textComposite);*/

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
            channelInput.setText(meta.getSelectedChannel());
            standardSuccessButton.setSelection(meta.isSuccessMsg());
            standardFailureButton.setSelection(meta.isFailureMsg());
            customMessageButton.setSelection(meta.isCustomMsg());
        }
        wName.selectAll();

        // choosing the configured value for the outcome on the selector box
        //wOutcome.select(meta.getOutcome()?0:1);

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
        meta.setSelectedChannel(channelInput.getText());
        meta.setSuccessMsg(standardSuccessButton.getSelection());
        meta.setFailureMsg(standardFailureButton.getSelection());
        meta.setCustomMsg(customMessageButton.getSelection());
//        meta.setCustomText();

        // close dialog window and clean up
        dispose();
    }
}
