package ca.regina.ballroom

import scalaz._
import scalaz.effect._
import scalaz.iteratee._
import scalaz.Validation.FlatMap._
import java.io._
import org.apache.pdfbox.pdmodel._
import org.apache.pdfbox.pdmodel.common._
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import scala.swing._
import scala.swing.event._
import org.apache.commons.mail._
import ca.regina.ballroom.gui_elements._
import ca.regina.ballroom.models._


abstract class Length
object Millimeter extends Length


object Main extends scala.swing.SimpleSwingApplication {
  val STANDARD_WIDTH = (241f, Millimeter)
  val STANDARD_HEIGHT = (104f, Millimeter)
  val DEFAULT_USER_SPACE_UNIT_DPI = 72
  val MM_TO_UNITS = DEFAULT_USER_SPACE_UNIT_DPI/(10*2.54f)
  
  def top: Frame = new MainFrame {
    val mainFrameInstance = this
    size = new java.awt.Dimension(600, 480)
    visible = true
    
    private val leftGridPanel = new LeftGridPanel
    private val emailCenterBoxPanel = new EmailCenterBoxPanel
    private val rightBoxPanel = new RightBoxPanel 
    
    
    contents = new BorderPanel() {
      layout(leftGridPanel) = BorderPanel.Position.West
      layout(emailCenterBoxPanel) = BorderPanel.Position.Center
      layout(rightBoxPanel) =  BorderPanel.Position.East
      
      layout(new Button {
        text = "Begin mass email"
          
        listenTo(this)
        reactions += {
          case ButtonClicked(_) => Swing.onEDT {
            //mainFrameInstance.visible = false
            //new TimedWindow(Milliseconds(10000), triggerStartEmailConfirmation).open
            
            new TimedDialog(Milliseconds(10000), triggerStartEmailConfirmation, mainFrameInstance).open
          }
        }
      }) = BorderPanel.Position.South
    }
    
      
    def validateAllFields() : Validation[List[String], StartEmailContext] = {
      val hostnameEither = leftGridPanel.enterSMPTServerHostnameTextField.text.trim() match {
        case "" => Failure("Please enter a hostname field.")
        case x => Success(x)
      }
      val portNumberEither = leftGridPanel.enterSMPTServerPortTextField.text.trim() match {
        case "" => Failure("Please enter a portnumber for the email server.")
        case x => try {
          Success(x.toInt)
        } catch {
          case _: Throwable => Failure("The port number must be a valid integer number.")
        }
      }
      val usernameEither = leftGridPanel.enterEMAILTextField.text.trim() match {
        case "" => Failure("Please enter an email address username for the server.")
        case x => Success(x)
      }
      
      
      val passwordEither = leftGridPanel.emailPWPasswordField.password.mkString("").trim() match {
        case "" => Failure("Please enter a password for the server.")
        case x => Success(x)
      }
      val subjectLineEither = emailCenterBoxPanel.emailSubjectLineTextField.text.trim() match {
        case "" => Failure("Please enter a subject line for the email message to be sent to all members.")
        case x => Success(x)
      }
      val subjectBodyEither = emailCenterBoxPanel.emailSubjectTextArea.text.trim() match {
        case "" => Failure("Please enter a message body for the email address to be sent to all members.")
        case x => Success(x)
      }
      
      val accessDatabaseFileEither = leftGridPanel.getRBDCAccessDatabase match {
        case Some(f: java.io.File) => Success(f)
        case None => Failure("Please choose the RBDC access database")
      } 
      
      val rbdcEmptyRegistrationFormEither = leftGridPanel.getRBDCRegistrationForm match {
        case Some(f: java.io.File) => Success(f)
        case None => Failure("Please choose the RBDC empty Registration form")
      }
      
      val listOfEithers = hostnameEither :: portNumberEither :: usernameEither :: passwordEither :: subjectLineEither :: 
       subjectBodyEither :: accessDatabaseFileEither :: rbdcEmptyRegistrationFormEither :: Nil
       
      val errorLog = listOfEithers.foldLeft[List[String]](Nil)((u, v) => v match {
        case Failure(x) => x :: u
        case _ => u
      })
      
      val startEmailContextValidation = for (hostname <- hostnameEither;
    	portNumber <- portNumberEither;
    	username <- usernameEither;
    	password <- passwordEither;
    	subjectLine <- subjectLineEither;
    	subjectBody <- subjectBodyEither;
    	accessDatabaseFile <- accessDatabaseFileEither;
    	rbdcEmptyRegistrationForm <- rbdcEmptyRegistrationFormEither
      ) yield new StartEmailContext(hostname, 
          portNumber, 
          username, 
          password, 
          subjectLine, 
          subjectBody,
          accessDatabaseFile,
          rbdcEmptyRegistrationForm,
          getExtraAttachmentsList())
      
      startEmailContextValidation.fold(_ => Failure(errorLog), Success(_))
    }
    
    
    def getExtraAttachmentsList(): List[java.io.File] = {
      def asInstanceOf(a: Component): Option[RemoveAttachmentButton] = a.isInstanceOf[RemoveAttachmentButton] match {
        case true => Some(a.asInstanceOf[RemoveAttachmentButton])
        case false => None
      }
      
      rightBoxPanel.extraAttachmentsBoxPanel.contents.foldLeft[List[java.io.File]](Nil)((u, v) => asInstanceOf(v) match {
        case Some(ip) => ip.file :: u
        case None => u
      })
    }
    
    
    def triggerStartEmailConfirmation(label: Label): Unit = {
      val startEmailContextValidation = validateAllFields()
      startEmailContextValidation match {
        case Success(x) => new BallroomDanceProcessing(x).startEmailMethod
        case Failure(x) => Swing.onEDT {
          label.text = x.toString
          label.repaint
          label.revalidate
        }
      }
    }
  }
  
  
  
