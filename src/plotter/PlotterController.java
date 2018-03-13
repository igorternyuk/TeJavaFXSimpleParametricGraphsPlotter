package plotter;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import parser.Expression;
import parser.MathParser;

import java.net.URL;
import java.util.ResourceBundle;

public class PlotterController implements Initializable {
    private static final String EXIT_DIALOG_WINDOW_MESSAGE = "Do you really want to exit?";
    private static final String EXIT_DIALOG_WINDOW_TITLE = "Confirm exit, please";
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
        spinnerLineThickness.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100,
                2, 1));
        spinnerTmin.setEditable(true);
        spinnerTmin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, 0.0, 0.01));
        spinnerTmax.setEditable(true);
        spinnerTmax.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, 0.0, 0.01));
        spinnerDeltaT.setEditable(true);
        spinnerDeltaT.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE,
                Double.MAX_VALUE, 0.01, 0.01));
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

        System.out.println("Initialize");
    }

    private void draw() {
        if(prevPoint != null && currPoint != null){
            gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currPoint.getX(), currPoint.getY());
        }
    }

    /*
    * private void showErrorMessage(String msg){
        System.out.println(msg);
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }
    }
    * */

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
            tmin = Double.parseDouble(spinnerTmin.getEditor().getText());
            t = tmin;
            tmax = Double.parseDouble(spinnerTmax.getEditor().getText());
            dt = Double.parseDouble(spinnerDeltaT.getEditor().getText());
            parser.addVariable("t", tmin);
            funcX = parser.parse(txtFunctionX.getText());
            funcY = parser.parse(txtFunctionY.getText());
            gc.setStroke(lineColorPicker.getValue());
            gc.setLineWidth(spinnerLineThickness.getValue());
            startTimer();
        } catch (Exception ex){

        }
        startTimer();
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

    public void onSaveGraphAsImageButtonClicked(){
        /*
        * try{
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser
                    .ExtensionFilter("Select a file (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(filter);
            File file = fileChooser.showSaveDialog(null);

            Image snapshot = canvasBottom.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null),
                    "png", file != null ? file : new File("snapshot.png"));
        }
        catch(IOException ex){
            showErrorMessage(ex.toString());
        }
        * */
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
