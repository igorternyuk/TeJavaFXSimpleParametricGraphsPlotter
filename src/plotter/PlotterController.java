package plotter;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import parser.Expression;
import parser.MathParser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlotterController implements Initializable {
    private static final String EXIT_DIALOG_WINDOW_MESSAGE = "Do you really want to exit?";
    private static final String EXIT_DIALOG_WINDOW_TITLE = "Confirm exit, please";
    private static final String SNAPSHOT_SAVING_ERROR_TEXT = "Could not save snapshot";
    private static final String DEFAULT_SNAPSHOT_NAME = "snapshot.png";
    private static final int LINE_THICKNESS_MAX = 100;
    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final double SPINNER_STEP = 0.01;
    private static final double DEFAULT_PARAMETER_STEP = 0.01;

    @FXML
    private Canvas canvas;

    @FXML
    private ColorPicker lineColorPicker;

    @FXML
    private TextField txtFunctionY;

    @FXML
    private TextField txtFunctionX;

    @FXML
    private Spinner<Integer> spinnerLineThickness;

    @FXML
    private Spinner<Double> spinnerTmin;

    @FXML
    private Spinner<Double> spinnerTmax;

    @FXML
    private Spinner<Double> spinnerDeltaT;

    @FXML
    private Button btnPlot;

    @FXML
    private Button btnStartStopAnimation;

    @FXML
    private Button btnCloseApp;

    private GraphicsContext gc;

    private AnimationTimer timer;

    private MathParser parser;

    private double t = 0, tmin = 0, tmax = 0, dt = 0;
    private Point2D prevPoint = null, currPoint = null;
    private Expression funcX, funcY;

    private boolean isAnimationActive = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initialize();
    }

    public void initialize(){
        parser = new MathParser();
        gc = canvas.getGraphicsContext2D();
        spinnerLineThickness.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, LINE_THICKNESS_MAX,
                DEFAULT_LINE_WIDTH, 1));
        spinnerTmin.setEditable(true);
        spinnerTmin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, 0, SPINNER_STEP));
        spinnerTmax.setEditable(true);
        spinnerTmax.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, 0, SPINNER_STEP));
        spinnerDeltaT.setEditable(true);
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, DEFAULT_PARAMETER_STEP, SPINNER_STEP);
        spinnerDeltaT.setValueFactory(factory);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                t += dt;
                parser.addVariable("t", t);
                prevPoint = currPoint;
                currPoint = curveFunction();
                if(t >= tmax)
                    stopTimer();
                draw();
            }
        };
    }

    private void draw() {
        if(prevPoint != null && currPoint != null){
            gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currPoint.getX(), currPoint.getY());
        }
    }

    private Point2D curveFunction(){
        final double y = funcY.evaluate();
        final double x = funcX.evaluate();
        return reducePointCoordinates(x,y);
    }

    private Point2D reducePointCoordinates(final double px, final double py){
        final double reducedX = px + canvas.getWidth() / 2;
        final double reducedY = py + canvas.getHeight() / 2;
        return new Point2D(reducedX, reducedY);
    }

    public void onPlotButtonClicked(){
        try {
            prevPoint = null;
            currPoint = null;
            tmin = spinnerTmin.getValue();
            t = tmin;
            tmax = spinnerTmax.getValue();
            dt = spinnerDeltaT.getValue()/*.doubleValue()*/;
            parser.addVariable("t", tmin);
            funcX = parser.parse(txtFunctionX.getText());
            funcY = parser.parse(txtFunctionY.getText());
            gc.setStroke(lineColorPicker.getValue());
            gc.setLineWidth(spinnerLineThickness.getValue());
            startTimer();
        } catch (Exception ex){
            showErrorMessage(ex.getMessage());
            stopTimer();
        }
    }

    public void onStartStopAnimationButtonClicked(){
        if(isAnimationActive){
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void stopTimer(){
        timer.stop();
        isAnimationActive = false;
    }

    private void startTimer(){
        timer.start();
        isAnimationActive = true;
    }

    private void showErrorMessage(final String message) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(SNAPSHOT_SAVING_ERROR_TEXT);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) alert.close();
    }

    public void onSaveGraphAsImageButtonClicked(){
        try {
            final FileChooser fileChooser = new FileChooser();
            final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(filter);
            final File file = fileChooser.showSaveDialog(null);
            final Image snapshot = canvas.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png",
                    file != null ? file : new File(DEFAULT_SNAPSHOT_NAME));
        } catch (IOException ex) {
            showErrorMessage(ex.toString());
        }
    }

    public void onClearCanvasClicked(ActionEvent actionEvent) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        txtFunctionX.clear();
        txtFunctionY.clear();
    }

    public void onCloseAppButtonClicked(){
        Alert alert = new Alert(Alert.AlertType.WARNING, EXIT_DIALOG_WINDOW_MESSAGE, ButtonType.YES, ButtonType.NO);
        alert.setTitle(EXIT_DIALOG_WINDOW_TITLE);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Platform.exit();
            System.exit(0);
        } else if(alert.getResult() == ButtonType.NO){
            alert.close();
        }
    }
}
