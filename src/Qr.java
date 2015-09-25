import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * Created by mjansen on 21-9-15.
 */
public class Qr extends JFrame{


    private JPanel rootpanel;
    private JTextArea extraSettings;
    private JComboBox ipComboBox;
    private JCheckBox includeIPCheckBox;
    private JButton generateButton;
    private JLabel imageLabel;
    private JCheckBox processorCheckBox;
    private JCheckBox producerCheckBox;
    private JCheckBox queueCheckBox;


    /**
     * Constructor, sets up the panel and onclicklistener
     * and generates a QR of current ip
     *
     */
    public Qr() {
        setTitle("QR code");
        setContentPane(rootpanel);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        ArrayList<String> ips= null;
        try {
            ips = getLocalIp();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        for (String ip :ips) {
            ipComboBox.addItem(ip);
        }
        ipComboBox.setSelectedIndex(ipComboBox.getItemCount() -1);
        BufferedImage qrcode = generateQR("DISCOVERY_ETCD_SERVER_IP=" + ipComboBox.getSelectedItem());
        imageLabel.setIcon(new ImageIcon(qrcode));

        generateButton.addActionListener(e -> {
            //Translate options to content for QR code
            try {
                // Is a valid URL? generate QR.
                new URL(extraSettings.getText());
                imageLabel.setIcon(new ImageIcon(generateQR(extraSettings.getText())));
            } catch (MalformedURLException e1) {
                String content = "";
                //Not an URL? read options and add content
                if (includeIPCheckBox.isSelected()) {
                    content += "DISCOVERY_ETCD_SERVER_IP=";
                    content += ipComboBox.getSelectedItem() +"\n";
                }
                if (!extraSettings.getText().isEmpty()) {
                    Scanner sc = new Scanner(extraSettings.getText());
                    while (sc.hasNext()) {
                        content += sc.next() + "\n";
                    }
                }
                //If one of the autostart checkboxes is selected
                if (processorCheckBox.isSelected() || producerCheckBox.isSelected() || queueCheckBox.isSelected()) {
                    //Needed bundles for all of the autostart bundles
                    content += "cosgi.auto.start.1=";
                    content += "remote_service_admin_http.zip topology_manager.zip discovery_etcd.zip ";
                    if (processorCheckBox.isSelected() || producerCheckBox.isSelected()) {
                        content += "org.inaetics.demonstrator.api.queue.SampleQueue_proxy.zip ";
                        if (processorCheckBox.isSelected())
                            content += "org.inaetics.demonstrator.api.processor.Processor.zip ";
                        if (producerCheckBox.isSelected())
                            content += "org.inaetics.demonstrator.api.producer.Producer.zip ";
                    }
                    if (queueCheckBox.isSelected()) {
                        content += "org.inaetics.demonstrator.api.queue.SampleQueue_endpoint.zip ";
                        content += "org.inaetics.demonstrator.api.queue.SampleQueue.zip ";
                    }
                }
                if (!content.isEmpty())
                    imageLabel.setIcon(new ImageIcon(generateQR(content)));

            }
        });
    }

    /**
     * Generating a qr code with provided content
     * @param content       The content that should be in the QR
     * @return              An Buffered image object containing the qr code
     */
    private BufferedImage generateQR(String content) {
        final int QR_SIZE = 600;
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            return image;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a list of ipv4 adresses
     * @return      List of ipv4 adresses
     * @throws SocketException
     */
    private ArrayList<String> getLocalIp() throws SocketException {
        ArrayList<String> possibilities = new ArrayList<>();
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                if (!i.isLoopbackAddress() && i instanceof Inet4Address) {
                    possibilities.add(i.getHostAddress());
                }
            }
        }
        return possibilities;
    }
}
