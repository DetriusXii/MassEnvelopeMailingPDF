package ca.regina.ballroom.gui_elements

import scala.swing._
import scala.swing.event._
import java.io.File

class RemoveAttachmentButton(val file: File) extends Button {
  text = "Remove %s" format file.getName()
}