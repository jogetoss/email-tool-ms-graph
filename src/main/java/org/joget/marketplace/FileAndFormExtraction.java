package org.joget.marketplace;

import java.io.File;
import java.io.FileInputStream;
import org.joget.apps.form.service.FileUtil;
import java.util.Base64;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Hazael Frans Christian Given a form's details and a specific field
 * ID, will extract the file uploaded in that field also provides other info
 * about the form like their 'FormRowSet'
 */
public class FileAndFormExtraction {

    //Private global values
    private String formDefID;
    private WorkflowAssignment assignment;
    private AppDefinition appDef;

    public FileAndFormExtraction(String formDefId, AppDefinition appDef, WorkflowAssignment assignment) {
        /*
        Constructor
         */
        this.formDefID = formDefId;
        this.appDef = appDef;
        this.assignment = assignment;
    }

    private String getPk() {
        /*
        Returns the primary key or AKA, the ID
         */
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        String pk = appService.getOriginProcessId(this.assignment.getProcessId());

        return pk;
    }

    public FormRowSet getFormRowSet() {
        /*
        Returns the FormRowSet
         */
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        String pk = getPk();

        FormRowSet formRowSet = appService.loadFormData(appDef.getAppId(), appDef.getVersion().toString(), this.formDefID, pk);
        return formRowSet;
    }

    public File getFile(String fileField, String tableName) {
        /*
        Returns the file in "File" data type
         */
        FormRowSet formRowSet = getFormRowSet();

        //Null check
        if (formRowSet == null) {
            return null;
        }

        //Function starts
        try {
            if (formRowSet != null && !formRowSet.isEmpty()) {
                //Loop through the FormRowSet to find the correct field
                for (FormRow r : formRowSet) {
                    //Get file
                    String files = r.getProperty(fileField);
                    LogUtil.info(getClassName(), "Files found - " + files);

                    if (files != null && !files.isEmpty()) {
                        String[] file_paths = files.split(";");
                        for (String path : file_paths) {
                            //The file path
                            LogUtil.info(getClassName(), "File Path - " + path);

                            File file = FileUtil.getFile(path, tableName, getPk());
                            if (file != null && file.exists()) {
                                LogUtil.info(getClassName(), "File detected - True");
                                return file;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "File handling error");
        }

        //Didn't pass all of the null checks
        return null;
    }

    public String getFileBase64(File file) {
        /*
        Returns the file in a base 64 string format
         */
        if (file == null && !file.exists()) {
            return null;
        }

        String encodeString = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            encodeString = Base64.getEncoder().encodeToString(bytes);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            LogUtil.error(getClassName(), e, "File encoding error");
        }

        return encodeString;
    }
    
    public String getClassName() {
        return getClass().getName();
    }
}
