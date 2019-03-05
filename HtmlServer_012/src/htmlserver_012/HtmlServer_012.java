/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htmlserver_012;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 *
 * @author Администратор
 */
public class HtmlServer_012 {

    
    public static void main(String[] args) {
        // Запустить сервер в фоновом режиме (если файл конфигурации небудет найден, тогда запустится форма настройки сервера)
        // javaw -jar "D:\YandexDisk\WebServer\HTMLserver_010\dist\HTMLserver_010.jar" 1
        if (args.length == 1) {
            try {
                String setupPath = "";
                String os = System.getProperty("os.name").toLowerCase();
                if (os.indexOf("win") >= 0) {
                    setupPath = System.getProperty("java.io.tmpdir") + "setup.dat";
                } else {
                    setupPath = "setup.dat";
                }
                File f = new File(setupPath);
                if (f.exists() && !f.isDirectory()) {
                    FileInputStream fin = new FileInputStream(System.getProperty("java.io.tmpdir") + "setup.dat");
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    String rootPath = (String) ois.readObject();
                    String port = (String) ois.readObject();
                    String libPath = (String) ois.readObject();
                    String javaBinPath = (String) ois.readObject();
                    boolean isDev = (boolean) ois.readObject();
                    String startFile = (String) ois.readObject();
                    String TimeOut = (String) ois.readObject();
                    ois.close();
                    fin.close();
                    new HttpSrv().Start(rootPath,
                            startFile,
                            port,
                            libPath,
                            javaBinPath,
                            isDev,TimeOut);
                } else {
                    new MainForm().setVisible(true);
                }
            } catch (Exception e) {
                System.err.println("Error start web server:" + e.toString());
                new MainForm().setVisible(true);
            }
        } else {
            new MainForm().setVisible(true);
        }
    }
    
}
