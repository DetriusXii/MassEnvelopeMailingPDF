package ca.regina.ballroom.pdf_form_processing

import scalaz.effect.IO
import org.apache.pdfbox.pdmodel._
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.edit._

class FormProcessing(emptyRBDCFile: java.io.File, temporaryFile: java.io.File, 
    contactInformation: ContactInformation) {
  private def phoneNumberProcessing(phoneOption: Option[String]): String = {
    val stringBuilder = new StringBuilder(30, "")
    
    val range = 0 until 30
    range.foldLeft(stringBuilder)((u, v) => u += ' ')
    phoneOption.map(phone =>
    	phone.foldLeft((stringBuilder, 0))((u, v) => 
    		(u._1.insert(u._2, v), u._2 + 1)
    ))
    
    stringBuilder.toString
  }
  
  def getPolygon(radius: Float, center: java.awt.Point, numVertices: Int): Tuple2[Array[Float], Array[Float]] = {
    val xs = ((0 until numVertices) map (i =>  
    	(center.getX() + radius*Math.cos((2*Math.PI*i)/numVertices)).asInstanceOf[Float]
  	)).toArray
    
  	val ys = ((0 until numVertices) map (i =>
  		(center.getY() + radius*Math.sin((2*Math.PI*i)/numVertices)).asInstanceOf[Float]
  	)).toArray
  	
  	(xs, ys)
  }
  
  def runFormProcessing: IO[Unit] = { 
	
    val pdfDocument = PDDocument.load(emptyRBDCFile)
    val pdfPage = pdfDocument.getDocumentCatalog().getAllPages().get(0).asInstanceOf[PDPage]
	val pdfFont = PDType1Font.HELVETICA_BOLD
  
	val contentStream = new PDPageContentStream(pdfDocument, pdfPage, true, true)
    pdfPage.getContents().getStream()
    contentStream.beginText()
    contentStream.setFont(pdfFont, 10)
    contentStream.moveTextPositionByAmount(20, 665)
  
    contentStream.drawString("NAME: (last) %s" format contactInformation.lastName )
    contentStream.moveTextPositionByAmount(255, 0)
    contentStream.drawString("(first) %s" format contactInformation.firstName )
    contentStream.moveTextPositionByAmount(200, 0)
    contentStream.drawString("GENDER: M or F")
    contentStream.moveTextPositionByAmount(-455, -15)
    contentStream.drawString("ADDRESS: %s" format contactInformation.address)
    contentStream.moveTextPositionByAmount(250, 0)
    
    contentStream.drawString("CITY/TOWN: %s" format contactInformation.city)
    contentStream.moveTextPositionByAmount(-250, -15)
    contentStream.drawString("POSTAL CODE: %s" format contactInformation.postalCode)
    contentStream.moveTextPositionByAmount(200, 0)
    contentStream.drawString("EMAIL: %s" format contactInformation.email )
    contentStream.moveTextPositionByAmount(-200, -15)
    contentStream.drawString("PHONE: (H) %s" format contactInformation.homephone.getOrElse(""))
    contentStream.moveTextPositionByAmount(150, 0)
    contentStream.drawString("(W) %s" format contactInformation.workphone.getOrElse(""))
    contentStream.moveTextPositionByAmount(150, 0)
    contentStream.drawString("(C) %s" format contactInformation.cellphone.getOrElse(""))
    contentStream.moveTextPositionByAmount(-300, -15)
    contentStream.setFont(pdfFont, 8)
    contentStream.drawString("Are you registering with a dance partner?")
    contentStream.moveTextPositionByAmount(170, 0)
    contentStream.drawString("Yes")
    contentStream.moveTextPositionByAmount(30, 0)
    contentStream.drawString("No")
    contentStream.moveTextPositionByAmount(20, 0)
    contentStream.drawString("PARTNER's NAME (if applicable): %s" format contactInformation.partner.getOrElse(""))
    contentStream.endText()
    
    contentStream.setLineWidth(0.5f)
    contentStream.setStrokingColor(java.awt.Color.BLACK)
    contentStream.drawLine(80, 664, 250, 664)
    contentStream.drawLine(300, 664, 470, 664)
    contentStream.drawLine(75, 649, 250, 649)
    contentStream.drawLine(330, 649, 500, 649)
    contentStream.drawLine(100, 634, 200, 634)
    contentStream.drawLine(255, 634, 400, 634)
    contentStream.drawLine(80, 619, 150, 619)
    contentStream.drawLine(190, 619, 300, 619)
    contentStream.drawLine(340, 619, 450, 619)
    contentStream.drawLine(180, 604, 190, 604)
    contentStream.drawLine(205, 604, 220, 604)
    contentStream.drawLine(370, 604, 550, 604)
    
    
    val circle = contactInformation.gender match {
      case Gender.M => getPolygon(7, new java.awt.Point(527, 669), 1000)
      case Gender.F => getPolygon(7, new java.awt.Point(550, 669), 1000)  
    }
    
    contentStream.drawPolygon(circle._1, circle._2)
    
    contentStream.close()
    pdfDocument.save(temporaryFile)
	
    IO(pdfDocument.close())
  }
}