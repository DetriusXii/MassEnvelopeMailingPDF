package ca.regina.ballroom.gui_elements

import scala.swing._
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import scala.swing.event.ButtonClicked

case class Milliseconds(numMilliseconds: Int)

class TimedWindow(delayBeforeFirstStart: Milliseconds, delegatedMethod: () => Unit) extends Frame {
  var countdownTime: Milliseconds = delayBeforeFirstStart
  
  private val cancelButton = new Button {
    text = "Cancel submission"
    listenTo(this)
    reactions += {
      case ButtonClicked(_) => {
        timer.stop()
        runningThread.interrupt()
      }
    }
  }
  
  private val timedMessageLabel = new Label {
    text = "Starting email submission in %d seconds" format (delayBeforeFirstStart.numMilliseconds / 1000)
  }
  
  private val timer: javax.swing.Timer = new javax.swing.Timer(1000, new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      Swing.onEDT({
        timedMessageLabel.text =  "Starting email submission in %d seconds" format 
        		(countdownTime.numMilliseconds / 1000)
        timedMessageLabel.repaint()
        timedMessageLabel.revalidate()
        TimedWindow.this.repaint()
      })
      
      countdownTime match {
        case Milliseconds(x) if x <= 0 => {
          timer.stop()
          runningThread.start()
          Swing.onEDT({
            timedMessageLabel.text = "Emailing RBDC members.  Hit cancel to stop emailing."
          })
        }
        case _ => {
          countdownTime = Milliseconds(countdownTime.numMilliseconds -  1000)
        }
      }
    }
  })
 
  timer.setDelay(1000)
  timer.setRepeats(true)
  timer.start()
  
  private val runningThread = new Thread(new Runnable {
    def run(): Unit = delegatedMethod()
  })
  
  
  
	contents = new BoxPanel(Orientation.Vertical) {
	  contents += timedMessageLabel
	  contents += cancelButton
	}
  
  override def close(): Unit = {
    timer.stop()
    runningThread.interrupt()
    super.close
  }
}