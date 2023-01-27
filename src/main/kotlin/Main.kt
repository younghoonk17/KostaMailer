import java.io.File
import java.io.InputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


fun main(args: Array<String>) {

    print("Enter csv location: ")

    val csvLocation = readLine()!!
        //"C:\\Users\\cyhki\\Desktop\\input.csv"

    val inputStream = File(csvLocation).inputStream()
    val users = readCsv(inputStream)

    for ((index,user) in users.withIndex()) {
        println("Trying to send: ${user.name}")
        sendEmail(user.email, user.filePath, user.content, user.subject)
        println("Send complete for: ${user.name}")
        println("Total: ${index+1}/${users.count()}")
    }

}

data class CsvFile(
    val name: String,
    val filePath: String,
    val email: String,
    val content: String,
    val subject: String
)

fun readCsv(inputStream: InputStream): List<CsvFile> {
    val reader = inputStream.bufferedReader()
    val header = reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, filePath, email, subject, content) = it.split(',', ignoreCase = false, limit = 5)
            CsvFile(name.trim(), filePath.trim(), email.trim(), subject.trim(), content.trim())
        }.toList()
}

private fun sendEmail(
    emailTo: String,
    filepath: String,
    content: String,
    subject: String
) {
    val userName = "c.yhkim17@gmail.com"
    val password = "zphjpfmegcfbvdrk"

    val props = Properties()
    putIfMissing(props, "mail.smtp.host", "smtp.gmail.com")
    putIfMissing(props, "mail.smtp.port", "587")
    putIfMissing(props, "mail.smtp.auth", "true")
    putIfMissing(props, "mail.smtp.starttls.enable", "true")

    val session = Session.getDefaultInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(userName, password)
        }
    })

    session.debug = false

    try {
        val mimeMessage = MimeMessage(session)
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo, false))
        mimeMessage.subject = subject
        mimeMessage.sentDate = Date()

        val messageBodyPart: BodyPart = MimeBodyPart()
        messageBodyPart.setText(content)

        val attachmentPart = MimeBodyPart()
        attachmentPart.attachFile(File(filepath))

        val multipart: Multipart = MimeMultipart()
        multipart.addBodyPart(messageBodyPart)
        multipart.addBodyPart(attachmentPart)

        mimeMessage.setContent(multipart)

        val smtpTransport = session.getTransport("smtp")
        smtpTransport.connect()
        smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
        smtpTransport.close()
    } catch (messagingException: MessagingException) {
        messagingException.printStackTrace()
    }
}

private fun putIfMissing(props: Properties, key: String, value: String) {
    if (!props.containsKey(key)) {
        props[key] = value
    }
}
