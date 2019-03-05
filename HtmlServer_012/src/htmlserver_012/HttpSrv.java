package htmlserver_012;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.html.HTML;
import sun.misc.IOUtils;
import java.awt.event.*;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import net.arnx.jsonic.JSON;

import sun.reflect.Reflection;

/**
 *
 * @author myasnikov
 */
public class HttpSrv {

    private static int numComp = 0;
    private static String IPmac = "";
    private static String rootPath = "C:\\AppServ\\www";
    private static String startFile = "inex.htm";
    public static int port = 9090;
    public static int TimeOut = 300;
    private static String libPath = "C:\\AppServ\\www\\lib";
    private static String javaBinPath = "C:\\Java\\jdk1.7.0_21\\bin";
    static private boolean isDev = false;
    private boolean process = false;
    /**
     * список IP адресов которым ограничен доступ
     */
    public static Hashtable<String, String> BlackList = new Hashtable<String, String>(10, (float) 0.5);
    /**
     * списко текстовых фраз, для фильтрации
     */
    public static List<String> BlockText = new ArrayList<String>();
    public static Hashtable<String, String> ClientList = new Hashtable<String, String>(10, (float) 0.5);

    //            String authString = "user" + ":" + "123";
    //            code = new String(Base64.encode(authString.getBytes()));
    /**
     * Запуск вэб сервера<br>
     * Пример: new HTTP().start("C:\\AppServ\\www",
     * "9090","C:\\AppServ\\www\\lib", true);
     *
     * @param rootPath локальный адрес нахождения вэб страниц
     * @param port порт на котором будет работать сервер
     * @param libPath расположение подключаемых библиотек
     * @param isDev Режим разработчика сайта (true - классы java каждый раз
     * компилируются, false классы java компилируются 1 раз )
     *
     *
     */
    public void Start(String rootPath, String startFile, String port, String libPath, String javaBinPath, boolean isDev, String TimeOut) {
        process = true;

        // получаем IP адрес и MAC адрес сервера
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            IPmac = ip.getHostAddress() + "|" + new String(network.getHardwareAddress());
        } catch (Exception ex) {
            IPmac = "NoIP|Nomac";
        }
        if (rootPath.length() != 0) {
            this.rootPath = rootPath;
        }
        if (startFile.length() != 0) {
            this.startFile = startFile;
        }
        if (port.length() != 0) {
            this.port = Integer.valueOf(port);
        }
        if (TimeOut.length() != 0) {
            this.TimeOut = Integer.valueOf(TimeOut) * 1000;
        } else {
            this.TimeOut = this.TimeOut * 1000;
        }
        // if (libPath.length() != 0) {
        this.libPath = libPath;
        // }
        if (javaBinPath.length() != 0) {
            this.javaBinPath = javaBinPath;
        }
        this.isDev = isDev;
        File filesLibPath = new File((new File(HttpSrv.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + "\\lib");
        copyAllLib(filesLibPath);

        CreateBlackList();

        Thread myThready;
        myThready = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket ss = new ServerSocket(HttpSrv.port);
                    while (process == true) {
                        numComp++;
                        Socket socket = ss.accept();
                        new Thread(new SocketProcessor(socket)).start();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Throwable ex) {
                    Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        myThready.start();	//Запуск потока

    }

    /**
     * чтение черного списка из файла
     */
    private void CreateBlackList() {
        BlackList.clear();
        String osWin = System.getProperty("os.name").toLowerCase();
        String setupPathBlackListPath = "";
        if (osWin.indexOf("win") >= 0) {
            setupPathBlackListPath = System.getProperty("java.io.tmpdir") + "BlackList.dat";
        } else {
            setupPathBlackListPath = "BlackList.dat";
        }
        try {
            File f = new File(setupPathBlackListPath);
            if (f.exists() && !f.isDirectory()) {
                DefaultListModel philosophers = new DefaultListModel();
                InputStream fis = new FileInputStream(setupPathBlackListPath);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    BlackList.put(line, line);
                }
                br.close();
                isr.close();
                fis.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Копирование всех файлов из каталога LIB в папку lib располежения html
     * страниц Все копируется в одну папку
     *
     * @param filList
     */
    private void copyAllLib(File filList) {
        if (filList.exists()) {
            if (filList.isDirectory()) {
                File[] filesLib = filList.listFiles();
                for (File file : filesLib) {
                    if (file.isDirectory()) {
                        copyAllLib(file);
                    } else {
                        try {
                            File fileTo = new File(this.libPath + "\\" + file.getName());
                            Files.copy(file.toPath(), fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Остановить сервер
     */
    public void Stop() {
        process = false;
    }

    private static class SocketProcessor implements Runnable {

        private Hashtable<String, Object> Json = new Hashtable<String, Object>(10, (float) 0.5);
        private Hashtable<String, Object> JsonParam = new Hashtable<String, Object>(10, (float) 0.5);
        private static Object StandardLog;
        private Socket socket;
        private InputStream is;
        private OutputStream os;
        private String contentZapros = "";

        private SocketProcessor(Socket socket) throws Throwable {
            this.socket = socket;
            this.socket.setSoTimeout(TimeOut);
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
            Json.clear();
            JsonParam.clear();

            String Adress = socket.getRemoteSocketAddress().toString();
            Json.put("RemoteIPAdress", Adress);
            Json.put("libPath", libPath);
            Json.put("javaBinPath", javaBinPath);
            if (isDev == true) {
                Json.put("isDev", "1");
            }
            Adress = Adress.split(":")[0];
            Adress = Adress.replace("/", "");
            InetAddress address = InetAddress.getByName(Adress);
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                Json.put("NetworkInterface", ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    Json.put("RemoteMacAdress", sb.toString());
                }
            }
        }

        public void run() {
            try {
                PrintStream out = new PrintStream(os);
                System.setOut(out);
                System.setErr(out);

                readInputHeaders();
            } catch (Throwable t) {
            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
        }
        private List<String> param = new ArrayList<String>();
        private byte[] POST = new byte[0];
        private String Koderovka = "";
        //  private String getCommand = "";
        private String getCmd = "";

        /**
         * Чтение входных данных от клиента
         *
         * @throws IOException
         */
        private void readInputHeaders() throws IOException {
            //  FileWriter outLog = new FileWriter(rootPath + "\\log.txt", true); //the true will append the new data
            //  outLog.write("add a line\n");//appends the string to the file
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            contentZapros = "";
            StringBuffer sbInData = new StringBuffer();
            int numLin = 0;
            InputStreamReader isr = new InputStreamReader(is);
            int charInt;
            char[] charArray = new char[1024];
            // Читаем заголовок
            StringBuffer sb = new StringBuffer();
            StringBuffer sbTmp = new StringBuffer();
            while ((charInt = isr.read()) > 0) {
                if (socket.isConnected() == false) {
                    return;
                }
                //    os.write((char) charInt);
                //    outLog.write((char) charInt);
                sbTmp.append((char) charInt);
                if (sbTmp.toString().indexOf("\n") != -1) {
                    // если в первой строке невстречается слово GET или POST, тогда отключаем соединение
                    if (sb.toString().split("\n").length == 1) {
                        int res = sb.toString().indexOf("GET");
                        if (res == -1) {
                            res = sb.toString().indexOf("POST");
                            if (res == -1) {
                                is.close();
                                os.close();
                                socket.close();
                                return;
                            }
                        }
                    }
                    if (sbTmp.toString().length() == 2) {
                        break; // чтение заголовка окончено
                    }
                    sbTmp.setLength(0);
                }
                sb.append((char) charInt);
            }

            // FileWriter outLog = new FileWriter("D:\\HtmlServer_012\\LOG!!!!.txt", true); //the true will append the new data
            // outLog.write(sb.toString() + "\r\n-------------\r\n");
            if (sb.toString().indexOf("Content-Length: ") != -1) {
                String sbTmp2 = sb.toString().substring(sb.toString().indexOf("Content-Length: ") + "Content-Length: ".length(), sb.toString().length());
                String lengPostStr = sbTmp2.substring(0, sbTmp2.indexOf("\n")).replace("\r", "");
                int LengPOstBody = Integer.valueOf(lengPostStr);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((charInt = isr.read()) > 0) {
                    if (socket.isConnected() == false) {
                        return;
                    }
                    // outLog.write((char) charInt);
                    buffer.write((char) charInt);
                    LengPOstBody--;
                    //  outLog.write(LengPOstBody+"\r\n");
                    if (LengPOstBody == 0) {
                        break;
                    }
                }
                buffer.flush();
                POST = buffer.toByteArray();
                Json.put("PostBodyText", JSON.encode(new String(buffer.toByteArray())));
                Json.put("PostBodyByte", buffer.toByteArray());
            }
            // outLog.close();
            int indLine = 0;
            for (String TitleLine : sb.toString().split("\r\n")) {
                indLine++;
                if (indLine == 1) {
                    TitleLine = TitleLine.replaceAll("GET /", "");
                    TitleLine = TitleLine.replaceAll("POST /", "");
                    TitleLine = TitleLine.replaceAll(" HTTP/1.1", "");
                    TitleLine = TitleLine.replaceAll(" HTTP/1.0", "");
                    contentZapros = java.net.URLDecoder.decode(TitleLine, "UTF-8");
                    Json.put("ContentZapros", contentZapros);
                    if (contentZapros.indexOf("?") != -1) {
                        String tmp = contentZapros.substring(0, contentZapros.indexOf("?") + 1);
                        String param = contentZapros.replace(tmp, "");
                        getCmd = param;
                        Json.put("ParamAll", param);
                        int indParam = 0;
                        for (String par : param.split("&")) {
                            String[] val = par.split("=");
                            if (val.length == 2) {
                                val[0] = java.net.URLDecoder.decode(val[0], "UTF-8");
                                val[1] = java.net.URLDecoder.decode(val[1], "UTF-8");
                                Json.put(val[0], val[1]);
                                val[0] = val[0].replace(" ", "_");
                                Json.put(val[0], val[1]);
                                JsonParam.put(val[0], val[1]);
                                if (!val[0].equals("method")) {
                                    this.param.add(val[1]);
                                }
                            } else {
                                indParam++;
                                val[0] = java.net.URLDecoder.decode(val[0], "UTF-8");
                                Json.put("Param" + String.valueOf(indParam), val[0]);
                                this.param.add(val[0]);
                                JsonParam.put("Param" + String.valueOf(indParam), val[0]);
                            }
                        }
                        contentZapros = tmp.substring(0, tmp.length() - 1);//.toLowerCase()
                    }
                    Json.put("Zapros", contentZapros);
                    Json.put("RootPath", rootPath);
                    Json.put("AbsalutZapros", rootPath + "\\" + contentZapros);
                } else {
                    if (TitleLine == null || TitleLine.trim().length() == 0) {
                        break;
                    }
                    if (TitleLine.split(":").length > 0) {
                        String val = TitleLine.split(":")[0];
                        val = val.replace(" ", "_");
                        Json.put(val, TitleLine.replace(TitleLine.split(":")[0] + ":", ""));
                    }
                    if (TitleLine.indexOf("Authorization:") == 0) {
                        //Authorization: Basic dXNlcjoxMjM=
                        String coderead = TitleLine.replaceAll("Authorization: Basic ", "");
                        Json.put("Author", TitleLine.replaceAll("Authorization: Basic ", ""));
                    }
                }
            }
            //
            // кодировка входных данных
            if (Json.containsKey("Content-Type") == true) {
                // Content-Type: text/html; charset=windows-1251
                if (Json.get("Content-Type").toString().split("charset=").length == 2) {
                    Json.put("Charset", Json.get("Content-Type").toString().split("charset=")[1]);
                }
            }
            // Парсим Cookie если он есть
            if (Json.containsKey("Cookie") == true) {
                String Cookie = Json.get("Cookie").toString();
                Cookie = Cookie.substring(1, Cookie.length());// убираем лишний пробел сначала строки
                for (String elem : Cookie.split("; ")) {
                    String[] val = elem.split("=");
                    Json.put(val[0], val[1]);
                    val[0] = val[0].replace(" ", "_");
                    Json.put(val[0], val[1]);
                    JsonParam.put(val[0], val[1]);
                }
            }
            if (Json.containsKey("X-Forwarded-For") == true) {
                JsonParam.put("MacAddClient", GetMacClient(Json.get("X-Forwarded-For").toString()));
            }
            //  PrintWriter pw = new PrintWriter(new FileWriter("C:\\Intel\\srvLogInData.xml"));
            //  pw.write(sb.toString());
            //  pw.close();
            sb.setLength(0);
            if (Json.containsKey("RemoteIPAdress") == false) {
                PrintStream out = new PrintStream(os);
                System.setOut(out);
                System.setErr(out);
                System.out.println("Your IP- address is not defined, in this regard, there is a suspicion that you are the attacker .\n"
                        + "You have not yet added to the blacklist , but not for long .\n"
                        + "Good luck .\r\n");
                System.out.println("Ваш IP адрес не определен, в связи с этим есть подозрение, что вы злоумышленник.\n"
                        + "Вы еще не добавлены в черный список, но это ненадолго.\n"
                        + "Желаю удачи.\r\n");
                is.close();
                os.close();
                socket.close();
                return;
            }

            String clientIP = Json.get("RemoteIPAdress").toString().replace("/", "").split(":")[0];

            HttpSrv.ClientList.put(clientIP, contentZapros);

            String filtr = Json.get("ContentZapros").toString().toLowerCase();
            if (BlackList.containsKey(clientIP) == true) {
                PrintStream out = new PrintStream(os);
                System.setOut(out);
                System.setErr(out);
                System.out.println("Your IP address is blacklisted . Please nepytatsya obraschatsya longer at this address\r\n");
                System.out.println("Уважаемый злоумышленник, прежде чем продолжишь свое черное дело, тебе  придется изменить свой IP адрес, так как этот, добавлен в черный список. Удачи тебе в твоем не легком начинании. \r\n"
                        + "С уважением  создатель.\r\n");
                is.close();
                os.close();
                socket.close();
            }
            if (Json.get("Zapros").toString().length() < 2) {
                Json.put("Zapros", startFile);
            }

            if (Json.get("Zapros").toString().toLowerCase().length() > 0) {
                filtr = Json.get("ContentZapros").toString().toLowerCase();
            }
            if (Json.get("Zapros").toString().toLowerCase().length() > 0) {
                filtr = Json.get("Zapros").toString().toLowerCase();
            }
            if (filtr.length() > 0) {
                for (String exept : BlockText) {
                    if (filtr.contains(exept)) {
                        BlackList.put(clientIP, clientIP);
                        SaveBlackList();
                        PrintStream out = new PrintStream(os);
                        System.setOut(out);
                        System.setErr(out);
                        System.out.println("Your IP address is blacklisted . Please nepytatsya obraschatsya longer at this address");
                        is.close();
                        os.close();
                        socket.close();
                    }
                }
            }

            writeResponse();
        }

        private void SaveBlackList() {
            String os = System.getProperty("os.name").toLowerCase();
            String setupPathBlackListPath;
            if (os.indexOf("win") >= 0) {
                setupPathBlackListPath = System.getProperty("java.io.tmpdir") + "BlackList.dat";
            } else {
                setupPathBlackListPath = "BlackList.dat";
            }
            try {
                PrintWriter outBlackList = new PrintWriter(setupPathBlackListPath);
                for (String list : BlackList.keySet()) {
                    outBlackList.println(list);
                }
                outBlackList.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * метод отправки ответа клиенту
         */
        private void writeResponse() {
            if (socket.isConnected() == false) {
                return;
            }

            createIdComp();
            log();
            try {
                SendFavIcon();
                File pageFile = new File(rootPath + "\\" + Json.get("Zapros"));
                if (pageFile.exists()) {
                    // получаем кодировку читаемого файла
                    FileReader fileInput = new FileReader(pageFile);
                    String Code = fileInput.getEncoding();
                    Json.put("Charset", Code);

                    // расширение файла
                    String rashirenie = pageFile.getName().substring(pageFile.getName().lastIndexOf(".") + 1);
                    rashirenie = rashirenie.toLowerCase();
                    // Если файл существует, тогда вычитываем его целиком
                    if (rashirenie.equals("class")) {
                        runJavaClass(pageFile);
                        return;
                    }
                    if (rashirenie.equals("java")) {
                        runJavaSRC(pageFile);
                        return;
                    }
                    if (rashirenie.equals("jar")) {
                        runJavaJarDev(pageFile);
                        return;
                    }
                    sendRawFile(pageFile, os);
                }

            } catch (Exception ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // java -jar "C:\AppServ\www\dell\dist\dell.jar"
        /**
         * Компиляция и Запуск JAVA исходника на выполение
         *
         * @param pageFile
         */
        private void runJavaSRC(File pageFile) {
            // путь без файла
            String Dir = pageFile.getPath().replace(pageFile.getName(), "").toLowerCase();;
            // имя файла с расширением
            String FileName = pageFile.getName();
            // расширение файла
            String rashirenie = FileName.substring(FileName.lastIndexOf(".") + 1);
            // путь к файлу + имя файла - расширение файла
            String DirFile = pageFile.getPath().replace("." + rashirenie, "");
            // имя файла без расширения
            String File2 = FileName.replace("." + rashirenie, "");
            File dst = new File(DirFile + ".class");
            Dir = Dir.substring(0, Dir.lastIndexOf("\\")); // убираем последний символ "\"
            String cmdCompil = "cmd /c " + javaBinPath + "\\javac -cp \"" + Dir + "\\*\";\"" + Dir + "\" \"" + pageFile.getAbsolutePath() + "\"";
            if (libPath.length() > 0) {
                cmdCompil = "cmd /c " + javaBinPath + "\\javac -cp \"" + libPath + "\";\"" + libPath + "\\*\";\"" + Dir + "\\*\";\"" + Dir + "\" \"" + pageFile.getAbsolutePath() + "\"";
            }
            //  String cmdCompil = "cmd /c javac -cp \"C:\\AppServ\\www\\lib\";\"C:\\AppServ\\www\\lib\\\";\"C:\\AppServ\\www\\notepad11\\src\";\"C:\\AppServ\\www\\notepad11\\src\\\" \"C:\\AppServ\\www\\notepad11\\src\\Notepad11.java\"";
            //  String cmdCompil = "cmd /c javac -cp \""+libPath+"\":\""+Dir+"\" \"" + pageFile.getAbsolutePath() + "\"";
            try {
                // если файл уже скомпилирован, и невключен режим разработчика, тогда запускаем класс
                if (dst.exists() && (HttpSrv.isDev == false)) {
                    runJavaClass(dst);
                } else {
                    // иначе компилируем и запускаем
                    Process p = Runtime.getRuntime().exec(cmdCompil);
                    p.waitFor(); // ждем окончания компиляции
                    if (dst.exists()) { // если файл создан, тогда выполняем его
                        runJavaClass(dst);
                    } else {
                        System.out.println("Error create file:" + dst.getAbsolutePath());
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         *
         * @param pageFile
         */
        private void runJavaJarDev(File pageFile) {
            try {
                PrintStream out = new PrintStream(os);
                System.setOut(out);
                System.setErr(out);
                // путь без файла
                String Dir = pageFile.getPath().replace(pageFile.getName(), "").toLowerCase();
                // имя файла с расширением
                String FileName = pageFile.getName();
                // расширение файла
                String rashirenie = FileName.substring(FileName.lastIndexOf(".") + 1);
                rashirenie = rashirenie.toLowerCase();
                // имя файла без расширения
                String File2 = FileName.replace("." + rashirenie, "");
                // System.setProperty("java.library.path", "C:\\AppServ\\www\\lib");
                // Field fieldSysPath = ClassLoader.class.getDeclaredField("sqlite-jdbc-3.8.11.1.jar");
                URLClassLoader loaderf = (URLClassLoader) ClassLoader.getSystemClassLoader();
                HttpClassLoader myClassLoad = new HttpClassLoader(loaderf.getURLs());
                // l.addURL(new URL("file:"+"C:\\AppServ\\www\\bagtracker\\sqlite-jdbc-3.8.11.1.jar"));
                //
                //  если указана биректория с внешними библиотеками, тогда загружаем их
                if (libPath.length() > 0) {
                    File dependencyDirectory = new File(libPath);
                    File[] files = dependencyDirectory.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String fileName = files[i].getName().toLowerCase();
                        if (fileName.endsWith(".jar")) {
                            myClassLoad.addURL(new URL("file:" + files[i].getAbsolutePath()));
                        }
                    }
                }
                // директоря проекта хранилища библиотеки в проекте
                File DirectoryProject = new File(Dir + "\\" + "lib");
                if (DirectoryProject.exists() && DirectoryProject.isDirectory()) {
                    File[] filesProj = DirectoryProject.listFiles();
                    for (int i = 0; i < filesProj.length; i++) {
                        String fileName = filesProj[i].getName().toLowerCase();
                        if (fileName.endsWith(".jar")) {
                            myClassLoad.addURL(new URL("file:" + filesProj[i].getAbsolutePath()));
                        }
                    }
                }
                myClassLoad.addURL(new URL("file:" + pageFile.getAbsolutePath()));
                if (Json.containsKey("class") == true) {
                    File2 = Json.get("class").toString(); //  получение класса запускаемого 
                }
                Class cls = myClassLoad.loadClass(File2);
                String MetodName = "main";
                if (Json.containsKey("method") == true) {
                    MetodName = Json.get("method").toString();
                }
                Method[] allMethods = cls.getDeclaredMethods();
                for (Method meth : allMethods) {
                    if (meth.getName().equals(MetodName)) {
                        Type[] type = meth.getGenericParameterTypes();
                        if (type.length == 1) {
                            meth = cls.getMethod(MetodName, String[].class);
                            String[] stringParamArray = new String[]{JSON.encode(Json), JSON.encode(JsonParam), java.net.URLDecoder.decode(new String(POST), "UTF-8"), java.net.URLDecoder.decode(new String(getCmd), "UTF-8")};
                            meth.invoke(null, (Object) stringParamArray); // static met                    
                        }
                        if (type.length == 2) {
                            Class[] argTypes = new Class[]{String[].class, byte[].class};
                            meth = cls.getMethod(MetodName, argTypes);
                            String[] stringParamArray = new String[]{JSON.encode(Json), JSON.encode(JsonParam), java.net.URLDecoder.decode(new String(POST), "UTF-8"), java.net.URLDecoder.decode(new String(getCmd), "UTF-8")};
                            meth.invoke(null, (Object) stringParamArray, POST); // static met                    
                        }
                    }
                }
                myClassLoad.clearAssertionStatus();
                myClassLoad.close();
                is.close();
                os.close();
                socket.close();
                // System.out.println("\r\n" + myClassLoad.getURLs().length + "\r\n");
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            } catch (Exception ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Запуск JAVA class на выполнение
         *
         * @param pageFile
         */
        private void runJavaClass(File pageFile) {
            try {
                PrintStream out = new PrintStream(os);
                System.setOut(out);
                System.setErr(out);
                // путь без файла
                String Dir = pageFile.getPath().replace(pageFile.getName(), "").toLowerCase();
                // имя файла с расширением
                String FileName = pageFile.getName();
                // расширение файла
                String rashirenie = FileName.substring(FileName.lastIndexOf(".") + 1);
                rashirenie = rashirenie.toLowerCase();
                // имя файла без расширения
                String File2 = FileName.replace("." + rashirenie, "");
                URLClassLoader SystemLoadURL = (URLClassLoader) ClassLoader.getSystemClassLoader();
                URL[] urls2 = new URL[SystemLoadURL.getURLs().length + 1];
                URL[] sysLib = SystemLoadURL.getURLs();
                for (int i = 0; i < sysLib.length; i++) {
                    urls2[i] = sysLib[i];
                }
                urls2[sysLib.length] = new File(Dir).toURI().toURL();
                // запустить скомпелированый класс
                URLClassLoader ucl2 = new URLClassLoader(urls2);
                if (Json.containsKey("class") == true) {
                    File2 = Json.get("class").toString(); //  получение класса запускаемого 
                }
                Object o2 = ucl2.loadClass(File2).newInstance();
                Class c2 = o2.getClass();
                String MetodName = "main";
                if (Json.containsKey("method") == true) {
                    MetodName = Json.get("method").toString();
                }
                Method[] allMethods = c2.getDeclaredMethods();
                for (Method meth : allMethods) {
                    if (meth.getName().equals(MetodName)) {
                        Type[] type = meth.getGenericParameterTypes();
                        if (type.length == 1) {
                            meth = c2.getMethod(MetodName, String[].class);
                            String[] stringParamArray = new String[]{JSON.encode(Json), JSON.encode(JsonParam), java.net.URLDecoder.decode(new String(POST), "UTF-8"), java.net.URLDecoder.decode(new String(getCmd), "UTF-8")};
                            meth.invoke(null, (Object) stringParamArray); // static met                    
                        }
                        if (type.length == 2) {
                            Class[] argTypes = new Class[]{String[].class, byte[].class};
                            meth = c2.getMethod(MetodName, argTypes);
                            String[] stringParamArray = new String[]{JSON.encode(Json), JSON.encode(JsonParam), java.net.URLDecoder.decode(new String(POST), "UTF-8"), java.net.URLDecoder.decode(new String(getCmd), "UTF-8")};
                            meth.invoke(null, (Object) stringParamArray, POST); // static met                    
                            //  Class[] argTypes = new Class[]{String[].class, Hashtable.class};
                            //  meth = c2.getMethod(MetodName, argTypes);
                            //  String[] stringParamArray = new String[]{sbInData.toString(), Headers.get("param"), POST.toString()};
                            //  meth.invoke(null, (Object) stringParamArray, Headers); // static met                    
                        }
                    }
                }
                // https://docs.oracle.com/javase/tutorial/reflect/member/methodInvocation.html
                // http://d.hatena.ne.jp/Kazuhira/20130309/1362838458
                // https://today.java.net/article/2003/11/08/reflection-tiger
                // System.out.println( meth.toString());
                is.close();
                os.close();
                socket.close();
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            } catch (Exception ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Отправка бинарного файла клиенту
         *
         * @param pageFile
         * @param os
         */
        private void sendRawFile(File pageFile, OutputStream os) {
            try {
                String TypeCont = ContentType(pageFile);
                // Первая строка ответа
                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                // дата создания в GMT
                DateFormat df = DateFormat.getTimeInstance();
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                // Время последней модификации файла в GMT
                os.write(("Last-Modified: " + df.format(new Date(pageFile.lastModified())) + "\r\n").getBytes());
                // Длина файла
                os.write(("Content-Length: " + pageFile.length() + "\r\n").getBytes());
                os.write(("Content-Type: " + TypeCont + "; ").getBytes());
                os.write(("charset=" + Json.get("Charset") + "\r\n").getBytes());;
                // Остальные заголовки
                os.write("Connection: close\r\n".getBytes());
                os.write("Server: HTMLserver\r\n\r\n".getBytes());
                // Сам файл:
                FileInputStream fis = new FileInputStream(pageFile.getAbsolutePath());
                int lengRead = 1;
                byte buf[] = new byte[1024];
                while ((lengRead = fis.read(buf)) != -1) {
                    os.write(buf, 0, lengRead);
                    os.flush();
                }
                // закрыть файл
                fis.close();
                // завершаем соединение
                is.close();
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            } catch (IOException ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*
         private void sendRawFile(File pageFile) {
         try {
         String TypeCont = ContentType(pageFile);
         // Первая строка ответа
         os.write("HTTP/1.1 200 OK\r\n".getBytes());
         // дата создания в GMT
         DateFormat df = DateFormat.getTimeInstance();
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         // Время последней модификации файла в GMT
         os.write(("Last-Modified: " + df.format(new Date(pageFile.lastModified())) + "\r\n").getBytes());
         // Длина файла
         os.write(("Content-Length: " + pageFile.length() + "\r\n").getBytes());
         os.write(("Content-Type: " + TypeCont + "; ").getBytes());
         os.write(("charset=" + Json.get("Charset") + "\r\n").getBytes());;
         // Остальные заголовки
         os.write("Connection: close\r\n".getBytes());
         os.write("Server: HTMLserver\r\n\r\n".getBytes());
         // Сам файл:
         FileInputStream fis = new FileInputStream(pageFile.getAbsolutePath());
         int lengRead = 1;
         byte buf[] = new byte[1024];
         while ((lengRead = fis.read(buf)) != -1) {
         os.write(buf, 0, lengRead);
         }
         // закрыть файл
         fis.close();
         // завершаем соединение
         is.close();
         os.close();
         socket.close();
         System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
         } catch (IOException ex) {
         Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         */
        /**
         * Определить по файлу тип HTML контента
         *
         * @param pageFile
         * @return
         */
        private String ContentType(File pageFile) {
            String ras = null;
            // путь без файла
            String Dir = pageFile.getPath().replace(pageFile.getName(), "").toLowerCase();;
            // имя файла с расширением
            String FileName = pageFile.getName();
            // расширение файла
            String rashirenie = FileName.substring(FileName.lastIndexOf(".") + 1);
            // путь к файлу + имя файла - расширение файла
            String DirFile = pageFile.getPath().replace("." + rashirenie, "");
            // имя файла без расширения
            String File2 = FileName.replace("." + rashirenie, "");
            rashirenie = rashirenie.toLowerCase();// преобразуем в нижний регистр

            //  try {
            //   PrintWriter pw = new PrintWriter(new FileWriter("E:\\YandexDisk\\WebServer\\HtmlServer_012\\LOG!!!!.txt"));
            //   pw.write(rashirenie);
            //   pw.close();
            //  } catch (IOException ex) {
            //     Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            //  }

            if (rashirenie.equals("css")) {
                return "text/css";
            }
            if (rashirenie.equals("js")) {
                return "application/x-javascript";
            }
            if (rashirenie.equals("xml") || rashirenie.equals("dtd")) {
                return "text/xml";
            }
            if ((rashirenie.equals("txt")) || (rashirenie.equals("inf")) || (rashirenie.equals("nfo"))) {
                return "text/plain";
            }
            if ((rashirenie.equals("html")) || (rashirenie.equals("htm")) || (rashirenie.equals("shtml")) || (rashirenie.equals("shtm")) || (rashirenie.equals("stm")) || (rashirenie.equals("sht"))) {
                return "text/html";
            }
            if ((rashirenie.equals("mpeg")) || (rashirenie.equals("mpg")) || (rashirenie.equals("mpe"))) {
                return "video/mpeg";
            }
            if ((rashirenie.equals("ai")) || (rashirenie.equals("ps")) || (rashirenie.equals("eps"))) {
                return "application/postscript";
            }
            if (rashirenie.equals("rtf")) {
                return "application/rtf";
            }
            if ((rashirenie.equals("au")) || (rashirenie.equals("snd"))) {
                return "audio/basic";
            }
            if ((rashirenie.equals("bin")) || (rashirenie.equals("dms")) || (rashirenie.equals("lha")) || (rashirenie.equals("lzh")) || (rashirenie.equals("class")) || (rashirenie.equals("exe"))) {
                return "application/octet-stream";
            }
            if (rashirenie.equals("doc")) {
                return "application/msword";
            }
            if (rashirenie.equals("pdf")) {
                return "application/pdf";
            }
            if (rashirenie.equals("ppt")) {
                return "application/powerpoint";
            }
            if ((rashirenie.equals("smi")) || (rashirenie.equals("smil")) || (rashirenie.equals("sml"))) {
                return "pplication/smil";
            }
            if (rashirenie.equals("zip")) {
                return "application/zip";
            }
            if ((rashirenie.equals("midi")) || (rashirenie.equals("kar"))) {
                return "audio/midi";
            }
            if ((rashirenie.equals("mpga")) || (rashirenie.equals("mp2")) || (rashirenie.equals("mp3"))) {
                return "audio/mpeg";
            }
            if (rashirenie.equals("wav")) {
                return "audio/x-wav";
            }
            if (rashirenie.equals("ief")) {
                return "image/ief";
            }

            if ((rashirenie.equals("jpeg")) || (rashirenie.equals("jpg")) || (rashirenie.equals("jpe"))) {
                return "image/jpeg";
            }
            if (rashirenie.equals("png")) {
                return "image/png";
            }
            if (rashirenie.equals("ico")) {
                return "image/x-icon";
            }
            if ((rashirenie.equals("tiff")) || (rashirenie.equals("tif"))) {
                return "image/tiff";
            }
            if ((rashirenie.equals("wrl")) || (rashirenie.equals("vrml"))) {
                return "model/vrml";
            }
            if (rashirenie.equals("avi")) {
                return "video/x-msvideo";
            }
            if (rashirenie.equals("flv")) {
                return "video/x-flv";
            }
            if (rashirenie.equals("ogg")) {
                return "video/ogg";
            }
            return "application/octet-stream";
        }

        /**
         * Закодировать строку кодировкой MD5
         *
         * @param input
         * @return
         */
        private static String getMD5(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                String hashtext = number.toString(16);
                // Now we need to zero pad it if you actually want the full 32 chars.
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                return hashtext;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Получить МАС адрес клиента
         *
         * @param adress
         * @return
         */
        private String GetMacClient(String adress) {
            String macStr = null;
            try {
                InetAddress address = InetAddress.getByName(adress);
                NetworkInterface ni = NetworkInterface.getByInetAddress(address);
                if (ni != null) {
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        macStr = sb.toString();
                    }
                }
            } catch (SocketException ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
            return macStr;
        }

        private void log() {
            try {
                Date d = new Date();
                SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                SimpleDateFormat format2 = new SimpleDateFormat("dd_MM_yyyy_HH");
                // System.out.println(format1.format(d)); //25.02.2013 09:03
                // System.out.println(format2.format(d)); //День 25 Месяц 02 Год 2013 Время 09:03     
                File filesLibPath = new File((new File(HttpSrv.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + "\\LOG");
                if (!filesLibPath.exists()) {
                    filesLibPath.mkdirs();
                }
                /*
                 File f = new File("log.java");
                 if (!f.exists()) {
                 PrintWriter pw = new PrintWriter(new FileWriter("log.java"));
                 pw.write("import java.io.File;");
                 pw.write("import java.io.FileNotFoundException; ");
                 pw.write("import java.io.IOException; ");
                 pw.write("import java.nio.file.Files; ");
                 pw.write("import java.nio.file.Paths; ");
                 pw.write("import java.util.Hashtable;  ");
                 pw.write("public class log {  ");
                 pw.write("    public static void main(String[] args) throws FileNotFoundException, IOException {  ");
                 pw.write("        File f = new File(\"LOG\");  ");
                 pw.write("        File[] paths = f.listFiles();  ");
                 pw.write("        os.write((\"HTTP/1.1 200 OK\\r\\n\"   ");
                 pw.write("                + \"Content-Type: text/html; charset=utf-8\\r\\n\"  ");
                 pw.write("                + \"Connection: close\\r\\n\"     ");
                 pw.write("                + \"Server: HTMLserver\\r\\n\\r\\n\").getBytes());");
                 pw.write("        for (File path : paths) {");
                 pw.write("            if (path.exists() && !path.isDirectory()) {");
                 pw.write("                if (path.getName().contains(\".txt\")) {");
                 pw.write("                    os.write((\"<a style='color: rgb(10, 255, 128); ' href='/LOG/\" + path.getName() + \"'>\" + path.getName() + \"</a> <br>\").getBytes());  ");
                 pw.write("                }");
                 pw.write("            }");
                 pw.write("        }");
                 pw.write("    }");
                 pw.write("}");
                 pw.close();
                 }*/
                Hashtable<String, Object> Log = new Hashtable<String, Object>(10, (float) 0.5);
                for (String key : Json.keySet()) {
                    Log.put(key, Json.get(key));
                }
                for (String key : JsonParam.keySet()) {
                    Log.put(key, JsonParam.get(key));
                }
                Log.put("DataCreatRec", format1.format(d));
                FileWriter outLog = new FileWriter(filesLibPath.getAbsolutePath() + "/log_" + format2.format(d) + ".txt", true); //the true will append the new data
                //outLog.write(filesLibPath1.getAbsolutePath() + " \r\n");
                outLog.write(JSON.encode(Log) + " \r\n");
                outLog.close();
                Log.clear();
            } catch (Exception ex) {
            }
        }

        private void SendFavIcon() {
            /*
             // если в запросе Json "favicon.ico" Тогда отправляем исонку
             if (Json.get("Zapros").equals("favicon.ico")) {
             try {
             os.write(("HTTP/1.1 200 OK\r\n"
             + "Content-Type: image/jpeg; charset=utf-8\r\n"
             + "Connection: close\r\n"
             + "Server: HTMLserver\r\n\r\n").getBytes());
             String path = "../img/connect.ico";
             FileInputStream fis = new FileInputStream(path);
             int lengRead = 1;
             byte buf[] = new byte[1024];
             while ((lengRead = fis.read(buf)) != -1) {
             os.write(buf, 0, lengRead);
             }
             fis.close();
             is.close();
             os.close();
             socket.close();
             return;
             } catch (IOException ex) {
             Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
             }
             }
             */
        }

        /**
         * сздать идентификатор компьютера
         */
        private void createIdComp() {
            //
            // создаем идентификатор компьютера , сохраняем его в Кукисах и перезагружаем страницу
            if (Json.containsKey("WORCSTATIONID") == false) {
                try {
                    Date currentDate = new Date();
                    Long time = currentDate.getTime();
                    String IDcomp = getMD5(numComp + IPmac + time);
                    String initWORCSTation = ""
                            + "<script>"
                            + "    function setCookie(cname, cvalue, exdays) { var d = new Date(); d.setTime(d.getTime() + (exdays)); var expires = 'expires='+d.toUTCString();   		document.cookie = cname + '=' + cvalue + '; ' + expires;} \n"
                            + "    setCookie('WORCSTATIONID', '" + IDcomp + "', 157680000); "
                            + "    window.location.href=window.location.toString();"
                            + "</script>"; //31536000
                    os.write(("HTTP/1.1 200 OK\r\n").getBytes());
                    os.write(("Content-Type: text/html; ").getBytes());
                    os.write(("Content-Length: " + initWORCSTation.length() + "\r\n").getBytes());
                    os.write(("charset=utf-8\r\n").getBytes());;
                    os.write("Connection: close\r\n".getBytes());
                    os.write("Server: HTMLserver\r\n\r\n".getBytes());
                    os.write(initWORCSTation.getBytes());
                    is.close();
                    os.close();
                    socket.close();
                    return;
                } catch (Exception ex) {
                    System.err.println("Error create ID comp:" + ex.toString());
                    return;
                }
            }

        }
    }
}
