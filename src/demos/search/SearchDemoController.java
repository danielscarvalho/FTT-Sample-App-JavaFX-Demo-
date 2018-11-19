/*
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 */

package demos.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import restfx.web.GetQuery;
import restfx.web.Query;
import restfx.web.QueryListener;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Search demo controller.
 */
public class SearchDemoController implements Initializable {
  @FXML private TextField searchTermTextField;
  @FXML private Button searchButton;
  @FXML private Label statusLabel;
  @FXML private TableView<Map<String, Object>> resultsTableView;
  @FXML private ImageView artworkImageView;
  @FXML private Button previewButton;

  private ResourceBundle resources = null;

  private GetQuery getQuery = null;

  public static final String QUERY_HOSTNAME = 
                             "ax.phobos.apple.com.edgesuite.net";
  public static final String BASE_QUERY_PATH = 
                             "/WebObjects/MZStoreServices.woa/wa/itmsSearch";
  public static final String MEDIA = "music";
  public static final int LIMIT = 100;

  public static final ImageView SEARCH_IMAGE_VIEW;
  public static final ImageView CANCEL_IMAGE_VIEW;

  static {
    SEARCH_IMAGE_VIEW = new ImageView(new Image(SearchDemo.class
                                      .getResourceAsStream("magnifier.png")));
    CANCEL_IMAGE_VIEW = new ImageView(new Image(SearchDemo.class
                                   .getResourceAsStream("bullet_cross.png")));
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void initialize(URL location, ResourceBundle resources) {
    this.resources = resources;

    // Initialize the search button content
    searchButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    searchButton.setGraphic(SEARCH_IMAGE_VIEW);

    // Add a selection change listener to the table view
    resultsTableView.getSelectionModel().getSelectedCells()
                    .addListener(new ListChangeListener<TablePosition>() {
      @Override
      public void onChanged(Change<? extends TablePosition> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            updateArtwork(change.getAddedSubList().get(0).getRow());
          }
        }
      }
    });

    // Do an example initial search so that the table is populated on startup
    searchTermTextField.setText("Cheap Trick");
    handleSearchAction(null);
  }

  /**
   * Search action handler.
   *
   * @param event
   */
  @FXML
  protected void handleSearchAction(ActionEvent event) {
    if (getQuery == null) {
      String searchTerms = searchTermTextField.getText();

      if (searchTerms.length() > 0) {
        getQuery = new GetQuery(QUERY_HOSTNAME, BASE_QUERY_PATH);
        getQuery.getParameters().put("term", searchTerms);
        getQuery.getParameters().put("media", MEDIA);
        getQuery.getParameters().put("limit", Integer.toString(LIMIT));
        getQuery.getParameters().put("output", "json");

        System.out.println(getQuery.getLocation());

        statusLabel.setText(resources.getString("searching"));
        updateActivityState();

        getQuery.execute(new QueryListener<Object>() {
          @Override
          @SuppressWarnings("unchecked")
          public void queryExecuted(Query<Object> task) {
            if (task == getQuery) {
              if (task.isCancelled()) {
                statusLabel.setText(resources.getString("cancelled"));
                searchTermTextField.requestFocus();
              } 
              else {
                Throwable exception = task.getException();
                if (exception == null) {
                  Map<String, Object> value = 
                                      (Map<String, Object>)task.getValue();
                  List<Object> results = (List<Object>)value.get("results");

                  // Update the table data
                  ObservableList<?> items = 
                                     FXCollections.observableList(results);
                  resultsTableView.setItems(
                                  (ObservableList<Map<String, Object>>)items);
                  statusLabel.setText(String.format(resources
                             .getString("resultCountFormat"), results.size()));

                  if (results.size() > 0) {
                    resultsTableView.getSelectionModel().select(0);
                    resultsTableView.requestFocus();
                  } 
                  else {
                    searchTermTextField.requestFocus();
                  }
                } 
                else {
                  statusLabel.setText(exception.getMessage());
                  searchTermTextField.requestFocus();
                }
              }

              getQuery = null;
              searchButton.setDisable(false);

              updateActivityState();
            }
          }
        });
      }
    } 
    else {
      getQuery.cancel(true);

      searchButton.setDisable(true);
      statusLabel.setText(resources.getString("aborting"));
    }
  }

  /**
   * Preview action handler.
   *
   * @param event
   */
  @FXML
  protected void handlePreviewAction(ActionEvent event) {
    Map<String, Object> selectedResult = 
                      resultsTableView.getSelectionModel().getSelectedItem();

    URL url;
    try {
      url = new URL((String)selectedResult.get("previewUrl"));
    } 
    catch (MalformedURLException exception) {
      throw new RuntimeException(exception);
    }

    try {
      java.awt.Desktop.getDesktop().browse(url.toURI());
    } 
    catch (URISyntaxException exception) {
      throw new RuntimeException(exception);
    } 
    catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void updateActivityState() {
    boolean active = (getQuery != null);
    searchTermTextField.setDisable(active);
    searchButton.setGraphic(active ? CANCEL_IMAGE_VIEW : SEARCH_IMAGE_VIEW);
  }

  private void updateArtwork(int index) {
    Map<String, Object> result = resultsTableView.getItems().get(index);
    String artworkURL;
    if (result == null) {
      artworkURL = null;
      previewButton.setDisable(true);
    } 
    else {
      artworkURL = (String)result.get("artworkUrl100");
      System.out.println(result.get("itemName"));
      previewButton.setDisable(false);
    }
    artworkImageView.setImage(artworkURL == null ? 
                              null : new Image(artworkURL));
  }
}
