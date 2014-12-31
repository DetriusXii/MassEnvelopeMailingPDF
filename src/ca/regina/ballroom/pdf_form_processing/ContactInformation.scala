package ca.regina.ballroom.pdf_form_processing

object Gender extends Enumeration {
  type Gender = Value
  val M, F = Value
}

case class ContactInformation(
    id: Int,
    firstName: String, 
    lastName: String, 
    gender: Gender.Gender,
    cellphone: Option[String],
    workphone: Option[String],
    homephone: Option[String],
    email: String,
    address: String,
    city: String,
    postalCode: String,
    province: String,
    partner: Option[String])