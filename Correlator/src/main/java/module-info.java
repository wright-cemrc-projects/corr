module org.cemrc.correlator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.xml.bind;
	requires org.apache.commons.math4.legacy;
	requires org.apache.commons.math4.legacy.exception;
	requires net.sourceforge.argparse4j;
	requires javafx.swing;
	// requires junit;

    opens org.cemrc.correlator.controllers to javafx.fxml;
    opens org.cemrc.correlator.wizard to javafx.fxml;
    opens org.cemrc.data to java.xml.bind;
    opens org.cemrc.autodoc to com.sun.xml.bind;
    exports org.cemrc.correlator;
}
