package ca.regina.ballroom

import ca.regina.ballroom.models.StartEmailContext
import ca.regina.ballroom.database_processing.AccessDatabase
import org.apache.commons.mail.MultiPartEmail
import ca.regina.ballroom.pdf_form_processing.ContactInformation
import org.apache.commons.mail.DefaultAuthenticator
import ca.regina.ballroom.pdf_form_processing.FormProcessing
import scalaz.effect.IO
import scalaz.effect.IO._

class BallroomDanceProcessing(startEmailContext: StartEmailContext) {
	def startEmailMethod: Unit = {
      val membershipsIO = new AccessDatabase(startEmailContext.rbdcAccessDatabaseFile).readMembership
      
      val temporaryFile = java.io.File.createTempFile("RBDCRegistrationForm", ".pdf")
      
      
      def buildEmail(contactInformation: ContactInformation): IO[MultiPartEmail] = {
    	  val formProcessing = new FormProcessing(startEmailContext.rbdcEmptyRegistrationForm,
    			  temporaryFile, contactInformation
    	  )
    	  formProcessing.runFormProcessing.map(_ => {
    		  val email = new MultiPartEmail
      
		      email.setHostName(startEmailContext.hostname)
		      email.setSmtpPort(startEmailContext.portNumber)
		      email.setAuthenticator(new DefaultAuthenticator(startEmailContext.username, 
		          startEmailContext.password))
		      email.setSSLOnConnect(true)
		      email.setFrom(startEmailContext.username)
		      email.setMsg(startEmailContext.subjectBody)
		      email.setSubject(startEmailContext.subjectLine)
		      
		      email.attach(temporaryFile)
		      email.addTo(contactInformation.email)
		      
		      val extraAttachedEmail =
		        startEmailContext.extraAttachments.foldLeft(email)((u, v) => email.attach(v))
		      
		      extraAttachedEmail
    	  })
        
    	  
      }
	  
      membershipsIO.flatMap(_.foldLeft(IO(()))((_, v) => buildEmail(v).map(_.send()))).unsafePerformIO
    }
}