package ca.regina.ballroom.gui_elements

import scala.swing._
import scalaz.effect._
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import scala.swing.event.ButtonClicked

class TimedDialog (
	delayBeforeFirstStart: Milliseconds, delegatedMethod: (Label) => Unit, parentFrame: Frame) extends Dialog(parentFrame) {
  this.modal = true
  
  val countdownTimeIORef: IORef[Milliseconds] = IO.newIORef({delayBeforeFirstStart}).unsafePerformIO()
  
  private val timedMessageLabel = new Label {
    text = "Starting email submission in %d seconds" format (delayBeforeFirstStart.numMilliseconds / 1000)
  }
  private val cancelButton = new Button {
    text = "Cancel"
  }
  
  listenTo(cancelButton)
  reactions += {
    case ButtonClicked(_) => {
      timer.stop()
      runningThread.interrupt()
      Swing.onEDT {
        this.close()
      }
    }
  }
  
  contents = new BoxPanel(Orientation.Vertical) {
    contents += timedMessageLabel
    contents += cancelButton
  }
  
  private val timer: javax.swing.Timer = new javax.swing.Timer(1000, new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      Swing.onEDT({
        countdownTimeIORef.read.map(countdownTime => {
	        timedMessageLabel.text =  "Starting email submission in %d seconds" format 
        	        (countdownTime.numMilliseconds / 1000)
        		timedMessageLabel.repaint()
        		timedMessageLabel.revalidate()
        }).unsafePerformIO()
      })
      
      countdownTimeIORef.read.flatMap(_ match {
	        case Milliseconds(x) if x <= 0 => {
	          timer.stop()
	          runningThread.start()
	          Swing.onEDT({
	            timedMessageLabel.text = "Emailing RBDC members.  Hit cancel to stop emailing."
	          })
	          IO(())
	        }
	        case Milliseconds(x) => {
	          val newTime = x - 1000
	          countdownTimeIORef.write(Milliseconds(newTime)) //= // Milliseconds(countdownTime.numMilliseconds -  1000)
	        }
      	}).unsafePerformIO()
    }
  })
  
  
  
  timer.setDelay(1000)
  timer.setRepeats(true)
  timer.start()
  
  private val runningThread = new Thread(new Runnable {
    def run(): Unit = delegatedMethod(timedMessageLabel)
  })
  
}