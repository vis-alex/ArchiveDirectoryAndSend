
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static final String SOURCE = ".";
    public static final String ARCHIVE = "output.zip";
    public static final String PROPERTIES = "src/mail.properties";

    public static void main(String[] args) {
        File file = new File(SOURCE);

        try(ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(ARCHIVE)))
        {
            File[] files = file.listFiles();

            createZipDirectory(zout, files, SOURCE);

            file = new File(ARCHIVE);
            sendFile(file, PROPERTIES);

        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        } finally {
            file.delete();
        }

    }

    private static void sendFile(File file, String props) throws MessagingException, IOException {

        final Properties properties = new Properties();
        properties.load(new FileReader(props));

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("login"), properties.getProperty("password"));
            }
        });

        Message message = new MimeMessage(session);
        message.setSubject("Email with attached root java app directory");

        Address addressTo = new InternetAddress(properties.getProperty("receiver"));
        message.setRecipient(Message.RecipientType.TO, addressTo);

        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart attachment = new MimeBodyPart();
        attachment.attachFile(file);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("<h1>Please download the file and change extension for '.alex' to '.zip'" +
                " It was done because gmail doesnt allow to transfer '.zip'</h1>", "text/html");

        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachment);

        message.setContent(multipart);

        Transport.send(message);
    }

    private static void createZipDirectory(ZipOutputStream zout, File[] files, String path) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                createZipDirectory(zout, file.listFiles(), path + file.getName() + "/");
            } else if ( !file.getName().equals(ARCHIVE) ) {
                ZipEntry entry = new ZipEntry(path + file.getName());
                zout.putNextEntry(entry);

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[2048];
                int size = -1;
                while ( (size = fis.read(buffer)) != -1 ) {
                    zout.write(buffer, 0, size);
                }
            }
        }
    }
}
