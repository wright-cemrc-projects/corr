package org.cemrc.easycorr.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cemrc.data.EasyCorrDocument;
import org.cemrc.data.EasyCorrState;
import org.cemrc.easycorr.EasyCorr;

import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WizardController {
	
	Stage owner;
	
	@FXML
	ImageView imageViewStep1, imageViewStep2, imageViewStep3;
	
	@FXML
	Button btnNext, btnBack, btnCancel;
	
	@FXML
	VBox contentPanel;
	
	private List<IWizardPage> m_pages = new ArrayList<IWizardPage>();
	private EasyCorrDocument m_document;
	private EasyCorrState m_state;
	
	public void setState(EasyCorrState state) {
		m_state = state;
	}
	
	public void setDocument(EasyCorrDocument doc) {
		m_document = doc;

		for (IWizardPage page : m_pages) {
			page.setDocument(doc);
		}
	}
	
	public void setOwner(Stage owner) {
		this.owner = owner;
	}
	
	private final List<Parent> steps = new ArrayList<>();
	
	private final IntegerProperty currentStep = new SimpleIntegerProperty(-1);
	
	@FXML
	private void initialize() throws Exception {
		buildSteps();

		initButtons();

		setInitialContent();
	}
	
	private void buildSteps() throws java.io.IOException {

		final JavaFXBuilderFactory bf = new JavaFXBuilderFactory();

		FXMLLoader fxmlLoaderStep1 = new FXMLLoader(EasyCorr.class.getResource("/view/wizard/ImportMapPanel.fxml"));
		Parent step1 = fxmlLoaderStep1.load( );
		m_pages.add((WizardImportMapController)fxmlLoaderStep1.getController());
		
		FXMLLoader fxmlLoaderStep2 = new FXMLLoader(EasyCorr.class.getResource("/view/wizard/ImportPointsPanel.fxml"));
		Parent step2 = fxmlLoaderStep2.load( );
		m_pages.add((WizardImportPositionsController)fxmlLoaderStep2.getController());

		FXMLLoader fxmlLoaderStep3 = new FXMLLoader(EasyCorr.class.getResource("/view/wizard/ExportToNavigator.fxml"));
		Parent step3 = fxmlLoaderStep3.load( );
		m_pages.add((WizardExport)fxmlLoaderStep3.getController());
		
		FXMLLoader fxmlLoaderStep4 = new FXMLLoader(EasyCorr.class.getResource("/view/wizard/FinishPage.fxml"));
		Parent step4 = fxmlLoaderStep4.load( );
		m_pages.add((WizardFinish)fxmlLoaderStep4.getController());
		
		steps.addAll( Arrays.asList( step1, step2, step3, step4 ));
	}
	
	private void initButtons() {
		btnBack.disableProperty().bind( currentStep.lessThanOrEqualTo(0) );
		btnNext.disableProperty().bind( currentStep.greaterThanOrEqualTo(steps.size()) );
		
		btnNext.textProperty().bind(
				new When(
						currentStep.lessThan(steps.size()-1)
				)
						.then("Next")
						.otherwise("Finish")
		);
	}
	
	private void setInitialContent() {
		currentStep.set( 0 );  // first element
		contentPanel.getChildren().add( steps.get( currentStep.get() ));
	}
	
	@FXML
	public void next() {
		
		if( currentStep.get() < (steps.size()-1) ) {
			contentPanel.getChildren().remove( steps.get(currentStep.get()) );
			currentStep.set( currentStep.get() + 1 );
			contentPanel.getChildren().add( steps.get(currentStep.get()) );
		} else {
			complete();
		}
	}
	
	@FXML
	public void back() {
		
		if( currentStep.get() > 0 ) {
			contentPanel.getChildren().remove( steps.get(currentStep.get()) );
			currentStep.set( currentStep.get() - 1 );
			contentPanel.getChildren().add( steps.get(currentStep.get()) );
		}
	}
	
	@FXML
	public void cancel() {
		if (owner != null) {
			owner.close();
		}
	}
	
	public void complete() {
		m_state.setDocument(m_document);
		
		if (owner != null) {
			owner.close();
		}
	}
}
