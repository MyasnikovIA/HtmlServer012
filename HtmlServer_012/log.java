import java.io.File;import java.io.FileNotFoundException; import java.io.IOException; import java.nio.file.Files; import java.nio.file.Paths; import java.util.Hashtable;  public class log {      public static void main(String[] args) throws FileNotFoundException, IOException {          File f = new File("LOG");          File[] paths = f.listFiles();          System.out.write(("HTTP/1.1 200 OK\r\n"                   + "Content-Type: text/html; charset=utf-8\r\n"                  + "Connection: close\r\n"                     + "Server: HTMLserver\r\n\r\n").getBytes());        for (File path : paths) {            if (path.exists() && !path.isDirectory()) {                if (path.getName().contains(".txt")) {                    System.out.write(("<a style='color: rgb(10, 255, 128); ' href='/LOG/" + path.getName() + "'>" + path.getName() + "</a> <br>").getBytes());                  }            }        }    }}