package ca.regina.ballroom.gui_elements

import scala.swing._

class EmailCenterBoxPanel extends BoxPanel(Orientation.Vertical) {
	val emailSubjectLineLabel = new Label {
      text = "Email subject line"
    }
    val emailSubjectLineTextField = new TextField {
      columns = 20
    }
    
    val emailSubjectBodyLabel = new Label {
      text = "Email subject body"
    }
    val emailSubjectTextArea = new TextArea(10, 80) {
      text = "Proofread your subject body and all other documents before editing."
    }
    
    contents += emailSubjectLineLabel
    contents += emailSubjectLineTextField
    contents += emailSubjectBodyLabel
    contents += new ScrollPane(emailSubjectTextArea)
}