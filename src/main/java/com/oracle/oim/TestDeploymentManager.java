package com.oracle.oim;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.NamingException;

import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
import com.thortech.xl.vo.ddm.RootObject;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcBulkException;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import oracle.iam.platform.OIMClient;

public class TestDeploymentManager {

 private static OIMClient getOIMClient(){
  Hashtable env = new Hashtable(); 
  System.setProperty("java.security.auth.login.config", "authwl.conf");   //Update path of authwl.conf file according to your environment
  System.setProperty("OIM.AppServerType", "wls");  
  System.setProperty("APPSERVER_TYPE", "wls");   
  env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL,"weblogic.jndi.WLInitialContextFactory");
  env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, "t3://oimpoc.idhdw.com.au:14000");
  OIMClient oimClient = new OIMClient(env);
  try{
   oimClient.login("xelsysadm", "Poc$1234".toCharArray());
   System.out.println("oim login success");
  }catch(Exception e){
   e.printStackTrace();
  }
  return oimClient;

 }

  public static void importConfigXML(String importFileName, String category){
   OIMClient client= getOIMClient();
   StringBuffer sb= new StringBuffer();
   BufferedReader br = null;
   tcImportOperationsIntf importOpsintf = client.getService(tcImportOperationsIntf.class);
   try {
    
    importOpsintf.acquireLock(true);
    boolean acquire=importOpsintf.isLockAcquired();
    br = new BufferedReader(new FileReader(importFileName));
           String readLine=null;
           while((readLine =br.readLine()) != null)
           {
                       sb.append(readLine+"\n");
           }
           if(acquire){
            Collection importFiles=importOpsintf.addXMLFile(importFileName, sb.toString()); 
            Collection missingDepndencies= importOpsintf.getMissingDependencies(importFiles, category); 
              if(missingDepndencies.isEmpty())
                 {
               importOpsintf.performImport(importFiles);
               System.out.println("import success");
                 } 
               }
    
   } catch (DDMException e) {
    e.printStackTrace();
   } catch (tcAPIException e) {
    e.printStackTrace();
   } catch (SQLException e) {
    e.printStackTrace();
   } catch (NamingException e) {
    e.printStackTrace();
   } catch (FileNotFoundException e) {
    e.printStackTrace();
   } catch (IOException e) {
    e.printStackTrace();
   } catch (TransformationException e) {
    e.printStackTrace();
   } catch (tcBulkException e) {
    e.printStackTrace();
   }
  }


 public static void exportToConfigXML(String exportFile, String objectToExport, String export_object_type) {
  try {
   OIMClient client= getOIMClient();
   FileWriter fstream = new FileWriter(exportFile);
   BufferedWriter out = new BufferedWriter(fstream);
   tcExportOperationsIntf moExportUtility = (tcExportOperationsIntf) client.getService(tcExportOperationsIntf.class);
   Collection lstObjects = moExportUtility.findObjects(export_object_type, objectToExport);
   System.out.println(lstObjects);
   System.out.println("Dependencies --");
   System.out.println(moExportUtility.getDependencies(lstObjects));
   System.out.println("Children --");
   System.out.println(moExportUtility.retrieveChildren(lstObjects));
   System.out.println("Dependencie Tree --");
   System.out.println(moExportUtility.retrieveDependencyTree(lstObjects));
   lstObjects.addAll(moExportUtility.getDependencies(lstObjects));
   lstObjects.addAll(moExportUtility.retrieveChildren(lstObjects));
   lstObjects.addAll(moExportUtility.retrieveDependencyTree(lstObjects));
   String s = moExportUtility.getExportXML(lstObjects, "*");    
   out.write(s);
   System.out.println("Objects successfully exported");
   out.close();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }

 public static void main(String[] args){
                String export_object_type="Plugin";
  exportToConfigXML("C:\\Users\\souvsarkar\\Documents\\OIMLookups.xml", "com.identityhub.oim.tasks.IDHUBProcessEmploymentChangeTask",export_object_type);
 // importConfigXML("C:\\MyDocs\\export\\OIMLookups.xml","Lookup");
 }
}