  /*def accumulateLines: IterateeT[IoExceptionOr[String], IO, IoExceptionOr[List[String]]] = {
    def step(linesIoException: IoExceptionOr[List[String]]): Input[IoExceptionOr[String]] => IterateeT[IoExceptionOr[String], IO, IoExceptionOr[List[String]]] = {
      case Input.Element(x) => IterateeT(IO(StepT.scont(step(linesIoException.flatMap(lines => x.map(_ :: lines))))))
      case Input.Empty() => IterateeT(IO(StepT.scont(step(linesIoException))))
      case Input.Eof() => 
        IterateeT(IO(StepT.sdone(linesIoException.map(_.reverse), Input.eofInput[IoExceptionOr[String]])))
    }
    IterateeT(IO(StepT.scont(step(IoExceptionOr(Nil)))))
  }
  
  def parseCSVFile(csvFile: java.io.File): IO[List[FileNameEmailAddress]] = {
	  val fileReader = new java.io.FileReader(csvFile)
	  val bufferedReader = new java.io.BufferedReader(fileReader)
	  
	  def enumReaderLines(br: java.io.BufferedReader): EnumeratorT[IoExceptionOr[String], IO] = {
	    EnumeratorT.enumIoSource(() => IoExceptionOr(br.readLine()),
	        gotdata = (i: IoExceptionOr[String]) => i.exists(_ != null), 
	        render = ((line: String) => line)
	    )
	  }
	  
	  (accumulateLines &= enumReaderLines(bufferedReader)).
	  	run.
	  	map(_.valueOr(Nil)).
	  	map(_.map(fileLine => {
	  	  val separatedLine = fileLine.split(',')
	  	  val fileName =  separatedLine(0)
	  	  val emailAddress = separatedLine(1)
	  	  
	  	  new FileNameEmailAddress(fileName, emailAddress)
	  	}))  	
  }*/
  
  /*def printLines: IterV[String, IO[Unit]] = {
    def step(currentIO: IO[Unit])(input: Input[String]): IterV[String, IO[Unit]] = 
    	input match {
	      case El(x) => Cont(
	          step(
	              currentIO.flatMap(_ => putStrLn(x))
			  )
	      )
	      case IterV.Empty() => Cont(step(currentIO))
	      case EOF() => Done(currentIO, EOF[String])
	    }
    Cont(step(io()))
  }*/

}