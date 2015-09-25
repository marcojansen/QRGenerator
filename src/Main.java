import javax.swing.*;

/**
 * Created by mjansen on 21-9-15.
 */
public class Main {

    public static void main(String[] args) {
        //Invoke new QR form later
        SwingUtilities.invokeLater(Qr::new);
    }
}