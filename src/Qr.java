import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private JButton generateButton;
    private JLabel imageLabel;
    private JCheckBox processorCheckBox;
    private JCheckBox producerCheckBox;
    private JCheckBox queueCheckBox;
    private JTextField etcd_server_ip;
    private JTextField etcd_server_port;
    private JCheckBox deploymentCheckBox;


    /**
     * Constructor, sets up the panel and onclicklistener
     * and generates a QR of current ip
     *
     */
    public Qr() {
        setTitle("QR code");
        setContentPane(rootpanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);


        generateButton.addActionListener(e -> {
            //Translate options to content for QR code
            try {
                // Is a valid URL? generate QR.
                new URL(extraSettings.getText());
                imageLabel.setIcon(new ImageIcon(generateQR(extraSettings.getText())));
            } catch (MalformedURLException e1) {
                String content = "";
                //Not an URL? read options and add content
                if (!extraSettings.getText().isEmpty()) {
                    Scanner sc = new Scanner(extraSettings.getText());
                    while (sc.hasNext()) {
                        content += sc.next() + "\n";
                    }
                }
                if (!etcd_server_ip.getText().isEmpty()) {
                    content += "DISCOVERY_ETCD_SERVER_IP=" + etcd_server_ip.getText() + "\n";
                }
                if (!etcd_server_port.getText().isEmpty()) {
                    content += "DISCOVERY_ETCD_SERVER_PORT=" + etcd_server_port.getText() + "\n";
                }
                //If one of the autostart checkboxes is selected
                if (processorCheckBox.isSelected() || producerCheckBox.isSelected() || queueCheckBox.isSelected()) {
                    //Needed bundles for all of the autostart bundles
                    content += "cosgi.auto.start.1=";
                    content += "remote_service_admin_dfi.zip topology_manager.zip ";
                    if (processorCheckBox.isSelected()) {
                        content += "org.inaetics.demonstrator.api.processor.Processor.zip ";
                    }
                    if (producerCheckBox.isSelected()) {
                        content += "org.inaetics.demonstrator.api.producer.Producer.zip ";
                        content += "org.inaetics.demonstrator.android.shakedetection.zip ";
                    }
                    if (queueCheckBox.isSelected()) {
                        content += "org.inaetics.demonstrator.api.queue.SampleQueue.zip ";
                    }
                    content += "discovery_etcd.zip ";
                }
                if (deploymentCheckBox.isSelected()) {
                    content += "deployment_admin.zip ";
                }
                if (!content.isEmpty()) {
                    BufferedImage qr_code = generateQR(content);
                    imageLabel.setIcon(new ImageIcon(qr_code));
                    try {
                        ImageIO.write(qr_code, "png", new File("qr.png"));
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }

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
}