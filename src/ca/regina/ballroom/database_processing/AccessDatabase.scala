package ca.regina.ballroom.database_processing

import scalaz.effect.IO
import ca.regina.ballroom.pdf_form_processing.ContactInformation
import com.healthmarketscience.jackcess.DatabaseBuilder
import scala.collection.JavaConversions._
import com.healthmarketscience.jackcess.Row
import ca.regina.ballroom.pdf_form_processing.Gender

class AccessDatabase(rbdcDatabase: java.io.File) {
  private class PimpedRow(r: Row) {
    def getColumnField(columnName: String): Option[String] = {
      val columnValue = r.get(columnName)
      if (columnValue != null) Some(columnValue.toString)
      else None
    }
  }
  
	private def mapRowToContactInformation(r: Row): Option[ContactInformation] = {
	  val pimpedRow = new PimpedRow(r)
	  
	  val idOption = pimpedRow.getColumnField("ID").flatMap(idField => try {
	    Some(idField.toInt)
	  } catch {
	    case _: Throwable => None
	  })
	  val firstNameOption = pimpedRow.getColumnField("FIRST NAME")
	  val lastNameOption = pimpedRow.getColumnField("LAST NAME")
	  val genderOption = pimpedRow.getColumnField("GENDER")
	  val provinceOption = pimpedRow.getColumnField("PROV")
	  val postalCodeOption = pimpedRow.getColumnField("PSTL CODE")
	  val partnerOption = pimpedRow.getColumnField("PARTNER")
	  val cellphoneOption = pimpedRow.getColumnField("CELL")
	  val workphoneOption = pimpedRow.getColumnField("WORK")
	  val homephoneOption = pimpedRow.getColumnField("HOME")
	  val addressOption = pimpedRow.getColumnField("ADDRESS")
	  val cityOption = pimpedRow.getColumnField("CITY")
	  val emailOption = pimpedRow.getColumnField("E-MAIL")
	  
	  
	  for (id <- idOption;
		  firstName <- firstNameOption;
		  lastName <- lastNameOption;
		  gender <- genderOption;
		  province <- provinceOption;
		  email <- emailOption;
		  address <- addressOption;
		  city <- cityOption;
		  postalCode <- postalCodeOption;
		  province <- provinceOption) 
	  yield ContactInformation(id, 
	      firstName, lastName, Gender.withName(gender), 
	      cellphoneOption, workphoneOption, homephoneOption, 
	      email, address, city, postalCode, province, partnerOption)
	 
	}
  
	def readMembership: IO[List[ContactInformation]] = {
  	  val db = DatabaseBuilder.open(rbdcDatabase)
	  val membership = db.getTable("Membership")
	  val classTable = db.getTable("Class")
	  
	  val contactInformations = membership.foldLeft[List[ContactInformation]](Nil)((u, v) => 
	  	mapRowToContactInformation(v) match {
	  	  case Some(x) => x :: u
	  	  case None => u
	  	}
	  )
	  
	  IO(contactInformations)
	}
}