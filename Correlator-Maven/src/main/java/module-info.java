module org.cemrc.correlator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.xml.bind;
	requires commons.math3;
	requires net.sourceforge.argparse4j;
	requires javafx.swing;

    opens org.cemrc.correlator.controllers to javafx.fxml;
    opens org.cemrc.correlator.wizard to javafx.fxml;
    exports org.cemrc.correlator;
}
