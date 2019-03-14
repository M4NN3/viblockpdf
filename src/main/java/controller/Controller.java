package controller;

import com.itextpdf.kernel.pdf.*;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class Controller implements Initializable {
    private static final String PLUSNAME = "%1s%2s [ViBlockPDF].pdf";//path + name + plusname
    private static final String OWNER_PASSWORD = "#pz&=}_5WFY3KZ2>";
    private static final int FILE_MISSING = 2;
    private static final int JOB_DONE = 1;
    private static final int SOMETHING_WRONG = 3;
    private static final int NOTHING_TO_DO = 4;
    private int resultcode = 0;
    private int mode = 0;
    private WriterProperties writerProperties;
    //
    @FXML
    private AnchorPane container;
    //buttons
    @FXML
    private JFXButton btnSearchFile;
    @FXML
    private JFXButton btnApply;
    //edit-textview
    @FXML
    private TextField txtSourceFile;
    @FXML
    private TextField txtDestFile;
    @FXML
    private TextField txtPassword;
    //checkboxes
    @FXML
    private JFXCheckBox chkOpenAtEnd;
    @FXML
    private JFXCheckBox chkUsePassword;
    @FXML
    private JFXCheckBox chkCopyDisable;
    @FXML
    private JFXCheckBox chkPrintingDisable;
    @FXML
    private Label lblStatus;
    @FXML
    private JFXSpinner spinner;
    private Stage mainStage;
    //Thread myThread = null;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            lblStatus.setVisible(false);
            spinner.setVisible(false);
            //
            chkUsePassword.selectedProperty().addListener((observable, oldValue, newValue)
                    -> txtPassword.setEditable(newValue));
            //checkboxes validation
//            checkChekBoxes(chkPrintingDisable.selectedProperty(), chkCopyDisable.isSelected(), EncryptionConstants.ALLOW_COPY, EncryptionConstants.ALLOW_PRINTING);
//            checkChekBoxes(chkCopyDisable.selectedProperty(), chkPrintingDisable.isSelected(), EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ALLOW_COPY);
            chkCopyDisable.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue){
                    if (chkPrintingDisable.isSelected())
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40;//ambos en true
                    else
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40|EncryptionConstants.ALLOW_PRINTING;
                }
                else {
                    if (chkPrintingDisable.isSelected())
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40|EncryptionConstants.ALLOW_COPY;
                    else
                        mode = EncryptionConstants.ALLOW_PRINTING
                                |EncryptionConstants.ALLOW_COPY;
                }
            });
            chkPrintingDisable.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue){
                    if (chkCopyDisable.isSelected())
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40;//mbos true
                    else
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40|EncryptionConstants.ALLOW_COPY;
                }
                else {
                    if (chkCopyDisable.isSelected())
                        mode = EncryptionConstants.STANDARD_ENCRYPTION_40|EncryptionConstants.ALLOW_PRINTING;
                    else
                        mode = EncryptionConstants.ALLOW_PRINTING
                                |EncryptionConstants.ALLOW_COPY;// si ninguno de los dos está seleccionado
                }
            });
            btnSearchFile.setOnAction(this::searchForSourceFile);
            btnApply.setOnAction((ActionEvent v) -> {
                lblStatus.setVisible(true);
                lblStatus.setText("Processing...");
                btnApply.setDisable(true);
                btnSearchFile.setDisable(true);
                spinner.setVisible(true);
                try {
                    new Thread(this::encryptPDF).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //
            checkForDataFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void encryptPDF() {

        try {
            String password = "";
            if (!txtSourceFile.getText().isEmpty()) {
                File sourceFile = new File(txtSourceFile.getText());
                if (sourceFile.exists()) {
                    PdfReader reader = new PdfReader(txtSourceFile.getText());
                    if (chkUsePassword.isSelected() && !txtPassword.getText().isEmpty()) {
                        password = txtPassword.getText();
                    }
//                    if (!chkCopyDisable.isSelected() && !chkPrintingDisable.isSelected())
//                        mode = 512;
                    writerProperties = new WriterProperties()
                            .setStandardEncryption(password.isEmpty() ? null : password.getBytes(),
                                    OWNER_PASSWORD.getBytes(),
                                    mode,
                                    EncryptionConstants.ENCRYPTION_AES_128 |
                                            EncryptionConstants.DO_NOT_ENCRYPT_METADATA);
                    //
                    PdfWriter writer = new PdfWriter(txtDestFile.getText(), writerProperties);
                    PdfDocument pdfDoc = new PdfDocument(reader, writer);
                    pdfDoc.close();
                    if (chkOpenAtEnd.isSelected())
                        openPDF(txtDestFile.getText());
                    resultcode = JOB_DONE;
                }
                else {
                    resultcode = FILE_MISSING;
                }
            }else {
                resultcode = NOTHING_TO_DO;
            }
        } catch (Exception ex) {
            resultcode = SOMETHING_WRONG;
        }
        finally {
            Platform.runLater(() -> {
                spinner.setVisible(false);
                btnApply.setDisable(false);
                btnSearchFile.setDisable(false);
                switch (resultcode){
                    case JOB_DONE:
                        lblStatus.setText("Pdf created successfully");
                        break;
                    case SOMETHING_WRONG:
                        lblStatus.setText("Something went wrong. Try again :(");
                        break;
                    case NOTHING_TO_DO:
                        lblStatus.setText("Nothing to do");
                        break;
                    case FILE_MISSING:
                        lblStatus.setText("The source file does not exist, try another");
                }
//                System.out.println(""+ mode);
//                btnApply.requestFocus();
            });
        }
    }
    private void decrypt(){

    }

    @FXML
    private void searchForSourceFile(ActionEvent ae) {
        mainStage = (Stage) container.getScene().getWindow();
        //
        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfExtensionFilter =
                new FileChooser.ExtensionFilter(
                        "PDF - Portable Document Format (.pdf)", "*.pdf");
        fileChooser.setTitle("Select pdf file");
        fileChooser.getExtensionFilters().add(pdfExtensionFilter);
        fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
        if (txtSourceFile.getText().isEmpty())
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/documents"));
        else
            fileChooser.setInitialDirectory(new File(FilenameUtils.getFullPath(txtSourceFile.getText())));
        //
        try {
            File sourceFile = fileChooser.showOpenDialog(mainStage);
            if (sourceFile != null) {
                txtSourceFile.setText(sourceFile.getAbsolutePath());
                txtDestFile.setText(getPathForDestinationFile(sourceFile.getAbsolutePath()));
                //saveLastPath(sourceFile.getParent());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private String getPathForDestinationFile(String pathfile) {
        return String.format(PLUSNAME, FilenameUtils.getFullPath(pathfile), FilenameUtils.getBaseName(pathfile));

    }

    private void openPDF(String filepath) {
        if (Desktop.isDesktopSupported()) {
            try {
                File myFile = new File(filepath);
                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                //
            }
        }
    }
    @FXML
    private void copyMailToClipboard(MouseEvent ae){
        StringSelection selection = new StringSelection("mnrch.18@gmail.com");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @FXML
    private void openGHWebPage(MouseEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("http://github.com/M4NN3"));
    }

    @FXML
    private void openTWWebPage(MouseEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://twitter.com/elprogramadorxd"));
    }

    @FXML
    private void closeApp(ActionEvent ae) {
        Platform.exit();
    }

    @FXML
    private void removeSelectedFile(ActionEvent ae) {
        txtSourceFile.clear();
        txtDestFile.clear();
    }

    @FXML
    private void aboutDialog(ActionEvent ae) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ViBlockPDF");
        alert.setHeaderText(null);
        alert.setContentText("ViBlockPDF is a completely free tool that allow you to add " +
                "secutiry properties to your PDF document.\n\n" +
                "Developed by M4NN3 - March 2019\n\n" +
                "Current version: 1.1.3 - check for updates on my twitter :-)");
        alert.showAndWait();
    }

    //setting properties in file
    private void getProperties() {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(new File("config.properties"));
            prop.load(in);
            // source file
            File pathToFind = new File(FilenameUtils.getFullPath(prop.getProperty("initpath")));
            String pathToShow = pathToFind.exists() ? prop.getProperty("initpath") : "";
            txtSourceFile.setText(pathToShow);
            // endpath - dest file
            pathToFind = new File(FilenameUtils.getFullPath(prop.getProperty("endpath")));
            pathToShow = pathToFind.exists() ? prop.getProperty("endpath") : "";
            txtDestFile.setText(pathToShow);
            //vomboboxes
            chkOpenAtEnd.setSelected(Boolean.valueOf(prop.getProperty("chk-openfile")));
            chkCopyDisable.setSelected(Boolean.valueOf(prop.getProperty("chk-copy")));
            chkPrintingDisable.setSelected(Boolean.valueOf(prop.getProperty("chk-printing")));
            chkUsePassword.setSelected(Boolean.valueOf(prop.getProperty("chk-password")));
            txtPassword.setText(prop.getProperty("txt-password"));
            container.requestFocus();
        } catch (IOException io) {
            System.out.println("" + io.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
        //return new File(System.getProperty("user.home") + "/documents");
    }

    private void saveProperties(String initPath, String endPath) {
        OutputStream output = null;
        try {
            Properties prop = new Properties();
            output = new FileOutputStream(new File("config.properties"));
            prop.setProperty("initpath", initPath);
            prop.setProperty("endpath", endPath);
            prop.setProperty("chk-password", String.valueOf(chkUsePassword.isSelected()));
            prop.setProperty("chk-copy", String.valueOf(chkCopyDisable.isSelected()));
            prop.setProperty("chk-printing", String.valueOf(chkPrintingDisable.isSelected()));
            prop.setProperty("chk-openfile", String.valueOf(chkOpenAtEnd.isSelected()));

            prop.setProperty("txt-password", chkUsePassword.isSelected() ? txtPassword.getText(): "");
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {

                }
            }
        }
    }

    private void checkForDataFile() throws IOException {
        File storedPath = new File("config.properties");
        if (!storedPath.exists()) {
            if (storedPath.createNewFile()) {
                Properties prop = new Properties();
                OutputStream output;
                output = new FileOutputStream(new File("config.properties"));
                prop.setProperty("initpath", System.getProperty("user.home") + "/documents");
                prop.setProperty("chk-password", "false");
                prop.setProperty("chk-copy", "true");
                prop.setProperty("chk-printing", "true");
                prop.setProperty("chk-openfile", "true");
                prop.setProperty("txt-password", "");
                prop.store(output, null);
                output.close();
            }
        }
        else getProperties();
    }
    public void saveOnClose(){
        saveProperties(txtSourceFile.getText(), txtDestFile.getText());
        Platform.exit();
    }
}
