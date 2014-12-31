package ca.regina.ballroom.gui_elements

import scala.swing._
import scala.swing.event.ButtonClicked

class RightBoxPanel extends BoxPanel(Orientation.Vertical) {
	val addExtraAttachmentButton = new Button {
      text = "Add extra attachment"
    }
    
    val extraAttachmentsBoxPanel = new BoxPanel(Orientation.Vertical)
    
    contents += addExtraAttachmentButton
    contents += new ScrollPane(extraAttachmentsBoxPanel)
    
    private def removeItemFromListView(removeAttachmentButton: RemoveAttachmentButton): Unit = Swing.onEDT {
      extraAttachmentsBoxPanel.contents -= removeAttachmentButton
      extraAttachmentsBoxPanel.revalidate()
      extraAttachmentsBoxPanel.repaint()
    }
    
    listenTo(addExtraAttachmentButton)
    reactions += {
      case ButtonClicked(b) if b == addExtraAttachmentButton => {
        val fc = new FileChooser
        fc.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
        val selectedFileOption = fc.showOpenDialog(null) match {
            case FileChooser.Result.Approve => Some(fc.selectedFile)
            case _ => None
        }
        selectedFileOption.map(f => {
          val removeAttachmentButton = new RemoveAttachmentButton(f)
          listenTo(removeAttachmentButton)
		  reactions += {
		    case ButtonClicked(s) if s == removeAttachmentButton => removeItemFromListView(removeAttachmentButton)
		  }
  
        
          Swing.onEDT {
	    	extraAttachmentsBoxPanel.contents += removeAttachmentButton
			extraAttachmentsBoxPanel.revalidate()
			extraAttachmentsBoxPanel.repaint()
          }
        })
      }
    }
}