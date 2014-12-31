package ca.regina.ballroom.gui_elements

import scala.swing._
import scalaz.effect.IO
import scala.swing.event.ButtonClicked

class LeftGridPanel extends GridPanel(6, 2) {
	var selectedRBDCAccessDatabase: Option[java.io.File] = None
	var selectedRBDCRegistrationForm: Option[java.io.File] = None
  
	val rbdcAccessDatabaseLabel = new scala.swing.Label {
      text = "Choose the Access database"
    }
    val rbdcAccessDatabaseButton = new Button {
      text = "Access database"
    } 
    
    val rbdcEmptyRegistrationFormLabel = new scala.swing.Label {
      text = "Choose the location of the empty RBDC registration form"
    }
    val rbdcEmptyRegistrationFormButton = new scala.swing.Button {
      text = "RBDC Registration Form"
    }
    
    val extraAttachmentsLabel = new scala.swing.Label {
      text = "Add extra attachments common to all emails"
    }
    val enterSMPTServerHostnameLabel = new Label {
      text = "Enter SMPT hostname"
    }
    val enterSMPTServerHostnameTextField = new TextField  {
      columns = 20
      text = "plus.smtp.mail.yahoo.com"
    }
    
    
    val enterSMPTServerPortLabel =  new Label  {
      text = "Enter SMPT port number"
    }
    val enterSMPTServerPortTextField = new TextField {
      columns = 20
      text = 465.toString
    }
    
    val enterEMAILUsername = new Label  {
      text = "Enter the email address username"
    }
    val enterEMAILTextField = new TextField {
      columns = 20
      text = "contactrbdc@yahoo.com"
    }
    
    val emailPasswordLabel = new Label  {
      text = "Enter the email address password"
    }
    val emailPWPasswordField = new PasswordField {
      columns = 20
      text = "rbdc1977"
    }
    
    contents += rbdcAccessDatabaseLabel
	contents += rbdcAccessDatabaseButton
	contents += rbdcEmptyRegistrationFormLabel
	contents += rbdcEmptyRegistrationFormButton
	contents += enterSMPTServerHostnameLabel
	contents += enterSMPTServerHostnameTextField
	contents += enterSMPTServerPortLabel
	contents += enterSMPTServerPortTextField
	contents += enterEMAILUsername
	contents += enterEMAILTextField
	contents += emailPasswordLabel
	contents += emailPWPasswordField
	
	listenTo(rbdcAccessDatabaseButton)
    listenTo(rbdcEmptyRegistrationFormButton)
    reactions += {
      case ButtonClicked(b) if b == rbdcAccessDatabaseButton => {
        val fc = new FileChooser
        fc.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
        fc.showOpenDialog(null) match {
          case FileChooser.Result.Approve => selectedRBDCAccessDatabase = Some(fc.selectedFile)
          case _ => selectedRBDCAccessDatabase = None
        }
      }
      case ButtonClicked(b) if b == rbdcEmptyRegistrationFormButton => {
        val fc = new FileChooser
        fc.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
        fc.showOpenDialog(null) match {
          case FileChooser.Result.Approve => selectedRBDCRegistrationForm = Some(fc.selectedFile)
          case _ => selectedRBDCRegistrationForm = None
        }
      }
    }
    
    def getRBDCAccessDatabase: Option[java.io.File] = selectedRBDCAccessDatabase
    def getRBDCRegistrationForm: Option[java.io.File] = selectedRBDCRegistrationForm
}