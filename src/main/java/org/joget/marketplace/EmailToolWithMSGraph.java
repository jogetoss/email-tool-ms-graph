package org.joget.marketplace;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;

import org.joget.workflow.model.WorkflowAssignment;

import org.joget.commons.util.LogUtil;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;

public class EmailToolWithMSGraph extends DefaultApplicationPlugin { //extends EmailTool
    private final static String MESSAGE_PATH = "messages/EmailToolWithMSGraph";
    
    @Override
    public String getName() {
        return "Email Tool With the Microsoft Graph API";
    }
    
    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.marketplace.EmailToolWithMSGraph.pluginDesc", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
        
    @Override
    public String getLabel() {
        //return getName();
        return AppPluginUtil.getMessage("org.joget.marketplace.EmailToolWithMSGraph.pluginLabel", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/emailToolWithMSGraphTemplate.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public Object execute(Map properties) {
        //Run all the JSON Tools and Activity
        String accessToken = requestToken(properties);
        sendEmail(accessToken, properties);
        
        //End
        return null;
    }
    
    //Extra functionalities starts here
    public String requestToken(Map properties)
    {
        try
        {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            
            //Call plugin
            //String pluginName = "org.joget.apps.app.lib.JsonTool";
            String pluginName = "org.joget.marketplace.EnhancedJsonTool";
            Plugin plugin = pluginManager.getPlugin(pluginName);
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            
            //JSON URL
            String tenantID = (String) properties.get("tenantId");
            String jsonURL = "https://login.microsoftonline.com/" + tenantID + "/oauth2/v2.0/token";
            
            //Prepare request variables
            List params = new ArrayList();
            
            String clientID = (String) properties.get("clientId");
            Map param = new HashMap();
            param.put("name", "client_id");
            param.put("value", clientID);
            params.add(param);
            
            String clientSecret = (String) properties.get("clientSecret");
            param = new HashMap();
            param.put("name", "client_secret");
            param.put("value", clientSecret);
            params.add(param);
            
            param = new HashMap();
            param.put("name", "grant_type");
            param.put("value", "client_credentials");
            params.add(param);
            
            param = new HashMap();
            param.put("name", "scope");
            param.put("value", "https://graph.microsoft.com/.default");
            params.add(param);
            
            Object[] paramsArray = params.toArray();
            
            //Prepare request headers
            List paramsHeader = new ArrayList();
            
            Object[] paramsArrayHeader = paramsHeader.toArray();
            
            //Prepare rest of the configurations
            Map propertiesMap = new HashMap();
            propertiesMap.put("jsonUrl", jsonURL);
            propertiesMap.put("requestType", "post");
            propertiesMap.put("params", paramsArray);
            propertiesMap.put("headers", paramsArrayHeader);
            
            //Turn on debug mode
            propertiesMap.put("debugMode", "true");
            
            //Set response type
            propertiesMap.put("responseType", "JSON");
            propertiesMap.put("enableFormatResponse", "true");
            propertiesMap.put("script", "data.get(\"access_token\")");
            
            //Obtain properties set
            WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");
            propertiesMap = AppPluginUtil.getDefaultProperties(plugin, propertiesMap, appDef, wfAssignment);
            
            //Set properties
            if (plugin instanceof PropertyEditable) 
            {
                ((PropertyEditable) plugin).setProperties(propertiesMap);
                LogUtil.info("migrateProcess", "set properties");
            }
            
            //Invoke the JSON plugin
            Object res = plugin.execute(propertiesMap);
            
            //Log
            LogUtil.info("requestToken", "Execution end");
            LogUtil.info("requestToken", "The token is: " + res.toString());
            
            //End
            return res.toString();
        } catch (Exception e)
        {
            LogUtil.error("requestToken", e, "Execution error");
            return null;
        }
    }
    
    public void sendEmail(String accessToken, Map properties)
    {
        //When using "getPropetyString" or "getProperty" it returns the field ID
        //Maybe can use hash variables to get the value
        
        try
        {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            
            //Call plugin
            //String pluginName = "org.joget.apps.app.lib.JsonTool";
            String pluginName = "org.joget.marketplace.EnhancedJsonTool";
            Plugin plugin = pluginManager.getPlugin(pluginName);
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            
            //Get info from the form
            String senderEmail = "";
            String toEmail = "";
            String ccString = "";
            String bccString = "";
            String subject = "";
            String messageString = "";
            String attachmentString = "";
            
            FileAndFormExtraction getForm = new FileAndFormExtraction(getPropertyString("formId"), appDef, (WorkflowAssignment) properties.get("workflowAssignment"));
            FormRowSet formRowSet = getForm.getFormRowSet();
            for(FormRow r : formRowSet)
            {
                //The two required variables
                senderEmail = r.getProperty(getPropertyString("fromEmail"));
                toEmail = r.getProperty(getPropertyString("toEmail"));
                
                //The rest
                if(!getPropertyString("cc").isEmpty())
                {
                    ccString = r.getProperty(getPropertyString("cc"));
                }
                if(!getPropertyString("bcc").isEmpty())
                {
                    bccString = r.getProperty(getPropertyString("bcc"));
                }
                if(!getPropertyString("subject").isEmpty())
                {
                    subject = r.getProperty(getPropertyString("subject"));
                }
                if(!getPropertyString("message").isEmpty())
                {
                    messageString = r.getProperty(getPropertyString("message"));
                }
                if(!getPropertyString("attachments").isEmpty())
                {
                    //It returns the name of the file
                    attachmentString = r.getProperty(getPropertyString("attachments"));
                }
                
            }
            
            //Prepare map
            Map propertiesMap = new HashMap();
            Map param = new HashMap();
            List params = new ArrayList();
            Object[] paramsArray;
            
            //JSON URL
            String jsonUrl = "https://graph.microsoft.com/v1.0/users/" + senderEmail + "/sendMail";
            propertiesMap.put("jsonUrl", jsonUrl);
            
            //Turn on debug mode
            propertiesMap.put("debugMode", "true");

            //Set request type
            propertiesMap.put("requestType", "post");

            //Set request headers
            propertiesMap.put("postMethod", "custom");
            
            param.put("name", "Authorization");
            param.put("value", "Bearer " + accessToken);
            params.add(param);
            paramsArray = params.toArray();
            
            propertiesMap.put("headers", paramsArray);

            //Create the JSON payload
            String jsonObjectString = "";
            JSONObject jsonObject = new JSONObject();
            
            //Create Message object
            JSONObject message = new JSONObject();
            //toRecipients
            JSONArray toRecipients = new JSONArray();
            JSONObject emailAddress = new JSONObject();
            JSONObject address = new JSONObject();
            address.put("address", toEmail);
            emailAddress.put("emailAddress", address);
            toRecipients.put(emailAddress); 
            message.put("toRecipients", toRecipients);
            
            //Subject
            message.put("subject", subject);
            
            //Body
            JSONObject body = new JSONObject();
            body.put("contentType", "Text");
            body.put("content", messageString);
            message.put("body", body);

            //ccRecipients
            if(!ccString.isEmpty())
            {
                JSONArray ccRecipients = new JSONArray();
                JSONObject ccEmailAddress = new JSONObject();
                JSONObject ccAddress = new JSONObject();
                ccAddress.put("address", ccString);
                ccEmailAddress.put("emailAddress", ccAddress);
                ccRecipients.put(ccEmailAddress);
                message.put("ccRecipients", ccRecipients);
            }
            
            //bccRecipients
            if(!bccString.isEmpty())
            {
                JSONArray bccRecipients = new JSONArray();
                JSONObject bccEmailAddress = new JSONObject();
                JSONObject bccAddress = new JSONObject();
                bccAddress.put("address", bccString);
                bccEmailAddress.put("emailAddress", bccAddress);
                bccRecipients.put(bccEmailAddress);
                message.put("ccRecipients", bccRecipients);
            }
            
            //attachments
            if(!attachmentString.isEmpty())
            {
                //Get the file
                String fieldId = getPropertyString("attachments");
                AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                String tableName = appService.getFormTableName(appDef, getPropertyString("formId"));
                String fileString = getForm.getFileBase64(getForm.getFile(fieldId, tableName)); 
                
                //Make JSONArray
                JSONArray attachment = new JSONArray();
                JSONObject attachmentDetails = new JSONObject();
                attachmentDetails.put("@odata.type", "#microsoft.graph.fileAttachment");
                attachmentDetails.put("name", attachmentString);
                attachmentDetails.put("contentBytes", fileString);
                attachment.put(attachmentDetails);
                message.put("attachments", attachment);
            }
            
            jsonObject.put("message", message);
            jsonObject.put("saveToSentItems", "false");
            jsonObjectString = jsonObject.toString();
            propertiesMap.put("customPayload", jsonObjectString);
            
            //Obtain properties set
            WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");
            propertiesMap = AppPluginUtil.getDefaultProperties(plugin, propertiesMap, appDef, wfAssignment);
            
            //Set properties
            if (plugin instanceof PropertyEditable) 
            {
                ((PropertyEditable) plugin).setProperties(propertiesMap);
                LogUtil.info("migrateProcess", "set properties");
            }
            
            //Invoke the JSON plugin
            plugin.execute(propertiesMap);
            
            //Log end
            LogUtil.info("sendEmail", "Execution end");
            
        } catch (Exception e)
        {
            LogUtil.error("sendEmail", e, "Execution error");
        }
    }
}
